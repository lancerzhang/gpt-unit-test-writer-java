package com.example.demo.worker;

import com.example.demo.config.ApplicationProperties;
import com.example.demo.config.OpenAIProperties;
import com.example.demo.exception.BudgetExceededException;
import com.example.demo.exception.FailedGeneratedTestException;
import com.example.demo.model.CoverageDetails;
import com.example.demo.model.MethodCoverage;
import com.example.demo.model.MethodDetails;
import com.example.demo.model.Step;
import com.example.demo.model.openai.OpenAIResult;
import com.example.demo.service.OpenAIApiService;
import com.example.demo.utils.ChangeHelper;
import com.example.demo.utils.CommandUtils;
import com.example.demo.utils.CoverageUtils;
import com.example.demo.utils.JavaFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

@Service
public class CoverageWriter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JaCoCoReportAnalyzer analyzer = new JaCoCoReportAnalyzer();

    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    private OpenAIApiService openAIApiService;
    @Autowired
    private OpenAIProperties openAIProperties;
    @Value("classpath:prompts/coverage_exists.txt")
    private Resource coverageExistsResource;
    @Value("classpath:prompts/coverage_new.txt")
    private Resource coverageNewResource;
    private String projectPath;
    private String projectInfo;
    private CoverageDetailExtractor extractor;
    private double budget;

    public void setProjectPath(String projectPath) throws Exception {
        this.projectPath = projectPath;
        ProjectInfoExtractor projectInfoExtractor = new ProjectInfoExtractor(projectPath + "/pom.xml");
        this.projectInfo = projectInfoExtractor.getProjectInfo();
    }

    public void generateUnitTest() throws Exception {
        logger.info("start to run mvn test for: " + projectPath);
        CommandUtils.runJaCoCo(projectPath);

        this.extractor = new CoverageDetailExtractor(projectPath);

        logger.info("start to analyze jacoco report");
        Map<String, List<MethodCoverage>> lowCoverageMethods = analyzer.analyzeReport(projectPath);

        this.budget = openAIProperties.getProjectBudget();
        try {
            for (String classPathName : lowCoverageMethods.keySet()) {
                handleClass(classPathName, lowCoverageMethods.get(classPathName));
            }
        } catch (BudgetExceededException e) {
            logger.error("Exceed project budget.", e);
        }
    }

    protected void handleClass(String classPathName, List<MethodCoverage> methods) throws Exception {
        logger.info("Low coverage methods in class: " + classPathName + ":");

        if (classPathName.contains("$")) {
            // ignore nested class
            return;
        }

        String javaFilePath = new File(projectPath, "/src/main/java/" + classPathName + ".java").getPath();
        String[] classPathSegments = classPathName.split("/");
        String className = classPathSegments[classPathSegments.length - 1];

        Map<String, MethodDetails> methodDetailsMap = JavaFileUtils.extractMethodCode(javaFilePath, className);
        CoverageDetails coverageDetails = extractor.getCoverageDetails(classPathName);

        for (MethodCoverage method : methods) {
            String methodName = method.getMethodName();
            logger.info("\tmethodName: " + methodName);
            if (methodName.contains("$") || methodName.contains("<")) {
                // ignore nested method
                continue;
            }
            MethodDetails details = methodDetailsMap.get(methodName);
            handleMethod(classPathName, details, coverageDetails);
        }
    }

    protected void handleMethod(String classPathName, MethodDetails details, CoverageDetails coverageDetails) throws IOException {
        if (details == null) {
            return;
        }
        for (Step step : applicationProperties.getSteps().get("coverage")) {
            if (this.budget <= 0) {
                throw new BudgetExceededException("Budget exceeded. Ending execution.");
            }

            // Define the path of the test file
            String testFilePath = projectPath + "/src/test/java/" + classPathName + "Test.java";

            boolean hasTestFile = false;
            File originalFile = new File(testFilePath);
            if (originalFile.exists()) {
                hasTestFile = true;
            }

            // Backup the original file
            ChangeHelper changeHelper = new ChangeHelper(testFilePath, hasTestFile);
            changeHelper.backupFile();

            AbstractMap.SimpleEntry<String, String> coverageLines = CoverageUtils.filterAndConvertCoverageLines(details, coverageDetails);
            String notCoveredLinesString = coverageLines.getKey();
            String partlyCoveredLinesString = coverageLines.getValue();

            String promptTemplate = loadTemplate(hasTestFile);

            String prompt = String.format(promptTemplate, this.projectInfo, classPathName, details.getCodeWithLineNumbers(),
                    notCoveredLinesString, partlyCoveredLinesString);

            if (partlyCoveredLinesString.length() > 0) {
                prompt += "The following lines are partly covered:\n" + partlyCoveredLinesString;
            }
            logger.info(prompt);

            // Call to OpenAI API with the prompt here, and get the generated test
            OpenAIResult result = openAIApiService.generateUnitTest(step, prompt, hasTestFile);
            double cost = result.getCost();
            this.budget = this.budget - cost;

            try {
                List<String> codeBlocks = JavaFileUtils.extractMarkdownCodeBlocks(result.getContent());

                if (codeBlocks.size() != 1) {
                    throw new FailedGeneratedTestException("Expect one code block in openAI response but it's not.");
                }

                String generatedTest = result.getContent();

                // Write the test
                JavaFileUtils.writeTest(generatedTest, testFilePath, classPathName);

                // Run the test
                CommandUtils.runTest(this.projectPath, classPathName);

            } catch (FailedGeneratedTestException e) {
                changeHelper.rollbackChanges();
            }
        }
    }


    protected String loadTemplate(boolean hasTestFile) throws IOException {
        Resource resourceToUse = hasTestFile ? coverageExistsResource : coverageNewResource;

        byte[] bytes = FileCopyUtils.copyToByteArray(resourceToUse.getInputStream());
        return new String(bytes, StandardCharsets.UTF_8);
    }
}


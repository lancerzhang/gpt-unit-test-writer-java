package com.example.demo.worker;

import com.example.demo.config.ApplicationProperties;
import com.example.demo.config.OpenAIProperties;
import com.example.demo.exception.BudgetExceededException;
import com.example.demo.exception.FailedGeneratedTestException;
import com.example.demo.model.CoverageDetails;
import com.example.demo.model.MethodCoverage;
import com.example.demo.model.MethodDetails;
import com.example.demo.model.Step;
import com.example.demo.model.openai.Message;
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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Value("classpath:prompts/error_feedback.txt")
    private Resource errorFeedbackResource;
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
//        CommandUtils.runMvnTest(projectPath);

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

        AbstractMap.SimpleEntry<String, String> coverageLines = CoverageUtils.filterAndConvertCoverageLines(details, coverageDetails);
        String notCoveredLinesString = coverageLines.getKey();
        String partlyCoveredLinesString = coverageLines.getValue();

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

            String promptTemplate = loadTemplate(hasTestFile);

            String prompt = String.format(promptTemplate, this.projectInfo, classPathName, details.getCodeWithLineNumbers(),
                    notCoveredLinesString, partlyCoveredLinesString);

            if (partlyCoveredLinesString.length() > 0) {
                prompt += "The following lines are partly covered:\n" + partlyCoveredLinesString;
            }

            ArrayList<Message> messages = new ArrayList<>();
            Message systemMessage = new Message();
            systemMessage.setRole("system");
            systemMessage.setContent("You are a super smart java developer.");
            messages.add(systemMessage);
            Message userMessage = new Message();
            userMessage.setRole("user");
            userMessage.setContent(prompt);
            messages.add(userMessage);

            // Call to OpenAI API with the prompt here, and get the generated test
            OpenAIResult result = openAIApiService.generateUnitTest(step, messages, hasTestFile);
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
                if (step.getFeedback().equals("true")) {
                    // Backup the original file
                    ChangeHelper changeHelper2 = new ChangeHelper(testFilePath, hasTestFile);
                    changeHelper2.backupFile();

                    // Load error_feedback.txt as a prompt
                    String errorFeedback;
                    try (InputStream is = errorFeedbackResource.getInputStream()) {
                        errorFeedback = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                                .lines()
                                .collect(Collectors.joining("\n"));
                    }

                    // Add error feedback to messages as a user message
                    Message feedbackMessage = new Message();
                    feedbackMessage.setRole("user");
                    feedbackMessage.setContent(errorFeedback);
                    messages.add(feedbackMessage);

                    // Call to OpenAI API with the updated prompt here, and get the regenerated test
                    OpenAIResult regeneratedResult = openAIApiService.generateUnitTest(step, messages, hasTestFile);

                    // Write the test
                    JavaFileUtils.writeTest(regeneratedResult.getContent(), testFilePath, classPathName);

                    // Run the test again
                    CommandUtils.runTest(this.projectPath, classPathName);
                }
            }
        }
    }

    protected String loadTemplate(boolean hasTestFile) throws IOException {
        Resource resourceToUse = hasTestFile ? coverageExistsResource : coverageNewResource;

        byte[] bytes = FileCopyUtils.copyToByteArray(resourceToUse.getInputStream());
        return new String(bytes, StandardCharsets.UTF_8);
    }
}


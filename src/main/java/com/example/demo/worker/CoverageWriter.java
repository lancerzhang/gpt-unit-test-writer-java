package com.example.demo.worker;

import com.example.demo.config.ApplicationProperties;
import com.example.demo.config.OpenAIProperties;
import com.example.demo.exception.BudgetExceededException;
import com.example.demo.model.CoverageDetails;
import com.example.demo.model.MethodCoverage;
import com.example.demo.model.MethodDetails;
import com.example.demo.model.Step;
import com.example.demo.model.openai.OpenAIResult;
import com.example.demo.service.OpenAIApiService;
import com.example.demo.utils.UtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CoverageWriter {

    private final Map<String, String> backupFilePathMap = new HashMap<>();

    private final JaCoCoReportAnalyzer analyzer = new JaCoCoReportAnalyzer();

    private final JavaParser parser = new JavaParser();
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
        this.extractor = new CoverageDetailExtractor(projectPath);
    }

    public void generateUnitTest() throws Exception {
        System.out.println("start to run mvn test for: " + projectPath);
        analyzer.runJaCoCo(projectPath);

        System.out.println("start to analyze jacoco report");
        Map<String, List<MethodCoverage>> lowCoverageMethods = analyzer.analyzeReport(projectPath);

        this.budget = openAIProperties.getProjectBudget();
        try {
            for (String classPathName : lowCoverageMethods.keySet()) {
                handleClass(classPathName, lowCoverageMethods.get(classPathName));
            }
        } catch (BudgetExceededException e) {
            System.err.println(e.getMessage());
            // Perform any additional steps needed when the budget is exceeded...
        }
    }

    public void handleClass(String classPathName, List<MethodCoverage> methods) throws Exception {
        System.out.println("Low coverage methods in class: " + classPathName + ":");

        if (classPathName.contains("$")) {
            // ignore nested class
            return;
        }

        String javaFilePath = new File(projectPath, "/src/main/java/" + classPathName + ".java").getPath();
        String[] classPathSegments = classPathName.split("/");
        String className = classPathSegments[classPathSegments.length - 1];

        Map<String, MethodDetails> methodDetailsMap = parser.extractMethodCode(javaFilePath, className);
        CoverageDetails coverageDetails = extractor.getCoverageDetails(classPathName);

        for (MethodCoverage method : methods) {
            String methodName = method.getMethodName();
            System.out.println("\tmethodName: " + methodName);
            if (methodName.contains("$") || methodName.contains("<")) {
                // ignore nested method
                continue;
            }
            MethodDetails details = methodDetailsMap.get(methodName);
            handleMethod(classPathName, details, coverageDetails);
        }
    }

    private void handleMethod(String classPathName, MethodDetails details, CoverageDetails coverageDetails) throws IOException {
        if (details == null) {
            return;
        }
        for (Step step : applicationProperties.getSteps().get("coverage")) {
            // Define the path of the test file
            String testFilePath = projectPath + "/src/test/java/" + classPathName + "Test.java";

            // Backup the original file
            backupFile(testFilePath);

            AbstractMap.SimpleEntry<String, String> coverageLines = UtUtils.filterAndConvertCoverageLines(details, coverageDetails);
            String notCoveredLinesString = coverageLines.getKey();
            String partlyCoveredLinesString = coverageLines.getValue();

            String promptTemplate = loadTemplate(testFilePath);

            String prompt = String.format(promptTemplate, this.projectInfo, classPathName, details.getCodeWithLineNumbers(),
                    notCoveredLinesString, partlyCoveredLinesString);

            if (partlyCoveredLinesString.length() > 0) {
                prompt += "The following lines are partly covered:\n" + partlyCoveredLinesString;
            }
            System.out.println(prompt);

            // Call to OpenAI API with the prompt here, and get the generated test
            OpenAIResult result = openAIApiService.generateUnitTest(step, prompt);

            String generatedTest = result.getContent();


            // Write the test
            writeTest(generatedTest, testFilePath);

            // Run the test
            boolean testPassed = runTest();

            double cost = result.getCost();
            this.budget = this.budget - cost;
            if (this.budget <= 0) {
                throw new BudgetExceededException("Budget exceeded. Ending execution.");
            }

            // If the test did not pass, rollback the changes
            if (testPassed) {
                System.out.println("Test passed for generated unit test.");
                return;
            } else {
                rollbackChanges(testFilePath);
            }
        }
    }

    public boolean backupFile(String filePath) throws IOException {
        File originalFile = new File(filePath);
        if (!originalFile.exists()) {
            System.out.println("Original file doesn't exist at path " + filePath);
            return false;
        }
        String backupFilePath = filePath + ".bak";
        backupFilePathMap.put(filePath, backupFilePath);
        File backupFile = new File(backupFilePath);
        Files.copy(originalFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    public void writeTest(String generatedTest, String filePathStr) throws IOException {
        File testFile = new File(filePathStr);

        Path filePath = Paths.get(filePathStr);
        // Ensure directories exist
        Files.createDirectories(filePath.getParent());

        if (testFile.exists()) {
            // If test file already exists, append the new test
            String contentToAppend = "\n\n" + generatedTest;
            byte[] bytes = contentToAppend.getBytes(StandardCharsets.UTF_8);
            Files.write(filePath, bytes, StandardOpenOption.APPEND);
        } else {
            // If test file does not exist, create it and write the new test
            byte[] bytes = generatedTest.getBytes(StandardCharsets.UTF_8);
            Files.write(filePath, bytes);
        }
    }

    public boolean runTest() {
        // This is where you would implement logic to run the test.
        // If the test fails, return false. If the test passes, return true.
        return true;
    }

    public void rollbackChanges(String filePath) throws IOException {
        String backupFilePath = backupFilePathMap.get(filePath);
        if (backupFilePath != null) {
            File originalFile = new File(filePath);
            File backupFile = new File(backupFilePath);
            Files.copy(backupFile.toPath(), originalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public String loadTemplate(String testFilePath) throws IOException {
        File testFile = new File(testFilePath);
        Resource resourceToUse = testFile.exists() ? coverageExistsResource : coverageNewResource;

        byte[] bytes = FileCopyUtils.copyToByteArray(resourceToUse.getInputStream());
        return new String(bytes, StandardCharsets.UTF_8);
    }
}


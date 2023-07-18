package com.example.demo.worker;

import com.example.demo.model.CoverageDetails;
import com.example.demo.model.MethodCoverage;
import com.example.demo.model.MethodDetails;
import com.example.demo.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

@Component
public class UnitTestWriter {

    private String projectPath;
    private PomInfoExtractor pomExtractor;
    private String projectInfo;
    private final JaCoCoReportAnalyzer analyzer;
    private final JavaParser parser;
    private CoverageDetailExtractor extractor;
    private int limit;

    @Value("classpath:prompts/ut.txt")
    private Resource utTemplateResource;

    @Autowired
    public UnitTestWriter(JaCoCoReportAnalyzer analyzer, JavaParser parser) {
        this.analyzer = analyzer;
        this.parser = parser;
    }

    public void setProjectPath(String projectPath) throws Exception {
        this.projectPath = projectPath;
        this.pomExtractor = new PomInfoExtractor(projectPath + "/pom.xml");
        this.projectInfo = pomExtractor.getProjectInfo();
        this.extractor = new CoverageDetailExtractor(projectPath);
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void generateUnitTest() throws Exception {
        System.out.println("start to run mvn test for: " + projectPath);
//        analyzer.runJaCoCo(projectPath);

        System.out.println("start to analyze jacoco report");
        Map<String, List<MethodCoverage>> lowCoverageMethods = analyzer.analyzeReport(projectPath);

        for (String classPathName : lowCoverageMethods.keySet()) {
            handleClass(classPathName, lowCoverageMethods.get(classPathName));
        }
    }

    private void handleClass(String classPathName, List<MethodCoverage> methods) throws Exception {
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
            if (method.getMethodName().contains("$") || method.getMethodName().contains("<")) {
                // ignore nested method
                continue;
            }
            handleMethod(classPathName, method, methodDetailsMap, coverageDetails);
        }
    }

    private void handleMethod(String classPathName, MethodCoverage method, Map<String, MethodDetails> methodDetailsMap, CoverageDetails coverageDetails) throws IOException {
        String methodName = method.getMethodName();
        System.out.println("\tmethodName: " + methodName);

        if (limit <= 0) return;

        MethodDetails details = methodDetailsMap.get(methodName);
        if (details != null) {
            handleDetails(classPathName, details, coverageDetails);
            limit--;
        } else {
            System.out.println("Details not found for method: " + methodName);
        }
    }

    private void handleDetails(String className, MethodDetails details, CoverageDetails coverageDetails) throws IOException {
        int startLine = details.getStartLine();
        int endLine = details.getEndLine();

        // Filter the not covered and partly covered lines to only include those within the method
        List<Integer> notCoveredLines = coverageDetails.getNotCoveredLines().stream()
                .filter(line -> line >= startLine && line <= endLine)
                .collect(Collectors.toList());
        List<Integer> partlyCoveredLines = coverageDetails.getPartlyCoveredLines().stream()
                .filter(line -> line >= startLine && line <= endLine)
                .collect(Collectors.toList());

        // Change notCoveredLines and partlyCoveredLines to string
        String notCoveredLinesString = Utils.convertToRanges(notCoveredLines);
        String partlyCoveredLinesString = Utils.convertToRanges(partlyCoveredLines);

        String generatedTest = getGeneratedTest(className, details, notCoveredLinesString, partlyCoveredLinesString);

        // Define the path of the test file
        String testFilePath = projectPath + "/src/test/java/" + className + "Test.java";
        File testFile = new File(testFilePath);

        Path filePath = Paths.get(testFilePath);
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

    private String loadTemplate() throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(utTemplateResource.getInputStream());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String getGeneratedTest(String className, MethodDetails details, String notCoveredLines, String partlyCoveredLines) throws IOException {
        // Load template from file
        String promptTemplate = loadTemplate();

        String prompt = String.format(promptTemplate, this.projectInfo, className, details.getCodeWithLineNumbers(),
                notCoveredLines, partlyCoveredLines);

        System.out.println(prompt);

        // Call to OpenAI API with the prompt here, and get the generated test
        String generatedTest = ""; // replace this line with actual OpenAI API call

        return generatedTest;
    }


}


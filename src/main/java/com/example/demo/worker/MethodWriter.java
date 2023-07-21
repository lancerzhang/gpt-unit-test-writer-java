package com.example.demo.worker;

import com.example.demo.model.CoverageDetails;
import com.example.demo.model.MethodDetails;
import com.example.demo.model.openai.OpenAIResult;
import com.example.demo.service.OpenAIApiService;
import com.example.demo.utils.UtUtils;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;

public class MethodWriter {

    private final String projectPath;
    private final String projectInfo;
    private final OpenAIApiService openAIApiService;
    private final Resource utTemplateResource;
    private int cost = 0;

    public MethodWriter(String projectPath, String projectInfo, OpenAIApiService openAIApiService, Resource utTemplateResource) {
        this.projectPath = projectPath;
        this.projectInfo = projectInfo;
        this.openAIApiService = openAIApiService;
        this.utTemplateResource = utTemplateResource;
    }

    public String loadTemplate() throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(utTemplateResource.getInputStream());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public void generateUnitTest(String classPathName, MethodDetails details, CoverageDetails coverageDetails) throws Exception {
        if (details == null) {
            return;
        }
        AbstractMap.SimpleEntry<String, String> coverageLines = UtUtils.filterAndConvertCoverageLines(details, coverageDetails);
        String notCoveredLinesString = coverageLines.getKey();
        String partlyCoveredLinesString = coverageLines.getValue();

        String promptTemplate = loadTemplate();

        String prompt = String.format(promptTemplate, this.projectInfo, classPathName, details.getCodeWithLineNumbers(),
                notCoveredLinesString, partlyCoveredLinesString);

        System.out.println(prompt);

        // Call to OpenAI API with the prompt here, and get the generated test
        OpenAIResult result = openAIApiService.generateUnitTest(prompt);

        String generatedTest = result.getContent();

        // Define the path of the test file
        String testFilePath = projectPath + "/src/test/java/" + classPathName + "Test.java";
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

}

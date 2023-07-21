package com.example.demo.worker;

import com.example.demo.model.CoverageDetails;
import com.example.demo.model.MethodCoverage;
import com.example.demo.model.MethodDetails;
import com.example.demo.service.OpenAIApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service
public class ProjectWriter {

    @Autowired
    private JaCoCoReportAnalyzer analyzer;
    @Autowired
    private JavaParser parser;
    @Autowired
    private OpenAIApiService openAIApiService;
    @Value("classpath:prompts/ut.txt")
    private Resource utTemplateResource;

    private String projectPath;
    private PomInfoExtractor pomExtractor;
    private String projectInfo;
    private CoverageDetailExtractor extractor;


    public void setProjectPath(String projectPath) throws Exception {
        this.projectPath = projectPath;
        this.pomExtractor = new PomInfoExtractor(projectPath + "/pom.xml");
        this.projectInfo = pomExtractor.getProjectInfo();
        this.extractor = new CoverageDetailExtractor(projectPath);
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
            MethodWriter writer = new MethodWriter(this.projectPath, this.projectInfo, this.openAIApiService, this.utTemplateResource);
            writer.generateUnitTest(classPathName, details, coverageDetails);
        }
    }

}


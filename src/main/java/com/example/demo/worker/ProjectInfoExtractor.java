package com.example.demo.worker;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.demo.utils.FileUtils.changeToSystemFileSeparator;

public class ProjectInfoExtractor {

    private final Document doc;
    private final File projectDir;

    public ProjectInfoExtractor(String pomFilePath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        this.doc = builder.parse(pomFilePath);
        this.projectDir = new File(pomFilePath).getParentFile();
    }

    protected String extractJavaVersion() {
        NodeList nodes = doc.getElementsByTagName("java.version");
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }

    protected String extractSpringVersion() {
        NodeList nodes = doc.getElementsByTagName("parent");
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                Element parentElement = (Element) node;
                if (parentElement.getElementsByTagName("artifactId").item(0).getTextContent().equals("spring-boot-starter-parent")) {
                    return parentElement.getElementsByTagName("version").item(0).getTextContent();
                }
            }
        }
        return null;
    }

    protected String determineJUnitVersion() throws IOException {
        // You'll need a proper way to obtain all test files in your project.
        // This is a simplified example where we assume all test files are in src/test/java and end with Test.java
        File testDir = new File(projectDir, changeToSystemFileSeparator("/src/test/java"));
        Collection<File> testFiles = FileUtils.listFiles(testDir, new String[]{"java"}, true);

        for (File testFile : testFiles) {
            try (BufferedReader reader = new BufferedReader(new FileReader(testFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("import org.junit.jupiter")) {
                        return "JUnit 5";
                    } else if (line.contains("import org.junit.")) {
                        return "JUnit 4";
                    } else if (line.contains("@BeforeEach") || line.contains("@AfterEach") || line.contains("@Tag")) {
                        return "JUnit 5";
                    } else if (line.contains("@BeforeClass") || line.contains("@AfterClass") || line.contains("@Ignore")) {
                        return "JUnit 4";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Check POM for JUnit version if not found in code
        NodeList nodes = doc.getElementsByTagName("dependency");
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                Element dependencyElement = (Element) node;
                String groupId = dependencyElement.getElementsByTagName("groupId").item(0).getTextContent();
                String artifactId = dependencyElement.getElementsByTagName("artifactId").item(0).getTextContent();
                if ("junit".equals(groupId) && "junit".equals(artifactId)) {
                    return "JUnit 4";
                } else if ("org.junit.jupiter".equals(groupId) && "junit-jupiter".equals(artifactId)) {
                    return "JUnit 5";
                }
            }
        }

        // Assume JUnit 5 for Spring Boot 2.5 and above
        String springVersion = extractSpringVersion();
        if (springVersion.startsWith("2.") && Integer.parseInt(springVersion.split("\\.")[1]) >= 5) {
            return "JUnit 5";
        } else {
            return "JUnit 4";
        }
    }

    public String getProjectInfo() throws IOException {
        String javaVersion = extractJavaVersion();
        String springVersion = extractSpringVersion();
        String junitVersion = determineJUnitVersion();

        StringBuilder resultBuilder = new StringBuilder();
        if (javaVersion != null) {
            resultBuilder.append("Java ").append(javaVersion).append(",");
        }
        if (springVersion != null) {
            resultBuilder.append("Spring Boot ").append(springVersion).append(",");
        }
        if (junitVersion != null) {
            resultBuilder.append(junitVersion);
        }

        // Remove the trailing comma if it exists
        String result = resultBuilder.toString();
        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    public String extractBasePackage() {
        try {
            Path startPath = Paths.get(projectDir + changeToSystemFileSeparator("/src/main/java"));
            String packageStatement = Files.walk(startPath)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .flatMap(p -> extractPackageFromMainApp(p).stream())
                    .findFirst()
                    .orElse("No base package found.");
            int startIndex = packageStatement.indexOf(" ") + 1;
            int endIndex = packageStatement.lastIndexOf(";");
            return packageStatement.substring(startIndex, endIndex).trim().replaceAll("\\s+", ".");
        } catch (IOException e) {
            throw new RuntimeException("Error reading source files", e);
        }
    }

    private List<String> extractPackageFromMainApp(Path path) {
        try {
            List<String> lines = Files.readAllLines(path);
            boolean isMain = lines.stream().anyMatch(line -> line.contains("@SpringBootApplication"));
            if (isMain) {
                return lines.stream()
                        .filter(line -> line.startsWith("package"))
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file " + path, e);
        }
        return Collections.emptyList();
    }
}

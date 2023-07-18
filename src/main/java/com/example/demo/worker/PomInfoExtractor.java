package com.example.demo.worker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class PomInfoExtractor {
    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;
    private Document doc;

    public PomInfoExtractor(String pomFilePath) throws Exception {
        this.factory = DocumentBuilderFactory.newInstance();
        this.builder = factory.newDocumentBuilder();
        this.doc = builder.parse(pomFilePath);
    }

    private String extractJavaVersion() {
        NodeList nodes = doc.getElementsByTagName("java.version");
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return "";
    }

    private String extractSpringVersion() {
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
        return "";
    }

    private boolean hasLombokDependency() {
        NodeList nodes = doc.getElementsByTagName("dependency");
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                Element dependencyElement = (Element) node;
                if (dependencyElement.getElementsByTagName("artifactId").item(0).getTextContent().equals("lombok")) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getProjectInfo() {
        String javaVersion = extractJavaVersion();
        String springVersion = extractSpringVersion();
        boolean hasLombok = hasLombokDependency();

        StringBuilder projectInfo = new StringBuilder();
        projectInfo.append("Java ").append(javaVersion);
        projectInfo.append(", Spring Boot ").append(springVersion);

        if (hasLombok) {
            projectInfo.append(", Lombok");
        }

        return projectInfo.toString();
    }
}

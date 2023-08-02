package com.example.demo.worker;

import com.example.demo.model.MethodCoverage;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JaCoCoReportAnalyzer {

    public Map<String, List<MethodCoverage>> analyzeReport(String projectPath) throws Exception {
        Map<String, List<MethodCoverage>> lowCoverageMethods = new HashMap<>();

        File jacocoReport = new File(projectPath + "/target/site/jacoco/jacoco.xml");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(jacocoReport);

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();

        NodeList methodNodes = (NodeList) xpath.compile("//report/package/class/method").evaluate(document, XPathConstants.NODESET);

        for (int i = 0; i < methodNodes.getLength(); i++) {
            Node methodNode = methodNodes.item(i);
            NamedNodeMap methodAttrs = methodNode.getAttributes();
            String methodName = methodAttrs.getNamedItem("name").getNodeValue();
            String className = ((Element) methodNode.getParentNode()).getAttribute("name");

            Node counterNode = (Node) xpath.compile("counter[@type='METHOD']").evaluate(methodNode, XPathConstants.NODE);
            NamedNodeMap counterAttrs = counterNode.getAttributes();
            int missed = Integer.parseInt(counterAttrs.getNamedItem("missed").getNodeValue());
            int covered = Integer.parseInt(counterAttrs.getNamedItem("covered").getNodeValue());
            float coverage = (float) covered / (covered + missed);

            if (coverage < 0.8) {
                MethodCoverage lowCoverageMethod = new MethodCoverage();
                lowCoverageMethod.setMethodName(methodName);

                // Check if the class name already exists in the map
                if (!lowCoverageMethods.containsKey(className)) {
                    lowCoverageMethods.put(className, new ArrayList<>());
                }

                lowCoverageMethods.get(className).add(lowCoverageMethod);
            }
        }

        return lowCoverageMethods;
    }

}

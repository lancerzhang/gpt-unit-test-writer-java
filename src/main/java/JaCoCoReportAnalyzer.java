import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;

public class JaCoCoReportAnalyzer {

    public static void main(String[] args) throws Exception {
        String projectPath = "/Users/lancer/Development/ws/survey-server";
        runJaCoCo(projectPath);
        analyzeReport(projectPath);
    }

    private static void runJaCoCo(String projectPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("mvn", "test");
            pb.directory(new File(projectPath));
            Process p = pb.start();
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void analyzeReport(String projectPath) throws Exception {
        File jacocoReport = new File(projectPath + "/target/site/jacoco/jacoco.xml");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); // Disable DTD validation
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(jacocoReport);


        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();

        NodeList classNodes = (NodeList) xpath.compile("//report/package/class").evaluate(document, XPathConstants.NODESET);

        for (int i = 0; i < classNodes.getLength(); i++) {
            Node classNode = classNodes.item(i);
            NamedNodeMap classAttrs = classNode.getAttributes();
            String className = classAttrs.getNamedItem("name").getNodeValue();

            NodeList counterNodes = (NodeList) xpath.compile("counter[@type='LINE' or @type='METHOD']").evaluate(classNode, XPathConstants.NODESET);

            for (int j = 0; j < counterNodes.getLength(); j++) {
                Node counterNode = counterNodes.item(j);
                NamedNodeMap counterAttrs = counterNode.getAttributes();
                int missed = Integer.parseInt(counterAttrs.getNamedItem("missed").getNodeValue());
                int covered = Integer.parseInt(counterAttrs.getNamedItem("covered").getNodeValue());
                float coverage = (float) covered / (covered + missed);

                if (coverage < 0.8) {
                    System.out.println("Coverage less than 80% detected in class " + className);
                    break; // No need to continue checking this class
                }
            }
        }
    }

}

import java.util.List;
import java.util.Map;

public class UnitTestWriter {
    public static void main(String[] args) throws Exception {
        JaCoCoReportAnalyzer analyzer = new JaCoCoReportAnalyzer();
        String projectPath = "/Users/lancer/Development/ws/survey-server";
        System.out.println("start to run mvn test for: " + projectPath);
        analyzer.runJaCoCo(projectPath);
        System.out.println("start to analyze jacoco report");
        Map<String, List<String>> lowCoverageMethods = analyzer.analyzeReport(projectPath);
        for (Map.Entry<String, List<String>> entry : lowCoverageMethods.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            System.out.println("class: " + key);
            for (String value : values) {
                System.out.println("method: " + value);
            }
        }
    }
}

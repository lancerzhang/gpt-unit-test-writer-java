import java.io.File;
import java.util.List;
import java.util.Map;

public class UnitTestWriter {
    public static void main(String[] args) throws Exception {
        JaCoCoReportAnalyzer analyzer = new JaCoCoReportAnalyzer();
        JavaParser parser = new JavaParser();
        String projectPath = "/Users/lancer/Development/ws/survey-server";

        System.out.println("start to run mvn test for: " + projectPath);
        analyzer.runJaCoCo(projectPath);

        System.out.println("start to analyze jacoco report");
        Map<String, List<String>> lowCoverageMethods = analyzer.analyzeReport(projectPath);
        int limit = 1;
        for (Map.Entry<String, List<String>> entry : lowCoverageMethods.entrySet()) {
            String classPathName = entry.getKey();
            List<String> values = entry.getValue();
            System.out.println("class: " + classPathName);
            for (String methodName : values) {
                System.out.println("method: " + methodName);
                if (limit > 0) {
                    String javaFilePath = new File(projectPath, "/src/main/java/" +
                            classPathName.replace(".", "/") + ".java").getPath();

                    String[] classPathSegments = classPathName.split("/");
                    String className = classPathSegments[classPathSegments.length - 1];
                    String methodCode = parser.extractMethodCode(javaFilePath, className, methodName);
                    System.out.println(methodCode);
                    limit--;
                }
            }
        }
    }
}

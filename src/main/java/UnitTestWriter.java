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
        int limit = 1;
        Map<String, List<MethodCoverage>> lowCoverageMethods = analyzer.analyzeReport(projectPath);

        for (String classPathName : lowCoverageMethods.keySet()) {
            System.out.println("Low coverage methods in class: " + classPathName + ":");
            String javaFilePath = new File(projectPath, "/src/main/java/" +
                    classPathName.replace(".", "/") + ".java").getPath();
            String[] classPathSegments = classPathName.split("/");
            String className = classPathSegments[classPathSegments.length - 1];

            Map<String, MethodDetails> methodDetailsMap = parser.extractMethodCode(javaFilePath, className);
            for (MethodCoverage method : lowCoverageMethods.get(classPathName)) {
                String methodName = method.getMethodName();
                System.out.println("\tmethodName:" + methodName);
                if (limit > 0) {

                    limit--;
                }
            }
        }
    }
}

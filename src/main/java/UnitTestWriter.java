import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UnitTestWriter {
    public static void main(String[] args) throws Exception {
        String projectPath = "/Users/lancer/Development/ws/survey-server";
        JaCoCoReportAnalyzer analyzer = new JaCoCoReportAnalyzer();
        JavaParser parser = new JavaParser();
        CoverageDetailExtractor extractor = new CoverageDetailExtractor(projectPath);

        System.out.println("start to run mvn test for: " + projectPath);
        analyzer.runJaCoCo(projectPath);

        System.out.println("start to analyze jacoco report");
        int limit = 1;
        Map<String, List<MethodCoverage>> lowCoverageMethods = analyzer.analyzeReport(projectPath);

        for (String classPathName : lowCoverageMethods.keySet()) {
            System.out.println("Low coverage methods in class: " + classPathName + ":");
            String javaFilePath = new File(projectPath, "/src/main/java/" + classPathName + ".java").getPath();
            String[] classPathSegments = classPathName.split("/");
            String className = classPathSegments[classPathSegments.length - 1];
            if (className.contains("$")) {
                // ignore nested class
                continue;
            }

            Map<String, MethodDetails> methodDetailsMap = parser.extractMethodCode(javaFilePath, className);
            CoverageDetails coverageDetails = extractor.getCoverageDetails(classPathName);

            for (MethodCoverage method : lowCoverageMethods.get(classPathName)) {
                String methodName = method.getMethodName();
                System.out.println("\tmethodName: " + methodName);
                if (limit > 0) {
                    MethodDetails details = methodDetailsMap.get(methodName);
                    if (details != null) {
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

                        // Print method code, notCoveredLines and partlyCoveredLines line number string
                        System.out.println("Method code:\n" + details.getCodeWithLineNumbers());
                        System.out.println("Not covered lines: " + notCoveredLinesString);
                        System.out.println("Partly covered lines: " + partlyCoveredLinesString);

                        limit--;
                    } else {
                        System.out.println("Details not found for method: " + methodName);
                    }
                }
            }

        }
    }
}

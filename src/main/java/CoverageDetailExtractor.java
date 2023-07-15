import org.jacoco.core.analysis.*;
import org.jacoco.core.tools.ExecFileLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CoverageDetailExtractor {

    private final String projectPath;
    private final ExecFileLoader execFileLoader;

    public CoverageDetailExtractor(String projectPath) throws IOException {
        this.projectPath = projectPath;
        File EXEC_FILE = new File(projectPath + "/target/jacoco.exec");
        final ExecFileLoader execFileLoader = new ExecFileLoader();
        execFileLoader.load(EXEC_FILE);
        this.execFileLoader = execFileLoader;
    }

    public CoverageDetails getCoverageDetails(String className) throws IOException {
        List<Integer> notCovered = new ArrayList<>();
        List<Integer> partlyCovered = new ArrayList<>();

        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);
        analyzer.analyzeAll(new File(this.projectPath + "/target/classes/" + className.replace('.', '/') + ".class"));

        for (final IClassCoverage cc : coverageBuilder.getClasses()) {
            for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
                ILine line = cc.getLine(i);
                if (line.getStatus() == ICounter.NOT_COVERED) {
                    notCovered.add(i);
                } else if (line.getStatus() == ICounter.PARTLY_COVERED) {
                    partlyCovered.add(i);
                }
            }
        }

        return new CoverageDetails(notCovered, partlyCovered);
    }


}

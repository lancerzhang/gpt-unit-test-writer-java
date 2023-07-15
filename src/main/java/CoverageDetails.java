import java.util.List;

public class CoverageDetails {
    private final List<Integer> notCoveredLines;
    private final List<Integer> partlyCoveredLines;

    public CoverageDetails(List<Integer> notCoveredLines, List<Integer> partlyCoveredLines) {
        this.notCoveredLines = notCoveredLines;
        this.partlyCoveredLines = partlyCoveredLines;
    }

    public List<Integer> getNotCoveredLines() {
        return notCoveredLines;
    }

    public List<Integer> getPartlyCoveredLines() {
        return partlyCoveredLines;
    }
}
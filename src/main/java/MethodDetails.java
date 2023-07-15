public class MethodDetails {
    private String code;
    private int startLine;
    private int endLine;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public String getCodeWithLineNumbers() {
        StringBuilder sb = new StringBuilder();
        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length; i++) {
            sb.append(startLine + i)
                    .append(": ")
                    .append(lines[i])
                    .append("\n");
        }
        return sb.toString();
    }
}

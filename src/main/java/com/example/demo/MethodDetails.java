package com.example.demo;

import java.util.Set;

public class MethodDetails {
    private String code;
    private int startLine;
    private int endLine;

    private Set<String> importedClasses;

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

    public Set<String> getImportedClasses() {
        return importedClasses;
    }

    public void setImportedClasses(Set<String> importedClasses) {
        this.importedClasses = importedClasses;
    }

    public String getCodeWithLineNumbers() {
        StringBuilder sb = new StringBuilder();

        // Append imported classes
        if (importedClasses != null) {
            for (String importClass : importedClasses) {
                sb.append("import ").append(importClass).append(";\n");
            }
        }

        // Append method code with line numbers
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

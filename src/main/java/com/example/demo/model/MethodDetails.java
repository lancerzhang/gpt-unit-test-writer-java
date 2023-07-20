package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class MethodDetails {
    private String code;
    private int startLine;
    private int endLine;
    private Set<String> importedClasses;

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

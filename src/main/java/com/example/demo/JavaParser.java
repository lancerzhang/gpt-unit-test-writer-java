package com.example.demo;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JavaParser {

    public Map<String, MethodDetails> extractMethodCode(String filePath, String className) throws IOException {
        CompilationUnit cu = StaticJavaParser.parse(Files.newInputStream(Paths.get(filePath)));
        Set<String> imports = cu.getImports().stream()
                .map(ImportDeclaration::getNameAsString)
                .collect(Collectors.toSet());

        Map<String, MethodDetails> methodDetailsMap = new HashMap<>();

        for (TypeDeclaration<?> type : cu.getTypes()) {
            if (type.getNameAsString().equals(className)) {
                for (Node child : type.getChildNodes()) {
                    if (child instanceof MethodDeclaration) {
                        MethodDeclaration method = (MethodDeclaration) child;
                        String methodName = method.getNameAsString();

                        // Build the com.example.demo.MethodDetails object
                        MethodDetails details = new MethodDetails();
                        details.setCode(method.toString());
                        details.setStartLine(method.getBegin().get().line);
                        details.setEndLine(method.getEnd().get().line);

                        // Determine which of the imported classes are used in this method
                        Set<String> methodImports = imports.stream()
                                .filter(importClass -> isClassUsedInMethod(importClass, method))
                                .collect(Collectors.toSet());

                        details.setImportedClasses(methodImports);

                        methodDetailsMap.put(methodName, details);
                    }
                }
            }
        }
        return methodDetailsMap;
    }

    private boolean isClassUsedInMethod(String className, MethodDeclaration method) {
        String shortName = className.substring(className.lastIndexOf('.') + 1);
        return method.toString().contains(shortName);
    }
}

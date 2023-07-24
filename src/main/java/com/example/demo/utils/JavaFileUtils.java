package com.example.demo.utils;

import com.example.demo.model.MethodDetails;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class JavaFileUtils {

    public static Map<String, MethodDetails> extractMethodCode(String filePath, String className) throws IOException {
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

                        // Build the com.example.demo.model.MethodDetails object
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

    protected static boolean isClassUsedInMethod(String className, MethodDeclaration method) {
        String shortName = className.substring(className.lastIndexOf('.') + 1);
        return method.toString().contains(shortName);
    }

    public static List<String> extractMarkdownCodeBlocks(String input) {
        List<String> codeBlocks = new ArrayList<>();
        Pattern pattern = Pattern.compile("```java\\s*(.*?)\\s*```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            codeBlocks.add(matcher.group(1));
        }

        return codeBlocks;
    }

    protected static void insertImports(String imports, CompilationUnit cu) {
        // Parse the imports as a CompilationUnit
        CompilationUnit importCu = StaticJavaParser.parse(imports);
        // Add all imports to the existing CompilationUnit
        for (ImportDeclaration id : importCu.getImports()) {
            cu.addImport(id);
        }
    }

    protected static void insertMethods(String methods, CompilationUnit cu) {
        // Parse the methods as a CompilationUnit
        CompilationUnit methodCu = StaticJavaParser.parse(methods);

        // Get all the methods from the methodCu
        List<MethodDeclaration> methodDeclarations = methodCu.findAll(MethodDeclaration.class);

        // Add the methods to the first class in the existing CompilationUnit
        TypeDeclaration<?> firstClass = cu.findFirst(TypeDeclaration.class).orElse(null);
        if (firstClass != null) {
            for (MethodDeclaration md : methodDeclarations) {
                firstClass.addMember(md);
            }
        }
    }

    protected static String insertPackage(String generatedTest, String classPathName) {
        // Split the classPathName into segments
        List<String> pathSegments = Arrays.asList(classPathName.split("/"));

        // Join the segments back together with periods instead of slashes, excluding the last segment
        String packageStatement = String.join(".", pathSegments.subList(0, pathSegments.size() - 1));

        // Add the package statement to the beginning of the contentToWrite
        return "package " + packageStatement + ";\n\n" + generatedTest;
    }

    public static void writeTest(String generatedTest, String filePathStr, String classPathName) throws IOException {
        File testFile = new File(filePathStr);

        List<String> codeBlocks = extractMarkdownCodeBlocks(generatedTest);

        if (codeBlocks.isEmpty()) {
            throw new IllegalArgumentException("No code block found in the provided markdown.");
        } else if (codeBlocks.size() > 1) {
            throw new IllegalArgumentException("More than one code blocks found.");
        }

        if (testFile.exists()) {

            // Parse the existing test file as a CompilationUnit
            CompilationUnit cu = StaticJavaParser.parse(testFile);

            // Insert imports and method
            insertImports(codeBlocks.get(0), cu);
            insertMethods(codeBlocks.get(0), cu);

            // Write the modified CompilationUnit back to the file
            Files.write(testFile.toPath(), cu.toString().getBytes(StandardCharsets.UTF_8));
        } else {
            // If test file does not exist, create it and write the new test
            String contentToWrite = codeBlocks.get(0);

            contentToWrite = insertPackage(contentToWrite, classPathName);

            byte[] bytes = contentToWrite.getBytes(StandardCharsets.UTF_8);
            Files.write(testFile.toPath(), bytes, StandardOpenOption.CREATE);
        }
    }

}

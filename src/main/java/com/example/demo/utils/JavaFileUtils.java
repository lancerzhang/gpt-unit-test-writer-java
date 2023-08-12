package com.example.demo.utils;

import com.example.demo.exception.FailedGeneratedTestException;
import com.example.demo.model.MethodDetails;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
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
                        details.setMethodName(methodName);
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

    protected static void insertImports(String imports, CompilationUnit cu) {
        // Parse the imports as a CompilationUnit
        CompilationUnit importCu = StaticJavaParser.parse(imports);
        // Add all imports to the existing CompilationUnit
        for (ImportDeclaration id : importCu.getImports()) {
            cu.addImport(id);
        }
    }

    protected static void insertFields(String fields, CompilationUnit cu) {
        // Parse the fields as a CompilationUnit
        CompilationUnit fieldCu = StaticJavaParser.parse(fields);

        // Get all the field declarations from the fieldCu
        List<FieldDeclaration> newFieldDeclarations = fieldCu.findAll(FieldDeclaration.class);

        // Get existing field declarations
        TypeDeclaration<?> firstClass = cu.findFirst(TypeDeclaration.class).orElse(null);
        if (firstClass != null) {
            List<FieldDeclaration> existingFieldDeclarations = firstClass.getFields();

            int insertIndex = 0;  // insert at the beginning
            for (FieldDeclaration newField : newFieldDeclarations) {
                // Skip if the field already exists
                if (fieldExists(newField, existingFieldDeclarations)) {
                    continue;
                }

                firstClass.getMembers().add(insertIndex++, newField);
            }
        }
    }

    private static boolean fieldExists(FieldDeclaration newField, List<FieldDeclaration> existingFieldDeclarations) {
        String newFieldName = newField.getVariable(0).getNameAsString();
        for (FieldDeclaration existingField : existingFieldDeclarations) {
            if (existingField.getVariable(0).getNameAsString().equals(newFieldName)) {
                return true;
            }
        }
        return false;
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

    public static void writeTest(String generatedTest, String filePathStr, String classPathName) {
        File testFile = new File(filePathStr);

        try {
            if (testFile.exists()) {

                // Parse the existing test file as a CompilationUnit
                CompilationUnit cu = null;

                cu = StaticJavaParser.parse(testFile);

                // Insert imports and method
                insertImports(generatedTest, cu);
                insertMethods(generatedTest, cu);
                insertFields(generatedTest, cu);

                // Write the modified CompilationUnit back to the file
                Files.write(testFile.toPath(), cu.toString().getBytes(StandardCharsets.UTF_8));
            } else {
                // If test file does not exist, create it and write the new test
                String contentToWrite = generatedTest;

                contentToWrite = insertPackage(contentToWrite, classPathName);

                byte[] bytes = contentToWrite.getBytes(StandardCharsets.UTF_8);
                // Ensure directories exist
                Path filePath = Paths.get(filePathStr);
                Files.createDirectories(filePath.getParent());

                Files.write(testFile.toPath(), bytes, StandardOpenOption.CREATE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new FailedGeneratedTestException("Failed to write unit test to file.");
        }

    }

}

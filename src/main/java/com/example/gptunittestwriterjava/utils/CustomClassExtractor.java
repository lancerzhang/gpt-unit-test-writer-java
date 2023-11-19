package com.example.gptunittestwriterjava.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomClassExtractor {

    private final Set<String> customClasses = new HashSet<>();

    public String extractCustomClassSignature(String projectPath, String basePackage, String filePath, String targetMethod) throws IOException {
        CompilationUnit cu = StaticJavaParser.parse(Files.newInputStream(Paths.get(filePath)));
        cu.accept(new TargetMethodVisitor(targetMethod), null);

        StringBuilder result = new StringBuilder();
        for (String className : customClasses) {
            String classFilePath = FileUtils.searchForFileInProjectPath(className + ".java", projectPath);
            if (classFilePath != null) {
                cu.accept(new ClassConstructorVisitor(className, classFilePath, basePackage), result);
            }
        }
        return result.toString();
    }

    public String extractCustomClassFields(String projectPath, String basePackage, String filePath, String targetMethod) throws IOException {
        CompilationUnit cu = StaticJavaParser.parse(Files.newInputStream(Paths.get(filePath)));
        cu.accept(new TargetMethodVisitor(targetMethod), null);

        StringBuilder result = new StringBuilder();
        for (String className : customClasses) {
            String classFilePath = FileUtils.searchForFileInProjectPath(className + ".java", projectPath);
            if (classFilePath != null) {
                cu.accept(new ClassFieldVisitor(className, classFilePath, basePackage), result);
            }
        }
        return result.toString();
    }

    private static class ClassConstructorVisitor extends VoidVisitorAdapter<StringBuilder> {
        private final String className;
        private final String classFilePath;
        private final String basePackage;

        public ClassConstructorVisitor(String className, String classFilePath, String basePackage) {
            this.className = className;
            this.classFilePath = classFilePath;
            this.basePackage = basePackage;
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, StringBuilder builder) {
            try {
                CompilationUnit classCU = StaticJavaParser.parse(Files.newInputStream(Paths.get(classFilePath)));
                PackageDeclaration packageDeclaration = classCU.getPackageDeclaration().orElse(null);

                if (packageDeclaration != null && packageDeclaration.getNameAsString().startsWith(basePackage)) {
                    // Retrieve the class or interface declaration from the classCU
                    Optional<ClassOrInterfaceDeclaration> classDeclarationOpt = classCU.findFirst(ClassOrInterfaceDeclaration.class, cid -> cid.getNameAsString().equals(className));

                    if (classDeclarationOpt.isPresent()) {
                        ClassOrInterfaceDeclaration classDeclaration = classDeclarationOpt.get();
                        String constructors = classDeclaration.getConstructors().stream()
                                .map(constructor -> constructor.getSignature().toString())
                                .collect(Collectors.joining("\n"));
                        // Do whatever you need with the constructors here
                        builder.append(constructors).append("\n");
                    }
                }
            } catch (IOException e) {
                // Handle the exception appropriately
                e.printStackTrace();
            }

        }

    }

    private static class ClassFieldVisitor extends VoidVisitorAdapter<StringBuilder> {
        private final String className;
        private final String classFilePath;
        private final String basePackage;

        public ClassFieldVisitor(String className, String classFilePath, String basePackage) {
            this.className = className;
            this.classFilePath = classFilePath;
            this.basePackage = basePackage;
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, StringBuilder builder) {
            try {
                CompilationUnit classCU = StaticJavaParser.parse(Files.newInputStream(Paths.get(classFilePath)));
                PackageDeclaration packageDeclaration = classCU.getPackageDeclaration().orElse(null);

                if (packageDeclaration != null && packageDeclaration.getNameAsString().startsWith(basePackage)) {
                    // Now, for extracting class fields
                    builder.append("public class ").append(className).append(" {\n");
                    for (FieldDeclaration field : n.getFields()) {
                        builder.append(field.toString()).append("\n");
                    }
                    builder.append("}\n");
                }
            } catch (IOException e) {
                // Handle the exception appropriately
                e.printStackTrace();
            }
        }

    }

    private class TargetMethodVisitor extends VoidVisitorAdapter<Void> {
        private final String targetMethod;

        public TargetMethodVisitor(String targetMethod) {
            this.targetMethod = targetMethod;
        }

        @Override
        public void visit(MethodDeclaration n, Void arg) {
            if (n.getNameAsString().equals(targetMethod)) {
                n.accept(new CustomClassVisitor(), null);
            }
            super.visit(n, arg);
        }
    }

    private class CustomClassVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(VariableDeclarationExpr n, Void arg) {
            // Add the type of the variable declaration
            customClasses.add(n.getElementType().asString());
            super.visit(n, arg);
        }

        @Override
        public void visit(Parameter n, Void arg) {
            // Add the type of the method parameter
            customClasses.add(n.getType().asString());
            super.visit(n, arg);
        }

        @Override
        public void visit(MethodDeclaration n, Void arg) {
            // Add the return type of the method directly
            customClasses.add(n.getType().asString());
            super.visit(n, arg);
        }

        @Override
        public void visit(ObjectCreationExpr n, Void arg) {
            // Add the type used in 'new' expression
            customClasses.add(n.getType().asString());
            super.visit(n, arg);
        }

    }

}

package com.example.demo.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
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
    private final Set<String> customMethods = new HashSet<>();

    public String extractCustomClassSignature(String projectPath, String basePackage, String filePath, String targetMethod) throws IOException {
        CompilationUnit cu = StaticJavaParser.parse(Files.newInputStream(Paths.get(filePath)));
        cu.accept(new TargetMethodVisitor(targetMethod), null);

        StringBuilder result = new StringBuilder();
        for (String className : customClasses) {
            cu.accept(new ClassConstructorVisitor(className, projectPath, basePackage), result);
        }
//        for (String methodName : customMethods) {
//            cu.accept(new MethodVisitor(methodName), result);
//        }

        return result.toString();
    }

    private static class ClassConstructorVisitor extends VoidVisitorAdapter<StringBuilder> {
        private final String className;
        private final String projectPath;
        private final String basePackage;

        public ClassConstructorVisitor(String className, String projectPath, String basePackage) {
            this.className = className;
            this.projectPath = projectPath;
            this.basePackage = basePackage;
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, StringBuilder builder) {
            String classFilePath = FileUtils.searchForFileInProjectPath(className + ".java", projectPath);

            if (classFilePath != null) {
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

    }

    private class TargetMethodVisitor extends VoidVisitorAdapter<Void> {
        private final String targetMethod;

        public TargetMethodVisitor(String targetMethod) {
            this.targetMethod = targetMethod;
        }

        @Override
        public void visit(MethodDeclaration n, Void arg) {
            if (n.getNameAsString().equals(targetMethod)) {
                n.accept(new CustomClassAndMethodVisitor(), null);
            }
            super.visit(n, arg);
        }
    }

    private class CustomClassAndMethodVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(ObjectCreationExpr n, Void arg) {
            customClasses.add(n.getType().asString());
            super.visit(n, arg);
        }

        @Override
        public void visit(MethodCallExpr n, Void arg) {
            customMethods.add(n.getNameAsString());
            super.visit(n, arg);
        }
    }

    private class MethodVisitor extends VoidVisitorAdapter<StringBuilder> {
        private final String methodName;

        public MethodVisitor(String methodName) {
            this.methodName = methodName;
        }

        @Override
        public void visit(MethodDeclaration n, StringBuilder builder) {
            if (n.getNameAsString().equals(methodName)) {
                builder.append(n.getSignature()).append("\n");
            }
            super.visit(n, builder);
        }
    }
}

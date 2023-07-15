import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JavaParser {
    public String extractMethodCode(String filePath, String className, String methodName) throws IOException {
        CompilationUnit cu = StaticJavaParser.parse(Files.newInputStream(Paths.get(filePath)));
        for (TypeDeclaration<?> type : cu.getTypes()) {
            if (type.getNameAsString().equals(className)) {
                for (Node child : type.getChildNodes()) {
                    if (child instanceof MethodDeclaration) {
                        MethodDeclaration method = (MethodDeclaration) child;
                        if (method.getNameAsString().equals(methodName)) {
                            return method.toString();
                        }
                    }
                }
            }
        }
        return null;
    }
}

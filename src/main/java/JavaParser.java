import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class JavaParser {

    public Map<String, MethodDetails> extractMethodCode(String filePath, String className) throws IOException {
        Map<String, MethodDetails> methodDetailsMap = new HashMap<>();

        CompilationUnit cu = StaticJavaParser.parse(Files.newInputStream(Paths.get(filePath)));
        for (TypeDeclaration<?> type : cu.getTypes()) {
            if (type.getNameAsString().equals(className)) {
                for (Node child : type.getChildNodes()) {
                    if (child instanceof MethodDeclaration) {
                        MethodDeclaration method = (MethodDeclaration) child;

                        MethodDetails details = new MethodDetails();
                        details.setCode(method.toString());
                        details.setStartLine(method.getBegin().get().line);
                        details.setEndLine(method.getEnd().get().line);

                        methodDetailsMap.put(method.getNameAsString(), details);
                    }
                }
            }
        }
        return methodDetailsMap;
    }
}

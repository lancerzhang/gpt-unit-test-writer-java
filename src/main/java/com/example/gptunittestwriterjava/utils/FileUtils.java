package com.example.gptunittestwriterjava.utils;

import java.io.File;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {

    public static String extractMarkdownCodeBlocks(String input) {
        String patternStr = "```(?:.*?\\n)?(.*?)```";
        Pattern pattern = Pattern.compile(patternStr, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);
        StringBuilder stringBuilder = new StringBuilder();
        int matches = 0;

        while (matcher.find()) {
            matches++;
            stringBuilder.append(matcher.group(1).trim()).append("\n");
        }

        if (matches > 1) {
            return null;
        } else if (matches == 1) {
            return stringBuilder.toString().trim();
        } else {
            return input;
        }
    }

    public static String changeToSystemFileSeparator(String input) {
        return input.replace("/", File.separator);
    }

    /**
     * Searches for a file with the given name within the project directory.
     *
     * @param fileName    The name of the file to search for.
     * @param projectPath The root directory path of the project.
     * @return The full path to the file if found, or null otherwise.
     */
    public static String searchForFileInProjectPath(String fileName, String projectPath) {
        File projectDir = new File(projectPath);
        Optional<File> result = searchForFileInDirectory(projectDir, fileName);

        return result.map(File::getAbsolutePath).orElse(null);
    }

    /**
     * Recursively searches for a file with the given name in the provided directory.
     *
     * @param directory The directory in which to search.
     * @param fileName  The name of the file to search for.
     * @return An Optional containing the File if found, or an empty Optional otherwise.
     */
    private static Optional<File> searchForFileInDirectory(File directory, String fileName) {
        File[] files = directory.listFiles();

        if (files == null) {
            return Optional.empty();
        }

        for (File file : files) {
            if (file.isDirectory()) {
                Optional<File> result = searchForFileInDirectory(file, fileName);
                if (result.isPresent()) {
                    return result;
                }
            } else if (fileName.equals(file.getName())) {
                return Optional.of(file);
            }
        }

        return Optional.empty();
    }
}

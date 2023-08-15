package com.example.gptunittestwriterjava.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CommandUtils {
    private static final Logger logger = LoggerFactory.getLogger(CommandUtils.class);
    private static final String mvnFileName = System.getProperties().getProperty("os.name").toLowerCase().contains("windows") ? "mvn.cmd" : "mvn";

    public static void runMvnTest(String projectPath) throws InterruptedException, IOException {
        String command = mvnFileName + " test";
        logger.info("Start to run command: " + command);
        Process process = Runtime.getRuntime().exec(command, null, new File(projectPath));
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            logger.debug(line);
        }
        process.waitFor();
        logger.info("Finished running command: " + command);
    }

    public static String runTest(String projectPath, String classPathName) throws IOException, InterruptedException {
        String command = mvnFileName + " -Dtest=" + classPathName.replace("/", ".") + "Test test";
        logger.info("Start to run command: " + command);
        Process process = Runtime.getRuntime().exec(command, null, new File(projectPath));

        // Capture and print command output
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        boolean isError = false;
        List<String> errorLines = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            logger.debug(line);
            if (line.contains("[ERROR] ")) {
                isError = true;
            }
            if (isError) {
                errorLines.add(line);
            }
        }

        int exitCode = process.waitFor();
        logger.info("Finished running command: " + command);
        if (exitCode != 0) {
            logger.error("exitCode is: " + exitCode);
            return String.join("\n", errorLines);
        }

        return null;
    }

    public static void runGitClone(String githubRepo, String branch, String targetDirectory)
            throws InterruptedException, IOException {
        String command = "git clone -b " + branch + " " + githubRepo + " " + targetDirectory;
        logger.info("Start to run command: " + command);

        Process process = Runtime.getRuntime().exec(command, null, new File(targetDirectory).getParentFile());
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            logger.debug(line);
        }
        process.waitFor();
        logger.info("Finished running command: " + command);
    }
}

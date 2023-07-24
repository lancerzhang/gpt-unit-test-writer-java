package com.example.demo.utils;

import com.example.demo.exception.FailedGeneratedTestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandUtils {
    private static final Logger logger = LoggerFactory.getLogger(CommandUtils.class);
    private static final String mvnFileName = System.getProperties().getProperty("os.name").toLowerCase().contains("windows") ? "mvn.cmd" : "mvn";

    public static void runJaCoCo(String projectPath) throws InterruptedException, IOException {
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

    public static void runTest(String projectPath, String classPathName) {
        try {
            String command = mvnFileName + " -Dtest=" + classPathName.replace("/", ".") + "Test test";
            logger.info("Start to run command: " + command);
            Process process = Runtime.getRuntime().exec(command, null, new File(projectPath));

            // Capture and print command output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                logger.debug(line);
            }

            int exitCode = process.waitFor();
            logger.info("Finished running command: " + command);
            if (exitCode != 0) {
                logger.error("exitCode is: " + exitCode);
                throw new FailedGeneratedTestException("Failed to run unit test.");
            }
        } catch (InterruptedException | IOException e) {
            logger.error("Failed to run unit test.", e);
            throw new FailedGeneratedTestException("Failed to run unit test.");
        }
    }
}

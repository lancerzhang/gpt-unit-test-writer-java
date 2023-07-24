package com.example.demo.utils;

import com.example.demo.exception.FailedGeneratedTestException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandUtils {

    public static void runJaCoCo(String projectPath) throws InterruptedException, IOException {
        String mvnFileName = System.getProperties().getProperty("os.name").toLowerCase().contains("windows") ? "mvn.cmd" : "mvn";
        Process process = Runtime.getRuntime().exec(mvnFileName + " test");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        process.waitFor();
    }

    public static void runTest(String projectPath, String classPathName) {
        try {
            String command = "mvn -Dtest=" + classPathName.replace("/", ".") + "Test test";
            Process process = Runtime.getRuntime().exec(command, null, new File(projectPath));
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new FailedGeneratedTestException("Failed to run unit test.");
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            throw new FailedGeneratedTestException("Failed to run unit test.");
        }
    }
}

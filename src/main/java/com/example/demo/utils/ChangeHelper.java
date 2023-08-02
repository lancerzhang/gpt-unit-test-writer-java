package com.example.demo.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ChangeHelper {

    private final boolean hasTestFile;
    private final String originalFilePath;
    private String backupFilePath;

    public ChangeHelper(String originalFilePath, boolean hasTestFile) {
        this.originalFilePath = originalFilePath;
        this.hasTestFile = hasTestFile;
    }

    public void complete() throws IOException {
        File backupFile = new File(backupFilePath);
        // Check if backup file exists, and delete if it does
        if (backupFile.exists()) {
            backupFile.delete();
        }
    }

    public void backupFile() throws IOException {
        if (hasTestFile) {
            File originalFile = new File(originalFilePath);
            String backupFilePath = originalFilePath + ".bak";
            File backupFile = new File(backupFilePath);
            this.backupFilePath = backupFilePath;

            // Check if backup file exists, and delete if it does
            if (backupFile.exists()) {
                backupFile.delete();
            }

            Files.copy(originalFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void rollbackChanges() throws IOException {
        File originalFile = new File(originalFilePath);
        if (hasTestFile) {
            File backupFile = new File(backupFilePath);
            Files.copy(backupFile.toPath(), originalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
            if (originalFile.exists()) {
                if (!originalFile.delete()) {
                    throw new IOException("Failed to delete original file: " + originalFilePath);
                }
            }
        }
    }
}

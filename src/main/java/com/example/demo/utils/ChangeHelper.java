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

    public void backupFile() throws IOException {
        if (hasTestFile) {
            File originalFile = new File(originalFilePath);
            String backupFilePath = originalFilePath + ".bak";
            File backupFile = new File(backupFilePath);
            this.backupFilePath = backupFilePath;
            Files.copy(originalFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void rollbackChanges() throws IOException {
        File originalFile = new File(this.originalFilePath);
        if (hasTestFile) {
            File backupFile = new File(this.backupFilePath);
            Files.copy(backupFile.toPath(), originalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
            if (originalFile.exists()) {
                if (!originalFile.delete()) {
                    throw new IOException("Failed to delete original file: " + this.originalFilePath);
                }
            }
        }
    }
}

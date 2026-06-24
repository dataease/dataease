package io.dataease.exportCenter.manage;

import io.dataease.exception.DEException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

final class ExportTaskFileTarget {

    record FileSize(Double size, String unit) {
    }

    private final String taskId;
    private final Path exportFilePath;

    ExportTaskFileTarget(String taskId, Path exportFilePath) {
        this.taskId = taskId;
        this.exportFilePath = exportFilePath;
    }

    String taskId() {
        return taskId;
    }

    String filePath() {
        return exportFilePath.toString();
    }

    void createParentDirectory() {
        try {
            Files.createDirectories(exportFilePath.getParent());
        } catch (IOException e) {
            DEException.throwException(e);
        }
    }

    OutputStream newOutputStream() throws IOException {
        return Files.newOutputStream(exportFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    InputStream newInputStream() throws IOException {
        return Files.newInputStream(exportFilePath);
    }

    long size() throws IOException {
        return Files.size(exportFilePath);
    }

    FileSize readFileSize() throws IOException {
        long length = size();
        if ((double) length / 1024 / 1024 > 1) {
            if ((double) length / 1024 / 1024 / 1024 > 1) {
                return new FileSize(Double.valueOf(String.format("%.2f", (double) length / 1024 / 1024 / 1024)), "Gb");
            }
            return new FileSize(Double.valueOf(String.format("%.2f", (double) length / 1024 / 1024)), "Mb");
        }
        return new FileSize(Double.valueOf(String.format("%.2f", (double) length / 1024)), "Kb");
    }
}

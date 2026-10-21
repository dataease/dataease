package io.dataease.extensions.sync.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * DE 侧的执行器 HTTP 请求和响应模型，将文件身份清单和实际内容分开传输
 * 检查阶段发送元数据，确认阶段编码文件，执行器独立定义兼容对象并重新校验
 */
public final class SyncDependencyTransfer {
    private SyncDependencyTransfer() {
    }

    /**
     * 单个待写入依赖，content 为 Base64 内容，directory 仅允许 lib 或 connectors
     */
    public record FileData(String name, String directory, String content) {
    }

    /**
     * 确认后的数据库类型、上传文件及用户选择
     */
    public record Request(String type, List<FileData> files, Map<String, SyncDependencyDirectory.Choice> choices) {
    }

    /**
     * 上传检查只发送文件清单，确认处理时才传输文件内容
     */
    public record FileSummary(String name, String directory, Set<String> identities,
                              boolean jdbc, String version, String checksum) {
    }

    /**
     * 上传预检查的文件身份清单，不包含 JAR 内容
     */
    public record Check(String type, List<FileSummary> files) {
    }

    public static List<FileSummary> summarize(List<SyncDependencyDirectory.Artifact> artifacts) {
        return artifacts.stream().map(a -> new FileSummary(a.name(), a.directory(), a.identities(),
                a.jdbc(), a.version(), a.checksum())).toList();
    }

    /**
     * 执行器返回的冲突列表，由 DE 合并后统一询问用户
     */
    public record Inspection(List<SyncDependencyDirectory.Conflict> conflicts) {
    }

    public static List<FileData> encode(List<SyncDependencyDirectory.Artifact> artifacts) throws IOException {
        List<FileData> files = new ArrayList<>();
        for (var artifact : artifacts) files.add(new FileData(artifact.name(), artifact.directory(),
                Base64.getEncoder().encodeToString(Files.readAllBytes(artifact.file()))));
        return files;
    }

}

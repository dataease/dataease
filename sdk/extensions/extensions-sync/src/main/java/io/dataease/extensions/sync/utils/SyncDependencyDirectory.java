package io.dataease.extensions.sync.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.jar.JarFile;

/**
 * DE 同步插件的依赖文件规则，负责上传暂存、驱动识别、内容比较和确认后的替换
 * 不负责执行器本地复制、网络通信、插件持久化或运行任务管理
 */
public final class SyncDependencyDirectory {
    private SyncDependencyDirectory() {
    }

    /**
     * 用户确认的驱动处理方式，保留完全跳过对应文件
     */
    public enum Choice { KEEP, REPLACE }

    /**
     * 解析后的依赖文件及身份信息，版本用于展示，校验值用于比较内容
     */
    public record Artifact(Path file, String name, String directory, Set<String> identities,
                           boolean jdbc, String version, String checksum) {
    }

    /**
     * 供调用方展示的单项冲突，不暴露内部文件路径
     */
    public record Conflict(String name, String currentVersion, String uploadedVersion) {
    }

    /**
     * 上传依赖与运行目录中匹配文件的对应关系
     */
    public record Change(Artifact incoming, List<Artifact> existing) {
        public boolean conflicts() {
            return !existing.isEmpty() && (existing.size() != 1
                    || !existing.getFirst().checksum().equals(incoming.checksum()));
        }
    }

    /**
     * 检查得到的操作计划，生成计划本身不修改文件
     */
    public record Plan(Path directory, List<Change> changes) {
        public List<Conflict> conflicts() {
            return changes.stream().filter(Change::conflicts).map(c -> new Conflict(c.incoming().name(),
                    String.join(" / ", c.existing().stream().map(Artifact::version).toList()),
                    c.incoming().version())).toList();
        }
    }

    public static String databaseType(String value) throws IOException {
        String type = value == null ? "" : value.toLowerCase(Locale.ROOT);
        if (!type.matches("[a-z][a-z0-9_-]*")) throw new IOException("无效的同步数据库类型: " + value);
        return type;
    }

    /**
     * 提取到调用方管理的暂存目录，不触碰运行依赖
     */
    public static List<Artifact> stage(Path plugin, Path temporary) throws IOException {
        Files.createDirectories(temporary);
        List<Artifact> result = new ArrayList<>();
        long total = 0;
        try (JarFile jar = new JarFile(plugin.toFile())) {
            for (var entry : jar.stream().filter(e -> !e.isDirectory() && e.getName().startsWith("sync/")
                    && e.getName().endsWith(".jar")).toList()) {
                String filename = Path.of(entry.getName()).getFileName().toString();
                if (!filename.matches("[A-Za-z0-9_][A-Za-z0-9._-]*\\.jar")) throw new IOException("无效的依赖文件名");
                Path staged = temporary.resolve(filename);
                try (InputStream input = jar.getInputStream(entry);
                     var output = Files.newOutputStream(staged, StandardOpenOption.CREATE_NEW)) {
                    byte[] buffer = new byte[8192];
                    int length;
                    while ((length = input.read(buffer)) != -1) {
                        total += length;
                        if (total > 512L * 1024 * 1024) throw new IOException("依赖解压大小超过限制");
                        output.write(buffer, 0, length);
                    }
                }
                String target = entry.getName().startsWith("sync/connectors/") ? "connectors"
                        : entry.getName().startsWith("sync/lib/") ? "lib" : null;
                Artifact candidate = inspect(staged, filename, target);
                for (Artifact previous : result) {
                    if (sameDriver(previous, candidate)) throw new IOException("插件包内存在重复依赖: " + filename);
                }
                result.add(candidate);
            }
        }
        return List.copyOf(result);
    }

    public static Plan plan(List<Artifact> incoming, Path directory, Path legacyDirectory) throws IOException {
        safePath(directory);
        List<Artifact> installed = new ArrayList<>();
        for (String name : List.of("lib", "connectors")) {
            Path existing = directory.resolve(name);
            safePath(existing);
            if (Files.isDirectory(existing)) try (var files = Files.list(existing)) {
                for (Path file : files.filter(p -> p.toString().endsWith(".jar")).sorted().toList()) {
                    safePath(file);
                    installed.add(inspect(file, file.getFileName().toString(), name));
                }
            }
        }
        if (legacyDirectory != null && Files.isDirectory(legacyDirectory)) {
            safePath(legacyDirectory);
            try (var files = Files.list(legacyDirectory)) {
                for (Path file : files.filter(p -> p.toString().endsWith(".jar")).sorted().toList()) {
                    safePath(file);
                    Artifact artifact = inspect(file, file.getFileName().toString(), null);
                    if (incoming.stream().anyMatch(candidate -> sameDriver(artifact, candidate))) installed.add(artifact);
                }
            }
        }
        List<Change> changes = new ArrayList<>();
        for (Artifact candidate : incoming) {
            List<Artifact> matches = installed.stream().filter(e -> sameDriver(e, candidate)).toList();
            Change change = new Change(candidate, matches);
            if (matches.stream().anyMatch(e -> !e.directory().equals(candidate.directory()))) {
                throw new IOException("依赖目录不一致: " + candidate.name());
            }
            if (change.conflicts() && (!candidate.jdbc() || matches.stream().anyMatch(e -> !e.jdbc()))) {
                throw new IOException("连接器或其他依赖冲突，请单独处理: " + candidate.name());
            }
            changes.add(change);
        }
        return new Plan(directory, List.copyOf(changes));
    }

    /**
     * KEEP 完全跳过对应驱动，REPLACE 先写入新文件再删除不同文件名的旧版本
     * 不备份或回滚，失败由调用方报告实际发生的位置
     */
    public static synchronized void apply(Plan plan, Map<String, Choice> choices) throws IOException {
        for (var change : plan.changes()) {
            if (choices.get(change.incoming().name()) == Choice.KEEP) continue;
            if (change.conflicts() && choices.get(change.incoming().name()) != Choice.REPLACE) {
                throw new IOException("驱动文件已存在，请先确认处理方式: " + change.incoming().name());
            }
        }
        for (var change : plan.changes()) {
            var incoming = change.incoming();
            if (choices.get(incoming.name()) == Choice.KEEP) continue;
            if (!change.conflicts() && !change.existing().isEmpty()) continue;
            Path target = plan.directory().resolve(incoming.directory()).resolve(incoming.name());
            safePath(target);
            Files.createDirectories(target.getParent());
            copyVerified(incoming.file(), target, incoming.checksum());
            for (var old : change.existing()) {
                if (!old.file().equals(target)) Files.delete(old.file());
            }
        }
    }

    public static Artifact inspect(Path file, String name, String directory) throws IOException {
        Set<String> identities = new HashSet<>();
        identities.add("artifact:" + name.replaceFirst("-\\d.*\\.jar$", ""));
        String version = name;
        boolean jdbc = false;
        try (JarFile jar = new JarFile(file.toFile())) {
            if (jar.stream().anyMatch(e -> e.getName().startsWith("plugin/") && e.getName().endsWith(".json"))) {
                throw new IOException("sync 资源不能包含 DE 插件主包: " + name);
            }
            boolean connector = name.startsWith("connector-");
            for (String service : List.of("org.apache.seatunnel.api.source.SeaTunnelSource", "org.apache.seatunnel.api.sink.SeaTunnelSink",
                    "org.apache.seatunnel.api.table.factory.TableSourceFactory", "org.apache.seatunnel.api.table.factory.TableSinkFactory")) {
                connector |= jar.getJarEntry("META-INF/services/" + service) != null;
            }
            if (directory == null) directory = connector ? "connectors" : "lib";
            if (connector && !directory.equals("connectors")) throw new IOException("连接器应放入 sync/connectors: " + name);
            var service = jar.getJarEntry("META-INF/services/java.sql.Driver");
            if (service != null) try (InputStream input = jar.getInputStream(service)) {
                List<String> drivers = new String(input.readAllBytes(), StandardCharsets.UTF_8).lines()
                        .map(line -> line.split("#", 2)[0].trim()).filter(line -> !line.isEmpty()).toList();
                jdbc = !connector && directory.equals("lib") && !drivers.isEmpty();
                drivers.forEach(driver -> identities.add("jdbc:" + driver));
            }
            if (jar.getManifest() != null) {
                var attributes = jar.getManifest().getMainAttributes();
                for (String key : List.of("Implementation-Version", "Bundle-Version", "Specification-Version")) {
                    String value = attributes.getValue(key);
                    if (value != null && !value.isBlank()) { version = value; break; }
                }
            }
            for (var entry : jar.stream().filter(e -> e.getName().startsWith("META-INF/maven/")
                    && e.getName().endsWith("/pom.properties")).toList()) {
                Properties properties = new Properties();
                try (InputStream input = jar.getInputStream(entry)) { properties.load(input); }
                String artifact = properties.getProperty("artifactId");
                if (artifact != null && (name.equals(artifact + ".jar") || name.startsWith(artifact + "-"))) {
                    identities.add("maven:" + properties.getProperty("groupId", "") + ":" + artifact);
                    version = properties.getProperty("version", version);
                }
            }
        }
        return new Artifact(file, name, directory, Set.copyOf(identities), jdbc, version, checksum(file));
    }

    public static boolean sameDriver(Artifact first, Artifact second) {
        return first.name().equals(second.name()) || !Collections.disjoint(first.identities(), second.identities());
    }

    public static String checksum(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = input.read(buffer)) != -1) digest.update(buffer, 0, length);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    public static void copyVerified(Path source, Path target, String checksum) throws IOException {
        Path temporary = Files.createTempFile(target.getParent(), ".sync-copy-", ".tmp");
        try {
            Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING);
            if (!checksum(temporary).equals(checksum)) throw new IOException("驱动复制校验失败");
            try { Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }

    private static void safePath(Path path) throws IOException {
        for (Path current = path.toAbsolutePath(); current != null; current = current.getParent()) {
            if (Files.isSymbolicLink(current)) throw new IOException("依赖路径不能为符号链接: " + path);
        }
    }

    public static void deleteTree(Path path) throws IOException {
        if (!Files.exists(path)) return;
        try (var files = Files.walk(path)) {
            for (Path file : files.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(file);
        }
    }
}

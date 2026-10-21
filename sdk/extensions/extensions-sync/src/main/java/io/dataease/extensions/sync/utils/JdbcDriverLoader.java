package io.dataease.extensions.sync.utils;

import io.dataease.extensions.datasource.provider.ExtendedJdbcClassLoader;
import io.dataease.extensions.datasource.utils.SpringContextUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author jianneng
 * @date 2025/11/12 16:12
 **/
public final class JdbcDriverLoader {
    private static final String DEFAULT_DRIVER_PATH = "/opt/dataease3.0/drivers";
    private static final Path LEGACY_DRIVER_PATH = Paths.get(DEFAULT_DRIVER_PATH).toAbsolutePath().normalize();
    /**
     * key 可以是 dirPath（默认复用）或 dirPath + "::" + driverClassName（按驱动隔离）
     */
    private static final ConcurrentHashMap<String, ExtendedJdbcClassLoader> CACHE = new ConcurrentHashMap<>();

    /**
     * 目录与驱动名的分隔符
     */
    private static final String SEP = "::";

    private JdbcDriverLoader() {
    }

    /**
     * 按目录获取或创建 ExtendedJdbcClassLoader（默认复用所有驱动）
     */
    public static ExtendedJdbcClassLoader getOrCreate(String dirPath) {
        return getOrCreate(dirPath, null);
    }

    /**
     * 按目录+驱动名获取或创建 ExtendedJdbcClassLoader
     * 如果 driverClassName 为 null 或空串，则等同于按目录复用
     * 使用场景：
     * - 想复用目录下所有驱动：调用 getOrCreate(dirPath)
     * - 想为某个具体驱动做类隔离：调用 getOrCreate(dirPath, "com.vendor.Driver")
     */
    public static ExtendedJdbcClassLoader getOrCreate(String dirPath, String driverClassName) {
        Objects.requireNonNull(dirPath, "driver path is null");
        if (dirPath.isEmpty()) {
            throw new IllegalArgumentException("driver path is empty");
        }
        String resolvedDirPath = resolveSelectedDirectory(dirPath, driverClassName);
        String key = (driverClassName == null || driverClassName.isEmpty())
                ? resolvedDirPath : resolvedDirPath + SEP + driverClassName;
        return CACHE.computeIfAbsent(key, k -> {
            try {
                return createLoader(resolvedDirPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 清理按目录缓存（仅清理目录通用的 loader）
     */
    public static void clear(String dirPath) {
        if (dirPath == null) {
            return;
        }
        CACHE.remove(resolveDriverDirectory(dirPath));
    }

    /**
     * 清理按目录+驱动名缓存（用于隔离的 key）
     */
    public static void clear(String dirPath, String driverClassName) {
        if (dirPath == null || driverClassName == null) {
            return;
        }
        CACHE.remove(resolveSelectedDirectory(dirPath, driverClassName) + SEP + driverClassName);
    }

    /**
     * 兼容旧插件调用签名，驱动选择在加载器创建前解析，已有加载器不热切换
     */
    static String resolveSelectedDirectory(String path, String driverClass) {
        Path requested = Path.of(resolveDriverDirectory(path));
        if (driverClass == null || driverClass.isBlank()) return requested.toString();
        try {
            Path root = requested.getFileName().toString().equals("lib")
                    ? requested.getParent().getParent() : requested;
            java.util.List<Path> directories = new java.util.ArrayList<>();
            directories.add(root);
            if (java.nio.file.Files.isDirectory(root)) try (var children = java.nio.file.Files.list(root)) {
                children.filter(java.nio.file.Files::isDirectory).map(p -> p.resolve("lib"))
                        .filter(java.nio.file.Files::isDirectory).sorted().forEach(directories::add);
            }
            Path selected = null;
            String checksum = null;
            for (Path directory : directories) try (var files = java.nio.file.Files.list(directory)) {
                for (Path file : files.filter(p -> p.toString().endsWith(".jar")).toList()) {
                    var artifact = SyncDependencyDirectory.inspect(file, file.getFileName().toString(), "lib");
                    if (!artifact.identities().contains("jdbc:" + driverClass)) continue;
                    if (checksum != null && !checksum.equals(artifact.checksum())) {
                        throw new IOException("同一驱动存在多个版本，请更新同步插件并选择覆盖: " + driverClass);
                    }
                    selected = directory;
                    checksum = artifact.checksum();
                }
            }
            return selected == null ? requested.toString() : selected.toString();
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * 同步旧路径映射到持久化目录，其他历史路径仍遵循 dataease.path.driver
     */
    static String resolveDriverDirectory(String dirPath) {
        Path input = Paths.get(dirPath).toAbsolutePath().normalize();
        if (input.startsWith(LEGACY_DRIVER_PATH.resolve("plugin/sync"))
                || input.startsWith(Path.of("/opt/dataease3.0/data/driver/plugin/sync"))) {
            return io.dataease.extensions.sync.plugin.DataEaseSyncDatasourcePlugin.resolveDriverDirectory(dirPath).toString();
        }
        String applicationDriverPath = DEFAULT_DRIVER_PATH;
        if (SpringContextUtil.getApplicationContext() != null) {
            applicationDriverPath = SpringContextUtil.getApplicationContext().getEnvironment()
                    .getProperty("dataease.path.driver", DEFAULT_DRIVER_PATH);
        }
        Path requestedPath = Paths.get(dirPath).toAbsolutePath().normalize();
        if (!requestedPath.startsWith(LEGACY_DRIVER_PATH)) {
            return requestedPath.toString();
        }
        Path currentDriverPath = Paths.get(applicationDriverPath).toAbsolutePath().normalize();
        return currentDriverPath.resolve(LEGACY_DRIVER_PATH.relativize(requestedPath)).normalize().toString();
    }

    private static ExtendedJdbcClassLoader createLoader(String dirPath) throws IOException {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException("driver path not found or not directory: " + dirPath);
        }
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        URL[] urls = new URL[]{dir.toURI().toURL()};
        ExtendedJdbcClassLoader loader = new ExtendedJdbcClassLoader(urls, parent);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().endsWith(".jar")) {
                    try {
                        loader.addFile(f);
                    } catch (IOException e) {
                        loader.close();
                        throw e;
                    }
                }
            }
        }
        return loader;
    }
}

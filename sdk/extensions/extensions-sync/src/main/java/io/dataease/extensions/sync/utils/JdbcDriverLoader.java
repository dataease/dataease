package io.dataease.extensions.sync.utils;

import io.dataease.extensions.datasource.provider.ExtendedJdbcClassLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author jianneng
 * @date 2025/11/12 16:12
 **/
public final class JdbcDriverLoader {
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
        String key = (driverClassName == null || driverClassName.isEmpty()) ? dirPath : dirPath + SEP + driverClassName;
        return CACHE.computeIfAbsent(key, k -> {
            try {
                return createLoader(dirPath);
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
        CACHE.remove(dirPath);
    }

    /**
     * 清理按目录+驱动名缓存（用于隔离的 key）
     */
    public static void clear(String dirPath, String driverClassName) {
        if (dirPath == null || driverClassName == null) {
            return;
        }
        CACHE.remove(dirPath + SEP + driverClassName);
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
                    } catch (IOException ignored) {

                    }
                }
            }
        }
        return loader;
    }
}

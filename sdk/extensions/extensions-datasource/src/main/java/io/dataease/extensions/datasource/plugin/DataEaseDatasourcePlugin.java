package io.dataease.extensions.datasource.plugin;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.factory.ProviderFactory;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.datasource.utils.SpringContextUtil;
import io.dataease.extensions.datasource.vo.XpackPluginsDatasourceVO;
import io.dataease.license.utils.JsonUtil;
import io.dataease.plugins.template.DataEasePlugin;
import io.dataease.plugins.vo.DataEasePluginVO;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * @Author Junjun
 */
public abstract class DataEaseDatasourcePlugin extends Provider implements DataEasePlugin {
    private static final String DEFAULT_DRIVER_PATH = "/opt/dataease3.0/drivers";
    private static final Path LEGACY_PLUGIN_DRIVER_PATH = Paths.get(DEFAULT_DRIVER_PATH, "plugin")
            .toAbsolutePath().normalize();
    private static final Pattern SAFE_DRIVER_FILE_NAME = Pattern.compile("[A-Za-z0-9._-]+\\.jar");


    @Override
    public List<String> getSchema(DatasourceRequest datasourceRequest) {
        return new ArrayList<>();
    }


    @Override
    public void loadPlugin() {
        XpackPluginsDatasourceVO datasourceConfig = getConfig();
        ProviderFactory.loadPlugin(datasourceConfig.getType(), this);
        try {
            loadDriver();
        } catch (Exception e) {
            DEException.throwException(e);
        }
    }

    private void loadDriver() throws Exception {
        XpackPluginsDatasourceVO config = getConfig();
        Path localPath = resolveDriverDirectory(config.getDriverPath());
        ProtectionDomain protectionDomain = this.getClass().getProtectionDomain();
        URI uri = protectionDomain.getCodeSource().getLocation().toURI();
        try (JarFile jarFile = new JarFile(new File(uri))) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!entry.isDirectory() && StringUtils.endsWith(entry.getName(), ".jar")) {
                    Path file = resolveDriverPath(localPath, entry);
                    Files.createDirectories(file.getParent());

                    try (InputStream inputStream = jarFile.getInputStream(entry);
                         FileOutputStream outputStream = new FileOutputStream(file.toFile())) {
                        byte[] bytes = new byte[1024];
                        int length;
                        while ((length = inputStream.read(bytes)) >= 0) {
                            outputStream.write(bytes, 0, length);
                        }
                    }
                }
            }
        }
    }

    public DataEasePluginVO getPluginMetadata() {
        return DatasourcePluginInfoLoader.load(this);
    }

    public XpackPluginsDatasourceVO getConfig() {
        DataEasePluginVO pluginInfo = getPluginMetadata();
        String config = pluginInfo.getConfig();
        XpackPluginsDatasourceVO vo = JsonUtil.parseObject(config, XpackPluginsDatasourceVO.class);
        vo.setIcon(pluginInfo.getIcon());
        return vo;
    }

    /**
     * 解析插件 JDBC 驱动的实际落盘目录。
     *
     * <p>历史插件元数据可能保存 /opt/dataease3.0/drivers/plugin 下的绝对路径。迁移到自定义目录后，
     * 这类“默认根目录路径”要等价映射到 dataease.path.driver/plugin，并保留其相对子目录；
     * 用户显式配置的其他外部路径则保持不变。加载和卸载共用该方法，避免从新目录加载却误删旧目录文件。</p>
     */
    private Path resolveDriverDirectory(String configuredPath) {
        String driverPath = DEFAULT_DRIVER_PATH;
        if (SpringContextUtil.getApplicationContext() != null) {
            driverPath = SpringContextUtil.getApplicationContext().getEnvironment()
                    .getProperty("dataease.path.driver", DEFAULT_DRIVER_PATH);
        }
        Path applicationPluginPath = Paths.get(driverPath, "plugin").toAbsolutePath().normalize();
        if (StringUtils.isBlank(configuredPath)) {
            return applicationPluginPath;
        }

        Path pluginPath = Paths.get(configuredPath).toAbsolutePath().normalize();
        if (pluginPath.startsWith(LEGACY_PLUGIN_DRIVER_PATH)) {
            // 例如 /opt/dataease3.0/drivers/plugin/dmDriver -> ${dataease.path.driver}/plugin/dmDriver。
            return applicationPluginPath.resolve(LEGACY_PLUGIN_DRIVER_PATH.relativize(pluginPath)).normalize();
        }
        return pluginPath;
    }

    private Path resolveDriverPath(Path localPath, JarEntry entry) {
        String fileName = extractSafeDriverFileName(entry);
        Path targetFile = localPath.resolve(fileName).normalize();
        if (!targetFile.startsWith(localPath)) {
            DEException.throwException("Invalid driver file path");
        }
        return targetFile;
    }

    private String extractSafeDriverFileName(JarEntry entry) {
        String entryName = entry.getName();
        if (StringUtils.isBlank(entryName)) {
            DEException.throwException("Invalid driver entry path");
        }
        String normalizedEntryName = entryName.replace('\\', '/');
        if (StringUtils.contains(normalizedEntryName, "..")
                || StringUtils.startsWith(normalizedEntryName, "/")
                || StringUtils.startsWith(normalizedEntryName, "\\")
                || StringUtils.contains(normalizedEntryName, ":")) {
            DEException.throwException("Invalid driver entry path");
        }
        Path normalizedEntryPath = Paths.get(normalizedEntryName).normalize();
        Path fileNamePath = normalizedEntryPath.getFileName();
        if (fileNamePath == null) {
            DEException.throwException("Invalid driver entry path");
        }
        String fileName = fileNamePath.toString();
        if (!SAFE_DRIVER_FILE_NAME.matcher(fileName).matches()) {
            DEException.throwException("Invalid driver file name");
        }
        return fileName;
    }

    @Override
    public void unloadPlugin() {
        try {
            Path localPath = resolveDriverDirectory(getConfig().getDriverPath());
            ProtectionDomain protectionDomain = this.getClass().getProtectionDomain();
            URI uri = protectionDomain.getCodeSource().getLocation().toURI();
            try (JarFile jarFile = new JarFile(new File(uri))) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (!entry.isDirectory() && StringUtils.endsWith(entry.getName(), ".jar")) {
                        Path file = resolveDriverPath(localPath, entry);
                        Files.deleteIfExists(file);
                    }
                }
            }
        } catch (Exception e) {
            DEException.throwException(e);
        }
    }
}

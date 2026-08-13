package io.dataease.extensions.datasource.plugin;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.factory.ProviderFactory;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.datasource.vo.XpackPluginsDatasourceVO;
import io.dataease.license.utils.JsonUtil;
import io.dataease.plugins.template.DataEasePlugin;
import io.dataease.plugins.vo.DataEasePluginVO;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private final String DEFAULT_FILE_PATH = "/opt/dataease2.0/drivers/plugin";
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
        String localPath = StringUtils.isEmpty(config.getDriverPath()) ? DEFAULT_FILE_PATH : config.getDriverPath();
        ProtectionDomain protectionDomain = this.getClass().getProtectionDomain();
        URI uri = protectionDomain.getCodeSource().getLocation().toURI();
        try (JarFile jarFile = new JarFile(new File(uri))) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
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

    @Override
    public void unloadPlugin() {
        try {
            ProtectionDomain protectionDomain = this.getClass().getProtectionDomain();
            URI uri = protectionDomain.getCodeSource().getLocation().toURI();
            try (JarFile jarFile = new JarFile(new File(uri))) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (!entry.isDirectory() && StringUtils.endsWith(entry.getName(), ".jar")) {
                        Path file = resolveDriverPath(DEFAULT_FILE_PATH, entry);
                        Files.deleteIfExists(file);
                    }
                }
            }
        } catch (Exception e) {
            DEException.throwException(e);
        }
    }

    private Path resolveDriverPath(String localPath, JarEntry entry) {
        String fileName = extractSafeDriverFileName(entry);
        Path targetDirectory = Paths.get(localPath).toAbsolutePath().normalize();
        Path targetFile = targetDirectory.resolve(fileName).normalize();
        if (!targetFile.startsWith(targetDirectory)) {
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
}

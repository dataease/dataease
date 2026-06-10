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
                String name = entry.getName();
                if (!entry.isDirectory() && StringUtils.endsWith(name, ".jar")) {
                    String fileName = extractSafeDriverFileName(name);
                    File file = resolveDriverFile(localPath, fileName);
                    if (!file.getParentFile().exists()) {
                        if (!file.getParentFile().mkdirs()) {
                            DEException.throwException("Failed to create driver directory");
                        }
                    }

                    try (InputStream inputStream = jarFile.getInputStream(entry);
                         FileOutputStream outputStream = new FileOutputStream(file)) {
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
                    String name = entry.getName();
                    if (!entry.isDirectory() && StringUtils.endsWith(name, ".jar")) {
                        String fileName = extractSafeDriverFileName(name);
                        File file = resolveDriverFile(DEFAULT_FILE_PATH, fileName);
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            DEException.throwException(e);
        }
    }

    private String extractSafeDriverFileName(String entryName) {
        if (StringUtils.isBlank(entryName)
                || StringUtils.contains(entryName, "..")
                || StringUtils.startsWith(entryName, "/")
                || StringUtils.startsWith(entryName, "\\")
                || StringUtils.contains(entryName, ":")) {
            DEException.throwException("Invalid driver entry path");
        }
        String normalizedEntryName = entryName.replace('\\', '/');
        int lastSeparatorIndex = normalizedEntryName.lastIndexOf('/');
        String fileName = lastSeparatorIndex >= 0 ? normalizedEntryName.substring(lastSeparatorIndex + 1) : normalizedEntryName;
        if (!SAFE_DRIVER_FILE_NAME.matcher(fileName).matches()) {
            DEException.throwException("Invalid driver file name");
        }
        return fileName;
    }

    private File resolveDriverFile(String localPath, String fileName) {
        File dirFile = new File(localPath);
        File file = new File(dirFile, fileName);
        try {
            if (!file.getCanonicalPath().startsWith(dirFile.getCanonicalPath() + File.separator)) {
                DEException.throwException("Invalid driver file path");
            }
        } catch (IOException e) {
            DEException.throwException(e);
        }
        return file;
    }
}

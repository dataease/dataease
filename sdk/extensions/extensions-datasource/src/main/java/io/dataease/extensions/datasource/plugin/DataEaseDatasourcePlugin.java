package io.dataease.extensions.datasource.plugin;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.factory.ProviderFactory;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.datasource.vo.XpackPluginsDatasourceVO;
import io.dataease.license.utils.JsonUtil;
import io.dataease.plugins.template.DataEasePlugin;
import io.dataease.plugins.vo.DataEasePluginVO;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @Author Junjun
 */
public abstract class DataEaseDatasourcePlugin extends Provider implements DataEasePlugin {
    private final String DEFAULT_FILE_PATH = "/opt/dataease2.0/drivers/plugin";

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
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL[] urls = ((URLClassLoader) classLoader).getURLs();
        String jarPath = urls[0].getPath();
        JarFile jarFile = new JarFile(jarPath);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = (JarEntry) entries.nextElement();
            String name = entry.getName();
            if (StringUtils.endsWith(name, ".jar")) {
                InputStream inputStream = jarFile.getInputStream(entry);
                File file = new File(localPath, name.substring(name.indexOf("/") + 1));
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = inputStream.read(bytes)) >= 0) {
                    outputStream.write(bytes, 0, length);
                }
            }
        }
    }

    public XpackPluginsDatasourceVO getConfig() {
        DataEasePluginVO pluginInfo = null;
        try {
            pluginInfo = getPluginInfo();
        } catch (Exception e) {
            DEException.throwException(e);
        }
        String config = pluginInfo.getConfig();
        XpackPluginsDatasourceVO vo = JsonUtil.parseObject(config, XpackPluginsDatasourceVO.class);
        vo.setIcon(pluginInfo.getIcon());
        return vo;
    }

    @Override
    public void unloadPlugin() {
        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            URL[] urls = ((URLClassLoader) classLoader).getURLs();
            String jarPath = urls[0].getPath();
            JarFile jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                String name = entry.getName();
                if (StringUtils.endsWith(name, ".jar")) {
                    File file = new File(DEFAULT_FILE_PATH, name.substring(name.indexOf("/") + 1));
                    file.delete();
                }
            }
        } catch (Exception e) {
            DEException.throwException(e);
        }
    }
}

package io.dataease.extensions.sync.plugin;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.utils.SpringContextUtil;
import io.dataease.extensions.sync.factory.SyncProviderFactory;
import io.dataease.extensions.sync.model.datasource.DatasourceRequest;
import io.dataease.extensions.sync.provider.SyncProvider;
import io.dataease.extensions.sync.vo.XpackPluginsSyncDatasourceVO;
import io.dataease.license.utils.JsonUtil;
import io.dataease.plugins.template.DataEasePlugin;
import io.dataease.plugins.vo.DataEasePluginVO;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * DataEase同步数据源插件抽象类
 *
 * @author jianneng
 **/
public abstract class DataEaseSyncDatasourcePlugin extends SyncProvider implements DataEasePlugin {
    private static final String DEFAULT_DRIVER_PATH = "/opt/dataease3.0/drivers";
    private static final Path LEGACY_PLUGIN_DRIVER_PATH = Paths.get(DEFAULT_DRIVER_PATH, "plugin")
            .toAbsolutePath().normalize();

    @Override
    public List<String> getSchema(DatasourceRequest datasourceRequest) {
        return new ArrayList<>();
    }

    @Override
    public void loadPlugin() {
        XpackPluginsSyncDatasourceVO datasourceConfig = getConfig();
        try {
            // 驱动成功解压后再注册 Provider，避免驱动目录不可用时留下“列表可见但无法校验”的半加载插件。
            loadDriver();
        } catch (Exception e) {
            DEException.throwException(e);
        }
        SyncProviderFactory.loadPlugin(datasourceConfig.getType(), datasourceConfig.getDatasourceRole(), this);
    }

    private void loadDriver() throws Exception {
        XpackPluginsSyncDatasourceVO config = getConfig();
        Path localPath = resolveDriverDirectory(config.getDriverPath());
        ProtectionDomain protectionDomain = this.getClass().getProtectionDomain();
        URI uri = protectionDomain.getCodeSource().getLocation().toURI();
        try (JarFile jarFile = new JarFile(new File(uri))) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (StringUtils.endsWith(name, ".jar")) {
                    File file = localPath.resolve(Paths.get(name).getFileName().toString()).toFile();
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
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

    public XpackPluginsSyncDatasourceVO getConfig() {
        DataEasePluginVO pluginInfo = null;
        try {
            pluginInfo = getPluginInfo();
        } catch (Exception e) {
            DEException.throwException(e);
        }
        String config = pluginInfo.getConfig();
        XpackPluginsSyncDatasourceVO vo = JsonUtil.parseObject(config, XpackPluginsSyncDatasourceVO.class);
        vo.setIcon(pluginInfo.getIcon());
        return vo;
    }

    /**
     * 解析同步插件 JDBC 驱动的实际目录。
     *
     * <p>V2/V3 历史插件配置可能持久化 /opt/dataease3.0/drivers/plugin 下的绝对路径。
     * 当迁移后的 V3 使用 dataease.path.driver 指向独立目录时，将默认根目录及其相对子目录映射过去；
     * 非默认根目录视为用户显式配置，保持原值。卸载阶段使用同一规则，确保只清理本次实际加载的文件。</p>
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
            // PostgreSQL 源/目标插件共享 sync 子目录，映射后仍保留该相对层级。
            return applicationPluginPath.resolve(LEGACY_PLUGIN_DRIVER_PATH.relativize(pluginPath)).normalize();
        }
        return pluginPath;
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
                    if (StringUtils.endsWith(name, ".jar")) {
                        File file = localPath.resolve(Paths.get(name).getFileName().toString()).toFile();
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            DEException.throwException(e);
        }
    }
}

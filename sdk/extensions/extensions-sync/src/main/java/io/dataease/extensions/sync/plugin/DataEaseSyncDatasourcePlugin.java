package io.dataease.extensions.sync.plugin;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.utils.SpringContextUtil;
import io.dataease.extensions.sync.factory.SyncProviderFactory;
import io.dataease.extensions.sync.utils.SyncDependencyDirectory;
import io.dataease.extensions.sync.model.datasource.DatasourceRequest;
import io.dataease.extensions.sync.provider.SyncProvider;
import io.dataease.extensions.sync.vo.XpackPluginsSyncDatasourceVO;
import io.dataease.license.utils.JsonUtil;
import io.dataease.plugins.template.DataEasePlugin;
import io.dataease.plugins.vo.DataEasePluginVO;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * DataEase同步数据源插件抽象类
 *
 * @author jianneng
 **/
public abstract class DataEaseSyncDatasourcePlugin extends SyncProvider implements DataEasePlugin {
    private static final String DEFAULT_DRIVER_PATH = "/opt/dataease3.0/drivers";
    private static final Path LEGACY_PLUGIN_DRIVER_PATH = Paths.get(DEFAULT_DRIVER_PATH, "plugin")
            .toAbsolutePath().normalize();

    private static final Path PREVIOUS_SYNC_PATH = Paths.get("/opt/dataease3.0/data/driver/plugin/sync");
    private static final Path SYNC_PATH = Paths.get("/opt/dataease3.0/data/plugin/sync-drivers");

    @Override
    public List<String> getSchema(DatasourceRequest datasourceRequest) {
        return new ArrayList<>();
    }

    @Override
    public void loadPlugin() {
        XpackPluginsSyncDatasourceVO datasourceConfig = getConfig();
        SyncProviderFactory.loadPlugin(datasourceConfig.getType(), datasourceConfig.getDatasourceRole(), this);
    }

    /**
     * 同一种数据库的源端和目标端使用同一 lib 目录，缺失时兼容历史平铺路径
     */
    protected Path getDriverDirectory() {
        XpackPluginsSyncDatasourceVO config = getConfig();
        Path root = resolveDriverDirectory(config.getDriverPath());
        try {
            Path directory = root.resolve(SyncDependencyDirectory.databaseType(config.getType())).resolve("lib");
            return java.nio.file.Files.isDirectory(directory) ? directory : root;
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    public DataEasePluginVO getPluginInfo() throws Exception {
        return SyncPluginInfoLoader.load(getClass());
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
     * 同步依赖复用插件持久化目录，避开 data/driver 的应用扩展类路径
     */
    public static Path resolveDriverDirectory(String configuredPath) {
        Path legacy = resolveLegacyDriverDirectory(configuredPath);
        Path oldSync = LEGACY_PLUGIN_DRIVER_PATH.resolve("sync");
        if (legacy.startsWith(oldSync)) return SYNC_PATH.resolve(oldSync.relativize(legacy));
        if (legacy.startsWith(PREVIOUS_SYNC_PATH)) return SYNC_PATH.resolve(PREVIOUS_SYNC_PATH.relativize(legacy));
        return legacy;
    }

    /**
     * 解析历史配置路径，保留明确设置的自定义目录
     */
    private static Path resolveLegacyDriverDirectory(String configuredPath) {
        String driverPath = DEFAULT_DRIVER_PATH;
        if (SpringContextUtil.getApplicationContext() != null) {
            driverPath = SpringContextUtil.getApplicationContext().getEnvironment()
                    .getProperty("dataease.path.driver", DEFAULT_DRIVER_PATH);
        }
        Path applicationPluginPath = Paths.get(driverPath, "plugin").toAbsolutePath().normalize();
        if (StringUtils.isBlank(configuredPath)) return applicationPluginPath.resolve("sync");
        Path pluginPath = Paths.get(configuredPath).toAbsolutePath().normalize();
        return pluginPath.startsWith(LEGACY_PLUGIN_DRIVER_PATH)
                ? applicationPluginPath.resolve(LEGACY_PLUGIN_DRIVER_PATH.relativize(pluginPath)).normalize()
                : pluginPath;
    }

    @Override
    public void unloadPlugin() {
        // 同数据库另一端仍可能使用驱动，卸载不删除共享依赖
    }
}

package io.dataease.extensions.sync.factory;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.utils.SpringContextUtil;
import io.dataease.extensions.sync.plugin.DataEaseSyncDatasourcePlugin;
import io.dataease.extensions.sync.provider.SyncProvider;
import io.dataease.extensions.sync.vo.DatasourceConfiguration;
import io.dataease.extensions.sync.vo.XpackPluginsSyncDatasourceVO;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.license.utils.LogUtil;
import io.dataease.plugins.factory.DataEasePluginFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 同步数据源提供者工厂
 *
 * @author jianneng
 */
public class SyncProviderFactory {

    /**
     * 同步数据源插件缓存
     */
    private static final Map<String, DataEaseSyncDatasourcePlugin> SYNC_DATASOURCE_PLUGIN_MAP = new ConcurrentHashMap<>();

    /**
     * 获取数据源提供者
     *
     * @param type           数据源类型
     * @param datasourceRole 数据源角色
     * @return 数据源提供者
     * @throws DEException 异常
     */
    public static SyncProvider getProvider(String type, Integer datasourceRole) throws DEException {
        // 先尝试通过内置枚举找到匹配的提供者
        DatasourceConfiguration.DatasourceType matched = Arrays.stream(DatasourceConfiguration.DatasourceType.values())
                .filter(t -> Objects.equals(t.getDatasourceRole(), datasourceRole))
                .filter(t -> Objects.equals(t.getType(), type))
                .findFirst()
                .orElse(null);

        if (matched != null) {
            return SpringContextUtil.getApplicationContext()
                    .getBean(matched.getProviderClassName(), SyncProvider.class);
        }

        // 回退到插件实例
        SyncProvider instance = getInstance(type, datasourceRole);
        if (instance == null) {
            DEException.throwException("Plugin exception, please check the plugin.");
        }
        return instance;
    }

    /**
     * 获取数据源插件实例
     *
     * @param type           数据源类型
     * @param datasourceRole 数据源角色
     * @return 数据源插件实例
     */
    public static SyncProvider getInstance(String type, Integer datasourceRole) {
        String key = type + "-" + datasourceRole;
        return SYNC_DATASOURCE_PLUGIN_MAP.get(key);
    }

    /**
     * 加载数据源插件
     *
     * @param type           数据源类型
     * @param datasourceRole 数据源角色
     * @param plugin         数据源插件实例
     */
    public static void loadPlugin(String type, Integer datasourceRole, DataEaseSyncDatasourcePlugin plugin) {
        if (!LicenseUtil.licenseValid()) {
            DEException.throwException("插件功能只对企业版本可用！");
        }
        String key = type + "-" + datasourceRole;
        if (SYNC_DATASOURCE_PLUGIN_MAP.containsKey(key)) {
            return;
        }
        SYNC_DATASOURCE_PLUGIN_MAP.put(key, plugin);
        try {
            String moduleName = plugin.getPluginInfo().getModuleName();
            DataEasePluginFactory.loadTemplate(moduleName, plugin);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), new Throwable(e));
            DEException.throwException(e);
        }
    }

    /**
     * 获取所有数据源插件配置
     *
     * @return 数据源插件配置列表
     */
    public static List<XpackPluginsSyncDatasourceVO> getDatasourceConfigList() {
        if (!LicenseUtil.licenseValid()) {
            DEException.throwException("插件功能只对企业版本可用！");
        }
        return SYNC_DATASOURCE_PLUGIN_MAP.values().stream().map(DataEaseSyncDatasourcePlugin::getConfig).toList();
    }
}

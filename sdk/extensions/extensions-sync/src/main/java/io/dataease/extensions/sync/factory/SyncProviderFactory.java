package io.dataease.extensions.sync.factory;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.utils.SpringContextUtil;
import io.dataease.extensions.sync.model.datasource.DatasourceRole;
import io.dataease.extensions.sync.plugin.DataEaseSyncDatasourcePlugin;
import io.dataease.extensions.sync.provider.SinkProvider;
import io.dataease.extensions.sync.provider.SourceProvider;
import io.dataease.extensions.sync.provider.SyncProvider;
import io.dataease.extensions.sync.vo.DatasourceConfiguration;
import io.dataease.extensions.sync.vo.XpackPluginsSyncDatasourceVO;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.license.utils.LogUtil;
import io.dataease.plugins.factory.DataEasePluginFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
        validateDatasourceIdentity(type, datasourceRole);
        // 先尝试通过内置枚举找到匹配的提供者
        DatasourceConfiguration.DatasourceType matched = Arrays.stream(DatasourceConfiguration.DatasourceType.values())
                .filter(t -> Objects.equals(t.getDatasourceRole(), datasourceRole))
                .filter(t -> t.getType().equalsIgnoreCase(type.trim()))
                .findFirst()
                .orElse(null);

        if (matched != null) {
            return SpringContextUtil.getApplicationContext()
                    .getBean(matched.getProviderClassName(), SyncProvider.class);
        }

        // 回退到插件实例
        SyncProvider instance = getInstance(type, datasourceRole);
        if (instance == null) {
            DEException.throwException("同步数据源插件无效或未加载：type=" + type.trim()
                    + "，datasourceRole=" + DatasourceRole.displayName(datasourceRole));
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
        return SYNC_DATASOURCE_PLUGIN_MAP.get(providerKey(type, datasourceRole));
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
        validateDatasourceIdentity(type, datasourceRole);
        validatePluginRole(datasourceRole, plugin);
        String key = providerKey(type, datasourceRole);
        DataEaseSyncDatasourcePlugin loadedPlugin = SYNC_DATASOURCE_PLUGIN_MAP.get(key);
        if (loadedPlugin == plugin) {
            return;
        }
        if (loadedPlugin != null) {
            DEException.throwException("同步数据源插件角色冲突：type=" + type.trim()
                    + "，datasourceRole=" + DatasourceRole.displayName(datasourceRole)
                    + "，已加载=" + loadedPlugin.getClass().getName()
                    + "，待加载=" + plugin.getClass().getName());
        }
        try {
            String moduleName = plugin.getPluginInfo().getModuleName();
            DataEasePluginFactory.loadTemplate(moduleName, plugin);
            // 模板注册成功后再对业务开放 Provider，避免半加载插件出现在数据源类型列表中。
            SYNC_DATASOURCE_PLUGIN_MAP.put(key, plugin);
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

    private static String providerKey(String type, Integer datasourceRole) {
        validateDatasourceIdentity(type, datasourceRole);
        return type.trim().toLowerCase(Locale.ROOT) + "-" + datasourceRole;
    }

    private static void validateDatasourceIdentity(String type, Integer datasourceRole) {
        if (type == null || type.isBlank()) {
            DEException.throwException("同步数据源类型不能为空");
        }
        if (!DatasourceRole.isValid(datasourceRole)) {
            DEException.throwException("同步数据源角色无效：datasourceRole="
                    + DatasourceRole.displayName(datasourceRole) + "，仅支持源端(1)或目标端(2)");
        }
    }

    private static void validatePluginRole(Integer datasourceRole, DataEaseSyncDatasourcePlugin plugin) {
        if (datasourceRole == DatasourceRole.SOURCE && !(plugin instanceof SourceProvider)) {
            DEException.throwException("同步源数据源插件必须实现 SourceProvider：" + plugin.getClass().getName());
        }
        if (datasourceRole == DatasourceRole.TARGET && !(plugin instanceof SinkProvider)) {
            DEException.throwException("同步目标数据源插件必须实现 SinkProvider：" + plugin.getClass().getName());
        }
    }
}

package io.dataease.api.xpack.plugin.vo;

import java.util.List;

/**
 * 上传预检查结果，为前端提供确认标识、插件展示信息及两侧驱动冲突
 * 非同步插件沿原流程完成安装后返回空 operation，无需再次确认
 */
public record PluginInstallCheck(String operation, List<Conflict> conflicts, String databaseType, String pluginType, String databaseName) {
    public PluginInstallCheck(String operation, List<Conflict> conflicts) {
        this(operation, conflicts, null, null, null);
    }

    /**
     * 标注现有版本属于 DE 同步目录还是执行器，不展示物理文件位置
     */
    public record Location(String service, String version) {
    }

    /**
     * 单个上传驱动的版本对比，是否冲突由后端按文件内容判断
     */
    public record Conflict(String name, String currentVersion, String uploadedVersion, List<Location> locations) {
    }
}

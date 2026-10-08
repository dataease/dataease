package io.dataease.api.xpack.plugin.dto;

import java.util.Map;

/**
 * 提交一次暂存上传的驱动选择，choices 按上传文件名保存 KEEP 或 REPLACE
 * 同一选择由后端应用到 DE 同步目录和执行器，不携带客户端文件路径
 */
public record PluginInstallConfirm(String operation, Map<String, String> choices) {
}

package io.dataease.api.spreadsheet.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
public class PluginQueryRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String type;
    // 复制字段归属与历史字符串实例 ID 分离，旧请求可以不传。
    private Long fieldOwnerId;
    private PluginQueryDataConfig data;
    private Map<String, Object> queryConfig;
    private boolean plugin;
}

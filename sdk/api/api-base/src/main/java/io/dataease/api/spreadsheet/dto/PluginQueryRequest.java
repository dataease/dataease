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
    private PluginQueryDataConfig data;
    private Map<String, Object> queryConfig;
    private boolean plugin;
}

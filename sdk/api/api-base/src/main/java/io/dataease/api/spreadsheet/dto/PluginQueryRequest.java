package io.dataease.api.spreadsheet.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PluginQueryRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String type;
    private PluginQueryDataConfig data;
    private boolean plugin;
}

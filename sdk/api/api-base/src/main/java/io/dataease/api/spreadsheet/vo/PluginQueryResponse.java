package io.dataease.api.spreadsheet.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PluginQueryResponse implements Serializable {
    private PluginQueryDataResponse data;
    private String sql;
}

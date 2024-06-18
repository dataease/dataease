package io.dataease.plugins.view.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class PluginDatasetTableField implements Serializable {

    private String id;
    private String tableId;
    private String originName;
    private String name;
    private String dataeaseName;
    private String groupType;
    private String type;
    private Integer size;
    private Integer deType;
    private Integer deTypeFormat;
    private Integer deExtractType;
    private Integer extField;
    private Boolean checked;
    private Integer columnIndex;
    private Long lastSyncTime;
    private String dateFormat;
}

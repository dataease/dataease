package io.dataease.api.dataset.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class DataSQLBotDatasetVO implements Serializable {

    private String tableId;

    private String tableName;

    private String dsId;

    private String dsName;

}

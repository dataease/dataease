package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class DatasetSqlLog implements Serializable {
    private String id;

    private String datasetId;

    private Long startTime;

    private Long endTime;

    private Long spend;

    private String sql;

    private String status;

    private static final long serialVersionUID = 1L;
}
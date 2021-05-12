package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class DatasetTableTaskLog implements Serializable {
    private String id;

    private String tableId;

    private String taskId;

    private Long startTime;

    private Long endTime;

    private String status;

    private Long createTime;

    private String info;

    private static final long serialVersionUID = 1L;
}
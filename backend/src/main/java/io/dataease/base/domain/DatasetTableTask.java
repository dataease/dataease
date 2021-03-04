package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class DatasetTableTask implements Serializable {
    private String id;

    private String tableId;

    private String name;

    private String type;

    private Long startTime;

    private String rate;

    private String cron;

    private String end;

    private Long endTime;

    private Long createTime;

    private static final long serialVersionUID = 1L;
}
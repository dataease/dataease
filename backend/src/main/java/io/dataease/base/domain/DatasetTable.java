package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class DatasetTable implements Serializable {
    private String id;

    private String name;

    private String sceneId;

    private String dataSourceId;

    private String type;

    private Integer mode;

    private String createBy;

    private Long createTime;

    private String qrtzInstance;

    private String syncStatus;

    private Long lastUpdateTime;

    private String info;

    private static final long serialVersionUID = 1L;
}
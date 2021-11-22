package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class DatasetRowPermissions implements Serializable {
    private String id;

    private String authTargetType;

    private Long authTargetId;

    private String datasetId;

    private String datasetFieldId;

    private String filter;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}
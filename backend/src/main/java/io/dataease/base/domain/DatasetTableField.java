package io.dataease.base.domain;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatasetTableField implements Serializable {
    private String id;

    private String tableId;

    private String originName;

    private String name;

    private String type;

    private Boolean checked;

    private Integer columnIndex;

    private Long lastSyncTime;

    private Integer deType;

    private static final long serialVersionUID = 1L;
}

package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ChartViewField implements Serializable {
    private String id;

    private String tableId;

    private String chartId;

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

    private String originName;

    private static final long serialVersionUID = 1L;
}

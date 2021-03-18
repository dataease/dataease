package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelShare implements Serializable {
    private Long shareId;

    private String panelGroupId;

    private Long targetId;

    private Long createTime;

    private Integer type;

    private static final long serialVersionUID = 1L;
}
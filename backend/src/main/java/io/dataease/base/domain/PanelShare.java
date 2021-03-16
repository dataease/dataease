package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelShare implements Serializable {
    private Long shareId;

    private String panelGroupId;

    private Long userId;

    private Long createTime;

    private static final long serialVersionUID = 1L;
}
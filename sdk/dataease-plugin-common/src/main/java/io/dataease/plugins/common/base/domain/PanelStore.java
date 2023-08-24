package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelStore implements Serializable {
    private Long storeId;

    private String panelGroupId;

    private Long userId;

    private Long createTime;

    private static final long serialVersionUID = 1L;
}
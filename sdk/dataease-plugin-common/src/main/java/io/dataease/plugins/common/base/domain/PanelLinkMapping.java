package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelLinkMapping implements Serializable {
    private Long id;

    private String resourceId;

    private Long userId;

    private String uuid;

    private static final long serialVersionUID = 1L;
}
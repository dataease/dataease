package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelLinkMapping implements Serializable {
    private Long id;

    private String resourceId;

    private static final long serialVersionUID = 1L;
}
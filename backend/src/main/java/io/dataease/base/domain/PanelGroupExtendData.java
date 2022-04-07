package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelGroupExtendData implements Serializable {
    private String id;

    private String panelId;

    private String viewId;

    private String viewDetails;

    private static final long serialVersionUID = 1L;
}
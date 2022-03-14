package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelGroupExtend implements Serializable {
    private String id;

    private String panelId;

    private String templateId;

    private String templateVersion;

    private String templateDynamicData;

    private static final long serialVersionUID = 1L;
}
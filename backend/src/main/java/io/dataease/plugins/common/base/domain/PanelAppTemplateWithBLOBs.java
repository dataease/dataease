package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PanelAppTemplateWithBLOBs extends PanelAppTemplate implements Serializable {
    private String applicationInfo;

    private String panelInfo;

    private String viewsInfo;

    private String datasetInfo;

    private String datasetFieldsInfo;

    private String datasetTasksInfo;

    private String datasourceInfo;

    private String snapshot;

    private static final long serialVersionUID = 1L;
}
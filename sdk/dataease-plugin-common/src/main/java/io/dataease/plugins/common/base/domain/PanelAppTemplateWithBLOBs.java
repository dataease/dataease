package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PanelAppTemplateWithBLOBs extends PanelAppTemplate implements Serializable {
    private String icon;

    private String applicationInfo;

    private String panelInfo;

    private String panelViewsInfo;

    private String chartViewsInfo;

    private String chartViewFieldsInfo;

    private String datasetTablesInfo;

    private String datasetTableFieldsInfo;

    private String datasetTasksInfo;

    private String datasourceInfo;

    private String linkJumps;

    private String linkJumpInfos;

    private String linkages;

    private String linkageFields;

    private String snapshot;

    private static final long serialVersionUID = 1L;
}
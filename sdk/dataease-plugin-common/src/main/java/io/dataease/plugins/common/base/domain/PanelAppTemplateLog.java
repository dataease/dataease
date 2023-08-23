package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelAppTemplateLog implements Serializable {
    private String id;

    private String appTemplateId;

    private String appTemplateName;

    private String datasourceId;

    private String datasourceFrom;

    private String sourceDatasourceName;

    private String datasetGroupId;

    private String sourceDatasetGroupName;

    private String panelId;

    private String sourcePanelName;

    private Long applyTime;

    private String applyPersion;

    private Boolean isSuccess;

    private String remark;

    private static final long serialVersionUID = 1L;
}
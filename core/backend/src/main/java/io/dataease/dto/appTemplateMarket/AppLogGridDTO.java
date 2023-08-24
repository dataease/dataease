package io.dataease.dto.appTemplateMarket;

import io.dataease.plugins.common.base.domain.PanelAppTemplateLog;
import lombok.Data;

import java.io.Serializable;

@Data
public class AppLogGridDTO extends PanelAppTemplateLog implements Serializable {

    private String appName;

    private String datasourceName;

    private String datasetGroupName;

    private String panelName;

    private String panelGroupPid;

    private String datasourceType;

    private String datasetGroupPid;

    private Boolean deleteResource;

    private String datasetPrivileges;

    private String panelPrivileges;

    private String datasourcePrivileges;

}

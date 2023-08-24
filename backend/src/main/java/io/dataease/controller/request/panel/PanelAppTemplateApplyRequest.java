package io.dataease.controller.request.panel;

import io.dataease.dto.DatasourceDTO;
import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2022/9/15
 * Description:
 */
@Data
public class PanelAppTemplateApplyRequest {

    private String logId;

    private String panelId;

    private String panelGroupPid;

    private String panelName;

    private String datasetGroupId;

    private String datasetGroupPid;

    private String datasetGroupName;

    private String appTemplateId;

    private String datasourceFrom;

    private String datasourceHistoryId;

    private String linkJumps;

    private String linkJumpInfos;

    private String linkages;

    private String linkageFields;

    private List<DatasourceDTO> datasourceList;
}

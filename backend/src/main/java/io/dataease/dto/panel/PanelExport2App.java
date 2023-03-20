package io.dataease.dto.panel;

import com.alibaba.fastjson.JSON;
import io.dataease.dto.DatasourceDTO;
import io.dataease.dto.dataset.DataSetTaskDTO;
import io.dataease.plugins.common.base.domain.*;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2022/9/8
 * Description:
 */
@Data
public class PanelExport2App {

    private Boolean checkStatus = false;

    private String checkMes;

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

    public PanelExport2App() {

    }

    public PanelExport2App(String checkMes) {
        this.checkMes = checkMes;
    }

    public PanelExport2App(List<ChartViewWithBLOBs> chartViewsInfo, List<ChartViewField> chartViewFieldsInfo,
                           List<DatasetTable> datasetTablesInfo, List<DatasetTableField> datasetTableFieldsInfo,
                           List<DataSetTaskDTO> datasetTasksInfo, List<DatasourceDTO> datasourceInfo,
                           List<PanelView> panelViewsInfo,List<PanelLinkJump> linkJumps,List<PanelLinkJumpInfo> linkJumpInfos,
                           List<PanelViewLinkage> linkages, List<PanelViewLinkageField> linkageFields) {
        List empty = new ArrayList();
        this.checkStatus = true;
        this.checkMes = "success";
        this.chartViewsInfo = JSON.toJSONString(chartViewsInfo!=null?chartViewsInfo:empty);
        this.chartViewFieldsInfo = JSON.toJSONString(chartViewFieldsInfo!=null?chartViewFieldsInfo:empty);
        this.datasetTablesInfo = JSON.toJSONString(datasetTablesInfo!=null?datasetTablesInfo:empty);
        this.datasetTableFieldsInfo = JSON.toJSONString(datasetTableFieldsInfo!=null?datasetTableFieldsInfo:empty);
        this.datasetTasksInfo = JSON.toJSONString(datasetTasksInfo!=null?datasetTasksInfo:empty);
        this.datasourceInfo = JSON.toJSONString(datasourceInfo!=null?datasourceInfo:empty);
        this.panelViewsInfo = JSON.toJSONString(panelViewsInfo!=null?panelViewsInfo:empty);
        this.linkJumps = JSON.toJSONString(linkJumps!=null?linkJumps:empty);
        this.linkJumpInfos = JSON.toJSONString(linkJumpInfos!=null?linkJumpInfos:empty);
        this.linkages = JSON.toJSONString(linkages!=null?linkages:empty);
        this.linkageFields = JSON.toJSONString(linkJumpInfos!=null?linkageFields:empty);
    }
}

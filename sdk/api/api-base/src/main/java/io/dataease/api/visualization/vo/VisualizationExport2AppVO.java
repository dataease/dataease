package io.dataease.api.visualization.vo;

import com.google.gson.Gson;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class VisualizationExport2AppVO {

    private Boolean checkStatus = false;

    private String checkMes;

    private String visualizationInfo;

    private String visualizationViewsInfo;

    private String chartViewsInfo;

    private String datasetGroupsInfo;

    private String datasetTablesInfo;

    private String datasetTableFieldsInfo;

    private String datasourceInfo;

    private String datasourceTaskInfo;

    private String linkJumps;

    private String linkJumpInfos;

    private String linkJumpTargetInfos;

    private String linkages;

    private String linkageFields;

    public VisualizationExport2AppVO() {

    }

    public VisualizationExport2AppVO(String checkMes) {
        this.checkMes = checkMes;
    }

    public VisualizationExport2AppVO(List<AppCoreChartViewVO> chartViewVOInfo,
                                     List<AppCoreDatasetGroupVO> datasetGroupVOInfo,
                                     List<AppCoreDatasetTableVO> datasetTableVOInfo,
                                     List<AppCoreDatasetTableFieldVO> datasetTableFieldVOInfo,
                                     List<AppCoreDatasourceVO> datasourceVOInfo,
                                     List<AppCoreDatasourceTaskVO> datasourceTaskVOInfo,
                                     List<VisualizationLinkJumpVO> linkJumpVOInfo,
                                     List<VisualizationLinkJumpInfoVO> linkJumpInfoVOInfo,
                                     List<VisualizationLinkJumpTargetViewInfoVO> linkJumpTargetViewVOInfo,
                                     List<VisualizationLinkageVO> linkagesVOInfo,
                                     List<VisualizationLinkageFieldVO> linkageFieldVOInfo) {
        List<Object> empty = new ArrayList<>();
        Gson gson = new Gson();
        this.checkStatus = true;
        this.checkMes = "success";
        this.chartViewsInfo = gson.toJson(chartViewVOInfo != null ? chartViewVOInfo : empty);
        this.datasetGroupsInfo = gson.toJson(datasetGroupVOInfo != null ? datasetGroupVOInfo : empty);
        this.datasetTablesInfo = gson.toJson(datasetTableVOInfo != null ? datasetTableVOInfo : empty);
        this.datasetTableFieldsInfo = gson.toJson(datasetTableFieldVOInfo != null ? datasetTableFieldVOInfo : empty);
        this.datasourceTaskInfo = gson.toJson(datasourceTaskVOInfo != null ? datasourceTaskVOInfo : empty);
        this.datasourceInfo = gson.toJson(datasourceVOInfo != null ? datasourceVOInfo : empty);
        this.linkJumps = gson.toJson(linkJumpVOInfo != null ? linkJumpVOInfo : empty);
        this.linkJumpInfos = gson.toJson(linkJumpInfoVOInfo != null ? linkJumpInfoVOInfo : empty);
        this.linkJumpTargetInfos = gson.toJson(linkJumpTargetViewVOInfo != null ? linkJumpTargetViewVOInfo : empty);
        this.linkages = gson.toJson(linkagesVOInfo != null ? linkagesVOInfo : empty);
        this.linkageFields = gson.toJson(linkageFieldVOInfo != null ? linkageFieldVOInfo : empty);
    }
}

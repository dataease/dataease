package io.dataease.api.visualization.vo;

import com.google.gson.Gson;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class VisualizationExport2AppVO {

    private Boolean checkStatus = false;

    private String checkMes;

    private String visualizationInfo;

    private String visualizationViewsInfo;

    List<AppCoreChartViewVO> chartViewsInfo;

    List<AppCoreDatasetGroupVO> datasetGroupsInfo;

    List<AppCoreDatasetTableVO> datasetTablesInfo;

    List<AppCoreDatasetTableFieldVO> datasetTableFieldsInfo;

    List<AppCoreDatasourceVO> datasourceInfo;

    List<AppCoreDatasourceTaskVO> datasourceTaskInfo;

    List<VisualizationLinkJumpVO> linkJumps;

    List<VisualizationLinkJumpInfoVO> linkJumpInfos;

    List<VisualizationLinkJumpTargetViewInfoVO> linkJumpTargetInfos;

    List<VisualizationLinkageVO> linkages;

    List<VisualizationLinkageFieldVO> linkageFields;

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
        this.checkStatus = true;
        this.checkMes = "success";
        this.chartViewsInfo = chartViewVOInfo != null ? chartViewVOInfo : new ArrayList<>();
        this.datasetGroupsInfo = datasetGroupVOInfo != null ? datasetGroupVOInfo : new ArrayList<>();
        this.datasetTablesInfo = datasetTableVOInfo != null ? datasetTableVOInfo : new ArrayList<>();
        this.datasetTableFieldsInfo = datasetTableFieldVOInfo != null ? datasetTableFieldVOInfo : new ArrayList<>();
        this.datasourceTaskInfo = datasourceTaskVOInfo != null ? datasourceTaskVOInfo : new ArrayList<>();
        this.datasourceInfo = datasourceVOInfo != null ? datasourceVOInfo : new ArrayList<>();
        this.linkJumps = linkJumpVOInfo != null ? linkJumpVOInfo : new ArrayList<>();
        this.linkJumpInfos = linkJumpInfoVOInfo != null ? linkJumpInfoVOInfo : new ArrayList<>();
        this.linkJumpTargetInfos = linkJumpTargetViewVOInfo != null ? linkJumpTargetViewVOInfo : new ArrayList<>();
        this.linkages = linkagesVOInfo != null ? linkagesVOInfo : new ArrayList<>();
        this.linkageFields = linkageFieldVOInfo != null ? linkageFieldVOInfo : new ArrayList<>();
    }
}

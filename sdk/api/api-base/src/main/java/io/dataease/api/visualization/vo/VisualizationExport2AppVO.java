package io.dataease.api.visualization.vo;

import com.google.gson.Gson;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
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

    private String chartViewFieldsInfo;

    private String datasetTablesInfo;

    private String datasetTableFieldsInfo;

    private String datasetTasksInfo;

    private String datasourceInfo;

    private String linkJumps;

    private String linkJumpInfos;

    private String linkages;

    private String linkageFields;

    public VisualizationExport2AppVO() {

    }

    public VisualizationExport2AppVO(String checkMes) {
        this.checkMes = checkMes;
    }

    public VisualizationExport2AppVO(List<ChartViewDTO> chartViewsInfo, List<ChartViewFieldDTO> chartViewFieldsInfo,
                                     List<DatasetTableDTO> datasetTablesInfo, List<DatasetTableFieldDTO> datasetTableFieldsInfo, List<DatasourceDTO> datasourceInfo, List<VisualizationLinkJumpVO> linkJumps, List<VisualizationLinkJumpInfoVO> linkJumpInfos,
                                     List<VisualizationLinkageVO> linkages, List<VisualizationLinkageFieldVO> linkageFields) {
        List empty = new ArrayList();
        Gson gson = new Gson();
        this.checkStatus = true;
        this.checkMes = "success";
        this.chartViewsInfo = gson.toJson(chartViewsInfo != null ? chartViewsInfo : empty);
        this.chartViewFieldsInfo = gson.toJson(chartViewFieldsInfo != null ? chartViewFieldsInfo : empty);
        this.datasetTablesInfo = gson.toJson(datasetTablesInfo != null ? datasetTablesInfo : empty);
        this.datasetTableFieldsInfo = gson.toJson(datasetTableFieldsInfo != null ? datasetTableFieldsInfo : empty);
        this.datasetTasksInfo = gson.toJson(datasetTasksInfo != null ? datasetTasksInfo : empty);
        this.datasourceInfo = gson.toJson(datasourceInfo != null ? datasourceInfo : empty);
        this.visualizationViewsInfo = gson.toJson(visualizationViewsInfo != null ? visualizationViewsInfo : empty);
        this.linkJumps = gson.toJson(linkJumps != null ? linkJumps : empty);
        this.linkJumpInfos = gson.toJson(linkJumpInfos != null ? linkJumpInfos : empty);
        this.linkages = gson.toJson(linkages != null ? linkages : empty);
        this.linkageFields = gson.toJson(linkJumpInfos != null ? linkageFields : empty);
    }
}

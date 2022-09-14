package io.dataease.dto.panel;

import com.alibaba.fastjson.JSON;
import io.dataease.dto.DatasourceDTO;
import io.dataease.dto.dataset.DataSetTaskDTO;
import io.dataease.plugins.common.base.domain.ChartViewField;
import io.dataease.plugins.common.base.domain.ChartViewWithBLOBs;
import io.dataease.plugins.common.base.domain.DatasetTable;
import io.dataease.plugins.common.base.domain.DatasetTableField;
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

    private String chartViewsInfo;

    private String chartViewFieldsInfo;

    private String datasetTablesInfo;

    private String datasetTableFieldsInfo;

    private String dataSetTasksInfo;

    private String datasourceDTOS;

    public PanelExport2App() {

    }

    public PanelExport2App(String checkMes) {
        this.checkMes = checkMes;
    }

    public PanelExport2App(List<ChartViewWithBLOBs> chartViewsInfo, List<ChartViewField> chartViewFieldsInfo, List<DatasetTable> datasetTablesInfo, List<DatasetTableField> datasetTableFieldsInfo, List<DataSetTaskDTO> dataSetTasksInfo, List<DatasourceDTO> datasourceDTOS) {
        List empty = new ArrayList();
        this.checkStatus = true;
        this.checkMes = "success";
        this.chartViewsInfo = JSON.toJSONString(chartViewsInfo!=null?chartViewsInfo:empty);
        this.chartViewFieldsInfo = JSON.toJSONString(chartViewFieldsInfo!=null?chartViewFieldsInfo:empty);
        this.datasetTablesInfo = JSON.toJSONString(datasetTablesInfo!=null?datasetTablesInfo:empty);
        this.datasetTableFieldsInfo = JSON.toJSONString(datasetTableFieldsInfo!=null?datasetTableFieldsInfo:empty);
        this.dataSetTasksInfo = JSON.toJSONString(dataSetTasksInfo!=null?dataSetTasksInfo:empty);
        this.datasourceDTOS = JSON.toJSONString(datasourceDTOS!=null?datasourceDTOS:empty);
    }
}

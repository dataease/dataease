package io.dataease.exportCenter.manage;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.chart.request.ChartExcelRequest;
import io.dataease.chart.constant.ChartConstants;
import io.dataease.chart.manage.ChartDataManage;
import io.dataease.constant.CommonConstants;
import io.dataease.dataset.manage.DatasetTableFieldManage;
import io.dataease.dataset.manage.PermissionManage;
import io.dataease.dataset.utils.DatasetUtils;
import io.dataease.exportCenter.util.ExportCenterUtils;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import io.dataease.utils.JsonUtil;
import io.dataease.visualization.manage.VisualizationTemplateExtendDataManage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 导出内容准备服务：为导出执行器逐页准备图表/明细数据。
 * 只依赖 manage/Repository/Provider，不依赖任何 *Server。
 */
@Component
public class ExportContentService {

    @Resource
    private DatasetTableFieldManage datasetTableFieldManage;
    @Resource
    private PermissionManage permissionManage;
    @Resource
    private VisualizationTemplateExtendDataManage extendDataManage;
    @Resource
    private ChartDataManage chartDataManage;

    public ChartViewDTO findExcelData(ChartExcelRequest request) {
        ChartViewDTO chartViewInfo = new ChartViewDTO();
        try {
            ChartViewDTO viewDTO = request.getViewInfo();
            viewDTO.setIsExcelExport(true);
            String[] dsHeader = null;
            Integer[] dsTypes = null;
            //downloadType = dataset 为下载原始名字 这里做数据转换模拟 table-info类型图表导出
            if ("dataset".equals(request.getDownloadType())) {
                viewDTO.setExportDatasetOriginData(true);
                viewDTO.setResultMode(ChartConstants.VIEW_RESULT_MODE.ALL);
                viewDTO.setType("table-info");
                viewDTO.setRender("antv");
                List<DatasetTableFieldDTO> sourceFields = datasetTableFieldManage.selectByDatasetGroupId(viewDTO.getTableId());
                DatasetUtils.listEncode(sourceFields);
                List<String> fileNames = permissionManage.filterColumnPermissions(sourceFields, new HashMap<>(), viewDTO.getTableId(), null).stream().map(DatasetTableFieldDTO::getDataeaseName).collect(Collectors.toList());
                sourceFields = sourceFields.stream().filter(datasetTableFieldDTO -> fileNames.contains(datasetTableFieldDTO.getDataeaseName())).collect(Collectors.toList());
                dsHeader = sourceFields.stream().map(DatasetTableFieldDTO::getName).toArray(String[]::new);
                dsTypes = sourceFields.stream().map(DatasetTableFieldDTO::getDeType).toArray(Integer[]::new);
                TypeReference<List<ChartViewFieldDTO>> listTypeReference = new TypeReference<List<ChartViewFieldDTO>>() {
                };
                viewDTO.setXAxis(JsonUtil.parseList(JsonUtil.toJSONString(sourceFields).toString(), listTypeReference));
                viewDTO.getXAxis().forEach(x -> {
                    if (x.getOrderChecked() != null && x.getOrderChecked()) {
                        x.setSort("asc");
                    }
                });
            }
            viewDTO.setResultCount(getExcelExportLimit(request.getDownloadType()));
            if (CommonConstants.VIEW_DATA_FROM.TEMPLATE.equalsIgnoreCase(viewDTO.getDataFrom())) {
                chartViewInfo = extendDataManage.getChartDataInfo(viewDTO.getId(), viewDTO);
            } else {
                // 要走明细表的逻辑
                viewDTO.setIsPlugin(false);
                chartViewInfo = chartDataManage.calcData(viewDTO);
            }
            List<Object[]> tableRow = (List) chartViewInfo.getData().get("sourceData");
            if ("dataset".equals(request.getDownloadType())) {
                request.setHeader(dsHeader);
                request.setExcelTypes(dsTypes);
            }
            viewDTO.setData(chartViewInfo.getData());
            request.setDetails(tableRow);
            request.setData(chartViewInfo.getData());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return chartViewInfo;
    }

    private int getExcelExportLimit(String downloadType) {
        long viewLimit = ExportCenterUtils.getExportLimit("view");
        if ("dataset".equals(downloadType)) {
            long datasetLimit = ExportCenterUtils.getExportLimit("dataset");
            return Math.toIntExact(Math.min(viewLimit, datasetLimit));
        }
        return Math.toIntExact(viewLimit);
    }
}

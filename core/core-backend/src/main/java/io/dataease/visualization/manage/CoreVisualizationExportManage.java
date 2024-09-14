package io.dataease.visualization.manage;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.visualization.vo.DataVisualizationVO;
import io.dataease.chart.manage.ChartDataManage;
import io.dataease.chart.manage.ChartViewManege;
import io.dataease.constant.CommonConstants;
import io.dataease.dataset.server.DatasetFieldServer;
import io.dataease.engine.constant.DeTypeConstants;
import io.dataease.exception.DEException;
import io.dataease.extensions.view.dto.ChartExtFilterDTO;
import io.dataease.extensions.view.dto.ChartExtRequest;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.JsonUtil;
import io.dataease.visualization.bo.ExcelSheetModel;
import io.dataease.visualization.dao.ext.mapper.ExtDataVisualizationMapper;
import io.dataease.visualization.template.FilterBuildTemplate;
import io.dataease.visualization.utils.VisualizationExcelUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class CoreVisualizationExportManage {
    @Resource
    private ExtDataVisualizationMapper extDataVisualizationMapper;

    @Resource
    private ChartViewManege chartViewManege;

    @Resource
    private ChartDataManage chartDataManage;

    @Resource
    private VisualizationTemplateExtendDataManage extendDataManage;

    @Resource
    private DatasetFieldServer datasetFieldServer;

    public String getResourceName(Long dvId, String busiFlag) {
        DataVisualizationVO visualization = extDataVisualizationMapper.findDvInfo(dvId, busiFlag);
        if (ObjectUtils.isEmpty(visualization)) DEException.throwException("资源不存在或已经被删除...");
        return visualization.getName();
    }

    public File exportExcel(Long dvId, String busiFlag, List<Long> viewIdList, boolean onlyDisplay) throws Exception {
        DataVisualizationVO visualization = extDataVisualizationMapper.findDvInfo(dvId, busiFlag);
        if (ObjectUtils.isEmpty(visualization)) DEException.throwException("资源不存在或已经被删除...");
        List<ChartViewDTO> chartViewDTOS = chartViewManege.listBySceneId(dvId);

        String componentsJson = visualization.getComponentData();
        List<Map<String, Object>> components = JsonUtil.parseList(componentsJson, tokenType);
        List<Long> idList = components.stream().filter(c -> ObjectUtils.isNotEmpty(c.get("id"))).map(component -> Long.parseLong(component.get("id").toString())).toList();

        if (CollectionUtils.isNotEmpty(viewIdList)) {
            chartViewDTOS = chartViewDTOS.stream().filter(item -> idList.contains(item.getId()) && viewIdList.contains(item.getId())).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(chartViewDTOS)) return null;
        Map<String, ChartExtRequest> chartExtRequestMap = buildViewRequest(visualization, onlyDisplay);
        List<ExcelSheetModel> sheets = new ArrayList<>();
        for (int i = 0; i < chartViewDTOS.size(); i++) {
            ChartViewDTO view = chartViewDTOS.get(i);
            ChartExtRequest extRequest = chartExtRequestMap.get(view.getId().toString());
            if (ObjectUtils.isNotEmpty(extRequest)) {
                view.setChartExtRequest(extRequest);
            }
            view.getChartExtRequest().setUser(AuthUtils.getUser().getUserId());
            view.setTitle((i + 1) + "-" + view.getTitle());
            sheets.addAll(exportViewData(view));
        }

        return VisualizationExcelUtils.exportExcel(sheets, visualization.getName(), visualization.getId().toString());
    }

    private ExcelSheetModel exportSingleData(Map<String, Object> chart, String title) {
        ExcelSheetModel result = new ExcelSheetModel();
        Object objectFields = chart.get("fields");
        List<ChartViewFieldDTO> fields = (List<ChartViewFieldDTO>) objectFields;
        List<String> heads = new ArrayList<>();
        List<String> headKeys = new ArrayList<>();
        List<Integer> fieldTypes = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(fields)) {
            fields.forEach(field -> {
                Object name = field.getName();
                Object dataeaseName = field.getDataeaseName();
                Object deType = field.getDeType();
                if (ObjectUtils.isNotEmpty(name) && ObjectUtils.isNotEmpty(dataeaseName)) {
                    heads.add(name.toString());
                    headKeys.add(dataeaseName.toString());
                    if (deType == null) {
                        field.setDeType(DeTypeConstants.DE_STRING);
                        deType = DeTypeConstants.DE_STRING;
                    }
                    fieldTypes.add((int) deType);
                }
            });
        }
        Object objectTableRow = chart.get("tableRow");
        if (objectTableRow == null) {
            objectTableRow = chart.get("sourceData");
        }
        List<Map<String, Object>> tableRow = (List<Map<String, Object>>) objectTableRow;

        List<List<String>> details = tableRow.stream().map(row -> {
            List<String> tempList = new ArrayList<>();
            for (int i = 0; i < headKeys.size(); i++) {
                String key = headKeys.get(i);
                Object val = row.get(key);
                if (ObjectUtils.isEmpty(val)) {
                    tempList.add(StringUtils.EMPTY);
                } else if (fieldTypes.get(i) == 3) {
                    tempList.add(filterInvalidDecimal(val.toString()));
                } else {
                    tempList.add(val.toString());
                }
            }
            return tempList;
        }).collect(Collectors.toList());
        result.setHeads(heads);
        result.setData(details);
        result.setFiledTypes(fieldTypes);
        result.setSheetName(title);
        return result;
    }

    private List<ExcelSheetModel> exportViewData(ChartViewDTO request) {

        ChartViewDTO chartViewDTO = null;
        request.setIsExcelExport(true);
        if (CommonConstants.VIEW_DATA_FROM.TEMPLATE.equalsIgnoreCase(request.getDataFrom())) {
            chartViewDTO = extendDataManage.getChartDataInfo(request.getId(), request);
        } else {
            try {
                chartViewDTO = chartDataManage.calcData(request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        String title = chartViewDTO.getTitle();
        Map<String, Object> chart = chartViewDTO.getData();
        List<ExcelSheetModel> resultList = new ArrayList<>();
        boolean leftExist = ObjectUtils.isNotEmpty(chart.get("left"));
        boolean rightExist = ObjectUtils.isNotEmpty(chart.get("right"));
        if (!leftExist && !rightExist) {
            ExcelSheetModel sheetModel = exportSingleData(chart, title);
            resultList.add(sheetModel);
            return resultList;
        }
        if (leftExist) {
            ExcelSheetModel sheetModel = exportSingleData((Map<String, Object>) chart.get("left"), title + "_left");
            resultList.add(sheetModel);
        }
        if (rightExist) {
            ExcelSheetModel sheetModel = exportSingleData((Map<String, Object>) chart.get("right"), title + "_right");
            resultList.add(sheetModel);
        }
        return resultList;
    }

    private String filterInvalidDecimal(String sourceNumberStr) {
        if (StringUtils.isNotBlank(sourceNumberStr) && StringUtils.contains(sourceNumberStr, ".")) {
            sourceNumberStr = sourceNumberStr.replaceAll("0+?$", "");
            sourceNumberStr = sourceNumberStr.replaceAll("[.]$", "");
        }
        return sourceNumberStr;
    }

    private final TypeReference<List<Map<String, Object>>> tokenType = new TypeReference<List<Map<String, Object>>>() {
    };


    private Map<String, ChartExtRequest> buildViewRequest(DataVisualizationVO panelDto, Boolean justView) {
        String componentsJson = panelDto.getComponentData();
        List<Map<String, Object>> components = JsonUtil.parseList(componentsJson, tokenType);
        Map<String, ChartExtRequest> result = new HashMap<>();
        Map<String, List<ChartExtFilterDTO>> panelFilters = FilterBuildTemplate.buildEmpty(components);
        // List<String> tableInfoViewIds = findTableInfoViewIds(components);
        for (Map.Entry<String, List<ChartExtFilterDTO>> entry : panelFilters.entrySet()) {
            List<ChartExtFilterDTO> chartExtFilterRequests = entry.getValue();
            ChartExtRequest chartExtRequest = new ChartExtRequest();
            chartExtRequest.setQueryFrom("panel");
            chartExtRequest.setFilter(chartExtFilterRequests);
            chartExtRequest.setResultCount((int) 1000);
            chartExtRequest.setResultMode("all");
            result.put(entry.getKey(), chartExtRequest);
        }
        return result;
    }

}

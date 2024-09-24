package io.dataease.service.chart;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import io.dataease.auth.annotation.DePermissionProxy;
import io.dataease.commons.model.excel.ExcelSheetModel;
import io.dataease.commons.utils.ExcelUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.controller.request.chart.ChartExtRequest;
import io.dataease.dto.PermissionProxy;
import io.dataease.dto.chart.ChartViewDTO;
import io.dataease.dto.panel.PanelGroupDTO;
import io.dataease.plugins.common.dto.chart.ChartViewFieldDTO;
import io.dataease.plugins.common.exception.DataEaseException;
import io.dataease.plugins.common.request.chart.ChartExtFilterRequest;
import io.dataease.plugins.common.util.SpringContextUtil;
import io.dataease.service.panel.PanelGroupService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ViewExportExcel {

    private final static Gson gson = new Gson();
    private final Type tokenType = new TypeToken<List<Map<String, Object>>>() {
    }.getType();

    private final Type fieldTokenType = new TypeToken<List<ChartViewFieldDTO>>() {
    }.getType();

    @DePermissionProxy(paramIndex = 2)
    public List<File> export(String panelId, List<String> viewIds, PermissionProxy proxy, Boolean justView, String taskId) throws Exception {
        if (CollectionUtils.isEmpty(viewIds)) {
            return null;
        }
        PanelGroupService panelGroupService = SpringContextUtil.getBean(PanelGroupService.class);

        PanelGroupDTO panelDto = panelGroupService.findOne(panelId);

        Map<String, ChartExtRequest> stringChartExtRequestMap = buildViewRequest(panelDto, justView);
        List<File> results = new ArrayList<>();
        List<ExcelSheetModel> sheets = viewIds.stream().map(viewId -> viewFiles(viewId, stringChartExtRequestMap.get(viewId))).collect(Collectors.toList());
        File excelFile = ExcelUtils.exportExcel(sheets, getSafeFileName(panelDto.getName()), panelDto.getId() + "_" + taskId);
        results.add(excelFile);
        return results;
    }

    private String getSafeFileName(String fileName) {
        return fileName.replace("/", "_");
    }


    private Map<String, ChartExtRequest> buildViewRequest(PanelGroupDTO panelDto, Boolean justView) {
        String componentsJson = panelDto.getPanelData();
        List<Map<String, Object>> components = gson.fromJson(componentsJson, tokenType);
        String panelStyle = panelDto.getPanelStyle();
        Map map = gson.fromJson(panelStyle, Map.class);
        Map panelMap = (LinkedTreeMap) map.get("panel");
        double resultCount = ObjectUtils.isEmpty(panelMap.get("resultCount")) ? 1000 : Double.parseDouble(panelMap.get("resultCount").toString());
        String resultMode = null;
        if (ObjectUtils.isNotEmpty(panelMap.get("resultMode"))) {
            resultMode = panelMap.get("resultMode").toString();
        }

        Map<String, ChartExtRequest> result = new HashMap<>();
        Map<String, List<ChartExtFilterRequest>> panelFilters = justView ? FilterBuildTemplate.buildFilters(components) : FilterBuildTemplate.buildEmpty(components);
        List<String> tableInfoViewIds = findTableInfoViewIds(components);
        for (Map.Entry<String, List<ChartExtFilterRequest>> entry : panelFilters.entrySet()) {
            List<ChartExtFilterRequest> chartExtFilterRequests = entry.getValue();
            ChartExtRequest chartExtRequest = new ChartExtRequest();
            chartExtRequest.setQueryFrom("panel");
            chartExtRequest.setFilter(chartExtFilterRequests);
            chartExtRequest.setResultCount((int) resultCount);
            chartExtRequest.setResultMode(resultMode);
            if (tableInfoViewIds.contains(entry.getKey())) {
                chartExtRequest.setGoPage(1L);
                chartExtRequest.setPageSize(1000000L);
                chartExtRequest.setExcelExportFlag(true);
            }
            result.put(entry.getKey(), chartExtRequest);
        }
        return result;
    }

    private List<String> findTableInfoViewIds(List<Map<String, Object>> components) {
        List<String> tableInfoViewIds = new ArrayList<>();
        components.forEach(element -> {
            if (StringUtils.equals(String.valueOf(element.get("type")), "view") && StringUtils.equals(String.valueOf(((Map<String, Object>) element.get("propValue")).get("innerType")), "table-info")) {
                tableInfoViewIds.add(((Map<String, Object>) element.get("propValue")).get("viewId").toString());
            }
        });
        return tableInfoViewIds;
    }

    private ExcelSheetModel viewFiles(String viewId, ChartExtRequest request) {
        ExcelSheetModel result = new ExcelSheetModel();
        ChartViewDTO chartViewDTO = null;
        try {
            ChartViewService chartViewService = SpringContextUtil.getBean(ChartViewService.class);
            chartViewDTO = chartViewService.getData(viewId, request);
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
            DataEaseException.throwException(e);
        }
        String title = Optional.ofNullable(chartViewDTO.getTitle()).orElse(chartViewDTO.getName());
        Map<String, Object> chart = chartViewDTO.getData();

        Object objectFields = chart.get("fields");
        List<ChartViewFieldDTO> fields = gson.fromJson(gson.toJson(objectFields), fieldTokenType);
        List<String> heads = new ArrayList<>();
        List<String> headKeys = new ArrayList<>();
        List<Integer> fieldTypes = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(fields)) {
            fields.forEach(field -> {
                if (ObjectUtils.isNotEmpty(field.getName()) && ObjectUtils.isNotEmpty(field.getDataeaseName())) {
                    heads.add(field.getName());
                    headKeys.add(field.getDataeaseName());
                    fieldTypes.add(field.getDeType());
                }
            });
        }
        Object objectTableRow = chart.get("tableRow");
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

    private String filterInvalidDecimal(String sourceNumberStr) {
        if (StringUtils.isNotBlank(sourceNumberStr) && StringUtils.contains(sourceNumberStr, ".")) {
            sourceNumberStr = sourceNumberStr.replaceAll("0+?$", "");
            sourceNumberStr = sourceNumberStr.replaceAll("[.]$", "");
        }
        return sourceNumberStr;
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add(null);
        System.out.println(111);
    }
}

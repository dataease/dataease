package io.dataease.service.chart;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import io.dataease.auth.annotation.DePermissionProxy;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.model.excel.ExcelSheetModel;
import io.dataease.commons.utils.ExcelUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.controller.request.chart.ChartExtRequest;
import io.dataease.dto.PermissionProxy;
import io.dataease.dto.chart.ChartViewDTO;
import io.dataease.dto.panel.PanelGroupDTO;
import io.dataease.plugins.common.dto.chart.ChartViewFieldDTO;
import io.dataease.plugins.common.request.chart.ChartExtFilterRequest;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.service.panel.PanelGroupService;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.reflect.TypeToken;

@Service
public class ViewExportExcel {

    private final static Gson gson = new Gson();
    private Type tokenType = new TypeToken<List<Map<String, Object>>>() {
    }.getType();

    private Type fieldTokenType = new TypeToken<List<ChartViewFieldDTO>>() {
    }.getType();

    @DePermissionProxy(paramIndex = 2)
    public List<File> export(String panelId, List<String> viewIds, PermissionProxy proxy) throws Exception {
        if (CollectionUtils.isEmpty(viewIds)) {
            return null;
        }
        PanelGroupService panelGroupService = SpringContextUtil.getBean(PanelGroupService.class);

        PanelGroupDTO panelDto = panelGroupService.findOne(panelId);
        String componentsJson = panelDto.getPanelData();
        List<Map<String, Object>> components = gson.fromJson(componentsJson, tokenType);
        ChartExtRequest chartExtRequest = buildViewRequest(FilterBuildTemplate.componentsFilter(components, "custom", null, null));
        List<File> results = new ArrayList<>();
        List<ExcelSheetModel> sheets = viewIds.stream().map(viewId -> viewFiles(viewId, chartExtRequest)).collect(Collectors.toList());
        File excelFile = ExcelUtils.exportExcel(sheets, panelDto.getName());
        results.add(excelFile);
        return results;
    }



    private ChartExtRequest buildViewRequest(List<Map<String, Object>> filters) {
        ChartExtRequest chartExtRequest = new ChartExtRequest();
        filters = Optional.ofNullable(filters).orElse(new ArrayList<>());

        List<ChartExtFilterRequest> panelFilters = filters.stream().map(item -> {
            ChartExtFilterRequest curentFilter = new ChartExtFilterRequest();
            return curentFilter;
        }).collect(Collectors.toList());

        chartExtRequest.setQueryFrom("panel");
        chartExtRequest.setFilter(panelFilters);
        return chartExtRequest;
    }

    private List<ChartExtFilterRequest> initFilters(List<Map<String, Object>> components) {

        return null;
    }

    private ExcelSheetModel viewFiles(String viewId, ChartExtRequest request) {
        ExcelSheetModel result = new ExcelSheetModel();
        ChartViewDTO chartViewDTO = null;
        try {
            ChartViewService chartViewService = SpringContextUtil.getBean(ChartViewService.class);
            chartViewDTO = chartViewService.getData(viewId, request);
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
            DEException.throwException(e);
        }
        String title = Optional.ofNullable(chartViewDTO.getTitle()).orElse(chartViewDTO.getName());
        Map<String, Object> chart = chartViewDTO.getData();

        Object objectFields = chart.get("fields");
        List<ChartViewFieldDTO> fields = gson.fromJson(gson.toJson(objectFields), fieldTokenType);
        List<String> heads = new ArrayList<>();
        List<String> headKeys = new ArrayList<>();
        fields.forEach(field -> {
            if (ObjectUtils.isNotEmpty(field.getName()) && ObjectUtils.isNotEmpty(field.getDataeaseName())) {
                heads.add(field.getName());
                headKeys.add(field.getDataeaseName());
            }
        });

        Object objectTableRow = chart.get("tableRow");
        List<Map<String, Object>> tableRow = (List<Map<String, Object>>) objectTableRow;

        List<List<String>> details = tableRow.stream().map(row -> headKeys.stream().map(key -> {
            Object val = row.get(key);
            if (ObjectUtils.isEmpty(val))
                return StringUtils.EMPTY;
            return filterInvalidDecimal(val.toString());
        }).collect(Collectors.toList())).collect(Collectors.toList());
        result.setHeads(heads);
        result.setDatas(details);

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
}

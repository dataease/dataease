package io.dataease.visualization.manage;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.chart.dto.ChartExtFilterDTO;
import io.dataease.api.chart.dto.ChartViewDTO;
import io.dataease.api.chart.dto.ChartViewFieldDTO;
import io.dataease.api.chart.request.ChartExtRequest;
import io.dataease.api.visualization.vo.DataVisualizationVO;
import io.dataease.chart.manage.ChartDataManage;
import io.dataease.chart.manage.ChartViewManege;
import io.dataease.constant.CommonConstants;
import io.dataease.exception.DEException;
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

    public File exportExcel(Long dvId, String busiFlag, List<Long> viewIdList, boolean onlyDisplay) throws Exception {
        DataVisualizationVO visualization = extDataVisualizationMapper.findDvInfo(dvId, busiFlag);
        if (ObjectUtils.isEmpty(visualization)) DEException.throwException("资源不存在或已经被删除...");
        List<ChartViewDTO> chartViewDTOS = chartViewManege.listBySceneId(dvId);

        if (CollectionUtils.isNotEmpty(viewIdList)) {
            chartViewDTOS = chartViewDTOS.stream().filter(item -> viewIdList.contains(item.getId())).collect(Collectors.toList());
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
            sheets.add(exportViewData(view));
        }

        return VisualizationExcelUtils.exportExcel(sheets, visualization.getName(), visualization.getId().toString());
    }

    private ExcelSheetModel exportViewData(ChartViewDTO request) {
        ExcelSheetModel result = new ExcelSheetModel();
        ChartViewDTO chartViewDTO = null;
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
                    fieldTypes.add((int) deType);
                }
            });
        }
        Object objectTableRow = chart.get("tableRow");
        List<Map<String, Object>> tableRow = (List<Map<String, Object>>) objectTableRow;

        List<List<String>> details = tableRow.stream().map(row -> headKeys.stream().map(key -> {
            Object val = row.get(key);
            if (ObjectUtils.isEmpty(val))
                return StringUtils.EMPTY;
            return filterInvalidDecimal(val.toString());
        }).collect(Collectors.toList())).collect(Collectors.toList());
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

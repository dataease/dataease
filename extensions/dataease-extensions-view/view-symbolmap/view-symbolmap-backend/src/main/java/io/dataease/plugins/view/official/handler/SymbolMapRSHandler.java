package io.dataease.plugins.view.official.handler;

import io.dataease.plugins.common.dto.chart.AxisChartDataAntVDTO;
import io.dataease.plugins.common.dto.chart.ChartDimensionDTO;
import io.dataease.plugins.common.dto.chart.ChartQuotaDTO;
import io.dataease.plugins.view.entity.*;
import io.dataease.plugins.view.handler.PluginViewRSHandler;
import io.dataease.plugins.view.official.dto.SymbolMapResultDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

@Component
public class SymbolMapRSHandler implements PluginViewRSHandler<Map> {

    private static List<String> trans2Ykeys = new ArrayList<String>();

    @PostConstruct
    public void init() {
        trans2Ykeys.add("labelAxis");
        trans2Ykeys.add("tooltipAxis");
    }

    public List<String> getTrans2Ykeys() {
        return trans2Ykeys;
    }

    @Override
    public Map format(PluginViewParam pluginViewParam, List<String[]> data, boolean isDrill) {
        List<PluginViewField> xAxis = new ArrayList<>();
        List<PluginViewField> yAxis = new ArrayList<>();
        List<PluginViewField> xAxisExt = new ArrayList<>();

        pluginViewParam.getPluginViewFields().forEach(pluginViewField -> {
            if (StringUtils.equals(pluginViewField.getTypeField(), "xAxis")) {
                xAxis.add(pluginViewField);
            }
            if (StringUtils.equals(pluginViewField.getTypeField(), "xAxisExt")) {
                xAxisExt.add(pluginViewField);
            }
            if (StringUtils.equals(pluginViewField.getTypeField(), "yAxis")
                    || trans2Ykeys.contains(pluginViewField.getTypeField())) {
                yAxis.add(pluginViewField);
            }
        });
        Map<String, Object> map = new HashMap<>();
        List<AxisChartDataAntVDTO> datalist = new ArrayList<>();

        for (int i1 = 0; i1 < data.size(); i1++) {
            String[] row = data.get(i1);

            StringBuilder a = new StringBuilder();
            if (isDrill) {
                a.append(row[xAxis.size() - 1]);
            } else {
                for (int i = 0; i < xAxis.size(); i++) {
                    if (i == xAxis.size() - 1) {
                        a.append(row[i]);
                    } else {
                        a.append(row[i]).append("\n");
                    }
                }
            }

            if (CollectionUtils.isEmpty(yAxis)) {
                SymbolMapResultDTO axisChartDataDTO = new SymbolMapResultDTO();
                axisChartDataDTO.setField(a.toString());
                axisChartDataDTO.setName(a.toString());

                List<ChartDimensionDTO> dimensionList = new ArrayList<>();

                for (int j = 0; j < xAxis.size(); j++) {
                    ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                    chartDimensionDTO.setId(xAxis.get(j).getId());
                    chartDimensionDTO.setValue(row[j]);
                    dimensionList.add(chartDimensionDTO);
                }
                for (int j = 0; j < xAxisExt.size(); j++) {
                    ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                    chartDimensionDTO.setId(xAxisExt.get(j).getId());
                    chartDimensionDTO.setValue(row[j + xAxis.size()]);
                    dimensionList.add(chartDimensionDTO);
                }
                axisChartDataDTO.setDimensionList(dimensionList);
                axisChartDataDTO.setLongitude(dimensionList.get(0).getValue());
                axisChartDataDTO.setLatitude(dimensionList.get(1).getValue());
                if (!xAxisExt.isEmpty()) {
                    axisChartDataDTO.setColor(dimensionList.get(2).getValue());
                }
                datalist.add(axisChartDataDTO);
            } else {
                SymbolMapResultDTO axisChartDataDTO = new SymbolMapResultDTO();
                axisChartDataDTO.setField(a.toString());
                axisChartDataDTO.setName(a.toString());

                List<ChartDimensionDTO> dimensionList = new ArrayList<>();

                for (int j = 0; j < xAxis.size(); j++) {
                    ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                    chartDimensionDTO.setId(xAxis.get(j).getId());
                    chartDimensionDTO.setValue(row[j]);
                    dimensionList.add(chartDimensionDTO);
                }
                for (int j = 0; j < xAxisExt.size(); j++) {
                    ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                    chartDimensionDTO.setId(xAxisExt.get(j).getId());
                    chartDimensionDTO.setValue(row[j + xAxis.size()]);
                    dimensionList.add(chartDimensionDTO);
                }
                if (!xAxisExt.isEmpty()) {
                    axisChartDataDTO.setColor(dimensionList.get(2).getValue());
                }
                axisChartDataDTO.setDimensionList(dimensionList);
                axisChartDataDTO.setQuotaList(new ArrayList<>());
                axisChartDataDTO.setProperties(new HashMap<>());
                int step = xAxis.size() + xAxisExt.size();
                Boolean valueFilled = false;
                for (int i = 0; i < yAxis.size(); i++) {
                    PluginViewField curY = yAxis.get(i);
                    ChartQuotaDTO chartQuotaDTO = new ChartQuotaDTO();
                    chartQuotaDTO.setId(curY.getId());
                    axisChartDataDTO.getQuotaList().add(chartQuotaDTO);
                    axisChartDataDTO.getProperties().put(curY.getName(), formatLabel(curY, row[i + step]));
                    axisChartDataDTO.setLongitude(dimensionList.get(0).getValue());
                    axisChartDataDTO.setLatitude(dimensionList.get(1).getValue());
                    if (StringUtils.equals(curY.getTypeField(), "yAxis") && !valueFilled) {
                        axisChartDataDTO.setCategory(curY.getName());
                        axisChartDataDTO.setBusiValue(row[i + step]);
                        valueFilled = true;
                    }
                }
                datalist.add(axisChartDataDTO);
            }
        }
        map.put("data", datalist);
        return map;
    }

    private String formatLabel(PluginViewField field, String val) {
        if (StringUtils.isBlank(val)) return val;
        String typeField = field.getTypeField();
        if (StringUtils.isNotBlank(typeField) && trans2Ykeys.contains(typeField)) {
            return Arrays.stream(val.split(",")).map(StringUtils::trim).distinct().collect(Collectors.joining(","));
        }
        return val;
    }
}

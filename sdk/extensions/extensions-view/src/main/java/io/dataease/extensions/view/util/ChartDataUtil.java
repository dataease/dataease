package io.dataease.extensions.view.util;

import io.dataease.extensions.view.dto.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ChartDataUtil {
    // 对结果排序
    public static List<String[]> resultCustomSort(List<ChartViewFieldDTO> xAxis, List<String[]> data) {
        List<String[]> res = new ArrayList<>(data);
        if (xAxis.size() > 0) {
            // 找到对应维度
            for (int i = 0; i < xAxis.size(); i++) {
                ChartViewFieldDTO item = xAxis.get(i);
                if (StringUtils.equalsIgnoreCase(item.getSort(), "custom_sort")) {
                    // 获取自定义值与data对应列的结果
                    if (i > 0) {
                        // 首先根据优先级高的字段分类，在每个前置字段相同的组里排序
                        Map<String, List<String[]>> map = new LinkedHashMap<>();
                        for (String[] d : res) {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int j = 0; j < i; j++) {
                                if (StringUtils.equalsIgnoreCase(xAxis.get(j).getSort(), "none")) {
                                    continue;
                                }
                                stringBuilder.append(d[j]);
                            }
                            if (ObjectUtils.isEmpty(map.get(stringBuilder.toString()))) {
                                map.put(stringBuilder.toString(), new ArrayList<>());
                            }
                            map.get(stringBuilder.toString()).add(d);
                        }
                        Iterator<Map.Entry<String, List<String[]>>> iterator = map.entrySet().iterator();
                        List<String[]> list = new ArrayList<>();
                        while (iterator.hasNext()) {
                            Map.Entry<String, List<String[]>> next = iterator.next();
                            list.addAll(customSort(Optional.ofNullable(item.getCustomSort()).orElse(new ArrayList<>()), next.getValue(), i));
                        }
                        res.clear();
                        res.addAll(list);
                    } else {
                        res = customSort(Optional.ofNullable(item.getCustomSort()).orElse(new ArrayList<>()), res, i);
                    }
                }
            }
        }
        return res;
    }

    public static List<String[]> customSort(List<String> custom, List<String[]> data, int index) {
        List<String[]> res = new ArrayList<>();

        List<Integer> indexArr = new ArrayList<>();
        List<String[]> joinArr = new ArrayList<>();
        for (int i = 0; i < custom.size(); i++) {
            String ele = custom.get(i);
            for (int j = 0; j < data.size(); j++) {
                String[] d = data.get(j);
                if (StringUtils.equalsIgnoreCase(ele, d[index])) {
                    joinArr.add(d);
                    indexArr.add(j);
                }
            }
        }
        // 取得 joinArr 就是两者的交集
        List<Integer> indexArrData = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            indexArrData.add(i);
        }
        List<Integer> indexResult = new ArrayList<>();
        for (int i = 0; i < indexArrData.size(); i++) {
            if (!indexArr.contains(indexArrData.get(i))) {
                indexResult.add(indexArrData.get(i));
            }
        }

        List<String[]> subArr = new ArrayList<>();
        for (int i = 0; i < indexResult.size(); i++) {
            subArr.add(data.get(indexResult.get(i)));
        }
        res.addAll(joinArr);
        res.addAll(subArr);
        return res;
    }

    public static Map<String, Object> transTableNormal(List<ChartViewFieldDTO> fields, ChartViewDTO view, List<String[]> data, Map<String, ColumnPermissionItem> desensitizationList) {
        Map<String, Object> map = new TreeMap<>();
        List<Map<String, Object>> tableRow = new ArrayList<>();
        data.forEach(ele -> {
            Map<String, Object> d = new HashMap<>();
            for (int i = 0; i < fields.size(); i++) {
                if (ObjectUtils.isNotEmpty(desensitizationList.keySet()) && desensitizationList.keySet().contains(fields.get(i).getDataeaseName())) {
                    d.put(fields.get(i).getDataeaseName(), desensitizationValue(desensitizationList.get(fields.get(i).getDataeaseName()), String.valueOf(ele[i])));
                    continue;
                }
                if (i == ele.length) break;
                ChartViewFieldDTO chartViewFieldDTO = fields.get(i);
                if (chartViewFieldDTO.getDeType() == 0 || chartViewFieldDTO.getDeType() == 1 || chartViewFieldDTO.getDeType() == 5) {
                    d.put(fields.get(i).getDataeaseName(), StringUtils.isEmpty(ele[i]) ? "" : ele[i]);
                } else if (chartViewFieldDTO.getDeType() == 2 || chartViewFieldDTO.getDeType() == 3 || chartViewFieldDTO.getDeType() == 4) {
                    d.put(fields.get(i).getDataeaseName(), StringUtils.isEmpty(ele[i]) ? null : new BigDecimal(ele[i]).setScale(8, RoundingMode.HALF_UP));
                }
            }
            tableRow.add(d);
        });
        map.put("fields", fields);
        map.put("tableRow", tableRow);
        return map;
    }

    public static String desensitizationValue(ColumnPermissionItem columnPermissionItem, String originStr) {
        String desensitizationStr = "";
        if (!columnPermissionItem.getDesensitizationRule().getBuiltInRule().toString().equalsIgnoreCase("custom")) {
            switch (columnPermissionItem.getDesensitizationRule().getBuiltInRule()) {
                case CompleteDesensitization:
                    desensitizationStr = ColumnPermissionItem.CompleteDesensitization;
                    break;
                case KeepMiddleThreeCharacters:
                    if (StringUtils.isEmpty(originStr) || originStr.length() < 4) {
                        desensitizationStr = ColumnPermissionItem.KeepMiddleThreeCharacters;
                    } else {
                        desensitizationStr = "***" + StringUtils.substring(originStr, originStr.length() / 2 - 1, originStr.length() / 2 + 2) + "***";
                    }
                    break;
                case KeepFirstAndLastThreeCharacters:
                    if (StringUtils.isEmpty(originStr) || originStr.length() < 7) {
                        desensitizationStr = ColumnPermissionItem.KeepFirstAndLastThreeCharacters;
                    } else {
                        desensitizationStr = StringUtils.substring(originStr, 0, 3) + "***" + StringUtils.substring(originStr, originStr.length() - 3, originStr.length());
                    }
                    break;
                default:
                    break;

            }
        } else {
            switch (columnPermissionItem.getDesensitizationRule().getCustomBuiltInRule()) {
                case RetainBeforeMAndAfterN:
                    if (StringUtils.isEmpty(originStr) || originStr.length() < columnPermissionItem.getDesensitizationRule().getM() + columnPermissionItem.getDesensitizationRule().getN()) {
                        desensitizationStr = String.join("", Collections.nCopies(columnPermissionItem.getDesensitizationRule().getM(), "X")) + "***" + String.join("", Collections.nCopies(columnPermissionItem.getDesensitizationRule().getN(), "X"));
                    } else {
                        desensitizationStr = StringUtils.substring(originStr, 0, columnPermissionItem.getDesensitizationRule().getM()) + "***" + StringUtils.substring(originStr, originStr.length() - columnPermissionItem.getDesensitizationRule().getN(), originStr.length());
                    }
                    break;
                case RetainMToN:
                    if (columnPermissionItem.getDesensitizationRule().getM() > columnPermissionItem.getDesensitizationRule().getN()) {
                        desensitizationStr = "*** ***";
                        break;
                    }
                    if (StringUtils.isEmpty(originStr) || originStr.length() < columnPermissionItem.getDesensitizationRule().getM()) {
                        desensitizationStr = "*** ***";
                        break;
                    }
                    if (columnPermissionItem.getDesensitizationRule().getM() == 1) {
                        desensitizationStr = StringUtils.substring(originStr, columnPermissionItem.getDesensitizationRule().getM() - 1, columnPermissionItem.getDesensitizationRule().getN()) + "***";
                        break;
                    } else {
                        desensitizationStr = "***" + StringUtils.substring(originStr, columnPermissionItem.getDesensitizationRule().getM() - 1, columnPermissionItem.getDesensitizationRule().getN()) + "***";
                        break;
                    }
                default:
                    break;

            }
        }
        return desensitizationStr;
    }

    public static Map<String, Object> transChartData(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewDTO view, List<String[]> data, boolean isDrill) {
        Map<String, Object> map = new HashMap<>();

        List<AxisChartDataAntVDTO> dataList = new ArrayList<>();
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
            // yAxis最后的数据对应extLabel和extTooltip，将他们从yAxis中去掉，同时转换成动态值
            int size = xAxis.size() + yAxis.size();
            int extSize = view.getExtLabel().size() + view.getExtTooltip().size();

            for (int i = xAxis.size(); i < size - extSize; i++) {
                AxisChartDataAntVDTO axisChartDataDTO = new AxisChartDataAntVDTO();
                axisChartDataDTO.setField(a.toString());
                axisChartDataDTO.setName(a.toString());

                List<ChartDimensionDTO> dimensionList = new ArrayList<>();
                List<ChartQuotaDTO> quotaList = new ArrayList<>();

                for (int j = 0; j < xAxis.size(); j++) {
                    ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                    chartDimensionDTO.setId(xAxis.get(j).getId());
                    chartDimensionDTO.setValue(row[j]);
                    dimensionList.add(chartDimensionDTO);
                }
                axisChartDataDTO.setDimensionList(dimensionList);

                int j = i - xAxis.size();
                ChartQuotaDTO chartQuotaDTO = new ChartQuotaDTO();
                chartQuotaDTO.setId(yAxis.get(j).getId());
                quotaList.add(chartQuotaDTO);
                axisChartDataDTO.setQuotaList(quotaList);
                try {
                    axisChartDataDTO.setValue(StringUtils.isEmpty(row[i]) ? null : new BigDecimal(row[i]));
                } catch (Exception e) {
                    axisChartDataDTO.setValue(new BigDecimal(0));
                }
                axisChartDataDTO.setCategory(StringUtils.defaultIfBlank(yAxis.get(j).getChartShowName(), yAxis.get(j).getName()));
                buildDynamicValue(view, axisChartDataDTO, row, size, extSize);
                dataList.add(axisChartDataDTO);
            }
        }
        map.put("data", dataList);
        return map;
    }

    // 计算动态标签和提示
    private static void buildDynamicValue(ChartViewDTO view, AxisChartDataAntVDTO axisChartDataDTO, String[] row, int size, int extSize) {
        List<DynamicValueDTO> dynamicLabelValue = new ArrayList<>();
        List<DynamicValueDTO> dynamicTooltipValue = new ArrayList<>();
        // 计算动态标签和提示
        if (ObjectUtils.isNotEmpty(view.getExtLabel())) {
            for (int ii = 0; ii < view.getExtLabel().size(); ii++) {
                DynamicValueDTO valueDTO = new DynamicValueDTO();
                ChartViewFieldDTO chartViewFieldDTO = view.getExtLabel().get(ii);
                BigDecimal value = StringUtils.isEmpty(row[ii + (size - extSize)]) ? null : new BigDecimal(row[ii + (size - extSize)]);
                valueDTO.setFieldId(chartViewFieldDTO.getId());
                valueDTO.setValue(value);
                dynamicLabelValue.add(valueDTO);
            }
        }
        if (ObjectUtils.isNotEmpty(view.getExtTooltip())) {
            for (int ii = 0; ii < view.getExtTooltip().size(); ii++) {
                DynamicValueDTO valueDTO = new DynamicValueDTO();
                ChartViewFieldDTO chartViewFieldDTO = view.getExtTooltip().get(ii);
                BigDecimal value = StringUtils.isEmpty(row[ii + (size - extSize) + view.getExtLabel().size()]) ? null : new BigDecimal(row[ii + (size - extSize) + view.getExtLabel().size()]);
                valueDTO.setFieldId(chartViewFieldDTO.getId());
                valueDTO.setValue(value);
                dynamicTooltipValue.add(valueDTO);
            }
        }

        axisChartDataDTO.setDynamicLabelValue(dynamicLabelValue);
        axisChartDataDTO.setDynamicTooltipValue(dynamicTooltipValue);
    }
}

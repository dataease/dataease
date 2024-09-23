package io.dataease.chart.utils;

import io.dataease.api.chart.dto.ScatterChartDataDTO;
import io.dataease.api.chart.dto.Series;
import io.dataease.extensions.view.dto.*;
import io.dataease.i18n.Lang;
import io.dataease.i18n.Translator;
import io.dataease.utils.IDUtils;
import io.dataease.utils.JsonUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ChartDataBuild {

    private final static String format = "(%s)";

    // AntV
    public static Map<String, Object> transChartDataAntV(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewDTO view, List<String[]> data, boolean isDrill) {
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

            if (StringUtils.containsIgnoreCase(view.getType(), "table")) {
                for (int i = 0; i < xAxis.size() + yAxis.size(); i++) {
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
                    if (j > -1) {
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
                    }
                    dataList.add(axisChartDataDTO);
                }
            } else {
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
        }
        map.put("data", dataList);
        return map;
    }

    public static Map<String, Object> transHeatMapChartDataAntV(List<ChartViewFieldDTO> xAxisBase, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewDTO view, List<String[]> data, boolean isDrill) {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> dataList = new ArrayList<>();

        if (xAxisBase.size() != 2) {
            map.put("data", dataList);
            return map;
        }

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

                Map<String, Object> object = JsonUtil.parse((String) JsonUtil.toJSONString(axisChartDataDTO), HashMap.class);

                object.put("x", new BigDecimal(row[0]));
                object.put("y", new BigDecimal(row[1]));

                dataList.add(object);
            }

        }
        map.put("data", dataList);
        return map;
    }

    public static Map<String, Object> transBaseGroupDataAntV(List<ChartViewFieldDTO> xAxisBase, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> xAxisExt, List<ChartViewFieldDTO> yAxis, ChartViewDTO view, List<String[]> data, boolean isDrill) {
        Map<String, Object> map = new HashMap<>();

        List<AxisChartDataAntVDTO> dataList = new ArrayList<>();
        for (int i1 = 0; i1 < data.size(); i1++) {
            String[] row = data.get(i1);

            StringBuilder a = new StringBuilder();
            if (isDrill) {
                a.append(row[xAxis.size() - 1]);
            } else {
                for (int i = 0; i < xAxisBase.size(); i++) {
                    if (i == xAxisBase.size() - 1) {
                        a.append(row[i]);
                    } else {
                        a.append(row[i]).append("\n");
                    }
                }
            }

            StringBuilder b = new StringBuilder();
            for (int i = xAxisBase.size(); i < xAxisBase.size() + xAxisExt.size(); i++) {
                if (i == xAxisBase.size() + xAxisExt.size() - 1) {
                    b.append(row[i]);
                } else {
                    b.append(row[i]).append("\n");
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
                axisChartDataDTO.setCategory(b.toString());
                buildDynamicValue(view, axisChartDataDTO, row, size, extSize);
                dataList.add(axisChartDataDTO);

                if ("line".equals(view.getType())) {
                    if (ObjectUtils.isEmpty(xAxisExt)) {
                        axisChartDataDTO.setCategory(StringUtils.defaultIfBlank(yAxis.get(j).getChartShowName(), yAxis.get(j).getName()));
                    } else {
                        // 多指标只取第一个
                        break;
                    }
                }
            }
        }
        map.put("data", dataList);
        return map;
    }

    // AntV柱状堆叠图
    public static Map<String, Object> transStackChartDataAntV(List<ChartViewFieldDTO> xAxisBase, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewDTO view, List<String[]> data, List<ChartViewFieldDTO> extStack, boolean isDrill) {
        Map<String, Object> map = new HashMap<>();

        List<AxisChartDataAntVDTO> dataList = new ArrayList<>();

        if (ObjectUtils.isNotEmpty(extStack)) {
            for (int i1 = 0; i1 < data.size(); i1++) {
                String[] row = data.get(i1);

                AxisChartDataAntVDTO axisChartDataDTO = new AxisChartDataAntVDTO();
                StringBuilder a = new StringBuilder();
                if (isDrill) {
                    a.append(row[xAxis.size() - 1]);
                } else {
                    for (int i = 0; i < xAxisBase.size(); i++) {
                        if (i == xAxisBase.size() - 1) {
                            a.append(row[i]);
                        } else {
                            a.append(row[i]).append("\n");
                        }
                    }
                }
                axisChartDataDTO.setField(a.toString());
                axisChartDataDTO.setName(a.toString());
                axisChartDataDTO.setCategory(row[xAxisBase.size()]);

                List<ChartDimensionDTO> dimensionList = new ArrayList<>();
                List<ChartQuotaDTO> quotaList = new ArrayList<>();

                for (int k = 0; k < xAxis.size(); k++) {
                    ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                    chartDimensionDTO.setId(xAxis.get(k).getId());
                    chartDimensionDTO.setValue(row[k]);
                    dimensionList.add(chartDimensionDTO);
                }
                axisChartDataDTO.setDimensionList(dimensionList);

                if (ObjectUtils.isNotEmpty(yAxis)) {
                    int valueIndex = xAxis.size();
                    ChartQuotaDTO chartQuotaDTO = new ChartQuotaDTO();
                    chartQuotaDTO.setId(yAxis.get(0).getId());
                    quotaList.add(chartQuotaDTO);
                    axisChartDataDTO.setQuotaList(quotaList);
                    try {
                        axisChartDataDTO.setValue(StringUtils.isEmpty(row[valueIndex]) ? null : new BigDecimal(row[valueIndex]));
                    } catch (Exception e) {
                        axisChartDataDTO.setValue(new BigDecimal(0));
                    }
                } else {
                    axisChartDataDTO.setQuotaList(quotaList);
                    axisChartDataDTO.setValue(new BigDecimal(0));
                }
                dataList.add(axisChartDataDTO);
            }
        } else {
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

                for (int i = xAxis.size(); i < xAxis.size() + yAxis.size(); i++) {
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
                    dataList.add(axisChartDataDTO);
                }
            }
        }
        map.put("data", dataList);
        return map;
    }

    //AntV scatter
    public static Map<String, Object> transScatterDataAntV(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewDTO view, List<String[]> data, List<ChartViewFieldDTO> extBubble, boolean isDrill) {
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
            int size = xAxis.size() + yAxis.size() + extBubble.size();
            int extSize = view.getExtLabel().size() + view.getExtTooltip().size() + extBubble.size();

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
                buildDynamicValue(view, axisChartDataDTO, row, size, ObjectUtils.isNotEmpty(extBubble) ? extSize - 1 : extSize);
                // pop
                if (ObjectUtils.isNotEmpty(extBubble)) {
                    try {
                        axisChartDataDTO.setPopSize(StringUtils.isEmpty(row[2]) ? null : new BigDecimal(row[2]));
                        ChartQuotaDTO bubbleQuotaDTO = new ChartQuotaDTO();
                        bubbleQuotaDTO.setId(extBubble.get(0).getId());
                        quotaList.add(bubbleQuotaDTO);
                    } catch (Exception e) {
                        axisChartDataDTO.setPopSize(new BigDecimal(0));
                    }
                }
                dataList.add(axisChartDataDTO);
            }
        }
        map.put("data", dataList);
        return map;
    }

    // antv radar
    public static Map<String, Object> transRadarChartDataAntV(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewDTO view, List<String[]> data, boolean isDrill) {
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

    // antV组合图形
    public static Map<String, Object> transMixChartDataAntV(List<ChartViewFieldDTO> xAxisBase, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> xAxisExt, List<ChartViewFieldDTO> yAxis, ChartViewDTO view, List<String[]> data, boolean isDrill) {
        return transMixChartDataAntV(xAxisBase, xAxis, xAxisExt, yAxis, view, data, isDrill, false);
    }

    public static Map<String, Object> transMixChartDataAntV(List<ChartViewFieldDTO> xAxisBase, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> xAxisExt, List<ChartViewFieldDTO> yAxis, ChartViewDTO view, List<String[]> data, boolean isDrill, boolean isLine) {

        Map<String, Object> map = new HashMap<>();

        List<Series> series = new ArrayList<>();
        for (ChartViewFieldDTO y : yAxis) {
            Series series1 = new Series();
            series1.setName(y.getName());
            series1.setType(y.getChartType());
            series1.setData(new ArrayList<>());
            series.add(series1);
        }
        Set<String> categories = new HashSet<>();

        for (int i1 = 0; i1 < data.size(); i1++) {
            String[] d = data.get(i1);

            // yAxis最后的数据对应extLabel和extTooltip，将他们从yAxis中去掉，同时转换成动态值
            int size = xAxis.size() + yAxis.size();
            int extSize = view.getExtLabel().size() + view.getExtTooltip().size();

            for (int i = xAxis.size(); i < size - extSize; i++) {
                AxisChartDataAntVDTO axisChartDataDTO = new AxisChartDataAntVDTO();

                StringBuilder a = new StringBuilder();
                if (isDrill) {
                    a.append(d[xAxis.size() - 1]);
                } else {
                    for (int ii = 0; ii < xAxisBase.size(); ii++) {
                        if (ii == xAxisBase.size() - 1) {
                            a.append(d[ii]);
                        } else {
                            a.append(d[ii]).append("\n");
                        }
                    }
                }
                StringBuilder b = new StringBuilder();
                for (int ii = xAxisBase.size(); ii < xAxisBase.size() + xAxisExt.size(); ii++) {
                    if (ii == xAxisBase.size() + xAxisExt.size() - 1) {
                        b.append(d[ii]);
                    } else {
                        b.append(d[ii]).append("\n");
                    }
                }

                axisChartDataDTO.setName(a.toString());
                axisChartDataDTO.setField(a.toString());

                List<ChartDimensionDTO> dimensionList = new ArrayList<>();
                List<ChartQuotaDTO> quotaList = new ArrayList<>();

                for (int j = 0; j < xAxis.size(); j++) {
                    ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                    chartDimensionDTO.setId(xAxis.get(j).getId());
                    chartDimensionDTO.setValue(d[j]);
                    dimensionList.add(chartDimensionDTO);
                }
                axisChartDataDTO.setDimensionList(dimensionList);

                int j = i - xAxis.size();
                ChartQuotaDTO chartQuotaDTO = new ChartQuotaDTO();
                chartQuotaDTO.setId(yAxis.get(j).getId());
                quotaList.add(chartQuotaDTO);
                axisChartDataDTO.setQuotaList(quotaList);
                try {
                    axisChartDataDTO.setValue(StringUtils.isEmpty(d[i]) ? null : new BigDecimal(d[i]));
                } catch (Exception e) {
                    axisChartDataDTO.setValue(new BigDecimal(0));
                }

                String category = StringUtils.defaultIfBlank(b.toString(),
                        StringUtils.defaultIfBlank(yAxis.get(j).getChartShowName(), yAxis.get(j).getName()));

                if (isLine) {
                    if (ObjectUtils.isEmpty(xAxisExt)) {
                        category = StringUtils.defaultIfBlank(yAxis.get(j).getChartShowName(), yAxis.get(j).getName());
                    }
                }

                axisChartDataDTO.setCategory(category);
                categories.add(category);

                buildDynamicValue(view, axisChartDataDTO, d, size, extSize);
                series.get(j).getData().add(axisChartDataDTO);
            }
        }
        if (CollectionUtils.isNotEmpty(series)) {
            series.get(0).setCategories(categories);
        }

        map.put("data", series);
        return map;
    }

    public static Map<String, Object> transMixChartStackDataAntV(List<ChartViewFieldDTO> xAxisBase, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> extStack, List<ChartViewFieldDTO> yAxis, ChartViewDTO view, List<String[]> data, boolean isDrill) {

        if (CollectionUtils.isEmpty(extStack)) {
            return transMixChartDataAntV(xAxisBase, xAxis, new ArrayList<>(), yAxis, view, data, isDrill);
        }

        Map<String, Object> map = new HashMap<>();

        List<Series> series = new ArrayList<>();
        for (ChartViewFieldDTO y : yAxis) {
            Series series1 = new Series();
            series1.setName(y.getName());
            series1.setType(y.getChartType());
            series1.setData(new ArrayList<>());
            series.add(series1);
        }
        Set<String> categories = new HashSet<>();

        for (int i1 = 0; i1 < data.size(); i1++) {
            String[] row = data.get(i1);

            AxisChartDataAntVDTO axisChartDataDTO = new AxisChartDataAntVDTO();
            StringBuilder a = new StringBuilder();
            if (isDrill) {
                a.append(row[xAxis.size() - 1]);
            } else {
                for (int i = 0; i < xAxisBase.size(); i++) {
                    if (i == xAxisBase.size() - 1) {
                        a.append(row[i]);
                    } else {
                        a.append(row[i]).append("\n");
                    }
                }
            }
            axisChartDataDTO.setField(a.toString());
            axisChartDataDTO.setName(a.toString());
            String category = row[xAxisBase.size()];
            axisChartDataDTO.setCategory(category);
            if (category != null) {
                categories.add(category);
            }

            List<ChartDimensionDTO> dimensionList = new ArrayList<>();
            List<ChartQuotaDTO> quotaList = new ArrayList<>();

            for (int k = 0; k < xAxis.size(); k++) {
                ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                chartDimensionDTO.setId(xAxis.get(k).getId());
                chartDimensionDTO.setValue(row[k]);
                dimensionList.add(chartDimensionDTO);
            }
            axisChartDataDTO.setDimensionList(dimensionList);

            if (ObjectUtils.isNotEmpty(yAxis)) {
                int valueIndex = xAxis.size();
                ChartQuotaDTO chartQuotaDTO = new ChartQuotaDTO();
                chartQuotaDTO.setId(yAxis.get(0).getId());
                quotaList.add(chartQuotaDTO);
                axisChartDataDTO.setQuotaList(quotaList);
                try {
                    axisChartDataDTO.setValue(StringUtils.isEmpty(row[valueIndex]) ? null : new BigDecimal(row[valueIndex]));
                } catch (Exception e) {
                    axisChartDataDTO.setValue(new BigDecimal(0));
                }
            } else {
                axisChartDataDTO.setQuotaList(quotaList);
                axisChartDataDTO.setValue(new BigDecimal(0));
            }
            series.get(0).getData().add(axisChartDataDTO);
        }

        if (CollectionUtils.isNotEmpty(series)) {
            series.get(0).setCategories(categories);
        }

        map.put("data", series);
        return map;
    }

    // 基础图形
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

    // 组合图形
    public static Map<String, Object> transMixChartData(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewDTO view, List<String[]> data, boolean isDrill) {
        Map<String, Object> map = new HashMap<>();

        List<String> x = new ArrayList<>();
        List<Series> series = new ArrayList<>();
        for (ChartViewFieldDTO y : yAxis) {
            Series series1 = new Series();
            series1.setName(y.getName());
            series1.setType(y.getChartType());
            series1.setData(new ArrayList<>());
            series.add(series1);
        }
        for (int i1 = 0; i1 < data.size(); i1++) {
            String[] d = data.get(i1);

            StringBuilder a = new StringBuilder();
            for (int i = xAxis.size(); i < xAxis.size() + yAxis.size(); i++) {
                List<ChartDimensionDTO> dimensionList = new ArrayList<>();
                List<ChartQuotaDTO> quotaList = new ArrayList<>();
                AxisChartDataDTO axisChartDataDTO = new AxisChartDataDTO();

                for (int j = 0; j < xAxis.size(); j++) {
                    ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                    chartDimensionDTO.setId(xAxis.get(j).getId());
                    chartDimensionDTO.setValue(d[j]);
                    dimensionList.add(chartDimensionDTO);
                }
                axisChartDataDTO.setDimensionList(dimensionList);

                int j = i - xAxis.size();
                ChartQuotaDTO chartQuotaDTO = new ChartQuotaDTO();
                chartQuotaDTO.setId(yAxis.get(j).getId());
                quotaList.add(chartQuotaDTO);
                axisChartDataDTO.setQuotaList(quotaList);
                try {
                    axisChartDataDTO.setValue(StringUtils.isEmpty(d[i]) ? null : new BigDecimal(d[i]));
                } catch (Exception e) {
                    axisChartDataDTO.setValue(new BigDecimal(0));
                }
                series.get(j).getData().add(axisChartDataDTO);
            }
            if (isDrill) {
                a.append(d[xAxis.size() - 1]);
            } else {
                for (int i = 0; i < xAxis.size(); i++) {
                    if (i == xAxis.size() - 1) {
                        a.append(d[i]);
                    } else {
                        a.append(d[i]).append("\n");
                    }
                }
            }
            x.add(a.toString());
        }

        map.put("x", x);
        map.put("series", series);
        return map;
    }

    // 文本卡图形
    public static Map<String, Object> transLabelChartData(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewDTO view, List<String[]> data, boolean isDrill) {
        Map<String, Object> map = new HashMap<>();

        List<String> x = new ArrayList<>();
        List<Series> series = new ArrayList<>();
        Series series1 = new Series();
        series1.setName(xAxis.get(0).getName());
        series1.setType(view.getType());
        series1.setData(new ArrayList<>());
        series.add(series1);
        for (String[] d : data) {
            StringBuilder a = new StringBuilder();
            if (isDrill) {
                a.append(d[xAxis.size() - 1]);
            } else {
                for (int i = 0; i < xAxis.size(); i++) {
                    if (i == xAxis.size() - 1) {
                        a.append(d[i]);
                    } else {
                        a.append(d[i]).append("\n");
                    }
                }
            }
            x.add(a.toString());
            series.get(0).getData().add(a.toString());
        }

        map.put("x", x);
        map.put("series", series);
        return map;
    }

    // 常规图形
    public static Map<String, Object> transNormalChartData(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewDTO view, List<String[]> data, boolean isDrill) {
        Map<String, Object> map = new HashMap<>();

        List<String> x = new ArrayList<>();
        List<Series> series = new ArrayList<>();
        for (ChartViewFieldDTO y : yAxis) {
            Series series1 = new Series();
            series1.setName(y.getName());
            series1.setType(view.getType());
            series1.setData(new ArrayList<>());
            series.add(series1);
        }
        for (String[] d : data) {
            StringBuilder a = new StringBuilder();
            if (isDrill) {
                a.append(d[xAxis.size() - 1]);
            } else {
                for (int i = 0; i < xAxis.size(); i++) {
                    if (i == xAxis.size() - 1) {
                        a.append(d[i]);
                    } else {
                        a.append(d[i]).append("\n");
                    }
                }
            }
            x.add(a.toString());
            for (int i = xAxis.size(); i < xAxis.size() + yAxis.size(); i++) {
                int j = i - xAxis.size();
                try {
                    series.get(j).getData().add(StringUtils.isEmpty(d[i]) ? null : new BigDecimal(d[i]));
                } catch (Exception e) {
                    series.get(j).getData().add(new BigDecimal(0));
                }
            }
        }

        map.put("x", x);
        map.put("series", series);
        return map;
    }

    // radar图
    public static Map<String, Object> transRadarChartData(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewDTO view, List<String[]> data, boolean isDrill) {
        Map<String, Object> map = new HashMap<>();

        List<String> x = new ArrayList<>();
        List<Series> series = new ArrayList<>();
        for (ChartViewFieldDTO y : yAxis) {
            Series series1 = new Series();
            series1.setName(y.getName());
            series1.setType(view.getType());
            series1.setData(new ArrayList<>());
            series.add(series1);
        }
        for (String[] d : data) {
            StringBuilder a = new StringBuilder();
            if (isDrill) {
                a.append(d[xAxis.size() - 1]);
            } else {
                for (int i = 0; i < xAxis.size(); i++) {
                    if (i == xAxis.size() - 1) {
                        a.append(d[i]);
                    } else {
                        a.append(d[i]).append("\n");
                    }
                }
            }
            x.add(a.toString());
            for (int i = xAxis.size(); i < xAxis.size() + yAxis.size(); i++) {
                int j = i - xAxis.size();
                try {
                    series.get(j).getData().add(StringUtils.isEmpty(d[i]) ? null : new BigDecimal(d[i]));
                } catch (Exception e) {
                    series.get(j).getData().add(new BigDecimal(0));
                }
            }
        }

        map.put("x", x);
        map.put("series", series);
        return map;
    }

    // 堆叠图
    public static Map<String, Object> transStackChartData(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewDTO view, List<String[]> data, List<ChartViewFieldDTO> extStack, boolean isDrill) {
        Map<String, Object> map = new HashMap<>();

        List<String> x = new ArrayList<>();
        List<String> stack = new ArrayList<>();
        List<Series> series = new ArrayList<>();

        if (ObjectUtils.isNotEmpty(extStack)) {
            AxisChartDataDTO defaultAxisChartDataDTO = new AxisChartDataDTO();
            BigDecimal defaultValue = StringUtils.containsIgnoreCase(view.getType(), "line") ? new BigDecimal(0) : null;
            defaultAxisChartDataDTO.setValue(defaultValue);
            // 构建横轴
            for (String[] d : data) {
                StringBuilder a = new StringBuilder();
                if (isDrill) {
                    a.append(d[xAxis.size() - 1]);
                } else {
                    for (int i = 0; i < xAxis.size(); i++) {
                        if (i == xAxis.size() - 1) {
                            a.append(d[i]);
                        } else {
                            a.append(d[i]).append("\n");
                        }
                    }
                }
                x.add(a.toString());
            }
            x = x.stream().distinct().collect(Collectors.toList());
            // 构建堆叠
            for (String[] d : data) {
                stack.add(d[xAxis.size()]);
            }
            stack = stack.stream().distinct().collect(Collectors.toList());
            for (String s : stack) {
                Series series1 = new Series();
                series1.setName(s);
                series1.setType(view.getType());
                List<Object> list = new ArrayList<>();
                for (int i = 0; i < x.size(); i++) {
                    list.add(defaultAxisChartDataDTO);
                }
                series1.setData(list);
                series.add(series1);
            }
            for (Series ss : series) {
                for (int i = 0; i < x.size(); i++) {
                    for (String[] row : data) {
                        String stackColumn = row[xAxis.size()];
                        if (StringUtils.equals(ss.getName(), stackColumn)) {
                            StringBuilder a = new StringBuilder();
                            if (isDrill) {
                                a.append(row[xAxis.size() - 1]);
                            } else {
                                for (int j = 0; j < xAxis.size(); j++) {
                                    if (j == xAxis.size() - 1) {
                                        a.append(row[j]);
                                    } else {
                                        a.append(row[j]).append("\n");
                                    }
                                }
                            }
                            if (StringUtils.equals(a.toString(), x.get(i))) {
                                if (row.length > xAxis.size() + extStack.size()) {
                                    List<ChartDimensionDTO> dimensionList = new ArrayList<>();
                                    List<ChartQuotaDTO> quotaList = new ArrayList<>();
                                    AxisChartDataDTO axisChartDataDTO = new AxisChartDataDTO();

                                    ChartQuotaDTO chartQuotaDTO = new ChartQuotaDTO();
                                    chartQuotaDTO.setId(yAxis.get(0).getId());
                                    quotaList.add(chartQuotaDTO);
                                    axisChartDataDTO.setQuotaList(quotaList);

                                    for (int k = 0; k < xAxis.size(); k++) {
                                        ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                                        chartDimensionDTO.setId(xAxis.get(k).getId());
                                        chartDimensionDTO.setValue(row[k]);
                                        dimensionList.add(chartDimensionDTO);
                                    }
                                    ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                                    chartDimensionDTO.setId(extStack.get(0).getId());
                                    chartDimensionDTO.setValue(row[xAxis.size()]);
                                    dimensionList.add(chartDimensionDTO);
                                    axisChartDataDTO.setDimensionList(dimensionList);

                                    String s = row[xAxis.size() + extStack.size()];
                                    if (StringUtils.isNotEmpty(s)) {
                                        axisChartDataDTO.setValue(new BigDecimal(s));
                                        ss.getData().set(i, axisChartDataDTO);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            for (ChartViewFieldDTO y : yAxis) {
                Series series1 = new Series();
                series1.setName(y.getName());
                series1.setType(view.getType());
                series1.setData(new ArrayList<>());
                series.add(series1);
            }
            for (int i1 = 0; i1 < data.size(); i1++) {
                String[] d = data.get(i1);

                StringBuilder a = new StringBuilder();
                for (int i = xAxis.size(); i < xAxis.size() + yAxis.size(); i++) {
                    List<ChartDimensionDTO> dimensionList = new ArrayList<>();
                    List<ChartQuotaDTO> quotaList = new ArrayList<>();
                    AxisChartDataDTO axisChartDataDTO = new AxisChartDataDTO();

                    for (int j = 0; j < xAxis.size(); j++) {
                        ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                        chartDimensionDTO.setId(xAxis.get(j).getId());
                        chartDimensionDTO.setValue(d[j]);
                        dimensionList.add(chartDimensionDTO);
                    }
                    axisChartDataDTO.setDimensionList(dimensionList);

                    int j = i - xAxis.size();
                    ChartQuotaDTO chartQuotaDTO = new ChartQuotaDTO();
                    chartQuotaDTO.setId(yAxis.get(j).getId());
                    quotaList.add(chartQuotaDTO);
                    axisChartDataDTO.setQuotaList(quotaList);
                    try {
                        axisChartDataDTO.setValue(StringUtils.isEmpty(d[i]) ? null : new BigDecimal(d[i]));
                    } catch (Exception e) {
                        axisChartDataDTO.setValue(new BigDecimal(0));
                    }
                    series.get(j).getData().add(axisChartDataDTO);
                }
                if (isDrill) {
                    a.append(d[xAxis.size() - 1]);
                } else {
                    for (int i = 0; i < xAxis.size(); i++) {
                        if (i == xAxis.size() - 1) {
                            a.append(d[i]);
                        } else {
                            a.append(d[i]).append("\n");
                        }
                    }
                }
                x.add(a.toString());
            }
        }

        map.put("x", x);
        map.put("series", series);
        return map;
    }

    // 散点图
    public static Map<String, Object> transScatterData(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewDTO view, List<String[]> data, List<ChartViewFieldDTO> extBubble, boolean isDrill) {
        Map<String, Object> map = new HashMap<>();

        List<String> x = new ArrayList<>();
        List<Series> series = new ArrayList<>();
        for (ChartViewFieldDTO y : yAxis) {
            Series series1 = new Series();
            series1.setName(y.getName());
            series1.setType(view.getType());
            series1.setData(new ArrayList<>());
            series.add(series1);
        }
        for (int i1 = 0; i1 < data.size(); i1++) {
            String[] d = data.get(i1);

            StringBuilder a = new StringBuilder();
            if (isDrill) {
                a.append(d[xAxis.size() - 1]);
            } else {
                for (int i = 0; i < xAxis.size(); i++) {
                    if (i == xAxis.size() - 1) {
                        a.append(d[i]);
                    } else {
                        a.append(d[i]).append("\n");
                    }
                }
            }
            x.add(a.toString());
            for (int i = xAxis.size(); i < xAxis.size() + yAxis.size(); i++) {
                List<ChartDimensionDTO> dimensionList = new ArrayList<>();
                List<ChartQuotaDTO> quotaList = new ArrayList<>();
                ScatterChartDataDTO scatterChartDataDTO = new ScatterChartDataDTO();

                for (int j = 0; j < xAxis.size(); j++) {
                    ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                    chartDimensionDTO.setId(xAxis.get(j).getId());
                    chartDimensionDTO.setValue(d[j]);
                    dimensionList.add(chartDimensionDTO);
                }
                scatterChartDataDTO.setDimensionList(dimensionList);

                int j = i - xAxis.size();
                ChartQuotaDTO chartQuotaDTO = new ChartQuotaDTO();
                chartQuotaDTO.setId(yAxis.get(j).getId());
                quotaList.add(chartQuotaDTO);
                scatterChartDataDTO.setQuotaList(quotaList);

                if (ObjectUtils.isNotEmpty(extBubble) && extBubble.size() > 0) {
                    try {
                        scatterChartDataDTO.setValue(new Object[]{
                                a.toString(),
                                StringUtils.isEmpty(d[i]) ? null : new BigDecimal(d[i]),
                                StringUtils.isEmpty(d[xAxis.size() + yAxis.size()]) ? null : new BigDecimal(d[xAxis.size() + yAxis.size()])
                        });
                    } catch (Exception e) {
                        scatterChartDataDTO.setValue(new Object[]{a.toString(), new BigDecimal(0), new BigDecimal(0)});
                    }
                } else {
                    try {
                        scatterChartDataDTO.setValue(new Object[]{
                                a.toString(),
                                StringUtils.isEmpty(d[i]) ? null : new BigDecimal(d[i])
                        });
                    } catch (Exception e) {
                        scatterChartDataDTO.setValue(new Object[]{a.toString(), new BigDecimal(0)});
                    }
                }
                series.get(j).getData().add(scatterChartDataDTO);
            }
        }

        map.put("x", x);
        map.put("series", series);
        return map;
    }

    // 表格
    public static Map<String, Object> transTableNormal(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewDTO view, List<String[]> data, List<ChartViewFieldDTO> extStack, Map<String, ColumnPermissionItem> desensitizationList) {
        List<ChartViewFieldDTO> fields = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(xAxis)) {
            fields.addAll(xAxis);
        }
        if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
            if (ObjectUtils.isNotEmpty(extStack)) {
                fields.addAll(extStack);
            }
        }
        fields.addAll(yAxis);
        return transTableNormal(fields, view, data, desensitizationList);
    }

    public static Map<String, Object> transTableNormalWithDetail(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<String[]> data, List<ChartViewFieldDTO> detailFields, List<String[]> detailData, Map<String, ColumnPermissionItem> desensitizationList) {
        int detailIndex = xAxis.size();

        List<ChartViewFieldDTO> realDetailFields = detailFields.subList(detailIndex, detailFields.size());

        List<ChartViewFieldDTO> fields = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(xAxis))
            fields.addAll(xAxis);
        if (ObjectUtils.isNotEmpty(yAxis))
            fields.addAll(yAxis);
        Map<String, Object> map = transTableNormal(fields, null, data, desensitizationList);
        List<Map<String, Object>> tableRow = (List<Map<String, Object>>) map.get("tableRow");
        final int xEndIndex = detailIndex;
        Map<String, List<String[]>> groupDataList = detailData.stream().collect(Collectors.groupingBy(item -> "(" + StringUtils.join(ArrayUtils.subarray(item, 0, xEndIndex), ")-de-(") + ")"));

        tableRow.forEach(row -> {
            String key = xAxis.stream().map(x -> String.format(format, row.get(x.getDataeaseName()).toString())).collect(Collectors.joining("-de-"));
            List<String[]> detailFieldValueList = groupDataList.get(key);
            List<Map<String, Object>> detailValueMapList = detailFieldValueList.stream().map((detailArr -> {
                Map<String, Object> temp = new HashMap<>();
                for (int i = 0; i < realDetailFields.size(); i++) {
                    ChartViewFieldDTO realDetailField = realDetailFields.get(i);
                    temp.put(realDetailField.getDataeaseName(), detailArr[detailIndex + i]);
                }
                return temp;
            })).collect(Collectors.toList());
            row.put("details", detailValueMapList);
        });

        ChartViewFieldDTO detailFieldDTO = new ChartViewFieldDTO();
        detailFieldDTO.setId(IDUtils.snowID());
        detailFieldDTO.setName("detail");
        detailFieldDTO.setDataeaseName("detail");
        fields.add(detailFieldDTO);
        map.put("fields", fields);
        map.put("detailFields", realDetailFields);
        map.put("tableRow", tableRow);
        return map;
    }

    // 表格
    public static Map<String, Object> transTableNormal(Map<String, List<ChartViewFieldDTO>> fieldMap, ChartViewDTO view, List<String[]> data, Map<String, ColumnPermissionItem> desensitizationList) {

        List<ChartViewFieldDTO> fields = new ArrayList<>();
        List<ChartViewFieldDTO> yfields = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(fieldMap.get("xAxis"))) fields.addAll(fieldMap.get("xAxis"));
        if (ObjectUtils.isNotEmpty(fieldMap.get("tooltipAxis"))) {
            fieldMap.get("tooltipAxis").forEach(field -> {
                Integer deType = field.getDeType();
                if (deType == 2 || deType == 3) {
                    yfields.add(field);
                } else {
                    fields.add(field);
                }
            });
        }
        if (ObjectUtils.isNotEmpty(fieldMap.get("labelAxis"))) {
            fieldMap.get("labelAxis").forEach(field -> {
                Integer deType = field.getDeType();
                if (deType == 2 || deType == 3) {
                    yfields.add(field);
                } else {
                    fields.add(field);
                }
            });
        }
        if (ObjectUtils.isNotEmpty(fieldMap.get("yAxis"))) fields.addAll(fieldMap.get("yAxis"));
        if (ObjectUtils.isNotEmpty(yfields)) fields.addAll(yfields);
        return transTableNormal(fields, view, data, desensitizationList);
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

    public static Map<String, Object> transTableNormal(List<ChartViewFieldDTO> fields, ChartViewDTO view, List<String[]> data, Map<String, ColumnPermissionItem> desensitizationList) {
        Map<String, Object> map = new TreeMap<>();
        List<Map<String, Object>> tableRow = new ArrayList<>();
        data.forEach(ele -> {
            Map<String, Object> d = new HashMap<>();
            for (int i = 0; i < fields.size(); i++) {
                if (ObjectUtils.isNotEmpty(desensitizationList.keySet()) && desensitizationList.containsKey(fields.get(i).getDataeaseName())) {
                    String desensitizationValue = desensitizationValue(desensitizationList.get(fields.get(i).getDataeaseName()), String.valueOf(ele[i]));
                    ele[i] = desensitizationValue;
                    d.put(fields.get(i).getDataeaseName(), desensitizationValue);
                    continue;
                }
                if (i == ele.length) break;
                ChartViewFieldDTO chartViewFieldDTO = fields.get(i);
                if (chartViewFieldDTO.getDeType() == 0 || chartViewFieldDTO.getDeType() == 1 || chartViewFieldDTO.getDeType() == 5 || chartViewFieldDTO.getDeType() == 7) {
                    d.put(fields.get(i).getDataeaseName(), StringUtils.isEmpty(ele[i]) ? "" : ele[i]);
                } else if (chartViewFieldDTO.getDeType() == 2 || chartViewFieldDTO.getDeType() == 3 || chartViewFieldDTO.getDeType() == 4) {
                    if (view.getIsExcelExport()) {
                        d.put(fields.get(i).getDataeaseName(), StringUtils.isEmpty(ele[i]) ? "" : ele[i]);
                    } else {
                        d.put(fields.get(i).getDataeaseName(), StringUtils.isEmpty(ele[i]) ? null : new BigDecimal(ele[i]).setScale(8, RoundingMode.HALF_UP));
                    }
                }
            }
            tableRow.add(d);
        });
        map.put("fields", fields);
        map.put("tableRow", tableRow);
        return map;
    }

    public static Map<String, Object> transGroupStackDataAntV(List<ChartViewFieldDTO> xAxisBase, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> xAxisExt, List<ChartViewFieldDTO> yAxis, List<ChartViewFieldDTO> extStack, List<String[]> data, ChartViewDTO view, boolean isDrill) {
        // 堆叠柱状图
        if (ObjectUtils.isEmpty(xAxisExt)) {
            return transStackChartDataAntV(xAxisBase, xAxis, yAxis, view, data, extStack, isDrill);
            //  分组柱状图
        } else if (ObjectUtils.isNotEmpty(xAxisExt) && ObjectUtils.isEmpty(extStack)) {
            return transBaseGroupDataAntV(xAxisBase, xAxis, xAxisExt, yAxis, view, data, isDrill);
            // 分组堆叠柱状图
        } else {
            Map<String, Object> map = new HashMap<>();

            List<AxisChartDataAntVDTO> dataList = new ArrayList<>();
            for (int i1 = 0; i1 < data.size(); i1++) {
                String[] row = data.get(i1);

                StringBuilder xField = new StringBuilder();
                if (isDrill) {
                    xField.append(row[xAxis.size() - 1]);
                } else {
                    for (int i = 0; i < xAxisBase.size(); i++) {
                        if (i == xAxisBase.size() - 1) {
                            xField.append(row[i]);
                        } else {
                            xField.append(row[i]).append("\n");
                        }
                    }
                }

                StringBuilder groupField = new StringBuilder();
                for (int i = xAxisBase.size(); i < xAxisBase.size() + xAxisExt.size(); i++) {
                    if (i == xAxisBase.size() + xAxisExt.size() - 1) {
                        groupField.append(row[i]);
                    } else {
                        groupField.append(row[i]).append("\n");
                    }
                }

                StringBuilder stackField = new StringBuilder();
                for (int i = xAxisBase.size() + xAxisExt.size(); i < xAxisBase.size() + xAxisExt.size() + extStack.size(); i++) {
                    if (i == xAxisBase.size() + xAxisExt.size() + extStack.size() - 1) {
                        stackField.append(row[i]);
                    } else {
                        stackField.append(row[i]).append("\n");
                    }
                }

                AxisChartDataAntVDTO axisChartDataDTO = new AxisChartDataAntVDTO();
                axisChartDataDTO.setField(xField.toString());
                axisChartDataDTO.setName(xField.toString());

                List<ChartDimensionDTO> dimensionList = new ArrayList<>();
                List<io.dataease.extensions.view.dto.ChartQuotaDTO> quotaList = new ArrayList<>();

                for (int j = 0; j < xAxis.size(); j++) {
                    ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                    chartDimensionDTO.setId(xAxis.get(j).getId());
                    chartDimensionDTO.setValue(row[j]);
                    dimensionList.add(chartDimensionDTO);
                }
                ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                chartDimensionDTO.setId(extStack.get(0).getId());
                chartDimensionDTO.setValue(row[xAxis.size()]);
                dimensionList.add(chartDimensionDTO);
                axisChartDataDTO.setDimensionList(dimensionList);

                if (ObjectUtils.isNotEmpty(yAxis)) {
                    int valueIndex = xAxis.size();
                    ChartQuotaDTO chartQuotaDTO = new ChartQuotaDTO();
                    chartQuotaDTO.setId(yAxis.get(0).getId());
                    quotaList.add(chartQuotaDTO);
                    axisChartDataDTO.setQuotaList(quotaList);
                    try {
                        axisChartDataDTO.setValue(StringUtils.isEmpty(row[valueIndex]) ? null : new BigDecimal(row[valueIndex]));
                    } catch (Exception e) {
                        axisChartDataDTO.setValue(new BigDecimal(0));
                    }
                } else {
                    axisChartDataDTO.setQuotaList(quotaList);
                    axisChartDataDTO.setValue(new BigDecimal(0));
                }
                axisChartDataDTO.setGroup(groupField.toString());
                axisChartDataDTO.setCategory(stackField.toString());
                dataList.add(axisChartDataDTO);
            }
            map.put("data", dataList);
            return map;
        }
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

    //AntV quadrant
    public static Map<String, Object> transQuadrantDataAntV(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewDTO view, List<String[]> data, List<ChartViewFieldDTO> extBubble, boolean isDrill) {
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
            for (int i = 0; i < xAxis.size() + yAxis.size(); i++) {
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
                if (j > -1) {
                    ChartQuotaDTO chartQuotaDTO = new ChartQuotaDTO();
                    chartQuotaDTO.setId(yAxis.get(j).getId());
                    quotaList.add(chartQuotaDTO);
                    axisChartDataDTO.setQuotaList(quotaList);
                    try {
                        axisChartDataDTO.setValue(StringUtils.isEmpty(row[i]) ? null : new BigDecimal(row[i]));
                        axisChartDataDTO.setField(yAxis.get(j).getOriginName());
                        axisChartDataDTO.setName(yAxis.get(j).getName());
                    } catch (Exception e) {
                        axisChartDataDTO.setValue(new BigDecimal(0));
                    }
                    axisChartDataDTO.setCategory(StringUtils.defaultIfBlank(yAxis.get(j).getChartShowName(), yAxis.get(j).getName()));
                }
                dataList.add(axisChartDataDTO);
            }
        }
        map.put("data", dataList);
        return map;
    }

    public static Map<String, Object> transBarRangeDataAntV(boolean skipBarRange, boolean isDate, List<ChartViewFieldDTO> xAxisBase, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewDTO view, List<String[]> data, boolean isDrill) {

        Map<String, Object> map = new HashMap<>();
        if (skipBarRange) {
            map.put("data", new ArrayList<>());
            return map;
        }

        List<Date> dates = new ArrayList<>();
        List<BigDecimal> numbers = new ArrayList<>();

        ChartViewFieldDTO dateAxis1 = null;

        SimpleDateFormat sdf = null;
        if (isDate) {
            if (BooleanUtils.isTrue(view.getAggregate())) {
                dateAxis1 = yAxis.get(0);
            } else {
                dateAxis1 = xAxis.get(xAxisBase.size());
            }
            sdf = new SimpleDateFormat(getDateFormat(dateAxis1.getDateStyle(), dateAxis1.getDatePattern()));
        }

        List<Object> dataList = new ArrayList<>();
        for (int i1 = 0; i1 < data.size(); i1++) {
            String[] row = data.get(i1);

            StringBuilder xField = new StringBuilder();
            if (isDrill) {
                xField.append(row[xAxis.size() - 1]);
            } else {
                for (int i = 0; i < xAxisBase.size(); i++) {
                    if (i == xAxisBase.size() - 1) {
                        xField.append(row[i]);
                    } else {
                        xField.append(row[i]).append("\n");
                    }
                }
            }


            Map<String, Object> obj = new HashMap<>();
            obj.put("field", xField.toString());
            obj.put("category", xField.toString());

            List<ChartDimensionDTO> dimensionList = new ArrayList<>();

            for (int i = 0; i < xAxisBase.size(); i++) {
                ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                chartDimensionDTO.setId(xAxis.get(i).getId());
                chartDimensionDTO.setValue(row[i]);
                dimensionList.add(chartDimensionDTO);
            }
            if (isDrill) {
                int index = xAxis.size() - 1;
                ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                chartDimensionDTO.setId(xAxis.get(index).getId());
                chartDimensionDTO.setValue(row[index]);
                dimensionList.add(chartDimensionDTO);
            }
            obj.put("dimensionList", dimensionList);


            List<Object> values = new ArrayList<>();

            if (row[xAxisBase.size()] == null || row[xAxisBase.size() + 1] == null) {
                continue;
            }

            if (isDate) {
                int index;
                if (BooleanUtils.isTrue(view.getAggregate())) {
                    index = xAxis.size();
                } else {
                    index = xAxisBase.size();
                }

                values.add(row[index]);
                values.add(row[index + 1]);
                obj.put("values", values);
                Date date1 = null, date2 = null;
                try {
                    date1 = sdf.parse(row[index]);
                    if (date1 != null) {
                        dates.add(date1);
                    }
                } catch (Exception ignore) {
                }
                try {
                    date2 = sdf.parse(row[index + 1]);
                    if (date2 != null) {
                        dates.add(date2);
                    }
                } catch (Exception ignore) {
                }
                //间隔时间
                obj.put("gap", getTimeGap(date1, date2, dateAxis1.getDateStyle()));

            } else {
                values.add(new BigDecimal(row[xAxis.size()]));
                values.add(new BigDecimal(row[xAxis.size() + 1]));
                obj.put("values", values);

                numbers.add(new BigDecimal(row[xAxis.size()]));
                numbers.add(new BigDecimal(row[xAxis.size() + 1]));

                //间隔差
                obj.put("gap", new BigDecimal(row[xAxis.size() + 1]).subtract(new BigDecimal(row[xAxis.size()])));
            }

            dataList.add(obj);
        }

        if (isDate) {
            Date minDate = dates.stream().min(Date::compareTo).orElse(null);
            if (minDate != null) {
                map.put("minTime", sdf.format(minDate));
            }
            Date maxDate = dates.stream().max(Date::compareTo).orElse(null);
            if (maxDate != null) {
                map.put("maxTime", sdf.format(maxDate));
            }
        } else {
            map.put("min", numbers.stream().min(BigDecimal::compareTo).orElse(null));
            map.put("max", numbers.stream().max(BigDecimal::compareTo).orElse(null));
        }

        map.put("isDate", isDate);
        map.put("data", dataList);
        return map;

    }

    private static String getDateFormat(String dateStyle, String datePattern) {
        String split;
        if (StringUtils.equalsIgnoreCase(datePattern, "date_split")) {
            split = "/";
        } else {
            split = "-";
        }
        switch (dateStyle) {
            case "y":
                return "yyyy";
            case "y_M":
                return "yyyy" + split + "MM";
            case "y_M_d":
                return "yyyy" + split + "MM" + split + "dd";
            case "H_m_s":
                return "HH:mm:ss";
            case "y_M_d_H":
                return "yyyy" + split + "MM" + split + "dd" + " HH";
            case "y_M_d_H_m":
                return "yyyy" + split + "MM" + split + "dd" + " HH:mm";
            case "y_M_d_H_m_s":
                return "yyyy" + split + "MM" + split + "dd" + " HH:mm:ss";
            default:
                return "yyyy-MM-dd HH:mm:ss";
        }
    }

    private static String getTimeGap(Date from, Date to, String dateStyle) {
        if (from == null || to == null) {
            return "";
        }
        Calendar fromCalender = Calendar.getInstance();
        fromCalender.setTime(from);

        Calendar toCalender = Calendar.getInstance();
        toCalender.setTime(to);

        long yearGap = 0;
        long monthGap = 0;
        long dayGap = (toCalender.getTimeInMillis() - fromCalender.getTimeInMillis()) / (1000 * 3600 * 24);
        long hourGap = ((toCalender.getTimeInMillis() - fromCalender.getTimeInMillis()) / (1000 * 3600)) % 24;
        long minuteGap = ((toCalender.getTimeInMillis() - fromCalender.getTimeInMillis()) / (1000 * 60)) % 60;
        long secondGap = ((toCalender.getTimeInMillis() - fromCalender.getTimeInMillis()) / 1000) % 60;

        String language = "zh-CN"; //国际化
        Lang lang = Lang.getLangWithoutDefault(language);
        boolean isEnUs = Lang.en_US.equals(lang);
        String splitter = isEnUs ? " " : "";

        String yearGapStr = "";
        String monthGapStr = "";

        String dayGapStr = "";
        if (dayGap != 0) {
            dayGapStr = dayGap + splitter + Translator.get("i18n_day") + (isEnUs && dayGap != 1 ? "s" : "");
        }
        String hourGapStr = "";
        if (hourGap != 0) {
            hourGapStr = hourGap + splitter + Translator.get("i18n_hour") + (isEnUs && hourGap != 1 ? "s" : "");
        }
        String minuteGapStr = "";
        if (minuteGap != 0) {
            minuteGapStr = minuteGap + splitter + Translator.get("i18n_minute") + (isEnUs && minuteGap != 1 ? "s" : "");
        }
        String secondGapStr = "";
        if (secondGap != 0) {
            secondGapStr = secondGap + splitter + Translator.get("i18n_second") + (isEnUs && secondGap != 1 ? "s" : "");
        }

        List<String> list = new ArrayList<>();

        switch (dateStyle) {
            case "y":
                yearGap = toCalender.get(Calendar.YEAR) - fromCalender.get(Calendar.YEAR);
                yearGapStr = yearGap == 0 ? "" : (yearGap + splitter + Translator.get("i18n_year") + (isEnUs && yearGap != 1 ? "s" : ""));
                return yearGapStr;
            case "y_M":
                yearGap = ((toCalender.get(Calendar.YEAR) - fromCalender.get(Calendar.YEAR)) * 12L + (toCalender.get(Calendar.MONTH) - fromCalender.get(Calendar.MONTH))) / 12;
                monthGap = ((toCalender.get(Calendar.YEAR) - fromCalender.get(Calendar.YEAR)) * 12L + (toCalender.get(Calendar.MONTH) - fromCalender.get(Calendar.MONTH))) % 12;

                yearGapStr = yearGap == 0 ? "" : (yearGap + splitter + Translator.get("i18n_year") + (isEnUs && yearGap != 1 ? "s" : ""));
                monthGapStr = monthGap == 0 ? "" : (monthGap + splitter + Translator.get("i18n_month") + (isEnUs && monthGap != 1 ? "s" : ""));

                if (!yearGapStr.isEmpty()) {
                    list.add(yearGapStr);
                }
                if (!monthGapStr.isEmpty()) {
                    list.add(monthGapStr);
                }
                return StringUtils.join(list, splitter);
            case "y_M_d":
                return dayGapStr;
            case "y_M_d_H":
                if (!dayGapStr.isEmpty()) {
                    list.add(dayGapStr);
                }
                if (!hourGapStr.isEmpty()) {
                    list.add(hourGapStr);
                }
                return StringUtils.join(list, splitter);
            case "y_M_d_H_m":
                if (!dayGapStr.isEmpty()) {
                    list.add(dayGapStr);
                }
                if (!hourGapStr.isEmpty()) {
                    list.add(hourGapStr);
                }
                if (!minuteGapStr.isEmpty()) {
                    list.add(minuteGapStr);
                }
                return StringUtils.join(list, splitter);
            case "H_m_s":
            case "y_M_d_H_m_s":
                if (!dayGapStr.isEmpty()) {
                    list.add(dayGapStr);
                }
                if (!hourGapStr.isEmpty()) {
                    list.add(hourGapStr);
                }
                if (!minuteGapStr.isEmpty()) {
                    list.add(minuteGapStr);
                }
                if (!secondGapStr.isEmpty()) {
                    list.add(secondGapStr);
                }
                return StringUtils.join(list, splitter);
            default:
                return "";
        }
    }

    public static Map<String, Object> transSymbolicMapNormalWithDetail(ChartViewDTO view, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartViewFieldDTO> extBubble, List<String[]> data, List<ChartViewFieldDTO> detailFields, List<String[]> detailData) {
        int detailIndex = xAxis.size();

        List<ChartViewFieldDTO> realDetailFields = detailFields.subList(detailIndex, detailFields.size());

        List<ChartViewFieldDTO> fields = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(xAxis))
            fields.addAll(xAxis);
        if (ObjectUtils.isNotEmpty(extBubble))
            fields.addAll(extBubble);
        if (ObjectUtils.isNotEmpty(yAxis))
            fields.addAll(yAxis);
        Map<String, Object> map = transTableNormal(fields, view, data, new HashMap<>());
        List<Map<String, Object>> tableRow = (List<Map<String, Object>>) map.get("tableRow");
        final int xEndIndex = detailIndex;
        Map<String, List<String[]>> groupDataList = detailData.stream().collect(Collectors.groupingBy(item -> "(" + StringUtils.join(ArrayUtils.subarray(item, 0, xEndIndex), ")-de-(") + ")"));
        String extBubbleDataeaseName = ObjectUtils.isNotEmpty(extBubble) ? extBubble.get(0).getDataeaseName() : "";
        tableRow.forEach(row -> {
            BigDecimal rowValue = row.get(extBubbleDataeaseName) == null ? BigDecimal.ZERO : new BigDecimal(row.get(extBubbleDataeaseName).toString());
            String key = xAxis.stream().map(x -> String.format(format, row.get(x.getDataeaseName()).toString())).collect(Collectors.joining("-de-"));
            List<String[]> detailFieldValueList = groupDataList.get(key);
            List<Map<String, Object>> detailValueMapList = Optional.ofNullable(detailFieldValueList).orElse(new ArrayList<>()).stream().map((detailArr -> {
                Map<String, Object> temp = new HashMap<>();
                for (int i = 0; i < realDetailFields.size(); i++) {
                    ChartViewFieldDTO realDetailField = realDetailFields.get(i);
                    if (StringUtils.equalsIgnoreCase(extBubbleDataeaseName, realDetailField.getDataeaseName())) {
                        temp.put(realDetailField.getDataeaseName(), rowValue);
                    } else {
                        temp.put(realDetailField.getDataeaseName(), detailArr[detailIndex + i]);
                    }
                }
                return temp;
            })).collect(Collectors.toList());
            //详情只要一个
            row.put("details", !detailValueMapList.isEmpty() ? Collections.singletonList(detailValueMapList.getFirst()) : detailValueMapList);
        });
        // 先过滤掉所有记录数字段
        List<ChartViewFieldDTO> filterCountAxis = fields.stream()
                .filter(item -> !StringUtils.equalsIgnoreCase(item.getDataeaseName(), "*"))
                .collect(Collectors.toList());
        // 如果气泡大小是记录数，添加到字段列表中
        if (ObjectUtils.isNotEmpty(extBubble) && "*".equals(extBubble.get(0).getDataeaseName())) {
            filterCountAxis.addAll(yAxis);
        }
        map.put("fields", filterCountAxis);
        map.put("detailFields", realDetailFields);
        map.put("tableRow", tableRow);
        return map;
    }

}

package io.dataease.plugins.view.official.impl;

import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.dto.StaticResource;
import io.dataease.plugins.common.dto.chart.ChartFieldCompareDTO;
import io.dataease.plugins.view.entity.*;
import io.dataease.plugins.view.official.handler.ChartMixViewStatHandler;
import io.dataease.plugins.view.service.ViewPluginService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ChartMixService extends ViewPluginService {
    private static final String VIEW_TYPE_VALUE = "chart-mix";

    /* 下版这些常量移到sdk */
    private static final String TYPE = "-type";
    private static final String DATA = "-data";
    private static final String STYLE = "-style";
    private static final String VIEW = "-view";
    private static final String SUFFIX = "svg";
    /* 下版这些常量移到sdk */
    private static final String VIEW_TYPE = VIEW_TYPE_VALUE + TYPE;
    private static final String VIEW_DATA = VIEW_TYPE_VALUE + DATA;
    private static final String VIEW_STYLE = VIEW_TYPE_VALUE + STYLE;
    private static final String VIEW_VIEW = VIEW_TYPE_VALUE + VIEW;

    private static final String[] VIEW_STYLE_PROPERTIES = {
            "color-selector",
            "label-selector",
            "tooltip-selector-ant-v",
            "title-selector-ant-v"
    };

    private static final Map<String, String[]> VIEW_STYLE_PROPERTY_INNER = new HashMap<>();

    static {
        VIEW_STYLE_PROPERTY_INNER.put("color-selector", new String[]{"value"});
        VIEW_STYLE_PROPERTY_INNER.put("label-selector", new String[]{"show", "fontSize", "color"});
        VIEW_STYLE_PROPERTY_INNER.put("tooltip-selector-ant-v", new String[]{"show", "fontSize", "color", "backgroundColor"});
        VIEW_STYLE_PROPERTY_INNER.put("title-selector-ant-v", new String[]{"show", "title", "fontSize", "color", "hPosition", "vPosition", "isItalic", "isBolder"});
    }

    @Override
    public PluginViewType viewType() {
        PluginViewType pluginViewType = new PluginViewType();
        pluginViewType.setRender("antv");
        pluginViewType.setCategory("chart.chart_type_trend");
        pluginViewType.setValue(VIEW_TYPE_VALUE);
        pluginViewType.setProperties(VIEW_STYLE_PROPERTIES);
        pluginViewType.setPropertyInner(VIEW_STYLE_PROPERTY_INNER);
        return pluginViewType;
    }

    @Override
    public Object format(Object o) {
        return null;
    }

    @Override
    public List<String> components() {
        List<String> results = new ArrayList<>();
        results.add(VIEW_VIEW);
        results.add(VIEW_DATA);
        results.add(VIEW_TYPE);
        results.add(VIEW_STYLE);
        return results;
    }

    @Override
    public List<StaticResource> staticResources() {
        List<StaticResource> results = new ArrayList<>();
        StaticResource staticResource = new StaticResource();
        staticResource.setName(VIEW_TYPE_VALUE);
        staticResource.setSuffix(SUFFIX);
        results.add(staticResource);
        results.add(pluginSvg());
        return results;
    }

    private StaticResource pluginSvg() {
        StaticResource staticResource = new StaticResource();
        staticResource.setName("view-chartmix-backend");
        staticResource.setSuffix("svg");
        return staticResource;
    }


    @Override
    protected InputStream readContent(String s) {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("static/" + s);
        return resourceAsStream;
    }

    @Override
    public String generateSQL(PluginViewParam param) {
        List<PluginViewField> xAxis = param.getFieldsByType("xAxis");
        List<PluginViewField> yAxis = param.getFieldsByType("yAxis");
        /*if (yAxis == null) {
            yAxis = new ArrayList<>();
        }
        List<PluginViewField> yAxisExt = param.getFieldsByType("yAxisExt");
        if (CollectionUtils.isNotEmpty(yAxisExt)) {
            yAxis.addAll(yAxisExt);
        }*/
        if (CollectionUtils.isEmpty(xAxis) || CollectionUtils.isEmpty(yAxis)) {
            return null;
        }
        String sql = new ChartMixViewStatHandler().build(param, this);
        return sql;

    }

    @Override
    public List<String[]> yoy(PluginViewParam pluginViewParam, List<String[]> data, Datasource ds) {
        List<PluginViewField> xAxis = new ArrayList<>();
        List<PluginViewField> yAxis = new ArrayList<>();


        pluginViewParam.getPluginViewFields().forEach(pluginViewField -> {
            if (StringUtils.equals(pluginViewField.getTypeField(), "xAxis")) {
                xAxis.add(pluginViewField);
            }
            if (StringUtils.equals(pluginViewField.getTypeField(), "yAxis")) {
                yAxis.add(pluginViewField);
            }
        });

        // 同比/环比计算
        // 调整data数据
        for (int i = 0; i < yAxis.size(); i++) {
            PluginViewField chartViewFieldDTO = yAxis.get(i);
            ChartFieldCompareDTO compareCalc = chartViewFieldDTO.getCompareCalc();
            if (ObjectUtils.isEmpty(compareCalc)) {
                continue;
            }
            if (StringUtils.isNotEmpty(compareCalc.getType())
                    && !StringUtils.equalsIgnoreCase(compareCalc.getType(), "none")) {
                String compareFieldId = compareCalc.getField();// 选中字段
                // 计算指标对应的下标
                int dataIndex = 0;// 数据字段下标

                dataIndex = xAxis.size() + i;

                if (Arrays.asList(ChartConstants.M_Y).contains(compareCalc.getType())) {
                    String resultData = compareCalc.getResultData();// 数据设置
                    // 获取选中字段以及下标
                    List<PluginViewField> checkedField = new ArrayList<>(xAxis);

                    int timeIndex = 0;// 时间字段下标
                    PluginViewField timeField = null;
                    for (int j = 0; j < checkedField.size(); j++) {
                        if (StringUtils.equalsIgnoreCase(checkedField.get(j).getId(), compareFieldId)) {
                            timeIndex = j;
                            timeField = checkedField.get(j);
                        }
                    }
                    // 无选中字段，或者选中字段已经不在维度list中，或者选中字段日期格式不符合对比类型的，直接将对应数据置为null
                    if (ObjectUtils.isEmpty(timeField) || !checkCalcType(timeField.getDateStyle(), compareCalc.getType())) {
                        // set null
                        for (String[] item : data) {
                            item[dataIndex] = null;
                        }
                    } else {
                        // 计算 同比/环比
                        // 1，处理当期数据；2，根据type计算上一期数据；3，根据resultData计算结果
                        Map<String, String> currentMap = new LinkedHashMap<>();
                        for (String[] item : data) {
                            String[] dimension = Arrays.copyOfRange(item, 0, checkedField.size());
                            currentMap.put(StringUtils.join(dimension, "-"), item[dataIndex]);
                        }

                        for (int index = 0; index < data.size(); index++) {
                            String[] item = data.get(index);
                            String cTime = item[timeIndex];
                            String cValue = item[dataIndex];

                            // 获取计算后的时间，并且与所有维度拼接
                            String lastTime = calcLastTime(cTime, compareCalc.getType(), timeField.getDateStyle(), timeField.getDatePattern(), ds);
                            String[] dimension = Arrays.copyOfRange(item, 0, checkedField.size());
                            dimension[timeIndex] = lastTime;

                            String lastValue = currentMap.get(StringUtils.join(dimension, "-"));
                            if (StringUtils.isEmpty(cValue) || StringUtils.isEmpty(lastValue)) {
                                item[dataIndex] = null;
                            } else {
                                if (StringUtils.equalsIgnoreCase(resultData, "sub")) {
                                    item[dataIndex] = new BigDecimal(cValue).subtract(new BigDecimal(lastValue)).toString();
                                } else if (StringUtils.equalsIgnoreCase(resultData, "percent")) {
                                    if (new BigDecimal(lastValue).compareTo(BigDecimal.ZERO) == 0) {
                                        item[dataIndex] = null;
                                    } else {
                                        item[dataIndex] = new BigDecimal(cValue)
                                                .divide(new BigDecimal(lastValue), 8, RoundingMode.HALF_UP)
                                                .subtract(new BigDecimal(1))
                                                .setScale(8, RoundingMode.HALF_UP)
                                                .toString();
                                    }
                                }
                            }
                        }
                    }
                } else if (StringUtils.equalsIgnoreCase(compareCalc.getType(), "percent")) {
                    // 求和
                    BigDecimal sum = new BigDecimal(0);
                    for (int index = 0; index < data.size(); index++) {
                        String[] item = data.get(index);
                        String cValue = item[dataIndex];
                        if (StringUtils.isEmpty(cValue)) {
                            continue;
                        }
                        sum = sum.add(new BigDecimal(cValue));
                    }
                    // 计算占比
                    for (int index = 0; index < data.size(); index++) {
                        String[] item = data.get(index);
                        String cValue = item[dataIndex];
                        if (StringUtils.isEmpty(cValue)) {
                            continue;
                        }
                        if (sum.equals(new BigDecimal(0))) {
                            continue;
                        }
                        item[dataIndex] = new BigDecimal(cValue)
                                .divide(sum, 8, RoundingMode.HALF_UP)
                                .toString();
                    }
                }
            }
        }

        // 同比/环比计算 over
        return data;
    }

    @Override
    public Map<String, Object> formatResult(PluginViewParam pluginViewParam, List<String[]> data, Boolean isDrill) {
        List<PluginViewField> xAxis = new ArrayList<>();
        List<PluginViewField> yAxis = new ArrayList<>();


        pluginViewParam.getPluginViewFields().forEach(pluginViewField -> {
            if (StringUtils.equals(pluginViewField.getTypeField(), "xAxis")) {
                xAxis.add(pluginViewField);
            }
            if (StringUtils.equals(pluginViewField.getTypeField(), "yAxis")) {
                yAxis.add(pluginViewField);
            }
        });
        Map<String, Object> map = new HashMap<>();

        List<PluginSeries> series = format(pluginViewParam.getPluginViewLimit().getType(), data, xAxis, yAxis);

        map.put("data", series);


        return map;
    }

    private List<PluginSeries> format(String type, List<String[]> data, List<PluginViewField> xAxis, List<PluginViewField> yAxis) {
        List<PluginSeries> series = new ArrayList<>();
        for (PluginViewField y : yAxis) {
            PluginSeries series1 = new PluginSeries();
            series1.setName(y.getName());
            series1.setType(type);
            series1.setData(new ArrayList<>());
            series.add(series1);
        }
        for (int i1 = 0; i1 < data.size(); i1++) {
            String[] d = data.get(i1);

            for (int i = xAxis.size(); i < xAxis.size() + yAxis.size(); i++) {
                List<PluginChartDimension> dimensionList = new ArrayList<>();
                List<PluginChartQuota> quotaList = new ArrayList<>();
                PluginAxisChartData axisChartDataDTO = new PluginAxisChartData();

                for (int j = 0; j < xAxis.size(); j++) {
                    PluginChartDimension chartDimensionDTO = new PluginChartDimension();
                    chartDimensionDTO.setId(xAxis.get(j).getId());
                    chartDimensionDTO.setValue(d[j]);
                    dimensionList.add(chartDimensionDTO);
                }
                axisChartDataDTO.setDimensionList(dimensionList);

                int j = i - xAxis.size();
                PluginChartQuota chartQuotaDTO = new PluginChartQuota();
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

        }
        return series;
    }

    private boolean checkCalcType(String dateStyle, String calcType) {
        switch (dateStyle) {
            case "y":
                return StringUtils.equalsIgnoreCase(calcType, "year_mom");
            case "y_M":
                return StringUtils.equalsIgnoreCase(calcType, "month_mom")
                        || StringUtils.equalsIgnoreCase(calcType, "year_yoy");
            case "y_M_d":
                return StringUtils.equalsIgnoreCase(calcType, "day_mom")
                        || StringUtils.equalsIgnoreCase(calcType, "month_yoy")
                        || StringUtils.equalsIgnoreCase(calcType, "year_yoy");
            case "y_W":
                return StringUtils.equalsIgnoreCase(calcType, "week_mom")
                        || StringUtils.equalsIgnoreCase(calcType, "year_yoy");
        }
        return false;
    }

    private String calcLastTime(String cTime, String type, String dateStyle, String datePattern, Datasource ds) {
        try {
            String lastTime = null;
            Calendar calendar = Calendar.getInstance();
            if (StringUtils.equalsIgnoreCase(type, ChartConstants.YEAR_MOM)) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy");
                Date date = simpleDateFormat.parse(cTime);
                calendar.setTime(date);
                calendar.add(Calendar.YEAR, -1);
                lastTime = simpleDateFormat.format(calendar.getTime());
            } else if (StringUtils.equalsIgnoreCase(type, ChartConstants.MONTH_MOM)) {
                SimpleDateFormat simpleDateFormat = null;
                if (StringUtils.equalsIgnoreCase(datePattern, "date_split")) {
                    simpleDateFormat = new SimpleDateFormat("yyyy/MM");
                } else {
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM");
                }
                Date date = simpleDateFormat.parse(cTime);
                calendar.setTime(date);
                calendar.add(Calendar.MONTH, -1);
                lastTime = simpleDateFormat.format(calendar.getTime());
            } else if (StringUtils.equalsIgnoreCase(type, ChartConstants.YEAR_YOY)) {
                SimpleDateFormat simpleDateFormat = null;
                if (StringUtils.equalsIgnoreCase(dateStyle, "y_M")) {
                    if (StringUtils.equalsIgnoreCase(datePattern, "date_split")) {
                        simpleDateFormat = new SimpleDateFormat("yyyy/MM");
                    } else {
                        simpleDateFormat = new SimpleDateFormat("yyyy-MM");
                    }
                } else if (StringUtils.equalsIgnoreCase(dateStyle, "y_M_d")) {
                    if (StringUtils.equalsIgnoreCase(datePattern, "date_split")) {
                        simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    } else {
                        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    }
                } else if (StringUtils.equalsIgnoreCase(dateStyle, "y_W")) {
                    if (StringUtils.equalsIgnoreCase(datePattern, "date_split")) {
                        simpleDateFormat = new SimpleDateFormat("yyyy/'W'w");
                    } else {
                        simpleDateFormat = new SimpleDateFormat("yyyy-'W'w");
                    }
                }
                Date date = simpleDateFormat.parse(cTime);
                calendar.setTime(date);
                calendar.add(Calendar.YEAR, -1);
                lastTime = simpleDateFormat.format(calendar.getTime());
            } else if (StringUtils.equalsIgnoreCase(type, ChartConstants.DAY_MOM)) {
                SimpleDateFormat simpleDateFormat = null;
                if (StringUtils.equalsIgnoreCase(datePattern, "date_split")) {
                    simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                } else {
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                }
                Date date = simpleDateFormat.parse(cTime);
                calendar.setTime(date);
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                lastTime = simpleDateFormat.format(calendar.getTime());
            } else if (StringUtils.equalsIgnoreCase(type, ChartConstants.MONTH_YOY)) {
                SimpleDateFormat simpleDateFormat = null;
                if (StringUtils.equalsIgnoreCase(dateStyle, "y_M")) {
                    if (StringUtils.equalsIgnoreCase(datePattern, "date_split")) {
                        simpleDateFormat = new SimpleDateFormat("yyyy/MM");
                    } else {
                        simpleDateFormat = new SimpleDateFormat("yyyy-MM");
                    }
                } else if (StringUtils.equalsIgnoreCase(dateStyle, "y_M_d")) {
                    if (StringUtils.equalsIgnoreCase(datePattern, "date_split")) {
                        simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    } else {
                        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    }
                }
                Date date = simpleDateFormat.parse(cTime);
                calendar.setTime(date);
                calendar.add(Calendar.MONTH, -1);
                lastTime = simpleDateFormat.format(calendar.getTime());
            } else if (StringUtils.equalsIgnoreCase(type, ChartConstants.WEEK_MOM)) {
                SimpleDateFormat simpleDateFormat = null;
                if (StringUtils.equalsIgnoreCase(datePattern, "date_split")) {
                    if (StringUtils.equalsIgnoreCase(ds.getType(), "ck")) {
                        simpleDateFormat = new SimpleDateFormat("yyyy/'W'w");
                    } else {
                        simpleDateFormat = new SimpleDateFormat("yyyy/'W'ww");
                    }
                } else {
                    if (StringUtils.equalsIgnoreCase(ds.getType(), "ck")) {
                        simpleDateFormat = new SimpleDateFormat("yyyy-'W'w");
                    } else {
                        simpleDateFormat = new SimpleDateFormat("yyyy-'W'ww");
                    }
                }
                Date date = simpleDateFormat.parse(cTime);
                calendar.setTime(date);
                calendar.add(Calendar.DAY_OF_YEAR, 6);// 加6天用一周最后一天计算周，可避免跨年的问题
                calendar.add(Calendar.WEEK_OF_YEAR, -1);
                lastTime = simpleDateFormat.format(calendar.getTime());
            }
            return lastTime;
        } catch (Exception e) {
            return cTime;
        }
    }

}

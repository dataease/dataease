package io.dataease.chart.charts.impl;

import io.dataease.chart.charts.ChartHandlerManager;
import io.dataease.chart.constant.ChartConstants;
import io.dataease.chart.manage.ChartDataManage;
import io.dataease.chart.manage.ChartViewManege;
import io.dataease.chart.utils.ChartDataBuild;
import io.dataease.dataset.manage.DatasetTableFieldManage;
import io.dataease.engine.constant.SQLConstants;
import io.dataease.engine.sql.SQLProvider;
import io.dataease.engine.trans.Dimension2SQLObj;
import io.dataease.engine.trans.Quota2SQLObj;
import io.dataease.engine.utils.Utils;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.view.dto.*;
import io.dataease.extensions.view.plugin.AbstractChartPlugin;
import io.dataease.extensions.view.util.ChartDataUtil;
import io.dataease.extensions.view.util.FieldUtil;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DefaultChartHandler extends AbstractChartPlugin {
    public static Logger logger = LoggerFactory.getLogger(ChartDataManage.class);
    @Resource
    protected ChartHandlerManager chartHandlerManager;
    @Resource
    protected DatasetTableFieldManage datasetTableFieldManage;
    @Resource
    protected ChartViewManege chartViewManege;
    @Getter
    private String render = "antv";
    @Getter
    private String type = "*";
    @Autowired(required = false)
    public PluginManageApi pluginManage;

    @PostConstruct
    public void init() {
        chartHandlerManager.registerChartHandler(this.getRender(), this.getType(), this);
    }

    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var axisMap = new HashMap<ChartAxis, List<ChartViewFieldDTO>>();
        var context = new HashMap<String, Object>();
        var result = new AxisFormatResult(axisMap, context);
        axisMap.put(ChartAxis.xAxis, new ArrayList<>(view.getXAxis()));
        axisMap.put(ChartAxis.yAxis, new ArrayList<>(view.getYAxis()));
        axisMap.put(ChartAxis.drill, new ArrayList<>(view.getDrillFields()));
        return result;
    }

    @Override
    public <T extends CustomFilterResult> T customFilter(ChartViewDTO view, List<ChartExtFilterDTO> filterList, AxisFormatResult formatResult) {
        var desensitizationList = (Map<String, ColumnPermissionItem>) formatResult.getContext().get("desensitizationList");
        if (MapUtils.isNotEmpty(desensitizationList)) {
            formatResult.getAxisMap().forEach((axis, fields) -> {
                fields.removeIf(f -> desensitizationList.containsKey(f.getDataeaseName()));
            });
        }
        return (T) new CustomFilterResult(filterList, formatResult.getContext());
    }

    public Map<String, Object> buildResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, List<String[]> data) {
        boolean isDrill = filterResult
                .getFilterList()
                .stream()
                .anyMatch(ele -> ele.getFilterType() == 1);
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        Map<String, Object> result = ChartDataBuild.transChartData(xAxis, yAxis, view, data, isDrill);
        return result;
    }

    @Override
    public <T extends ChartCalcDataResult> T calcChartResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, Map<String, Object> sqlMap, SQLMeta sqlMeta, Provider provider) {
        var dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        List<String> dsList = new ArrayList<>();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
            dsList.add(next.getValue().getType());
        }
        boolean needOrder = Utils.isNeedOrder(dsList);
        boolean crossDs = Utils.isCrossDs(dsMap);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDsList(dsMap);
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        var allFields = (List<ChartViewFieldDTO>) filterResult.getContext().get("allFields");
        Dimension2SQLObj.dimension2sqlObj(sqlMeta, xAxis, FieldUtil.transFields(allFields), crossDs, dsMap, Utils.getParams(FieldUtil.transFields(allFields)), view.getCalParams(), pluginManage);
        Quota2SQLObj.quota2sqlObj(sqlMeta, yAxis, FieldUtil.transFields(allFields), crossDs, dsMap, Utils.getParams(FieldUtil.transFields(allFields)), view.getCalParams(), pluginManage);
        String querySql = SQLProvider.createQuerySQL(sqlMeta, true, needOrder, view);
        querySql = provider.rebuildSQL(querySql, sqlMeta, crossDs, dsMap);
        datasourceRequest.setQuery(querySql);
        logger.debug("calcite chart sql: " + querySql);
        List<String[]> data = (List<String[]>) provider.fetchResultField(datasourceRequest).get("data");
        //自定义排序
        data = ChartDataUtil.resultCustomSort(xAxis, data);
        //快速计算
        quickCalc(xAxis, yAxis, data);
        //数据重组逻辑可重载
        var result = this.buildResult(view, formatResult, filterResult, data);
        T calcResult = (T) new ChartCalcDataResult();
        calcResult.setData(result);
        calcResult.setContext(filterResult.getContext());
        calcResult.setQuerySql(querySql);
        calcResult.setOriginData(data);
        return calcResult;
    }

    @Override
    public ChartViewDTO buildChart(ChartViewDTO view, ChartCalcDataResult calcResult, AxisFormatResult formatResult, CustomFilterResult filterResult) {
        var desensitizationList = (Map<String, ColumnPermissionItem>) filterResult.getContext().get("desensitizationList");
        var allFields = (List<ChartViewFieldDTO>) filterResult.getContext().get("allFields");
        var xAxis = formatResult.getAxisMap().get(ChartAxis.xAxis);
        var yAxis = formatResult.getAxisMap().get(ChartAxis.yAxis);
        // 如果是表格导出查询 则在此处直接就可以返回
        var extStack = formatResult.getAxisMap().get(ChartAxis.extStack);
        if (CollectionUtils.isNotEmpty(extStack) && xAxis.size() > extStack.size()) {
            xAxis = xAxis.subList(0, xAxis.size() - extStack.size());
        }
        if (view.getIsExcelExport()) {
            Map<String, Object> sourceInfo = ChartDataBuild.transTableNormal(xAxis, yAxis, view, calcResult.getOriginData(), extStack, desensitizationList);
            sourceInfo.put("sourceData", calcResult.getOriginData());
            view.setData(sourceInfo);
            return view;
        }

        Map<String, Object> mapTableNormal = ChartDataBuild.transTableNormal(xAxis, yAxis, view, calcResult.getOriginData(), extStack, desensitizationList);
        var drillFilters = filterResult.getFilterList().stream().filter(f -> f.getFilterType() == 1).collect(Collectors.toList());
        // 日期下钻替换回去
        drillFilters.forEach(f -> {
            if (CollectionUtils.isNotEmpty(f.getOriginValue())) {
                f.setValue(f.getOriginValue());
            }
        });
        var isDrill = CollectionUtils.isNotEmpty(drillFilters);
        // 构建结果
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.putAll(calcResult.getData());
        dataMap.putAll(mapTableNormal);
        dataMap.put("sourceFields", allFields);
        mergeAssistField(calcResult.getDynamicAssistFields(), calcResult.getAssistData());
        dataMap.put("dynamicAssistLines", calcResult.getDynamicAssistFields());
        view.setData(dataMap);
        view.setSql(Base64.getEncoder().encodeToString(calcResult.getQuerySql().getBytes()));
        view.setDrill(isDrill);
        view.setDrillFilters(drillFilters);
        return view;
    }


    protected void mergeAssistField(List<ChartSeniorAssistDTO> dynamicAssistFields, List<String[]> assistData) {
        if (ObjectUtils.isEmpty(assistData)) {
            return;
        }
        String[] strings = assistData.get(0);
        for (int i = 0; i < dynamicAssistFields.size(); i++) {
            if (i < strings.length) {
                ChartSeniorAssistDTO chartSeniorAssistDTO = dynamicAssistFields.get(i);
                chartSeniorAssistDTO.setValue(strings[i]);
            }
        }
    }

    protected List<ChartSeniorAssistDTO> getDynamicAssistFields(ChartViewDTO view) {
        List<ChartSeniorAssistDTO> list = new ArrayList<>();

        Map<String, Object> senior = view.getSenior();
        if (ObjectUtils.isEmpty(senior)) {
            return list;
        }

        ChartSeniorAssistCfgDTO assistLineCfg = JsonUtil.parseObject((String) JsonUtil.toJSONString(senior.get("assistLineCfg")), ChartSeniorAssistCfgDTO.class);
        if (null == assistLineCfg || !assistLineCfg.isEnable()) {
            return list;
        }
        List<ChartSeniorAssistDTO> assistLines = assistLineCfg.getAssistLine();

        if (ObjectUtils.isEmpty(assistLines)) {
            return list;
        }

        for (ChartSeniorAssistDTO dto : assistLines) {
            if (StringUtils.equalsIgnoreCase(dto.getField(), "0")) {
                continue;
            }
            Long fieldId = dto.getFieldId();
            String summary = dto.getSummary();
            if (ObjectUtils.isEmpty(fieldId) || StringUtils.isEmpty(summary)) {
                continue;
            }

            DatasetTableFieldDTO datasetTableFieldDTO = datasetTableFieldManage.selectById(fieldId);

            if (ObjectUtils.isEmpty(datasetTableFieldDTO)) {
                continue;
            }
            list.add(dto);
        }
        return list;
    }

    protected List<ChartViewFieldDTO> getAssistFields(List<ChartSeniorAssistDTO> list, List<ChartViewFieldDTO> yAxis) {
        List<ChartViewFieldDTO> res = new ArrayList<>();
        for (ChartSeniorAssistDTO dto : list) {
            DatasetTableFieldDTO curField = dto.getCurField();
            ChartViewFieldDTO yField = null;
            String alias = "";
            for (int i = 0; i < yAxis.size(); i++) {
                ChartViewFieldDTO field = yAxis.get(i);
                if (Objects.equals(field.getId(), curField.getId())) {
                    yField = field;
                    alias = String.format(SQLConstants.FIELD_ALIAS_Y_PREFIX, i);
                    break;
                }
            }
            if (ObjectUtils.isEmpty(yField)) {
                continue;
            }

            ChartViewFieldDTO chartViewFieldDTO = new ChartViewFieldDTO();
            BeanUtils.copyBean(chartViewFieldDTO, curField);
            chartViewFieldDTO.setSummary(dto.getSummary());
            chartViewFieldDTO.setOriginName(alias);// yAxis的字段别名，就是查找的字段名
            res.add(chartViewFieldDTO);
        }
        return res;
    }

    protected List<ChartViewFieldDTO> getAssistFields(List<ChartSeniorAssistDTO> list, List<ChartViewFieldDTO> yAxis, List<ChartViewFieldDTO> xAxis) {
        List<ChartViewFieldDTO> res = new ArrayList<>();
        for (ChartSeniorAssistDTO dto : list) {
            DatasetTableFieldDTO curField = dto.getCurField();
            ChartViewFieldDTO field = null;
            String alias = "";
            for (int i = 0; i < yAxis.size(); i++) {
                ChartViewFieldDTO yField = yAxis.get(i);
                if (Objects.equals(yField.getId(), curField.getId())) {
                    field = yField;
                    alias = String.format(SQLConstants.FIELD_ALIAS_Y_PREFIX, i);
                    break;
                }
            }
            if (ObjectUtils.isEmpty(field) && CollectionUtils.isNotEmpty(xAxis)) {
                for (int i = 0; i < xAxis.size(); i++) {
                    ChartViewFieldDTO xField = xAxis.get(i);
                    if (StringUtils.equalsIgnoreCase(String.valueOf(xField.getId()), String.valueOf(curField.getId()))) {
                        field = xField;
                        alias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);
                        break;
                    }
                }
            }
            if (ObjectUtils.isEmpty(field)) {
                continue;
            }

            ChartViewFieldDTO chartViewFieldDTO = new ChartViewFieldDTO();
            BeanUtils.copyBean(chartViewFieldDTO, curField);
            chartViewFieldDTO.setSummary(dto.getSummary());
            chartViewFieldDTO.setOriginName(alias);// yAxis的字段别名，就是查找的字段名
            res.add(chartViewFieldDTO);
        }
        return res;
    }

    public List<ChartSeniorAssistDTO> getDynamicThresholdFields(ChartViewDTO view) {
        List<ChartSeniorAssistDTO> list = new ArrayList<>();
        Map<String, Object> senior = view.getSenior();
        if (ObjectUtils.isEmpty(senior)) {
            return list;
        }
        ChartSeniorThresholdCfgDTO thresholdCfg = JsonUtil.parseObject((String) JsonUtil.toJSONString(senior.get("threshold")), ChartSeniorThresholdCfgDTO.class);

        if (null == thresholdCfg || !thresholdCfg.isEnable()) {
            return list;
        }
        List<TableThresholdDTO> tableThreshold = thresholdCfg.getTableThreshold();

        if (ObjectUtils.isEmpty(tableThreshold)) {
            return list;
        }

        List<ChartSeniorThresholdDTO> conditionsList = tableThreshold.stream()
                .filter(item -> !ObjectUtils.isEmpty(item))
                .map(TableThresholdDTO::getConditions)
                .flatMap(List::stream)
                .filter(condition -> StringUtils.equalsAnyIgnoreCase(condition.getType(), "dynamic"))
                .toList();

        List<ChartSeniorAssistDTO> assistDTOs = conditionsList.stream()
                .flatMap(condition -> getConditionFields(condition).stream())
                .filter(this::solveThresholdCondition)
                .toList();

        list.addAll(assistDTOs);

        return list;
    }

    private boolean solveThresholdCondition(ChartSeniorAssistDTO fieldDTO) {
        Long fieldId = fieldDTO.getFieldId();
        String summary = fieldDTO.getValue();
        if (ObjectUtils.isEmpty(fieldId) || StringUtils.isEmpty(summary)) {
            return false;
        }

        DatasetTableFieldDTO datasetTableFieldDTO = datasetTableFieldManage.selectById(fieldId);
        if (ObjectUtils.isEmpty(datasetTableFieldDTO)) {
            return false;
        }
        ChartViewFieldDTO datasetTableField = new ChartViewFieldDTO();
        BeanUtils.copyBean(datasetTableField, datasetTableFieldDTO);
        fieldDTO.setCurField(datasetTableField);
        fieldDTO.setSummary(summary);
        return true;
    }

    private List<ChartSeniorAssistDTO> getConditionFields(ChartSeniorThresholdDTO condition) {
        List<ChartSeniorAssistDTO> list = new ArrayList<>();
        if ("between".equals(condition.getTerm())) {
            if (!StringUtils.equalsIgnoreCase(condition.getDynamicMaxField().getSummary(), "value")) {
                list.add(of(condition.getDynamicMaxField()));
            }
            if (!StringUtils.equalsIgnoreCase(condition.getDynamicMinField().getSummary(), "value")) {
                list.add(of(condition.getDynamicMinField()));
            }
        } else {
            if (!StringUtils.equalsIgnoreCase(condition.getDynamicField().getSummary(), "value")) {
                list.add(of(condition.getDynamicField()));
            }
        }

        return list;
    }

    private ChartSeniorAssistDTO of(ThresholdDynamicFieldDTO dynamicField) {
        ChartSeniorAssistDTO conditionField = new ChartSeniorAssistDTO();
        conditionField.setFieldId(Long.parseLong(dynamicField.getFieldId()));
        conditionField.setValue(dynamicField.getSummary());
        return conditionField;
    }

    protected String assistSQL(String sql, List<ChartViewFieldDTO> assistFields) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < assistFields.size(); i++) {
            ChartViewFieldDTO dto = assistFields.get(i);
            if (i == (assistFields.size() - 1)) {
                stringBuilder.append(dto.getSummary() + "(" + dto.getOriginName() + ")");
            } else {
                stringBuilder.append(dto.getSummary() + "(" + dto.getOriginName() + "),");
            }
        }
        return "SELECT " + stringBuilder + " FROM (" + sql + ") tmp";
    }

    protected void quickCalc(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<String[]> data) {
        for (int i = 0; i < yAxis.size(); i++) {
            ChartViewFieldDTO chartViewFieldDTO = yAxis.get(i);
            ChartFieldCompareDTO compareCalc = chartViewFieldDTO.getCompareCalc();
            if (ObjectUtils.isEmpty(compareCalc)) {
                continue;
            }
            if (StringUtils.isNotEmpty(compareCalc.getType())
                    && !StringUtils.equalsIgnoreCase(compareCalc.getType(), "none")) {
                Long compareFieldId = compareCalc.getField();// 选中字段
                // 数据字段下标
                int dataIndex = xAxis.size() + i;
                if (Arrays.asList(ChartConstants.M_Y).contains(compareCalc.getType())) {
                    String resultData = compareCalc.getResultData();// 数据设置
                    // 获取选中字段以及下标
                    List<ChartViewFieldDTO> checkedField = new ArrayList<>(xAxis);
                    int timeIndex = 0;// 时间字段下标
                    ChartViewFieldDTO timeField = null;
                    for (int j = 0; j < checkedField.size(); j++) {
                        if (Objects.equals(checkedField.get(j).getId(), compareFieldId)) {
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
                            String lastTime = calcLastTime(cTime, compareCalc.getType(), timeField.getDateStyle(), timeField.getDatePattern());
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
                                                .divide(new BigDecimal(lastValue).abs(), 8, RoundingMode.HALF_UP)
                                                .subtract(new BigDecimal(1))
                                                .setScale(8, RoundingMode.HALF_UP)
                                                .toString();
                                    }
                                } else if (StringUtils.equalsIgnoreCase(resultData, "pre")) {
                                    item[dataIndex] = new BigDecimal(lastValue).toString();
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
                        item[dataIndex] = new BigDecimal(cValue)
                                .divide(sum, 8, RoundingMode.HALF_UP)
                                .toString();
                    }
                }
            }
        }
    }

    private String calcLastTime(String cTime, String type, String dateStyle, String datePattern) {
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
            }
            return lastTime;
        } catch (Exception e) {
            return cTime;
        }
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
        }
        return false;
    }

    protected boolean checkYoyFilter(List<ChartExtFilterDTO> filter, List<ChartViewFieldDTO> yoyAxis) {
        boolean flag = false;
        for (ChartExtFilterDTO filterDTO : filter) {
            for (ChartViewFieldDTO chartViewFieldDTO : yoyAxis) {
                ChartFieldCompareDTO compareCalc = chartViewFieldDTO.getCompareCalc();
                if (ObjectUtils.isEmpty(compareCalc)) {
                    continue;
                }
                if (StringUtils.isNotEmpty(compareCalc.getType())
                        && !StringUtils.equalsIgnoreCase(compareCalc.getType(), "none")) {
                    if (Arrays.asList(ChartConstants.M_Y).contains(compareCalc.getType())) {
                        if (StringUtils.equalsIgnoreCase(compareCalc.getField() + "", filterDTO.getFieldId())
                                && (filterDTO.getFilterType() == 0 || filterDTO.getFilterType() == 2)) {
                            // -1 year
                            try {
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(new Date(Long.parseLong(filterDTO.getValue().getFirst())));
                                calendar.add(Calendar.YEAR, -1);
                                filterDTO.getValue().set(0, String.valueOf(calendar.getTime().getTime()));
                                flag = true;
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            }
        }
        return flag;
    }

    ;

    protected void groupStackDrill(List<ChartViewFieldDTO> xAxis,
                                   List<ChartExtFilterDTO> filterList,
                                   List<ChartViewFieldDTO> fieldsToFilter,
                                   List<ChartViewFieldDTO> drillFields,
                                   List<ChartDrillRequest> drillRequestList) {
        var fields = xAxis.stream().map(ChartViewFieldDTO::getId).collect(Collectors.toSet());
        ChartDrillRequest head = drillRequestList.getFirst();
        Map<Long, String> dimValMap = new HashMap<>();
        head.getDimensionList().forEach(item -> dimValMap.put(item.getId(), item.getValue()));
        Map<Long, ChartViewFieldDTO> fieldMap = xAxis.stream().collect(Collectors.toMap(ChartViewFieldDTO::getId, o -> o, ((p, n) -> p)));
        for (int i = 0; i < drillRequestList.size(); i++) {
            ChartDrillRequest request = drillRequestList.get(i);
            ChartViewFieldDTO chartViewFieldDTO = drillFields.get(i);
            for (ChartDimensionDTO requestDimension : request.getDimensionList()) {
                // 将钻取值作为条件传递，将所有钻取字段作为xAxis并加上下一个钻取字段
                if (Objects.equals(requestDimension.getId(), chartViewFieldDTO.getId())) {
                    fieldsToFilter.add(chartViewFieldDTO);
                    dimValMap.put(requestDimension.getId(), requestDimension.getValue());
                    if (!fields.contains(requestDimension.getId())) {
                        fieldMap.put(chartViewFieldDTO.getId(), chartViewFieldDTO);
                        chartViewFieldDTO.setSource(FieldSource.DRILL);
                        xAxis.add(chartViewFieldDTO);
                        fields.add(requestDimension.getId());
                    }
                    if (i == drillRequestList.size() - 1) {
                        ChartViewFieldDTO nextDrillField = drillFields.get(i + 1);
                        if (!fields.contains(nextDrillField.getId())) {
                            // get drill list first element's sort,then assign to nextDrillField
                            nextDrillField.setSort(getDrillSort(xAxis, drillFields.get(0)));
                            nextDrillField.setSource(FieldSource.DRILL);
                            xAxis.add(nextDrillField);
                            fields.add(nextDrillField.getId());
                        }
                    }
                }
            }
        }
        for (int i = 0; i < fieldsToFilter.size(); i++) {
            ChartViewFieldDTO tmpField = fieldsToFilter.get(i);
            ChartExtFilterDTO tmpFilter = new ChartExtFilterDTO();
            DatasetTableFieldDTO datasetTableField = datasetTableFieldManage.selectById(tmpField.getId());
            tmpFilter.setDatasetTableField(datasetTableField);
            tmpFilter.setDateStyle(fieldMap.get(tmpField.getId()).getDateStyle());
            tmpFilter.setDatePattern(fieldMap.get(tmpField.getId()).getDatePattern());
            tmpFilter.setFieldId(String.valueOf(tmpField.getId()));
            tmpFilter.setFilterType(1);
            if (datasetTableField.getDeType() == 1) {
                tmpFilter.setOriginValue(Collections.singletonList(dimValMap.get(tmpField.getId())));
                tmpFilter.setOperator("between");
                // 把value类似过滤组件处理，获得start time和end time
                Map<String, Long> stringLongMap = Utils.parseDateTimeValue(dimValMap.get(tmpField.getId()));
                tmpFilter.setValue(Arrays.asList(String.valueOf(stringLongMap.get("startTime")), String.valueOf(stringLongMap.get("endTime"))));
            } else {
                tmpFilter.setOperator("in");
                tmpFilter.setValue(Collections.singletonList(dimValMap.get(tmpField.getId())));
            }
            filterList.add(tmpFilter);
        }
    }

    private String getDrillSort(List<ChartViewFieldDTO> xAxis, ChartViewFieldDTO field) {
        String res = "";
        for (ChartViewFieldDTO f : xAxis) {
            if (Objects.equals(f.getId(), field.getId())) {
                if (StringUtils.equalsIgnoreCase(f.getSort(), "asc") || StringUtils.equalsIgnoreCase(f.getSort(), "desc")) {
                    res = f.getSort();
                    break;
                }
            }
        }
        return res;
    }
}

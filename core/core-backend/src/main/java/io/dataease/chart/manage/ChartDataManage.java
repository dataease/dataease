package io.dataease.chart.manage;

import io.dataease.api.chart.dto.*;
import io.dataease.api.chart.request.ChartDrillRequest;
import io.dataease.api.chart.request.ChartExtRequest;
import io.dataease.api.dataset.dto.SqlVariableDetails;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.union.model.SQLMeta;
import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.api.permissions.dataset.dto.DataSetRowPermissionsTreeDTO;
import io.dataease.chart.constant.ChartConstants;
import io.dataease.chart.utils.ChartDataBuild;
import io.dataease.constant.AuthEnum;
import io.dataease.dataset.dto.DatasourceSchemaDTO;
import io.dataease.dataset.manage.DatasetGroupManage;
import io.dataease.dataset.manage.DatasetSQLManage;
import io.dataease.dataset.manage.DatasetTableFieldManage;
import io.dataease.dataset.manage.PermissionManage;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import io.dataease.engine.constant.SQLConstants;
import io.dataease.engine.sql.SQLProvider;
import io.dataease.engine.trans.*;
import io.dataease.engine.utils.SQLUtils;
import io.dataease.engine.utils.Utils;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.result.ResultCode;
import io.dataease.system.manage.CorePermissionManage;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author Junjun
 */
@Component
public class ChartDataManage {
    @Resource
    private DatasetTableFieldManage datasetTableFieldManage;
    @Resource
    private DatasetGroupManage datasetGroupManage;
    @Resource
    private DatasetSQLManage datasetSQLManage;
    @Resource
    private CalciteProvider calciteProvider;
    @Resource
    private ChartViewManege chartViewManege;
    @Resource
    private PermissionManage permissionManage;

    @Resource
    private CorePermissionManage corePermissionManage;

    public static final String START_END_SEPARATOR = "_START_END_SPLIT";

    private static Logger logger = LoggerFactory.getLogger(ChartDataManage.class);

    public ChartViewDTO calcData(ChartViewDTO view) throws Exception {
        ChartExtRequest chartExtRequest = view.getChartExtRequest();
        if (chartExtRequest == null) {
            chartExtRequest = new ChartExtRequest();
        }

        ChartViewDTO chartViewDTO = new ChartViewDTO();
        if (ObjectUtils.isEmpty(view)) {
            DEException.throwException(ResultCode.DATA_IS_WRONG.code(), Translator.get("i18n_chart_delete"));
        }

        //如果是excel导出 则最多导出20万条； 如果是从仪表板获取视图数据，则仪表板的查询模式，查询结果的数量，覆盖视图对应的属性
        if (view.getIsExcelExport()) {
            view.setResultMode(ChartConstants.VIEW_RESULT_MODE.CUSTOM);
            view.setResultCount(200000);
        } else if (ChartConstants.VIEW_RESULT_MODE.CUSTOM.equals(chartExtRequest.getResultMode())) {
            view.setResultMode(chartExtRequest.getResultMode());
            view.setResultCount(chartExtRequest.getResultCount());
        }

        List<ChartViewFieldDTO> viewFields = new ArrayList<>(view.getViewFields());
        final Map<String, List<ChartViewFieldDTO>> extFieldsMap = new LinkedHashMap<>();
        if (ObjectUtils.isNotEmpty(viewFields)) {
            viewFields.forEach(field -> {
                String busiType = field.getBusiType();
                List<ChartViewFieldDTO> list = extFieldsMap.containsKey(busiType) ? extFieldsMap.get(busiType) : new ArrayList<>();
                list.add(field);
                extFieldsMap.put(field.getBusiType(), list);
            });
        }

        // get all fields
        List<ChartViewFieldDTO> allFields = getAllChartFields(view);

        List<ChartViewFieldDTO> xAxisBase = new ArrayList<>(view.getXAxis());
        List<ChartViewFieldDTO> xAxis = new ArrayList<>(view.getXAxis());
        List<ChartViewFieldDTO> xAxisExt = new ArrayList<>(view.getXAxisExt());
        if (StringUtils.equalsIgnoreCase(view.getType(), "table-pivot")
                || StringUtils.containsIgnoreCase(view.getType(), "group")
                || ("antv".equalsIgnoreCase(view.getRender()) && "line".equalsIgnoreCase(view.getType()))
                || StringUtils.equalsIgnoreCase(view.getType(), "flow-map")) {
            xAxis.addAll(xAxisExt);
        }
        List<ChartViewFieldDTO> yAxis = new ArrayList<>(view.getYAxis());
        if (StringUtils.equalsIgnoreCase(view.getType(), "chart-mix")) {
            List<ChartViewFieldDTO> yAxisExt = new ArrayList<>(view.getYAxisExt());
            yAxis.addAll(yAxisExt);
        }
        if (StringUtils.equalsIgnoreCase(view.getRender(), "antv") && StringUtils.equalsAnyIgnoreCase(view.getType(), "gauge", "liquid")) {
            List<ChartViewFieldDTO> sizeField = getSizeField(view);
            yAxis.addAll(sizeField);
        }
        List<ChartViewFieldDTO> extStack = new ArrayList<>(view.getExtStack());
        List<ChartViewFieldDTO> extBubble = new ArrayList<>(view.getExtBubble());
        if (ObjectUtils.isNotEmpty(view.getExtLabel()) && enableExtData(view.getType())) {
            List<ChartViewFieldDTO> extLabel = new ArrayList<>(view.getExtLabel());
            yAxis.addAll(extLabel);
        }
        if (ObjectUtils.isNotEmpty(view.getExtTooltip()) && enableExtData(view.getType())) {
            List<ChartViewFieldDTO> extTooltip = new ArrayList<>(view.getExtTooltip());
            yAxis.addAll(extTooltip);
        }
        List<ChartFieldCustomFilterDTO> fieldCustomFilter = new ArrayList<>(view.getCustomFilter());
        List<ChartViewFieldDTO> drill = new ArrayList<>(view.getDrillFields());

        DatasetGroupInfoDTO table = datasetGroupManage.get(view.getTableId(), null);
        if (table == null) {
            DEException.throwException(ResultCode.DATA_IS_WRONG.code(), Translator.get("i18n_no_ds"));
        }
        // check permission
        BusiPerCheckDTO dto = new BusiPerCheckDTO();
        dto.setId(table.getId());
        dto.setAuthEnum(AuthEnum.READ);
        boolean checked = corePermissionManage.checkAuth(dto);
        if (!checked) {
            DEException.throwException(Translator.get("i18n_no_datasource_permission"));
        }

        // column permission
        Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
        List<DatasetTableFieldDTO> columnPermissionFields = permissionManage.filterColumnPermissions(transFields(allFields), desensitizationList, table.getId(), chartExtRequest.getUser());
        //将没有权限的列删掉
        List<String> dataeaseNames = columnPermissionFields.stream().map(DatasetTableFieldDTO::getDataeaseName).collect(Collectors.toList());
        dataeaseNames.add("*");
        fieldCustomFilter = fieldCustomFilter.stream().filter(item -> !desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
        extStack = extStack.stream().filter(item -> !desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
        extBubble = extBubble.stream().filter(item -> !desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
        drill = drill.stream().filter(item -> !desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
        // row permission
        List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = permissionManage.getRowPermissionsTree(table.getId(), chartExtRequest.getUser());

        for (ChartFieldCustomFilterDTO ele : fieldCustomFilter) {
            ele.setField(datasetTableFieldManage.selectById(ele.getId()));
        }

        if (ObjectUtils.isEmpty(xAxis) && ObjectUtils.isEmpty(yAxis)) {
            return emptyChartViewDTO(view);
        }

        // 直连明细表分页
        Map<String, Object> mapAttr = view.getCustomAttr();
        Map<String, Object> mapSize = (Map<String, Object>) mapAttr.get("basicStyle");
        if (StringUtils.equalsIgnoreCase(view.getType(), "table-info") && table.getMode() == 0) {
            if (StringUtils.equalsIgnoreCase((String) mapSize.get("tablePageMode"), "page") && !chartExtRequest.getExcelExportFlag()) {
                if (chartExtRequest.getGoPage() == null) {
                    chartExtRequest.setGoPage(1L);
                }
                if (chartExtRequest.getPageSize() == null) {
                    int pageSize = (int) mapSize.get("tablePageSize");
                    if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom")) {
                        chartExtRequest.setPageSize(Math.min(pageSize, view.getResultCount().longValue()));
                    } else {
                        chartExtRequest.setPageSize((long) pageSize);
                    }
                }
            } else {
                if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom")) {
                    chartExtRequest.setGoPage(1L);
                    chartExtRequest.setPageSize(view.getResultCount().longValue());
                } else if (!chartExtRequest.getExcelExportFlag()) {
                    chartExtRequest.setGoPage(null);
                    chartExtRequest.setPageSize(null);
                }
            }
        } else {
            chartExtRequest.setGoPage(null);
            chartExtRequest.setPageSize(null);
        }

        switch (view.getType()) {
            case "label":
                xAxis = xAxis.stream().filter(item -> !desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                xAxisBase = xAxisBase.stream().filter(item -> !desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                yAxis = new ArrayList<>();
                if (ObjectUtils.isEmpty(xAxis)) {
                    return emptyChartViewDTO(view);
                }
                break;
            case "indicator":
            case "gauge":
            case "liquid":
                xAxis = new ArrayList<>();
                yAxis = yAxis.stream().filter(item -> !desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                if (ObjectUtils.isEmpty(yAxis)) {
                    return emptyChartViewDTO(view);
                }
                break;
            case "table-info":
                yAxis = new ArrayList<>();
                xAxis = xAxis.stream().filter(item -> dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                xAxisBase = xAxisBase.stream().filter(item -> dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                if (ObjectUtils.isEmpty(xAxis)) {
                    return emptyChartViewDTO(view);
                }
                break;
            case "table-normal":
                xAxis = xAxis.stream().filter(item -> dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                xAxisBase = xAxisBase.stream().filter(item -> dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                yAxis = yAxis.stream().filter(item -> dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                break;
            default:
                xAxis = xAxis.stream().filter(item -> !desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                xAxisBase = xAxisBase.stream().filter(item -> !desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                yAxis = yAxis.stream().filter(item -> !desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
        }

        // 过滤来自仪表板的条件
        List<ChartExtFilterDTO> extFilterList = new ArrayList<>();
        //组件过滤条件
        if (ObjectUtils.isNotEmpty(chartExtRequest.getFilter())) {
            for (ChartExtFilterDTO request : chartExtRequest.getFilter()) {
                // 解析多个fieldId,fieldId是一个逗号分隔的字符串
                String fieldId = request.getFieldId();
                if (request.getIsTree() == null) {
                    request.setIsTree(false);
                }

                boolean hasParameters = false;
                List<SqlVariableDetails> sqlVariables = datasetGroupManage.getSqlParams(Arrays.asList(view.getTableId()));
                if (CollectionUtils.isNotEmpty(sqlVariables)) {
                    for (SqlVariableDetails parameter : Optional.ofNullable(request.getParameters()).orElse(new ArrayList<>())) {
                        String parameterId = StringUtils.endsWith(parameter.getId(), START_END_SEPARATOR) ? parameter.getId().split(START_END_SEPARATOR)[0] : parameter.getId();
                        if (sqlVariables.stream().map(SqlVariableDetails::getId).collect(Collectors.toList()).contains(parameterId)) {
                            hasParameters = true;
                        }
                    }
                }

                if (hasParameters) {
                    continue;
                }

                if (StringUtils.isNotEmpty(fieldId)) {
                    List<Long> fieldIds = Arrays.stream(fieldId.split(",")).map(Long::valueOf).collect(Collectors.toList());

                    if (request.getIsTree()) {
                        ChartExtFilterDTO filterRequest = new ChartExtFilterDTO();
                        BeanUtils.copyBean(filterRequest, request);
                        filterRequest.setDatasetTableFieldList(new ArrayList<>());
                        for (Long fId : fieldIds) {
                            DatasetTableFieldDTO datasetTableField = datasetTableFieldManage.selectById(fId);
                            if (datasetTableField == null) {
                                continue;
                            }
                            if (Objects.equals(datasetTableField.getDatasetTableId(), view.getTableId())) {
                                if (ObjectUtils.isNotEmpty(filterRequest.getViewIds())) {
                                    if (filterRequest.getViewIds().contains(view.getId())) {
                                        filterRequest.getDatasetTableFieldList().add(datasetTableField);
                                    }
                                } else {
                                    filterRequest.getDatasetTableFieldList().add(datasetTableField);
                                }
                            }
                        }
                        if (ObjectUtils.isNotEmpty(filterRequest.getDatasetTableFieldList())) {
                            extFilterList.add(filterRequest);
                        }
                    } else {
                        for (Long fId : fieldIds) {
                            ChartExtFilterDTO filterRequest = new ChartExtFilterDTO();
                            BeanUtils.copyBean(filterRequest, request);
                            filterRequest.setFieldId(fId + "");

                            DatasetTableFieldDTO datasetTableField = datasetTableFieldManage.selectById(fId);
                            if (datasetTableField == null) {
                                continue;
                            }
                            filterRequest.setDatasetTableField(datasetTableField);
                            if (Objects.equals(datasetTableField.getDatasetGroupId(), view.getTableId())) {
                                if (ObjectUtils.isNotEmpty(filterRequest.getViewIds())) {
                                    if (filterRequest.getViewIds().contains(view.getId())) {
                                        extFilterList.add(filterRequest);
                                    }
                                } else {
                                    extFilterList.add(filterRequest);
                                }
                            }
                        }
                    }
                }
            }
        }

        List<ChartExtFilterDTO> filters = new ArrayList<>();
        // 联动条件
        if (ObjectUtils.isNotEmpty(chartExtRequest.getLinkageFilters())) {
            filters.addAll(chartExtRequest.getLinkageFilters());
        }

        // 外部参数条件
        if (ObjectUtils.isNotEmpty(chartExtRequest.getOuterParamsFilters())) {
            filters.addAll(chartExtRequest.getOuterParamsFilters());
        }

        //联动过滤条件和外部参数过滤条件全部加上
        if (ObjectUtils.isNotEmpty(filters)) {
            for (ChartExtFilterDTO request : filters) {
                DatasetTableFieldDTO datasetTableField = datasetTableFieldManage.selectById(Long.valueOf(request.getFieldId()));
                request.setDatasetTableField(datasetTableField);
                // 相同数据集
                if (Objects.equals(datasetTableField.getDatasetGroupId(), view.getTableId())) {
                    if (ObjectUtils.isNotEmpty(request.getViewIds())) {
                        if (request.getViewIds().contains(view.getId())) {
                            extFilterList.add(request);
                        }
                    } else {
                        extFilterList.add(request);
                    }
                }
            }
        }

        // 下钻
        List<ChartExtFilterDTO> drillFilters = new ArrayList<>();
        boolean isDrill = false;
        List<ChartDrillRequest> drillRequestList = chartExtRequest.getDrill();
        if (ObjectUtils.isNotEmpty(drillRequestList) && (drill.size() > drillRequestList.size())) {
            ArrayList<ChartViewFieldDTO> fieldsToFilter = new ArrayList<>();
            if (StringUtils.containsIgnoreCase(view.getType(), "group")) {
                if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    if (ObjectUtils.isNotEmpty(xAxisExt) && ObjectUtils.isNotEmpty(extStack)) {
                        if (Objects.equals(drill.get(0).getId(), xAxisExt.get(0).getId())) {
                            fieldsToFilter.addAll(xAxisBase);
                            fieldsToFilter.addAll(extStack);
                        }
                        if (Objects.equals(drill.get(0).getId(), extStack.get(0).getId())) {
                            fieldsToFilter.addAll(xAxisBase);
                            fieldsToFilter.addAll(xAxisExt);
                        }
                    }
                } else if (ObjectUtils.isNotEmpty(xAxisExt) &&
                        Objects.equals(drill.get(0).getId(), xAxisExt.get(0).getId())) {
                    fieldsToFilter.addAll(xAxisBase);
                }
            } else if (StringUtils.containsIgnoreCase(view.getType(), "stack") &&
                    ObjectUtils.isNotEmpty(extStack) &&
                    Objects.equals(drill.get(0).getId(), extStack.get(0).getId())) {
                fieldsToFilter.addAll(xAxisBase);
            }
            ChartDrillRequest head = drillRequestList.get(0);
            Map<Long, String> dimValMap = new HashMap<>();
            head.getDimensionList().forEach(item -> dimValMap.put(item.getId(), item.getValue()));
            Map<Long, ChartViewFieldDTO> fieldMap = Stream.of(xAxisBase, xAxisExt, extStack)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toMap(ChartViewFieldDTO::getId, o -> o, ((p, n) -> p)));
            for (int i = 0; i < drillRequestList.size(); i++) {
                ChartDrillRequest request = drillRequestList.get(i);
                ChartViewFieldDTO chartViewFieldDTO = drill.get(i);
                for (ChartDimensionDTO requestDimension : request.getDimensionList()) {
                    // 将钻取值作为条件传递，将所有钻取字段作为xAxis并加上下一个钻取字段
                    if (Objects.equals(requestDimension.getId(), chartViewFieldDTO.getId())) {
                        isDrill = true;
                        fieldsToFilter.add(chartViewFieldDTO);
                        dimValMap.put(requestDimension.getId(), requestDimension.getValue());
                        if (!checkDrillExist(xAxis, extStack, requestDimension.getId(), view)) {
                            fieldMap.put(chartViewFieldDTO.getId(), chartViewFieldDTO);
                            xAxis.add(chartViewFieldDTO);
                        }
                        if (i == drillRequestList.size() - 1) {
                            ChartViewFieldDTO nextDrillField = drill.get(i + 1);
                            if (!checkDrillExist(xAxis, extStack, nextDrillField.getId(), view)) {
                                // get drill list first element's sort,then assign to nextDrillField
                                nextDrillField.setSort(getDrillSort(xAxis, drill.get(0)));
                                xAxis.add(nextDrillField);
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
                tmpFilter.setOperator("in");
                tmpFilter.setDateStyle(fieldMap.get(tmpField.getId()).getDateStyle());
                tmpFilter.setDatePattern(fieldMap.get(tmpField.getId()).getDatePattern());
                tmpFilter.setFieldId(String.valueOf(tmpField.getId()));
                tmpFilter.setValue(Collections.singletonList(dimValMap.get(tmpField.getId())));
                extFilterList.add(tmpFilter);
                drillFilters.add(tmpFilter);
            }
        }

        // 获取dsMap,union sql
        Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(table, chartExtRequest);
        String sql = (String) sqlMap.get("sql");
        Map<Long, DatasourceSchemaDTO> dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        List<String> dsList = new ArrayList<>();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
            dsList.add(next.getValue().getType());
        }
        boolean needOrder = Utils.isNeedOrder(dsList);

        // 调用数据源的calcite获得data
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDsList(dsMap);

        List<String[]> data = new ArrayList<>();

        // senior dynamic assist
        DatasourceRequest datasourceAssistRequest = new DatasourceRequest();
        datasourceAssistRequest.setDsList(dsMap);
        List<String[]> assistData = new ArrayList<>();
        List<ChartSeniorAssistDTO> dynamicAssistFields = getDynamicAssistFields(view);
        List<ChartViewFieldDTO> assistFields = null;
        if (StringUtils.containsIgnoreCase(view.getType(), "bar")
                || StringUtils.containsIgnoreCase(view.getType(), "line")
                || StringUtils.containsIgnoreCase(view.getType(), "area")
                || StringUtils.containsIgnoreCase(view.getType(), "scatter")
                || StringUtils.containsIgnoreCase(view.getType(), "mix")
        ) {
            assistFields = getAssistFields(dynamicAssistFields, yAxis);
        }

        // 处理过滤条件中的单引号
        fieldCustomFilter = fieldCustomFilter.stream().peek(ele -> {
            if (ObjectUtils.isNotEmpty(ele.getEnumCheckField())) {
                List<String> collect = ele.getEnumCheckField().stream().map(SQLUtils::transKeyword).collect(Collectors.toList());
                ele.setEnumCheckField(collect);
            }
            if (ObjectUtils.isNotEmpty(ele.getFilter())) {
                List<ChartCustomFilterItemDTO> collect = ele.getFilter().stream().peek(f -> f.setValue(SQLUtils.transKeyword(f.getValue()))).collect(Collectors.toList());
                ele.setFilter(collect);
            }
        }).collect(Collectors.toList());

        extFilterList = extFilterList.stream().peek(ele -> {
            if (ObjectUtils.isNotEmpty(ele.getValue())) {
                List<String> collect = ele.getValue().stream().map(SQLUtils::transKeyword).collect(Collectors.toList());
                ele.setValue(collect);
            }
        }).collect(Collectors.toList());

        String querySql = null;
        long totalPage = 0L;
        long totalItems = 0L;
        String totalPageSql = null;
        PageInfo pageInfo = new PageInfo();
        pageInfo.setGoPage(chartExtRequest.getGoPage());
        if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom")) {
            if (StringUtils.equalsIgnoreCase(view.getType(), "table-info") && table.getMode() == 0) {
                pageInfo.setPageSize(Math.min(view.getResultCount() - (chartExtRequest.getGoPage() - 1) * chartExtRequest.getPageSize(), chartExtRequest.getPageSize()));
            }
        } else {
            pageInfo.setPageSize(chartExtRequest.getPageSize());
        }

        List<ChartViewFieldDTO> detailFieldList = new ArrayList<>();
        String detailFieldSql = null;
        List<String[]> detailData = new ArrayList<>();
        //如果不是插件视图 走原生逻辑
        if (table.getMode() == 0) {// 直连
            if (ObjectUtils.isEmpty(dsMap)) {
                DEException.throwException(ResultCode.DATA_IS_WRONG.code(), Translator.get("i18n_datasource_delete"));
            }
            for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
                DatasourceSchemaDTO ds = next.getValue();
                if (StringUtils.isNotEmpty(ds.getStatus()) && "Error".equalsIgnoreCase(ds.getStatus())) {
                    DEException.throwException(ResultCode.DATA_IS_WRONG.code(), Translator.get("i18n_invalid_ds"));
                }
            }

            SQLMeta sqlMeta = new SQLMeta();
            Table2SQLObj.table2sqlobj(sqlMeta, null, "(" + sql + ")");
            CustomWhere2Str.customWhere2sqlObj(sqlMeta, fieldCustomFilter, transFields(allFields));
            ExtWhere2Str.extWhere2sqlOjb(sqlMeta, extFilterList, transFields(allFields));
            WhereTree2Str.transFilterTrees(sqlMeta, rowPermissionsTree, transFields(allFields));

            if (StringUtils.equalsAnyIgnoreCase(view.getType(), "indicator", "gauge", "liquid")) {
                Quota2SQLObj.quota2sqlObj(sqlMeta, yAxis, transFields(allFields));
                querySql = SQLProvider.createQuerySQL(sqlMeta, true, needOrder, view);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                List<ChartViewFieldDTO> xFields = new ArrayList<>();
                xFields.addAll(xAxis);
                xFields.addAll(extStack);
                Dimension2SQLObj.dimension2sqlObj(sqlMeta, xFields, transFields(allFields));
                Quota2SQLObj.quota2sqlObj(sqlMeta, yAxis, transFields(allFields));
                querySql = SQLProvider.createQuerySQL(sqlMeta, true, needOrder, view);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                List<ChartViewFieldDTO> yFields = new ArrayList<>();
                yFields.addAll(yAxis);
                yFields.addAll(extBubble);
                Dimension2SQLObj.dimension2sqlObj(sqlMeta, xAxis, transFields(allFields));
                Quota2SQLObj.quota2sqlObj(sqlMeta, yFields, transFields(allFields));
                querySql = SQLProvider.createQuerySQL(sqlMeta, true, needOrder, view);
            } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                Dimension2SQLObj.dimension2sqlObj(sqlMeta, xAxis, transFields(allFields));
                String originSql = SQLProvider.createQuerySQL(sqlMeta, false, true, view);// 明细表强制加排序
                String limit = ((pageInfo.getGoPage() != null && pageInfo.getPageSize() != null) ? " LIMIT " + pageInfo.getPageSize() + " OFFSET " + (pageInfo.getGoPage() - 1) * pageInfo.getPageSize() : "");
                querySql = originSql + limit;
                totalPageSql = "SELECT COUNT(*) FROM (" + originSql + ") COUNT_TEMP";
            } else {
                Dimension2SQLObj.dimension2sqlObj(sqlMeta, xAxis, transFields(allFields));
                Quota2SQLObj.quota2sqlObj(sqlMeta, yAxis, transFields(allFields));
                querySql = SQLProvider.createQuerySQL(sqlMeta, true, needOrder, view);
                if (containDetailField(view) && ObjectUtils.isNotEmpty(viewFields)) {
                    detailFieldList.addAll(xAxis);
                    detailFieldList.addAll(viewFields);

                    Dimension2SQLObj.dimension2sqlObj(sqlMeta, detailFieldList, transFields(allFields));
                    String originSql = SQLProvider.createQuerySQL(sqlMeta, false, needOrder, view);
                    String limit = ((pageInfo.getGoPage() != null && pageInfo.getPageSize() != null) ? " LIMIT " + pageInfo.getPageSize() + " OFFSET " + (pageInfo.getGoPage() - 1) * pageInfo.getPageSize() : "");
                    detailFieldSql = originSql + limit;
                }
            }

            if (StringUtils.isNotEmpty(totalPageSql) && StringUtils.equalsIgnoreCase((String) mapSize.get("tablePageMode"), "page")) {
                datasourceRequest.setQuery(totalPageSql);
                datasourceRequest.setTotalPageFlag(true);
                List<String[]> tmpData = (List<String[]>) calciteProvider.fetchResultField(datasourceRequest).get("data");
                totalItems = ObjectUtils.isEmpty(tmpData) ? 0 : Long.valueOf(tmpData.get(0)[0]);
                if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom")) {
                    totalItems = totalItems <= view.getResultCount() ? totalItems : view.getResultCount();
                }
                totalPage = (totalItems / pageInfo.getPageSize()) + (totalItems % pageInfo.getPageSize() > 0 ? 1 : 0);
            }

            datasourceRequest.setQuery(querySql);
            logger.info("calcite chart sql: " + querySql);

            data = (List<String[]>) calciteProvider.fetchResultField(datasourceRequest).get("data");
            if (ObjectUtils.isNotEmpty(assistFields)) {
                datasourceAssistRequest.setQuery(assistSQL(datasourceRequest.getQuery(), assistFields));
                assistData = (List<String[]>) calciteProvider.fetchResultField(datasourceAssistRequest).get("data");
            }

            if (StringUtils.isNotBlank(detailFieldSql)) {
                datasourceRequest.setQuery(detailFieldSql);
                detailData = (List<String[]>) calciteProvider.fetchResultField(datasourceRequest).get("data");
            }
        }
        // 自定义排序
        if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
            List<ChartViewFieldDTO> list = new ArrayList<>();
            list.addAll(xAxis);
            list.addAll(extStack);
            data = resultCustomSort(list, data);
        } else {
            data = resultCustomSort(xAxis, data);
        }
        // 同比/环比计算，通过对比类型和数据设置，计算出对应指标的结果，然后替换结果data数组中的对应元素
        // 如果因维度变化（如时间字段缺失，时间字段的展示格式变化）导致无法计算结果的，则结果data数组中的对应元素全置为null
        // 根据不同图表类型，获得需要替换的指标index array
        for (int i = 0; i < yAxis.size(); i++) {
            ChartViewFieldDTO chartViewFieldDTO = yAxis.get(i);
            ChartFieldCompareDTO compareCalc = chartViewFieldDTO.getCompareCalc();
            if (ObjectUtils.isEmpty(compareCalc)) {
                continue;
            }
            if (StringUtils.isNotEmpty(compareCalc.getType())
                    && !StringUtils.equalsIgnoreCase(compareCalc.getType(), "none")) {
                Long compareFieldId = compareCalc.getField();// 选中字段
                // 计算指标对应的下标
                int dataIndex = 0;// 数据字段下标
                if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    dataIndex = xAxis.size() + extStack.size() + i;
                } else {
                    dataIndex = xAxis.size() + i;
                }
                if (Arrays.asList(ChartConstants.M_Y).contains(compareCalc.getType())) {
                    String resultData = compareCalc.getResultData();// 数据设置
                    // 获取选中字段以及下标
                    List<ChartViewFieldDTO> checkedField = new ArrayList<>(xAxis);
                    if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                        checkedField.addAll(extStack);
                    }
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
                        item[dataIndex] = new BigDecimal(cValue)
                                .divide(sum, 8, RoundingMode.HALF_UP)
                                .toString();
                    }
                }
            }
        }

        // 构建结果
        Map<String, Object> map = new TreeMap<>();
        // 图表组件可再扩展
        Map<String, Object> mapChart = new HashMap<>();
        if (StringUtils.equalsIgnoreCase(view.getRender(), "echarts")) {
            if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                mapChart = ChartDataBuild.transStackChartData(xAxis, yAxis, view, data, extStack, isDrill);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                mapChart = ChartDataBuild.transScatterData(xAxis, yAxis, view, data, extBubble, isDrill);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "radar")) {
                mapChart = ChartDataBuild.transRadarChartData(xAxis, yAxis, view, data, isDrill);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "indicator")
                    || StringUtils.containsIgnoreCase(view.getType(), "gauge")
                    || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                mapChart = ChartDataBuild.transNormalChartData(xAxis, yAxis, view, data, isDrill);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "chart-mix")) {
                mapChart = ChartDataBuild.transMixChartData(xAxis, yAxis, view, data, isDrill);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "label")) {
                mapChart = ChartDataBuild.transLabelChartData(xAxis, yAxis, view, data, isDrill);
            } else {
                mapChart = ChartDataBuild.transChartData(xAxis, yAxis, view, data, isDrill);
            }
        } else if (StringUtils.equalsIgnoreCase(view.getRender(), "antv")) {
            if (StringUtils.equalsAnyIgnoreCase(view.getType(), "bar-group", "line")) {
                mapChart = ChartDataBuild.transBaseGroupDataAntV(xAxisBase, xAxis, xAxisExt, yAxis, view, data, isDrill);
            } else if (StringUtils.equalsIgnoreCase(view.getType(), "bar-group-stack")) {
                mapChart = ChartDataBuild.transGroupStackDataAntV(xAxisBase, xAxis, xAxisExt, yAxis, extStack, data, view, isDrill);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "bar-stack")) {
                mapChart = ChartDataBuild.transStackChartDataAntV(xAxis, yAxis, view, data, extStack, isDrill);
            } else if (StringUtils.equalsAnyIgnoreCase(view.getType(), "line-stack", "area-stack")) {
                mapChart = ChartDataBuild.transStackChartDataAntV(xAxis, yAxis, view, data, extStack, isDrill);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                mapChart = ChartDataBuild.transScatterDataAntV(xAxis, yAxis, view, data, extBubble, isDrill);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "radar")) {
                mapChart = ChartDataBuild.transRadarChartDataAntV(xAxis, yAxis, view, data, isDrill);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "indicator")
                    || StringUtils.containsIgnoreCase(view.getType(), "gauge")
                    || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                mapChart = ChartDataBuild.transNormalChartData(xAxis, yAxis, view, data, isDrill);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "chart-mix")) {
                mapChart = ChartDataBuild.transMixChartDataAntV(xAxis, yAxis, view, data, isDrill);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "label")) {
                mapChart = ChartDataBuild.transLabelChartData(xAxis, yAxis, view, data, isDrill);
            } else {
                mapChart = ChartDataBuild.transChartDataAntV(xAxis, yAxis, view, data, isDrill);
            }
        } else if (StringUtils.equalsIgnoreCase(view.getRender(), "custom")) {
            if (StringUtils.containsIgnoreCase(view.getType(), "indicator")) {
                mapChart = ChartDataBuild.transNormalChartData(xAxis, yAxis, view, data, isDrill);
            }
        }
        // table组件，明细表，也用于导出数据
        Map<String, Object> mapTableNormal = null;
        if (ObjectUtils.isNotEmpty(detailData)) {
            mapTableNormal = ChartDataBuild.transTableNormalWithDetail(xAxis, yAxis, data, detailFieldList, detailData, desensitizationList);
        } else {
            mapTableNormal = ChartDataBuild.transTableNormal(xAxis, yAxis, view, data, extStack, desensitizationList);
        }
        chartViewDTO = uniteViewResult(datasourceRequest.getQuery(), mapChart, mapTableNormal, view, isDrill, drillFilters, dynamicAssistFields, assistData);
        chartViewDTO.setTotalPage(totalPage);
        chartViewDTO.setTotalItems(totalItems);
        return chartViewDTO;
    }

    private List<ChartViewFieldDTO> getSizeField(ChartViewDTO view) throws Exception {
        List<ChartViewFieldDTO> list = new ArrayList<>();
        Map<String, Object> customAttr = view.getCustomAttr();

        Map<String, Object> size = (Map<String, Object>) customAttr.get("misc");

        ChartViewFieldDTO gaugeMinViewField = getDynamicField(size, "gaugeMinType", "gaugeMinField");
        if (gaugeMinViewField != null) {
            list.add(gaugeMinViewField);
        }
        ChartViewFieldDTO gaugeMaxViewField = getDynamicField(size, "gaugeMaxType", "gaugeMaxField");
        if (gaugeMaxViewField != null) {
            list.add(gaugeMaxViewField);
        }
        ChartViewFieldDTO liquidMaxViewField = getDynamicField(size, "liquidMaxType", "liquidMaxField");
        if (liquidMaxViewField != null) {
            list.add(liquidMaxViewField);
        }

        return list;
    }

    private ChartViewFieldDTO getDynamicField(Map<String, Object> sizeObj, String type, String field) {
        String maxType = (String) sizeObj.get(type);
        if (StringUtils.equalsIgnoreCase("dynamic", maxType)) {
            Map<String, Object> maxField = (Map<String, Object>) sizeObj.get(field);
            Long id = Long.valueOf((String) maxField.get("id"));
            String summary = (String) maxField.get("summary");
            DatasetTableFieldDTO datasetTableField = datasetTableFieldManage.selectById(id);
            if (ObjectUtils.isNotEmpty(datasetTableField)) {
                if (datasetTableField.getDeType() == 0 || datasetTableField.getDeType() == 1 || datasetTableField.getDeType() == 5) {
                    if (!StringUtils.containsIgnoreCase(summary, "count")) {
                        DEException.throwException(Translator.get("i18n_gauge_field_change"));
                    }
                }
                ChartViewFieldDTO dto = new ChartViewFieldDTO();
                BeanUtils.copyBean(dto, datasetTableField);
                dto.setSummary(summary);
                return dto;
            } else {
                DEException.throwException(Translator.get("i18n_gauge_field_delete"));
            }
        }
        return null;
    }

    private ChartViewDTO emptyChartViewDTO(ChartViewDTO view) {
        ChartViewDTO dto = new ChartViewDTO();
        BeanUtils.copyBean(dto, view);
        return dto;
    }

    private boolean checkDrillExist(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> extStack, Long fieldId, ChartViewDTO view) {
        if (ObjectUtils.isNotEmpty(xAxis)) {
            for (ChartViewFieldDTO x : xAxis) {
                if (Objects.equals(x.getId(), fieldId)) {
                    return true;
                }
            }
        }
        if (StringUtils.containsIgnoreCase(view.getType(), "stack") && ObjectUtils.isNotEmpty(extStack)) {
            for (ChartViewFieldDTO x : extStack) {
                if (Objects.equals(x.getId(), fieldId)) {
                    return true;
                }
            }
        }
        return false;
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

    private List<ChartSeniorAssistDTO> getDynamicAssistFields(ChartViewDTO view) throws Exception {
        List<ChartSeniorAssistDTO> list = new ArrayList<>();

        Map<String, Object> senior = view.getSenior();
        if (ObjectUtils.isEmpty(senior)) {
            return list;
        }

        var assistLineCfg = JsonUtil.parseObject((String) JsonUtil.toJSONString(senior.get("assistLineCfg")), ChartSeniorAssistCfgDTO.class);
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

    private List<ChartViewFieldDTO> getAssistFields(List<ChartSeniorAssistDTO> list, List<ChartViewFieldDTO> yAxis) {
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

    private List<DatasetTableFieldDTO> transFields(List<? extends ChartViewFieldBaseDTO> list) {
        return list.stream().map(ele -> {
            DatasetTableFieldDTO dto = new DatasetTableFieldDTO();
            BeanUtils.copyBean(dto, ele);
            return dto;
        }).collect(Collectors.toList());
    }

    public Boolean containDetailField(ChartViewDTO view) {
        List<String> detailFieldViewTypes = new ArrayList<>();
        detailFieldViewTypes.add("map");
        return detailFieldViewTypes.contains(view.getType());
    }

    public String assistSQL(String sql, List<ChartViewFieldDTO> assistFields) {
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

    // 对结果排序
    public List<String[]> resultCustomSort(List<ChartViewFieldDTO> xAxis, List<String[]> data) {
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

    public List<String[]> customSort(List<String> custom, List<String[]> data, int index) {
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

    public ChartViewDTO uniteViewResult(String sql, Map<String, Object> chartData, Map<String, Object> tableData, ChartViewDTO view, Boolean isDrill, List<ChartExtFilterDTO> drillFilters, List<ChartSeniorAssistDTO> dynamicAssistFields, List<String[]> assistData) {

        Map<String, Object> map = new HashMap<>();
        map.putAll(chartData);
        map.putAll(tableData);

        // get all fields
        List<ChartViewFieldDTO> allFields = getAllChartFields(view);
        map.put("sourceFields", allFields);
        // merge assist result
        mergeAssistField(dynamicAssistFields, assistData);
        map.put("dynamicAssistLines", dynamicAssistFields);

        ChartViewDTO dto = new ChartViewDTO();
        BeanUtils.copyBean(dto, view);
        dto.setData(map);
        dto.setSql(java.util.Base64.getEncoder().encodeToString(sql.getBytes()));
        dto.setDrill(isDrill);
        dto.setDrillFilters(drillFilters);
        return dto;
    }

    private void mergeAssistField(List<ChartSeniorAssistDTO> dynamicAssistFields, List<String[]> assistData) {
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

    public List<String> getFieldData(ChartViewDTO view, Long fieldId, String fieldType) throws Exception {
        ChartExtRequest requestList = view.getChartExtRequest();
        List<String[]> sqlData = sqlData(view, requestList, fieldId);
        List<ChartViewFieldDTO> fieldList = new ArrayList<>();
        switch (fieldType) {
            case "xAxis" -> fieldList = view.getXAxis();
            case "xAxisExt" -> fieldList = view.getXAxisExt();
            case "extStack" -> fieldList = view.getExtStack();
        }
        DatasetTableFieldDTO field = datasetTableFieldManage.selectById(fieldId);

        List<String> res = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(field) && fieldList.size() > 0) {
            // 找到对应维度
            ChartViewFieldDTO chartViewFieldDTO = null;
            int index = 0;
            int getIndex = 0;
            for (int i = 0; i < fieldList.size(); i++) {
                ChartViewFieldDTO item = fieldList.get(i);
                if (StringUtils.equalsIgnoreCase(item.getSort(), "custom_sort")) {// 此处与已有的自定义字段对比
                    chartViewFieldDTO = item;
                    index = i;
                }
                if (Objects.equals(item.getId(), field.getId())) {// 获得当前自定义的字段
                    getIndex = i;
                }
            }
            if (StringUtils.equalsIgnoreCase(fieldType, "xAxisExt")) {
                List<ChartViewFieldDTO> xAxis = view.getXAxis();
                index += xAxis.size();
                getIndex += xAxis.size();
            }
            if (StringUtils.equalsIgnoreCase(fieldType, "extStack")) {
                int xAxisSize = CollectionUtils.size(view.getXAxis());
                int extSize = CollectionUtils.size(view.getXAxisExt());
                index += xAxisSize + extSize;
                getIndex += xAxisSize + extSize;
            }

            List<String[]> sortResult = resultCustomSort(fieldList, sqlData);
            if (ObjectUtils.isNotEmpty(chartViewFieldDTO) && (getIndex >= index)) {
                // 获取自定义值与data对应列的结果
                List<String[]> strings = customSort(Optional.ofNullable(chartViewFieldDTO.getCustomSort()).orElse(new ArrayList<>()), sortResult, index);
                for (int i = 0; i < strings.size(); i++) {
                    res.add(strings.get(i)[getIndex]);
                }
            } else {
                // 返回请求结果
                for (int i = 0; i < sortResult.size(); i++) {
                    res.add(sortResult.get(i)[getIndex]);
                }
            }
        }
        return res.stream().distinct().collect(Collectors.toList());
    }

    public List<String[]> sqlData(ChartViewDTO view, ChartExtRequest requestList, Long fieldId) throws Exception {
        if (ObjectUtils.isEmpty(view)) {
            DEException.throwException(Translator.get("i18n_chart_delete"));
        }

        // get all fields
        List<ChartViewFieldDTO> allFields = getAllChartFields(view);

        List<ChartViewFieldDTO> xAxisBase = new ArrayList<>(view.getXAxis());
        List<ChartViewFieldDTO> xAxis = new ArrayList<>(view.getXAxis());
        List<ChartViewFieldDTO> xAxisExt = new ArrayList<>(view.getXAxisExt());
        if (StringUtils.equalsIgnoreCase(view.getType(), "table-pivot")
                || StringUtils.containsIgnoreCase(view.getType(), "group")
                || ("antv".equalsIgnoreCase(view.getRender()) && "line".equalsIgnoreCase(view.getType()))
                || StringUtils.equalsIgnoreCase(view.getType(), "flow-map")) {
            xAxis.addAll(xAxisExt);
        }
        List<ChartViewFieldDTO> yAxis = new ArrayList<>(view.getYAxis());
        if (StringUtils.equalsIgnoreCase(view.getType(), "chart-mix")) {
            List<ChartViewFieldDTO> yAxisExt = new ArrayList<>(view.getYAxisExt());
            yAxis.addAll(yAxisExt);
        }
        if (StringUtils.equalsIgnoreCase(view.getRender(), "antv") && StringUtils.equalsAnyIgnoreCase(view.getType(), "gauge", "liquid")) {
            List<ChartViewFieldDTO> sizeField = getSizeField(view);
            yAxis.addAll(sizeField);
        }
        List<ChartViewFieldDTO> extStack = new ArrayList<>(view.getExtStack());
        List<ChartViewFieldDTO> extBubble = new ArrayList<>(view.getExtBubble());
        List<ChartFieldCustomFilterDTO> fieldCustomFilter = new ArrayList<>(view.getCustomFilter());
        List<ChartViewFieldDTO> drill = new ArrayList<>(view.getDrillFields());

        // 获取数据集,需校验权限
        DatasetGroupInfoDTO table = datasetGroupManage.get(view.getTableId(), null);// todo
        Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();// todo
        List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = permissionManage.getRowPermissionsTree(table.getId(), view.getChartExtRequest().getUser());

        for (ChartFieldCustomFilterDTO ele : fieldCustomFilter) {
            ele.setField(datasetTableFieldManage.selectById(ele.getId()));
        }

        if (ObjectUtils.isEmpty(xAxis) && ObjectUtils.isEmpty(yAxis)) {
            return new ArrayList<String[]>();
        }

        switch (view.getType()) {
            case "label":
                yAxis = new ArrayList<>();
                if (ObjectUtils.isEmpty(xAxis)) {
                    return new ArrayList<String[]>();
                }
                break;
            case "indicator":
            case "gauge":
            case "liquid":
                xAxis = new ArrayList<>();
                if (ObjectUtils.isEmpty(yAxis)) {
                    return new ArrayList<String[]>();
                }
                break;
            case "table-info":
                yAxis = new ArrayList<>();
                if (ObjectUtils.isEmpty(xAxis)) {
                    return new ArrayList<String[]>();
                }
                break;
            case "table-normal":
                break;
            case "bar-group":
            case "bar-group-stack":
            case "flow-map":
                break;
            default:
        }

        // 获取dsMap,union sql
        Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(table, null);
        String sql = (String) sqlMap.get("sql");
        Map<Long, DatasourceSchemaDTO> dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        List<String> dsList = new ArrayList<>();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
            dsList.add(next.getValue().getType());
        }
        boolean needOrder = Utils.isNeedOrder(dsList);

        // 调用数据源的calcite获得data
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDsList(dsMap);

        List<String[]> data = new ArrayList<>();

        String querySql = null;
        //如果不是插件视图 走原生逻辑
        if (table.getMode() == 0) {// 直连
            if (ObjectUtils.isEmpty(dsMap)) {
                DEException.throwException(Translator.get("i18n_datasource_delete"));
            }
            for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
                DatasourceSchemaDTO ds = next.getValue();
                if (StringUtils.isNotEmpty(ds.getStatus()) && "Error".equalsIgnoreCase(ds.getStatus())) {
                    DEException.throwException(Translator.get("i18n_invalid_ds"));
                }
            }

            SQLMeta sqlMeta = new SQLMeta();
            Table2SQLObj.table2sqlobj(sqlMeta, null, "(" + sql + ")");
            WhereTree2Str.transFilterTrees(sqlMeta, rowPermissionsTree, transFields(allFields));

            if (StringUtils.equalsAnyIgnoreCase(view.getType(), "indicator", "gauge", "liquid")) {
                Quota2SQLObj.quota2sqlObj(sqlMeta, yAxis, transFields(allFields));
                querySql = SQLProvider.createQuerySQL(sqlMeta, true, needOrder, view);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                List<ChartViewFieldDTO> xFields = new ArrayList<>();
                xFields.addAll(xAxis);
                xFields.addAll(extStack);
                Dimension2SQLObj.dimension2sqlObj(sqlMeta, xFields, transFields(allFields));
                Quota2SQLObj.quota2sqlObj(sqlMeta, yAxis, transFields(allFields));
                querySql = SQLProvider.createQuerySQL(sqlMeta, true, needOrder, view);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                List<ChartViewFieldDTO> yFields = new ArrayList<>();
                yFields.addAll(yAxis);
                yFields.addAll(extBubble);
                Dimension2SQLObj.dimension2sqlObj(sqlMeta, xAxis, transFields(allFields));
                Quota2SQLObj.quota2sqlObj(sqlMeta, yFields, transFields(allFields));
                querySql = SQLProvider.createQuerySQL(sqlMeta, true, needOrder, view);
            } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                Dimension2SQLObj.dimension2sqlObj(sqlMeta, xAxis, transFields(allFields));
                querySql = SQLProvider.createQuerySQL(sqlMeta, false, true, view);
            } else {
                Dimension2SQLObj.dimension2sqlObj(sqlMeta, xAxis, transFields(allFields));
                Quota2SQLObj.quota2sqlObj(sqlMeta, yAxis, transFields(allFields));
                querySql = SQLProvider.createQuerySQL(sqlMeta, true, needOrder, view);
            }

            datasourceRequest.setQuery(querySql);
            logger.info("calcite chart get field enum sql: " + querySql);

            data = (List<String[]>) calciteProvider.fetchResultField(datasourceRequest).get("data");
        }
        return data;
    }

    private boolean enableExtData(String type) {
        return StringUtils.containsAnyIgnoreCase(type, "bar", "line", "area", "pie", "radar", "map", "scatter", "funnel", "word-cloud", "waterfall");
    }

    private List<ChartViewFieldDTO> getAllChartFields(ChartViewDTO view) {
        // get all fields
        Map<String, List<ChartViewFieldDTO>> stringListMap = chartViewManege.listByDQ(view.getTableId(), view.getId());
        List<ChartViewFieldDTO> dimensionList = stringListMap.get("dimensionList");
        List<ChartViewFieldDTO> quotaList = stringListMap.get("quotaList");
        List<ChartViewFieldDTO> allFields = new ArrayList<>();
        allFields.addAll(dimensionList);
        allFields.addAll(quotaList);
        return allFields.stream().filter(ele -> ele.getId() != -1L).collect(Collectors.toList());
    }

    public void saveChartViewFromVisualization(String checkData, Long sceneId, Map<Long, ChartViewDTO> chartViewsInfo) {
        if (!MapUtils.isEmpty(chartViewsInfo)) {
            chartViewsInfo.forEach((key, chartViewDTO) -> {
                if (checkData.indexOf(chartViewDTO.getId() + "") > -1) {
                    try {
                        chartViewDTO.setSceneId(sceneId);
                        chartViewManege.save(chartViewDTO);
                    } catch (Exception e) {
                        DEException.throwException(e);
                    }
                }
            });
        }
    }
}

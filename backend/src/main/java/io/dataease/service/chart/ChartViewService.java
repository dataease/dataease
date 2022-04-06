package io.dataease.service.chart;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.service.AuthUserService;
import io.dataease.base.domain.*;
import io.dataease.base.mapper.ChartViewCacheMapper;
import io.dataease.base.mapper.ChartViewMapper;
import io.dataease.base.mapper.PanelViewMapper;
import io.dataease.base.mapper.ext.ExtChartGroupMapper;
import io.dataease.base.mapper.ext.ExtChartViewMapper;
import io.dataease.base.mapper.ext.ExtPanelGroupExtendDataMapper;
import io.dataease.commons.constants.ColumnPermissionConstants;
import io.dataease.commons.constants.CommonConstants;
import io.dataease.commons.constants.JdbcConstants;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.commons.utils.LogUtil;
import io.dataease.controller.request.chart.*;
import io.dataease.controller.request.datasource.DatasourceRequest;
import io.dataease.controller.response.ChartDetail;
import io.dataease.controller.response.DataSetDetail;
import io.dataease.dto.chart.*;
import io.dataease.dto.dataset.DataSetTableDTO;
import io.dataease.dto.dataset.DataSetTableUnionDTO;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import io.dataease.listener.util.CacheUtils;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.view.entity.*;
import io.dataease.plugins.view.service.ViewPluginService;
import io.dataease.provider.ProviderFactory;
import io.dataease.provider.datasource.DatasourceProvider;
import io.dataease.provider.QueryProvider;
import io.dataease.service.chart.util.ChartDataBuild;
import io.dataease.service.dataset.DataSetTableFieldsService;
import io.dataease.service.dataset.DataSetTableService;
import io.dataease.service.dataset.DataSetTableUnionService;
import io.dataease.service.dataset.PermissionService;
import io.dataease.service.datasource.DatasourceService;
import io.dataease.service.engine.EngineService;
import io.dataease.service.panel.PanelGroupExtendDataService;
import io.dataease.service.panel.PanelViewService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.pentaho.di.core.util.UUIDUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @Author gin
 * @Date 2021/3/1 12:34 下午
 */
@Service
public class ChartViewService {

    private static Gson gson = new Gson();

    @Resource
    private ChartViewMapper chartViewMapper;
    @Resource
    private ExtChartViewMapper extChartViewMapper;
    @Resource
    private DataSetTableService dataSetTableService;
    @Resource
    private DatasourceService datasourceService;
    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;
    @Resource
    private ExtChartGroupMapper extChartGroupMapper;
    @Resource
    private DataSetTableUnionService dataSetTableUnionService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private AuthUserService authUserService;
    @Resource
    private EngineService engineService;
    @Resource
    private ChartViewCacheMapper chartViewCacheMapper;
    @Resource
    private PanelViewMapper panelViewMapper;
    @Resource
    private PanelGroupExtendDataService extendDataService;
    @Resource
    private ExtPanelGroupExtendDataMapper extPanelGroupExtendDataMapper;
    @Resource
    private ChartViewCacheService chartViewCacheService;


    //默认使用非公平
    private ReentrantLock lock = new ReentrantLock();

    // 直接保存统一到缓存表
    public ChartViewDTO save(ChartViewCacheRequest chartView) {
        long timestamp = System.currentTimeMillis();
        chartView.setUpdateTime(timestamp);
        chartViewCacheMapper.updateByPrimaryKeySelective(chartView);
        Optional.ofNullable(chartView.getId()).ifPresent(id -> {
            CacheUtils.remove(JdbcConstants.VIEW_CACHE_KEY, id);
        });
        return getOne(chartView.getId(), "panel_edit");
    }

    public String checkTitle(ChartViewCacheRequest chartView){
        ChartViewCacheExample example = new ChartViewCacheExample();
        example.createCriteria().andTitleEqualTo(chartView.getTitle()).andSceneIdEqualTo(chartView.getSceneId()).andIdNotEqualTo(chartView.getId());
        List<ChartViewCache>  result =  chartViewCacheMapper.selectByExample(example);
        if(CollectionUtils.isNotEmpty(result)){
           return "fail";
        }else{
            return "success";
        }
    }

    public ChartViewWithBLOBs newOne(ChartViewWithBLOBs chartView) {
        long timestamp = System.currentTimeMillis();
        // 校验名称
        ChartViewExample queryExample = new ChartViewExample();
        queryExample.createCriteria().andSceneIdEqualTo(chartView.getSceneId()).andNameEqualTo(chartView.getName());
        List<ChartView>  result =  chartViewMapper.selectByExample(queryExample);
        if(CollectionUtils.isNotEmpty(result)){
            DEException.throwException(Translator.get("theme_name_repeat"));
        }
        chartView.setUpdateTime(timestamp);
        chartView.setId(UUID.randomUUID().toString());
        chartView.setCreateBy(AuthUtils.getUser().getUsername());
        chartView.setCreateTime(timestamp);
        chartView.setUpdateTime(timestamp);
        chartViewMapper.insertSelective(chartView);
        // 新建的视图也存入缓存表中
        chartViewCacheService.refreshCache(chartView.getId());

        PanelView newPanelView = new PanelView();
        newPanelView.setId(UUIDUtil.getUUIDAsString());
        newPanelView.setChartViewId(chartView.getId());
        newPanelView.setCreateBy(chartView.getCreateBy());
        newPanelView.setPanelId(chartView.getSceneId());
        newPanelView.setCreateTime(timestamp);
        newPanelView.setPosition("panel");
        panelViewMapper.insertSelective(newPanelView);
        return chartView;
    }


    // 直接保存统一到缓存表
    public void save2Cache(ChartViewCacheWithBLOBs chartView) {
        long timestamp = System.currentTimeMillis();
        chartView.setUpdateTime(timestamp);
        chartViewCacheMapper.updateByPrimaryKeySelective(chartView);
        Optional.ofNullable(chartView.getId()).ifPresent(id -> {
            CacheUtils.remove(JdbcConstants.VIEW_CACHE_KEY, id);
        });
    }

    public List<ChartViewDTO> list(ChartViewRequest chartViewRequest) {
        chartViewRequest.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        return extChartViewMapper.search(chartViewRequest);
    }

    public List<ChartViewDTO> listAndGroup(ChartViewRequest chartViewRequest) {
        chartViewRequest.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        List<ChartViewDTO> charts = extChartViewMapper.search(chartViewRequest);
        charts.forEach(ele -> ele.setIsLeaf(true));
        // 获取group下的子group
        ChartGroupRequest chartGroupRequest = new ChartGroupRequest();
        chartGroupRequest.setLevel(null);
        chartGroupRequest.setType("group");
        chartGroupRequest.setPid(chartViewRequest.getSceneId());
        chartGroupRequest.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        chartGroupRequest.setSort("name asc,create_time desc");
        List<ChartGroupDTO> groups = extChartGroupMapper.search(chartGroupRequest);
        List<ChartViewDTO> group = groups.stream().map(ele -> {
            ChartViewDTO dto = new ChartViewDTO();
            BeanUtils.copyBean(dto, ele);
            dto.setIsLeaf(false);
            dto.setType("group");
            return dto;
        }).collect(Collectors.toList());
        group.addAll(charts);
        return group;
    }

    public List<ChartViewDTO> search(ChartViewRequest chartViewRequest) {
        String userId = String.valueOf(AuthUtils.getUser().getUserId());
        chartViewRequest.setUserId(userId);
        chartViewRequest.setSort("name asc");
        List<ChartViewDTO> ds = extChartViewMapper.search(chartViewRequest);
        if (CollectionUtils.isEmpty(ds)) {
            return ds;
        }

        TreeSet<String> ids = new TreeSet<>();
        ds.forEach(ele -> {
            ele.setIsLeaf(true);
            ele.setPid(ele.getSceneId());
            ids.add(ele.getPid());
        });

        List<ChartViewDTO> group = new ArrayList<>();
        ChartGroupRequest chartGroupRequest = new ChartGroupRequest();
        chartGroupRequest.setUserId(userId);
        chartGroupRequest.setIds(ids);
        List<ChartGroupDTO> search = extChartGroupMapper.search(chartGroupRequest);
        while (CollectionUtils.isNotEmpty(search)) {
            ids.clear();
            search.forEach(ele -> {
                ChartViewDTO dto = new ChartViewDTO();
                BeanUtils.copyBean(dto, ele);
                dto.setIsLeaf(false);
                dto.setType("group");
                group.add(dto);
                ids.add(ele.getPid());
            });
            chartGroupRequest.setIds(ids);
            search = extChartGroupMapper.search(chartGroupRequest);
        }

        List<ChartViewDTO> res = new ArrayList<>();
        Map<String, ChartViewDTO> map = new TreeMap<>();
        group.forEach(ele -> map.put(ele.getId(), ele));
        Iterator<Map.Entry<String, ChartViewDTO>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            res.add(iterator.next().getValue());
        }
        res.sort(Comparator.comparing(ChartViewDTO::getName));
        res.addAll(ds);
        return res;
    }

    public ChartViewWithBLOBs get(String id) {
        return chartViewMapper.selectByPrimaryKey(id);
    }

    public ChartViewDTO getOneWithPermission(String id) {
        String userId = AuthUtils.getUser() != null ? String.valueOf(AuthUtils.getUser().getUserId()) : "NONE";
        return extChartViewMapper.searchOneWithPrivileges(userId, id);
    }


    @Transactional
    public ChartViewDTO getOne(String id, String queryFrom) {
        try{
            ChartViewDTO result;
            if(CommonConstants.VIEW_QUERY_FROM.PANEL_EDIT.equals(queryFrom)) {
                //仪表板编辑页面 从缓存表中取数据 缓存表中没有数据则进行插入
                result = extChartViewMapper.searchOneFromCache(id);
                if (result == null) {
                    chartViewCacheService.refreshCache(id);
                    result = extChartViewMapper.searchOneFromCache(id);
                }
            } else {
                result = extChartViewMapper.searchOne(id);
            }
            if(result==null){
                DataEaseException.throwException(Translator.get("i18n_chart_delete"));
            }
            return result;
        }catch (Exception e){
            e.printStackTrace();
            DataEaseException.throwException(e);
        }
        return null;

    }

    public void delete(String id) {
        chartViewMapper.deleteByPrimaryKey(id);
    }

    public void deleteBySceneId(String sceneId) {
        ChartViewExample chartViewExample = new ChartViewExample();
        chartViewExample.createCriteria().andSceneIdEqualTo(sceneId);
        chartViewMapper.deleteByExample(chartViewExample);
    }

    public ChartViewDTO getData(String id, ChartExtRequest request) throws Exception {
        try {
            ChartViewDTO view = this.getOne(id, request.getQueryFrom());
            // 如果是从仪表板获取视图数据，则仪表板的查询模式，查询结果的数量，覆盖视图对应的属性
            if (CommonConstants.VIEW_QUERY_FROM.PANEL.equals(request.getQueryFrom()) && CommonConstants.VIEW_RESULT_MODE.CUSTOM.equals(request.getResultMode())) {
                view.setResultMode(request.getResultMode());
                view.setResultCount(request.getResultCount());
            }
            // 数据来源在模板中直接从模板取数据
            if (CommonConstants.VIEW_DATA_FROM.TEMPLATE.equals(view.getDataFrom())) {
                return extendDataService.getChartDataInfo(id, view);
            } else {
                return calcData(view, request, request.isCache());
            }

        } catch (Exception e) {
            e.printStackTrace();
            DataEaseException.throwException(e);
        }
        return null;

    }

    public ChartViewDTO calcData(ChartViewDTO view, ChartExtRequest requestList, boolean cache) throws Exception {
        if (ObjectUtils.isEmpty(view)) {
            throw new RuntimeException(Translator.get("i18n_chart_delete"));
        }
        List<ChartViewFieldDTO> xAxis = new Gson().fromJson(view.getXAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType());
        if (StringUtils.equalsIgnoreCase(view.getType(), "table-pivot")) {
            List<ChartViewFieldDTO> xAxisExt = new Gson().fromJson(view.getXAxisExt(), new TypeToken<List<ChartViewFieldDTO>>() {
            }.getType());
            xAxis.addAll(xAxisExt);
        }
        List<ChartViewFieldDTO> yAxis = new Gson().fromJson(view.getYAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType());
        if (StringUtils.equalsIgnoreCase(view.getType(), "chart-mix")) {
            List<ChartViewFieldDTO> yAxisExt = new Gson().fromJson(view.getYAxisExt(), new TypeToken<List<ChartViewFieldDTO>>() {
            }.getType());
            yAxis.addAll(yAxisExt);
        }
        List<ChartViewFieldDTO> extStack = new Gson().fromJson(view.getExtStack(), new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType());
        List<ChartViewFieldDTO> extBubble = new Gson().fromJson(view.getExtBubble(), new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType());
        List<ChartFieldCustomFilterDTO> fieldCustomFilter = new Gson().fromJson(view.getCustomFilter(), new TypeToken<List<ChartFieldCustomFilterDTO>>() {
        }.getType());
        List<ChartViewFieldDTO> drill = new Gson().fromJson(view.getDrillFields(), new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType());


        DatasetTableField datasetTableFieldObj = DatasetTableField.builder().tableId(view.getTableId()).checked(Boolean.TRUE).build();
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableFieldObj);
        // 获取数据集,需校验权限
        DataSetTableDTO table = dataSetTableService.getWithPermission(view.getTableId(), requestList.getUser());
        checkPermission("use", table, requestList.getUser());

        //列权限
        List<String> desensitizationList = new ArrayList<>();
        List<DatasetTableField> columnPermissionFields = permissionService.filterColumnPermissons(fields, desensitizationList, table.getId(), requestList.getUser());
        //将没有权限的列删掉
        List<String> dataeaseNames = columnPermissionFields.stream().map(DatasetTableField::getDataeaseName).collect(Collectors.toList());
        dataeaseNames.add("*");
        fieldCustomFilter = fieldCustomFilter.stream().filter(item -> !desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
        extStack = extStack.stream().filter(item -> !desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
        extBubble = extBubble.stream().filter(item -> !desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
        drill = drill.stream().filter(item -> !desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());


        //行权限
        List<ChartFieldCustomFilterDTO> rowPermissionFields = permissionService.getCustomFilters(fields, table, requestList.getUser());
        fieldCustomFilter.addAll(rowPermissionFields);

        for (ChartFieldCustomFilterDTO ele : fieldCustomFilter) {
            ele.setField(dataSetTableFieldsService.get(ele.getId()));
        }

        if (CollectionUtils.isEmpty(xAxis) && CollectionUtils.isEmpty(yAxis)) {
            return emptyChartViewDTO(view);
        }

        switch (view.getType()) {
            case "label":
                xAxis = xAxis.stream().filter(item -> !desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                yAxis = new ArrayList<>();
                if (CollectionUtils.isEmpty(xAxis)) {
                    return emptyChartViewDTO(view);
                }
                break;
            case "text":
            case "gauge":
            case "liquid":
                xAxis = new ArrayList<>();
                yAxis = yAxis.stream().filter(item -> !desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(yAxis)) {
                    return emptyChartViewDTO(view);
                }
                break;
            case "table-info":
                yAxis = new ArrayList<>();
                xAxis = xAxis.stream().filter(item -> dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(xAxis)) {
                    return emptyChartViewDTO(view);
                }
                break;
            case "table-normal":
                xAxis = xAxis.stream().filter(item -> dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                yAxis = yAxis.stream().filter(item -> dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                break;
            default:
                xAxis = xAxis.stream().filter(item -> !desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                yAxis = yAxis.stream().filter(item -> !desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
        }

        // 过滤来自仪表板的条件
        List<ChartExtFilterRequest> extFilterList = new ArrayList<>();
        //组件过滤条件
        if (ObjectUtils.isNotEmpty(requestList.getFilter())) {
            for (ChartExtFilterRequest request : requestList.getFilter()) {
                // 解析多个fieldId,fieldId是一个逗号分隔的字符串
                String fieldId = request.getFieldId();
                if (StringUtils.isNotEmpty(fieldId)) {
                    String[] fieldIds = fieldId.split(",");
                    for (String fId : fieldIds) {
                        ChartExtFilterRequest filterRequest = new ChartExtFilterRequest();
                        BeanUtils.copyBean(filterRequest, request);
                        filterRequest.setFieldId(fId);

                        DatasetTableField datasetTableField = dataSetTableFieldsService.get(fId);
                        if (datasetTableField == null) {
                            continue;
                        }
                        if (!desensitizationList.contains(datasetTableField.getDataeaseName()) && dataeaseNames.contains(datasetTableField.getDataeaseName())) {
                            filterRequest.setDatasetTableField(datasetTableField);
                            if (StringUtils.equalsIgnoreCase(datasetTableField.getTableId(), view.getTableId())) {
                                if (CollectionUtils.isNotEmpty(filterRequest.getViewIds())) {
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

        List<ChartExtFilterRequest> filters = new ArrayList<>();
        // 联动条件
        if(ObjectUtils.isNotEmpty(requestList.getLinkageFilters())){
            filters.addAll(requestList.getLinkageFilters());
        }

        // 外部参数条件
        if(ObjectUtils.isNotEmpty(requestList.getOuterParamsFilters())){
            filters.addAll(requestList.getOuterParamsFilters());
        }

        //联动过滤条件和外部参数过滤条件全部加上
        if (ObjectUtils.isNotEmpty(filters)) {
            for (ChartExtFilterRequest request : filters) {
                DatasetTableField datasetTableField = dataSetTableFieldsService.get(request.getFieldId());
                if (!desensitizationList.contains(datasetTableField.getDataeaseName()) && dataeaseNames.contains(datasetTableField.getDataeaseName())) {
                    request.setDatasetTableField(datasetTableField);
                    if (StringUtils.equalsIgnoreCase(datasetTableField.getTableId(), view.getTableId())) {
                        if (CollectionUtils.isNotEmpty(request.getViewIds())) {
                            if (request.getViewIds().contains(view.getId())) {
                                extFilterList.add(request);
                            }
                        } else {
                            extFilterList.add(request);
                        }
                    }
                }
            }
        }

        // 下钻
        List<ChartExtFilterRequest> drillFilters = new ArrayList<>();
        boolean isDrill = false;
        List<ChartDrillRequest> drillRequest = requestList.getDrill();
        if (CollectionUtils.isNotEmpty(drillRequest) && (drill.size() > drillRequest.size())) {
            for (int i = 0; i < drillRequest.size(); i++) {
                ChartDrillRequest request = drillRequest.get(i);
                for (ChartDimensionDTO dto : request.getDimensionList()) {
                    ChartViewFieldDTO chartViewFieldDTO = drill.get(i);
                    // 将钻取值作为条件传递，将所有钻取字段作为xAxis并加上下一个钻取字段
                    if (StringUtils.equalsIgnoreCase(dto.getId(), chartViewFieldDTO.getId())) {
                        isDrill = true;
                        DatasetTableField datasetTableField = dataSetTableFieldsService.get(dto.getId());
                        ChartViewFieldDTO d = new ChartViewFieldDTO();
                        BeanUtils.copyBean(d, datasetTableField);

                        ChartExtFilterRequest drillFilter = new ChartExtFilterRequest();
                        drillFilter.setFieldId(dto.getId());
                        drillFilter.setValue(new ArrayList<String>() {{
                            add(dto.getValue());
                        }});
                        drillFilter.setOperator("in");
                        drillFilter.setDatasetTableField(datasetTableField);
                        extFilterList.add(drillFilter);

                        drillFilters.add(drillFilter);

                        if (!checkDrillExist(xAxis, extStack, d, view)) {
                            xAxis.add(d);
                        }
                        if (i == drillRequest.size() - 1) {
                            ChartViewFieldDTO nextDrillField = drill.get(i + 1);
                            if (!checkDrillExist(xAxis, extStack, nextDrillField, view)) {
                                xAxis.add(nextDrillField);
                            }
                        }
                    }
                }
            }
        }

        // 判断连接方式，直连或者定时抽取 table.mode
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        Datasource ds = table.getMode() == 0 ? datasourceService.get(table.getDataSourceId()) : engineService.getDeEngine();
        datasourceRequest.setDatasource(ds);
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        List<String[]> data = new ArrayList<>();


        // 如果是插件视图 走插件内部的逻辑
        if (ObjectUtils.isNotEmpty(view.getIsPlugin()) && view.getIsPlugin()) {
            Map<String, List<ChartViewFieldDTO>> fieldMap = new HashMap<>();
            fieldMap.put("xAxis",xAxis);
            fieldMap.put("yAxis",yAxis);
            fieldMap.put("extStack",extStack);
            fieldMap.put("extBubble",extBubble);
            PluginViewParam pluginViewParam = buildPluginParam(fieldMap, fieldCustomFilter, extFilterList, ds, table, view);
            String sql = pluginViewSql(pluginViewParam, view);
            if (StringUtils.isBlank(sql)) {
                return emptyChartViewDTO(view);
            }
            datasourceRequest.setQuery(sql);
            data = datasourceProvider.getData(datasourceRequest);

            Map<String, Object> mapChart = pluginViewResult(pluginViewParam, view, data, isDrill);
            Map<String, Object> mapTableNormal = ChartDataBuild.transTableNormal(xAxis, yAxis, view, data, extStack, desensitizationList);

            return uniteViewResult(datasourceRequest.getQuery(), mapChart, mapTableNormal,view, isDrill, drillFilters);
            // 如果是插件到此结束
        }

        //如果不是插件视图 走原生逻辑
        if (table.getMode() == 0) {// 直连
            // Datasource ds = datasourceService.get(table.getDataSourceId());
            if (ObjectUtils.isEmpty(ds)) {
                throw new RuntimeException(Translator.get("i18n_datasource_delete"));
            }
            if (StringUtils.isNotEmpty(ds.getStatus()) && ds.getStatus().equalsIgnoreCase("Error")) {
                throw new Exception(Translator.get("i18n_invalid_ds"));
            }
            // DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
            datasourceRequest.setDatasource(ds);
            DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(table.getInfo(), DataTableInfoDTO.class);
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            if (StringUtils.equalsIgnoreCase(table.getType(), "db")) {
                datasourceRequest.setTable(dataTableInfoDTO.getTable());
                if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType()) || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLSummary(dataTableInfoDTO.getTable(), yAxis, fieldCustomFilter, extFilterList, view, ds));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    datasourceRequest.setQuery(qp.getSQLStack(dataTableInfoDTO.getTable(), xAxis, yAxis, fieldCustomFilter, extFilterList, extStack, ds, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                    datasourceRequest.setQuery(qp.getSQLScatter(dataTableInfoDTO.getTable(), xAxis, yAxis, fieldCustomFilter, extFilterList, extBubble, ds, view));
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLTableInfo(dataTableInfoDTO.getTable(), xAxis, fieldCustomFilter, extFilterList, ds, view));
                } else {
                    datasourceRequest.setQuery(qp.getSQL(dataTableInfoDTO.getTable(), xAxis, yAxis, fieldCustomFilter, extFilterList, ds, view));
                }
            } else if (StringUtils.equalsIgnoreCase(table.getType(), "sql")) {
                if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType()) || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLSummaryAsTmp(dataTableInfoDTO.getSql(), yAxis, fieldCustomFilter, extFilterList, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpStack(dataTableInfoDTO.getSql(), xAxis, yAxis, fieldCustomFilter, extFilterList, extStack, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpScatter(dataTableInfoDTO.getSql(), xAxis, yAxis, fieldCustomFilter, extFilterList, extBubble, view));
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpTableInfo(dataTableInfoDTO.getSql(), xAxis, fieldCustomFilter, extFilterList, ds, view));
                } else {
                    datasourceRequest.setQuery(qp.getSQLAsTmp(dataTableInfoDTO.getSql(), xAxis, yAxis, fieldCustomFilter, extFilterList, view));
                }
            } else if (StringUtils.equalsIgnoreCase(table.getType(), "custom")) {
                DataTableInfoDTO dt = new Gson().fromJson(table.getInfo(), DataTableInfoDTO.class);
                List<DataSetTableUnionDTO> list = dataSetTableUnionService.listByTableId(dt.getList().get(0).getTableId());
                String sql = dataSetTableService.getCustomSQLDatasource(dt, list, ds);
                if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType()) || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLSummaryAsTmp(sql, yAxis, fieldCustomFilter, extFilterList, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpStack(sql, xAxis, yAxis, fieldCustomFilter, extFilterList, extStack, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpScatter(sql, xAxis, yAxis, fieldCustomFilter, extFilterList, extBubble, view));
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpTableInfo(sql, xAxis, fieldCustomFilter, extFilterList, ds, view));
                } else {
                    datasourceRequest.setQuery(qp.getSQLAsTmp(sql, xAxis, yAxis, fieldCustomFilter, extFilterList, view));
                }
            } else if (StringUtils.equalsIgnoreCase(table.getType(), "union")) {
                DataTableInfoDTO dt = new Gson().fromJson(table.getInfo(), DataTableInfoDTO.class);
                Map<String, Object> sqlMap = dataSetTableService.getUnionSQLDatasource(dt, ds);
                String sql = (String) sqlMap.get("sql");

                if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType()) || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLSummaryAsTmp(sql, yAxis, fieldCustomFilter, extFilterList, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpStack(sql, xAxis, yAxis, fieldCustomFilter, extFilterList, extStack, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpScatter(sql, xAxis, yAxis, fieldCustomFilter, extFilterList, extBubble, view));
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpTableInfo(sql, xAxis, fieldCustomFilter, extFilterList, ds, view));
                } else {
                    datasourceRequest.setQuery(qp.getSQLAsTmp(sql, xAxis, yAxis, fieldCustomFilter, extFilterList, view));
                }
            }
            data = datasourceProvider.getData(datasourceRequest);
        } else if (table.getMode() == 1) {// 抽取
            // 连接doris，构建doris数据源查询
            // Datasource ds = engineService.getDeEngine();
            // DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
            datasourceRequest.setDatasource(ds);
            String tableName = "ds_" + table.getId().replaceAll("-", "_");
            datasourceRequest.setTable(tableName);
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType()) || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                datasourceRequest.setQuery(qp.getSQLSummary(tableName, yAxis, fieldCustomFilter, extFilterList, view, ds));
            } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                datasourceRequest.setQuery(qp.getSQLStack(tableName, xAxis, yAxis, fieldCustomFilter, extFilterList, extStack, ds, view));
            } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                datasourceRequest.setQuery(qp.getSQLScatter(tableName, xAxis, yAxis, fieldCustomFilter, extFilterList, extBubble, ds, view));
            } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                datasourceRequest.setQuery(qp.getSQLTableInfo(tableName, xAxis, fieldCustomFilter, extFilterList, ds, view));
            } else {
                datasourceRequest.setQuery(qp.getSQL(tableName, xAxis, yAxis, fieldCustomFilter, extFilterList, ds, view));
            }
            // 仪表板有参数不使用缓存
            if (!cache || CollectionUtils.isNotEmpty(requestList.getFilter())
                    || CollectionUtils.isNotEmpty(requestList.getLinkageFilters())
                    || CollectionUtils.isNotEmpty(requestList.getDrill()) || CollectionUtils.isNotEmpty(rowPermissionFields) || fields.size() != columnPermissionFields.size()) {
                data = datasourceProvider.getData(datasourceRequest);
            } else {
                try {
                    data = cacheViewData(datasourceProvider, datasourceRequest, view.getId());
                } catch (Exception e) {
                    LogUtil.error(e);
                } finally {
                    // 如果当前对象被锁 且 当前线程冲入次数 > 0 则释放锁
                    if (lock.isLocked() && lock.getHoldCount() > 0) {
                        lock.unlock();
                    }
                }
            }
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
                String compareFieldId = compareCalc.getField();// 选中字段
                String resultData = compareCalc.getResultData();// 数据设置
                // 获取选中字段以及下标
                List<ChartViewFieldDTO> checkedField = new ArrayList<>(xAxis);
                if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    checkedField.addAll(extStack);
                }
                int timeIndex = 0;// 时间字段下标
                ChartViewFieldDTO timeField = null;
                for (int j = 0; j < checkedField.size(); j++) {
                    if (StringUtils.equalsIgnoreCase(checkedField.get(j).getId(), compareFieldId)) {
                        timeIndex = j;
                        timeField = checkedField.get(j);
                    }
                }
                // 计算指标对应的下标
                int dataIndex = 0;// 数据字段下标
                if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    dataIndex = xAxis.size() + extStack.size() + i;
                } else {
                    dataIndex = xAxis.size() + i;
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
                                            .divide(new BigDecimal(lastValue), 2, RoundingMode.HALF_UP)
                                            .subtract(new BigDecimal(1))
                                            .setScale(2, RoundingMode.HALF_UP)
                                            .toString();
                                }
                            }
                        }
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
            } else if (StringUtils.containsIgnoreCase(view.getType(), "text")
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
            if (StringUtils.containsIgnoreCase(view.getType(), "bar-stack")) {
                mapChart = ChartDataBuild.transStackChartDataAntV(xAxis, yAxis, view, data, extStack, isDrill);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "line-stack")) {
                mapChart = ChartDataBuild.transStackChartDataAntV(xAxis, yAxis, view, data, extStack, isDrill);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                mapChart = ChartDataBuild.transScatterDataAntV(xAxis, yAxis, view, data, extBubble, isDrill);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "radar")) {
                mapChart = ChartDataBuild.transRadarChartDataAntV(xAxis, yAxis, view, data, isDrill);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "text")
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
        }
        // table组件，明细表，也用于导出数据
        Map<String, Object> mapTableNormal = ChartDataBuild.transTableNormal(xAxis, yAxis, view, data, extStack, desensitizationList);
        return uniteViewResult(datasourceRequest.getQuery(), mapChart, mapTableNormal,view, isDrill, drillFilters);
    }

    public ChartViewDTO uniteViewResult(String sql, Map<String, Object> chartData, Map<String, Object> tabelData, ChartViewDTO view, Boolean isDrill, List<ChartExtFilterRequest> drillFilters) {

        Map<String, Object> map = new HashMap<>();
        map.putAll(chartData);
        map.putAll(tabelData);

        List<DatasetTableField> sourceFields = dataSetTableFieldsService.getFieldsByTableId(view.getTableId());
        map.put("sourceFields", sourceFields);

        ChartViewDTO dto = new ChartViewDTO();
        BeanUtils.copyBean(dto, view);
        dto.setData(map);
        dto.setSql(sql);
        dto.setDrill(isDrill);
        dto.setDrillFilters(drillFilters);
        return dto;
    }

    private PluginViewParam buildPluginParam(Map<String, List<ChartViewFieldDTO>> fieldMap, List<ChartFieldCustomFilterDTO> customFilters, List<ChartExtFilterRequest> extFilters, Datasource ds, DatasetTable table, ChartViewDTO view) {
        PluginViewParam pluginViewParam = new PluginViewParam();
        PluginViewSet pluginViewSet = BeanUtils.copyBean(new PluginViewSet(), table);
        pluginViewSet.setDsType(ds.getType());
        pluginViewSet.setTabelId(table.getId());
        PluginViewLimit pluginViewLimit = BeanUtils.copyBean(new PluginViewLimit(), view);




        List<PluginChartFieldCustomFilter> fieldFilters = customFilters.stream().map(filter -> gson.fromJson(gson.toJson(filter), PluginChartFieldCustomFilter.class)).collect(Collectors.toList());
        List<PluginChartExtFilter> panelFilters = extFilters.stream().map(filter -> gson.fromJson(gson.toJson(filter), PluginChartExtFilter.class)).collect(Collectors.toList());

        List<PluginViewField> pluginViewFields = fieldMap.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(field -> {
            PluginViewField pluginViewField = BeanUtils.copyBean(new PluginViewField(), field);
            pluginViewField.setTypeField(entry.getKey());
            return pluginViewField;
        })).collect(Collectors.toList());
        pluginViewParam.setPluginViewSet(pluginViewSet);
        pluginViewParam.setPluginViewFields(pluginViewFields);
        pluginViewParam.setPluginChartFieldCustomFilters(fieldFilters);
        pluginViewParam.setPluginChartExtFilters(panelFilters);
        pluginViewParam.setPluginViewLimit(pluginViewLimit);
        // pluginViewParam.setUserId(AuthUtils.getUser().getUserId());
        return pluginViewParam;
    }

    private ViewPluginService getPluginService(String viewType) {
        Map<String, ViewPluginService> beanMap = SpringContextUtil.getApplicationContext().getBeansOfType(ViewPluginService.class);

        if (beanMap.keySet().size() == 0) {
            DEException.throwException("没有此插件");

        }
        ViewPluginService viewPluginService = null;
        for (Map.Entry<String, ViewPluginService> entry : beanMap.entrySet()) {
            if (StringUtils.equals(entry.getValue().viewType().getValue(), viewType)) {
                viewPluginService = entry.getValue();
                return viewPluginService;
            }
        }
        if (null == viewPluginService) DEException.throwException("没有此插件");
        return viewPluginService;
    }

    private String pluginViewSql(PluginViewParam param, ChartViewDTO view) {
        ViewPluginService viewPluginService = getPluginService(view.getType());
        String sql = viewPluginService.generateSQL(param);
        return sql;
    }

    private Map<String, Object> pluginViewResult(PluginViewParam param, ChartViewDTO view, List<String[]> args, Boolean isDrill) {
        ViewPluginService viewPluginService = getPluginService(view.getType());
        Map<String, Object> result = viewPluginService.formatResult(param, args, isDrill);
        return result;
    }

    private ChartViewDTO emptyChartViewDTO(ChartViewDTO view) {
        ChartViewDTO dto = new ChartViewDTO();
        BeanUtils.copyBean(dto, view);
        return dto;
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

    private boolean checkDrillExist(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> extStack, ChartViewFieldDTO dto, ChartViewWithBLOBs view) {
        if (CollectionUtils.isNotEmpty(xAxis)) {
            for (ChartViewFieldDTO x : xAxis) {
                if (StringUtils.equalsIgnoreCase(x.getId(), dto.getId())) {
                    return true;
                }
            }
        }
        if (StringUtils.containsIgnoreCase(view.getType(), "stack") && CollectionUtils.isNotEmpty(extStack)) {
            for (ChartViewFieldDTO x : extStack) {
                if (StringUtils.equalsIgnoreCase(x.getId(), dto.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 避免缓存击穿
     * 虽然流量不一定能够达到击穿的水平
     *
     * @param datasourceProvider
     * @param datasourceRequest
     * @param viewId
     * @return
     * @throws Exception
     */
    public List<String[]> cacheViewData(DatasourceProvider datasourceProvider, DatasourceRequest datasourceRequest, String viewId) throws Exception {
        List<String[]> result;
        Object cache = CacheUtils.get(JdbcConstants.VIEW_CACHE_KEY, viewId);
        if (cache == null) {
            if (lock.tryLock()) {// 获取锁成功
                try {
                    result = datasourceProvider.getData(datasourceRequest);
                    if (result != null) {
                        CacheUtils.put(JdbcConstants.VIEW_CACHE_KEY, viewId, result, null, null);
                    }
                } catch (Exception e) {
                    LogUtil.error(e);
                    throw e;
                } finally {
                    lock.unlock();
                }
            } else {//获取锁失败
                Thread.sleep(100);//避免CAS自旋频率过大 占用cpu资源过高
                result = cacheViewData(datasourceProvider, datasourceRequest, viewId);
            }
        } else {
            result = (List<String[]>) cache;
        }
        return result;
    }

    private void checkName(ChartViewWithBLOBs chartView) {
        ChartViewExample chartViewExample = new ChartViewExample();
        ChartViewExample.Criteria criteria = chartViewExample.createCriteria();
        if (StringUtils.isNotEmpty(chartView.getId())) {
            criteria.andIdNotEqualTo(chartView.getId());
        }
        if (StringUtils.isNotEmpty(chartView.getSceneId())) {
            criteria.andSceneIdEqualTo(chartView.getSceneId());
        }
        if (StringUtils.isNotEmpty(chartView.getName())) {
            criteria.andNameEqualTo(chartView.getName());
        }
    }

    public ChartDetail getChartDetail(String id) {
        ChartDetail chartDetail = new ChartDetail();
        ChartViewWithBLOBs chartViewWithBLOBs = chartViewMapper.selectByPrimaryKey(id);
        chartDetail.setChart(chartViewWithBLOBs);
        if (ObjectUtils.isNotEmpty(chartViewWithBLOBs)) {
            DataSetDetail datasetDetail = dataSetTableService.getDatasetDetail(chartViewWithBLOBs.getTableId());
            chartDetail.setTable(datasetDetail.getTable());
            chartDetail.setDatasource(datasetDetail.getDatasource());
        }
        return chartDetail;
    }

    public List<ChartView> viewsByIds(List<String> viewIds) {
        ChartViewExample example = new ChartViewExample();
        example.createCriteria().andIdIn(viewIds);
        return chartViewMapper.selectByExample(example);
    }

    public ChartViewWithBLOBs findOne(String id) {
        return chartViewMapper.selectByPrimaryKey(id);
    }

    public String chartCopy(String id, String panelId) {
        String newChartId = UUID.randomUUID().toString();
        extChartViewMapper.chartCopy(newChartId, id, panelId);
        extChartViewMapper.copyCache(id,newChartId);
        extPanelGroupExtendDataMapper.copyExtendData(id,newChartId,panelId);
        chartViewCacheService.refreshCache(newChartId);
        return newChartId;
    }

    public String searchAdviceSceneId(String panelId) {
        return extChartViewMapper.searchAdviceSceneId(AuthUtils.getUser().getUserId().toString(), panelId);
    }

    public String checkSameDataSet(String viewIdSource, String viewIdTarget) {
        if (extChartViewMapper.checkSameDataSet(viewIdSource, viewIdTarget) == 1) {
            return "YES";
        } else {
            return "NO";
        }
    }

    // check permission
    private void checkPermission(String needPermission, DataSetTableDTO table, Long userId) {
        if (ObjectUtils.isEmpty(table)) {
            throw new RuntimeException(Translator.get("i18n_dataset_delete"));
        }
        SysUserEntity user = AuthUtils.getUser();
        user = userId != null ? authUserService.getUserById(userId) : user;
        if (!user.getIsAdmin()) {
            if (ObjectUtils.isEmpty(table.getPrivileges()) || !table.getPrivileges().contains(needPermission)) {
                throw new RuntimeException(Translator.get("i18n_dataset_no_permission"));
            }
        }
    }

    public void initViewCache(String panelId) {
        extChartViewMapper.deleteCacheWithPanel(null,panelId);
    }

}

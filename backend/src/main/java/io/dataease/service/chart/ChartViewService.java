package io.dataease.service.chart;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.service.AuthUserService;
import io.dataease.commons.constants.CommonConstants;
import io.dataease.commons.constants.JdbcConstants;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.model.PluginViewSetImpl;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.controller.request.chart.*;
import io.dataease.controller.response.ChartDetail;
import io.dataease.controller.response.DataSetDetail;
import io.dataease.dto.chart.*;
import io.dataease.dto.dataset.DataSetTableDTO;
import io.dataease.dto.dataset.DataSetTableUnionDTO;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.exception.DataEaseException;
import io.dataease.ext.ExtChartGroupMapper;
import io.dataease.ext.ExtChartViewMapper;
import io.dataease.ext.ExtPanelGroupExtendDataMapper;
import io.dataease.i18n.Translator;
import io.dataease.listener.util.CacheUtils;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.ChartViewCacheMapper;
import io.dataease.plugins.common.base.mapper.ChartViewMapper;
import io.dataease.plugins.common.base.mapper.DatasetTableFieldMapper;
import io.dataease.plugins.common.base.mapper.PanelViewMapper;
import io.dataease.plugins.common.constants.DatasetType;
import io.dataease.plugins.common.constants.datasource.SQLConstants;
import io.dataease.plugins.common.dto.chart.ChartFieldCompareDTO;
import io.dataease.plugins.common.dto.chart.ChartFieldCustomFilterDTO;
import io.dataease.plugins.common.dto.chart.ChartViewFieldDTO;
import io.dataease.plugins.common.dto.dataset.SqlVariableDetails;
import io.dataease.plugins.common.request.chart.ChartExtFilterRequest;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.common.request.permission.DataSetRowPermissionsTreeDTO;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.datasource.provider.Provider;
import io.dataease.plugins.datasource.query.QueryProvider;
import io.dataease.plugins.view.entity.*;
import io.dataease.plugins.view.service.ViewPluginService;
import io.dataease.provider.ProviderFactory;
import io.dataease.service.chart.util.ChartDataBuild;
import io.dataease.service.dataset.*;
import io.dataease.service.datasource.DatasourceService;
import io.dataease.service.engine.EngineService;
import io.dataease.service.panel.PanelGroupExtendDataService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.pentaho.di.core.util.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Type;
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
    @Resource
    private ChartViewFieldService chartViewFieldService;
    @Resource
    private PermissionsTreeService permissionsTreeService;
    @Resource
    private DatasetTableFieldMapper datasetTableFieldMapper;

    private static final Logger logger = LoggerFactory.getLogger(ChartViewService.class);


    //默认使用非公平
    private ReentrantLock lock = new ReentrantLock();

    // 直接保存统一到缓存表
    public ChartViewDTO save(ChartViewRequest chartView) {
        long timestamp = System.currentTimeMillis();
        chartView.setUpdateTime(timestamp);
        chartViewMapper.updateByPrimaryKeySelective(chartView);
        Optional.ofNullable(chartView.getId()).ifPresent(id -> {
            CacheUtils.remove(JdbcConstants.VIEW_CACHE_KEY, id);
        });
        return getOne(chartView.getId(), "panel_edit");
    }

    public String checkTitle(ChartViewCacheRequest chartView) {
        ChartViewCacheExample example = new ChartViewCacheExample();
        example.createCriteria().andTitleEqualTo(chartView.getTitle()).andSceneIdEqualTo(chartView.getSceneId()).andIdNotEqualTo(chartView.getId());
        List<ChartViewCache> result = chartViewCacheMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(result)) {
            return "fail";
        } else {
            return "success";
        }
    }

    public ChartViewWithBLOBs newOne(ChartViewWithBLOBs chartView) {
        if (StringUtils.isBlank(chartView.getViewFields())) {
            chartView.setViewFields("[]");
        }
        long timestamp = System.currentTimeMillis();
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


    /**
     * @param chartView
     * @Description 保存编辑的视图信息
     */
    public void viewEditSave(ChartViewWithBLOBs chartView) {
        long timestamp = System.currentTimeMillis();
        chartView.setUpdateTime(timestamp);
        chartViewMapper.updateByPrimaryKeySelective(chartView);
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


    public ChartViewDTO getOne(String id, String queryFrom) {
        try {
            ChartViewDTO result = extChartViewMapper.searchOne(id);
            if (result == null) {
                DataEaseException.throwException(Translator.get("i18n_chart_delete"));
            }
            return result;
        } catch (Exception e) {
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
            if (CommonConstants.VIEW_RESULT_MODE.CUSTOM.equals(request.getResultMode())) {
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

    public List<String[]> sqlData(ChartViewDTO view, ChartExtRequest requestList, boolean cache, String fieldId) throws Exception {
        if (ObjectUtils.isEmpty(view)) {
            throw new RuntimeException(Translator.get("i18n_chart_delete"));
        }
        List<ChartViewFieldDTO> xAxis = gson.fromJson(view.getXAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType());
        if (StringUtils.equalsIgnoreCase(view.getType(), "table-pivot")) {
            List<ChartViewFieldDTO> xAxisExt = gson.fromJson(view.getXAxisExt(), new TypeToken<List<ChartViewFieldDTO>>() {
            }.getType());
            xAxis.addAll(xAxisExt);
        }
        List<ChartViewFieldDTO> yAxis = gson.fromJson(view.getYAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType());
        if (StringUtils.equalsIgnoreCase(view.getType(), "chart-mix")) {
            List<ChartViewFieldDTO> yAxisExt = gson.fromJson(view.getYAxisExt(), new TypeToken<List<ChartViewFieldDTO>>() {
            }.getType());
            yAxis.addAll(yAxisExt);
        }
        List<ChartViewFieldDTO> extStack = gson.fromJson(view.getExtStack(), new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType());
        List<ChartViewFieldDTO> extBubble = gson.fromJson(view.getExtBubble(), new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType());
        List<ChartFieldCustomFilterDTO> fieldCustomFilter = new ArrayList<ChartFieldCustomFilterDTO>();
        List<ChartViewFieldDTO> drill = new ArrayList<ChartViewFieldDTO>();


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
        List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = permissionsTreeService.getRowPermissionsTree(fields, table, requestList.getUser());

        for (ChartFieldCustomFilterDTO ele : fieldCustomFilter) {
            ele.setField(dataSetTableFieldsService.get(ele.getId()));
        }

        if (CollectionUtils.isEmpty(xAxis) && CollectionUtils.isEmpty(yAxis)) {
            return new ArrayList<String[]>();
        }

        switch (view.getType()) {
            case "label":
                xAxis = xAxis.stream().filter(item -> !desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                yAxis = new ArrayList<>();
                if (CollectionUtils.isEmpty(xAxis)) {
                    return new ArrayList<String[]>();
                }
                break;
            case "text":
            case "gauge":
            case "liquid":
                xAxis = new ArrayList<>();
                yAxis = yAxis.stream().filter(item -> !desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(yAxis)) {
                    return new ArrayList<String[]>();
                }
                break;
            case "table-info":
                yAxis = new ArrayList<>();
                xAxis = xAxis.stream().filter(item -> dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(xAxis)) {
                    return new ArrayList<String[]>();
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

        List<ChartExtFilterRequest> extFilterList = new ArrayList<>();
        List<ChartExtFilterRequest> filters = new ArrayList<>();
        List<ChartExtFilterRequest> drillFilters = new ArrayList<>();
        boolean isDrill = false;

        // 判断连接方式，直连或者定时抽取 table.mode
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        Datasource ds = table.getMode() == 0 ? datasourceService.get(table.getDataSourceId()) : engineService.getDeEngine();
        datasourceRequest.setDatasource(ds);
        Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        List<String[]> data = new ArrayList<>();


        // 如果是插件视图 走插件内部的逻辑
        if (ObjectUtils.isNotEmpty(view.getIsPlugin()) && view.getIsPlugin()) {
            Map<String, List<ChartViewFieldDTO>> fieldMap = new HashMap<>();
            List<ChartViewFieldDTO> xAxisExt = gson.fromJson(view.getXAxisExt(), new TypeToken<List<ChartViewFieldDTO>>() {
            }.getType());
            fieldMap.put("xAxisExt", xAxisExt);
            fieldMap.put("xAxis", xAxis);
            fieldMap.put("yAxis", yAxis);
            fieldMap.put("extStack", extStack);
            fieldMap.put("extBubble", extBubble);
            PluginViewParam pluginViewParam = buildPluginParam(fieldMap, fieldCustomFilter, extFilterList, ds, table, view);
            String sql = pluginViewSql(pluginViewParam, view);
            if (StringUtils.isBlank(sql)) {
                return new ArrayList<String[]>();
            }
            datasourceRequest.setQuery(sql);
            data = datasourceProvider.getData(datasourceRequest);
            return data;
            // 如果是插件到此结束
        }

        //如果不是插件视图 走原生逻辑
        if (table.getMode() == 0) {// 直连
            if (ObjectUtils.isEmpty(ds)) {
                throw new RuntimeException(Translator.get("i18n_datasource_delete"));
            }
            if (StringUtils.isNotEmpty(ds.getStatus()) && ds.getStatus().equalsIgnoreCase("Error")) {
                throw new Exception(Translator.get("i18n_invalid_ds"));
            }
            datasourceRequest.setDatasource(ds);
            DataTableInfoDTO dataTableInfoDTO = gson.fromJson(table.getInfo(), DataTableInfoDTO.class);
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            if (StringUtils.equalsIgnoreCase(table.getType(), DatasetType.DB.name())) {
                datasourceRequest.setTable(dataTableInfoDTO.getTable());
                if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType()) || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLSummary(dataTableInfoDTO.getTable(), yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view, ds));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    datasourceRequest.setQuery(qp.getSQLStack(dataTableInfoDTO.getTable(), xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, ds, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                    datasourceRequest.setQuery(qp.getSQLScatter(dataTableInfoDTO.getTable(), xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, ds, view));
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLTableInfo(dataTableInfoDTO.getTable(), xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
                } else {
                    datasourceRequest.setQuery(qp.getSQL(dataTableInfoDTO.getTable(), xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
                }
            } else if (StringUtils.equalsIgnoreCase(table.getType(), DatasetType.SQL.name())) {
                String sql = dataTableInfoDTO.isBase64Encryption() ? new String(java.util.Base64.getDecoder().decode(dataTableInfoDTO.getSql())) : dataTableInfoDTO.getSql();
                sql = handleVariable(sql, requestList, qp, table, ds);
                if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType()) || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLSummaryAsTmp(sql, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpStack(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpScatter(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, view));
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
                } else {
                    datasourceRequest.setQuery(qp.getSQLAsTmp(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view));
                }
            } else if (StringUtils.equalsIgnoreCase(table.getType(), DatasetType.CUSTOM.name())) {
                DataTableInfoDTO dt = gson.fromJson(table.getInfo(), DataTableInfoDTO.class);
                List<DataSetTableUnionDTO> list = dataSetTableUnionService.listByTableId(dt.getList().get(0).getTableId());
                String sql = dataSetTableService.getCustomSQLDatasource(dt, list, ds);
                if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType()) || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLSummaryAsTmp(sql, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpStack(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpScatter(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, view));
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
                } else {
                    datasourceRequest.setQuery(qp.getSQLAsTmp(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view));
                }
            } else if (StringUtils.equalsIgnoreCase(table.getType(), DatasetType.UNION.name())) {
                DataTableInfoDTO dt = gson.fromJson(table.getInfo(), DataTableInfoDTO.class);
                Map<String, Object> sqlMap = dataSetTableService.getUnionSQLDatasource(dt, ds);
                String sql = (String) sqlMap.get("sql");

                if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType()) || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLSummaryAsTmp(sql, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpStack(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpScatter(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, view));
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
                } else {
                    datasourceRequest.setQuery(qp.getSQLAsTmp(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view));
                }
            }
            data = datasourceProvider.getData(datasourceRequest);
        } else if (table.getMode() == 1) {// 抽取
            datasourceRequest.setDatasource(ds);
            String tableName = "ds_" + table.getId().replaceAll("-", "_");
            datasourceRequest.setTable(tableName);
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType()) || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                datasourceRequest.setQuery(qp.getSQLSummary(tableName, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view, ds));
            } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                datasourceRequest.setQuery(qp.getSQLStack(tableName, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, ds, view));
            } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                datasourceRequest.setQuery(qp.getSQLScatter(tableName, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, ds, view));
            } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                datasourceRequest.setQuery(qp.getSQLTableInfo(tableName, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
            } else {
                datasourceRequest.setQuery(qp.getSQL(tableName, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
            }
            // 仪表板有参数不使用缓存
            if (!cache || CollectionUtils.isNotEmpty(requestList.getFilter())
                    || CollectionUtils.isNotEmpty(requestList.getLinkageFilters())
                    || CollectionUtils.isNotEmpty(requestList.getOuterParamsFilters())
                    || CollectionUtils.isNotEmpty(requestList.getDrill()) || CollectionUtils.isNotEmpty(rowPermissionsTree) || fields.size() != columnPermissionFields.size()) {
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
        return data;
    }

    public ChartViewDTO calcData(ChartViewDTO view, ChartExtRequest requestList, boolean cache) throws Exception {
        if (ObjectUtils.isEmpty(view)) {
            throw new RuntimeException(Translator.get("i18n_chart_delete"));
        }
        Type tokenType = new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType();
        Type filterTokenType = new TypeToken<List<ChartFieldCustomFilterDTO>>() {
        }.getType();

        List<ChartViewFieldDTO> viewFields = gson.fromJson(view.getViewFields(), tokenType);
        final Map<String, List<ChartViewFieldDTO>> extFieldsMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(viewFields)) {
            viewFields.forEach(field -> {
                String busiType = field.getBusiType();
                List<ChartViewFieldDTO> list = extFieldsMap.containsKey(busiType) ? extFieldsMap.get(busiType) : new ArrayList<>();
                list.add(field);
                extFieldsMap.put(field.getBusiType(), list);
            });
        }


        List<ChartViewFieldDTO> xAxisBase = gson.fromJson(view.getXAxis(), tokenType);
        List<ChartViewFieldDTO> xAxis = gson.fromJson(view.getXAxis(), tokenType);
        List<ChartViewFieldDTO> xAxisExt = gson.fromJson(view.getXAxisExt(), tokenType);
        if (StringUtils.equalsIgnoreCase(view.getType(), "table-pivot") || StringUtils.containsIgnoreCase(view.getType(), "group")) {
            xAxis.addAll(xAxisExt);
        }
        List<ChartViewFieldDTO> yAxis = gson.fromJson(view.getYAxis(), tokenType);
        if (StringUtils.equalsIgnoreCase(view.getType(), "chart-mix")) {
            List<ChartViewFieldDTO> yAxisExt = gson.fromJson(view.getYAxisExt(), tokenType);
            yAxis.addAll(yAxisExt);
        }
        if (StringUtils.equalsIgnoreCase(view.getRender(), "antv") && StringUtils.equalsIgnoreCase(view.getType(), "gauge")) {
            List<ChartViewFieldDTO> sizeField = getSizeField(view);
            yAxis.addAll(sizeField);
        }
        List<ChartViewFieldDTO> extStack = gson.fromJson(view.getExtStack(), tokenType);
        List<ChartViewFieldDTO> extBubble = gson.fromJson(view.getExtBubble(), tokenType);
        List<ChartFieldCustomFilterDTO> fieldCustomFilter = gson.fromJson(view.getCustomFilter(), filterTokenType);
        List<ChartViewFieldDTO> drill = gson.fromJson(view.getDrillFields(), tokenType);


        DatasetTableField datasetTableFieldObj = DatasetTableField.builder().tableId(view.getTableId()).checked(Boolean.TRUE).build();
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableFieldObj);
        // 获取数据集,需校验权限
        DataSetTableDTO table = dataSetTableService.getWithPermission(view.getTableId(), requestList.getUser());
        checkPermission("use", table, requestList.getUser());

        List<String> desensitizationList = new ArrayList<>();
        //列权限
        List<DatasetTableField> columnPermissionFields = permissionService.filterColumnPermissons(fields, desensitizationList, table.getId(), requestList.getUser());
        //将没有权限的列删掉
        List<String> dataeaseNames = columnPermissionFields.stream().map(DatasetTableField::getDataeaseName).collect(Collectors.toList());
        dataeaseNames.add("*");
        fieldCustomFilter = fieldCustomFilter.stream().filter(item -> StringUtils.isNotEmpty(item.getChartId()) || (!desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
        extStack = extStack.stream().filter(item -> StringUtils.isNotEmpty(item.getChartId()) || (!desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
        extBubble = extBubble.stream().filter(item -> StringUtils.isNotEmpty(item.getChartId()) || (!desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
        drill = drill.stream().filter(item -> StringUtils.isNotEmpty(item.getChartId()) || (!desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());


        //行权限
        List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = permissionsTreeService.getRowPermissionsTree(fields, table, requestList.getUser());

        for (ChartFieldCustomFilterDTO ele : fieldCustomFilter) {
            ele.setField(dataSetTableFieldsService.get(ele.getId()));
        }

        if (CollectionUtils.isEmpty(xAxis) && CollectionUtils.isEmpty(yAxis)) {
            return emptyChartViewDTO(view);
        }

        switch (view.getType()) {
            case "label":
                xAxis = xAxis.stream().filter(item -> StringUtils.isNotEmpty(item.getChartId()) || (!desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
                yAxis = new ArrayList<>();
                if (CollectionUtils.isEmpty(xAxis)) {
                    return emptyChartViewDTO(view);
                }
                break;
            case "text":
            case "gauge":
            case "liquid":
                xAxis = new ArrayList<>();
                yAxis = yAxis.stream().filter(item -> StringUtils.isNotEmpty(item.getChartId()) || (!desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(yAxis)) {
                    return emptyChartViewDTO(view);
                }
                break;
            case "table-info":
                yAxis = new ArrayList<>();
                xAxis = xAxis.stream().filter(item -> StringUtils.isNotEmpty(item.getChartId()) || dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(xAxis)) {
                    return emptyChartViewDTO(view);
                }
                break;
            case "table-normal":
                xAxis = xAxis.stream().filter(item -> StringUtils.isNotEmpty(item.getChartId()) || dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                yAxis = yAxis.stream().filter(item -> StringUtils.isNotEmpty(item.getChartId()) || dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                break;
            case "bar-group":
                xAxis = xAxis.stream().filter(item -> StringUtils.isNotEmpty(item.getChartId()) || (!desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
                yAxis = yAxis.stream().filter(item -> StringUtils.isNotEmpty(item.getChartId()) || (!desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
                xAxisBase = xAxisBase.stream().filter(item -> StringUtils.isNotEmpty(item.getChartId()) || (!desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
                xAxisExt = xAxisExt.stream().filter(item -> StringUtils.isNotEmpty(item.getChartId()) || (!desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
                break;
            default:
                xAxis = xAxis.stream().filter(item -> StringUtils.isNotEmpty(item.getChartId()) || (!desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
                yAxis = yAxis.stream().filter(item -> StringUtils.isNotEmpty(item.getChartId()) || (!desensitizationList.contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
        }

        // 过滤来自仪表板的条件
        List<ChartExtFilterRequest> extFilterList = new ArrayList<>();
        //组件过滤条件
        if (ObjectUtils.isNotEmpty(requestList.getFilter())) {
            for (ChartExtFilterRequest request : requestList.getFilter()) {
                // 解析多个fieldId,fieldId是一个逗号分隔的字符串
                String fieldId = request.getFieldId();
                if (request.getIsTree() == null) {
                    request.setIsTree(false);
                }
                boolean hasParameters = false;
                if (StringUtils.isNotEmpty(table.getSqlVariableDetails())) {
                    List<SqlVariableDetails> sqlVariables = new Gson().fromJson(table.getSqlVariableDetails(), new TypeToken<List<SqlVariableDetails>>() {}.getType());
                    for (String parameter : Optional.ofNullable(request.getParameters()).orElse(new ArrayList<>())) {
                        if (sqlVariables.stream().map(SqlVariableDetails::getVariableName).collect(Collectors.toList()).contains(parameter)) {
                            hasParameters = true;
                        }
                        if (parameter.contains("|DE|") && table.getId().equals(parameter.split("\\|DE\\|")[0]) && sqlVariables.stream().map(SqlVariableDetails::getVariableName).collect(Collectors.toList()).contains(parameter.split("\\|DE\\|")[1])) {
                            hasParameters = true;
                        }
                    }
                }

                if (hasParameters) {
                    continue;
                }
                if (StringUtils.isNotEmpty(fieldId)) {
                    String[] fieldIds = fieldId.split(",");
                    if (request.getIsTree()) {
                        ChartExtFilterRequest filterRequest = new ChartExtFilterRequest();
                        BeanUtils.copyBean(filterRequest, request);
                        filterRequest.setDatasetTableFieldList(new ArrayList<>());
                        for (String fId : fieldIds) {
                            DatasetTableField datasetTableField = dataSetTableFieldsService.get(fId);
                            if (datasetTableField == null) {
                                continue;
                            }
                            if (!desensitizationList.contains(datasetTableField.getDataeaseName()) && dataeaseNames.contains(datasetTableField.getDataeaseName())) {
                                if (StringUtils.equalsIgnoreCase(datasetTableField.getTableId(), view.getTableId())) {
                                    if (CollectionUtils.isNotEmpty(filterRequest.getViewIds())) {
                                        if (filterRequest.getViewIds().contains(view.getId())) {
                                            filterRequest.getDatasetTableFieldList().add(datasetTableField);
                                        }
                                    } else {
                                        filterRequest.getDatasetTableFieldList().add(datasetTableField);
                                    }
                                }
                            }
                        }
                        if (CollectionUtils.isNotEmpty(filterRequest.getDatasetTableFieldList())) {
                            extFilterList.add(filterRequest);
                        }
                    } else {
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
        }

        List<ChartExtFilterRequest> filters = new ArrayList<>();
        // 联动条件
        if (ObjectUtils.isNotEmpty(requestList.getLinkageFilters())) {
            filters.addAll(requestList.getLinkageFilters());
        }

        // 外部参数条件
        if (ObjectUtils.isNotEmpty(requestList.getOuterParamsFilters())) {
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
                                // get drill list first element's sort,then assign to nextDrillField
                                nextDrillField.setSort(getDrillSort(xAxis, drill.get(0)));
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
        Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        List<String[]> data = new ArrayList<>();

        // senior dynamic assist
        DatasourceRequest datasourceAssistRequest = new DatasourceRequest();
        datasourceAssistRequest.setDatasource(ds);
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

        // 如果是插件视图 走插件内部的逻辑
        if (ObjectUtils.isNotEmpty(view.getIsPlugin()) && view.getIsPlugin()) {
            Map<String, List<ChartViewFieldDTO>> fieldMap = ObjectUtils.isEmpty(extFieldsMap) ? new LinkedHashMap<>() : extFieldsMap;

            fieldMap.put("extStack", extStack);
            fieldMap.put("extBubble", extBubble);
            fieldMap.put("xAxis", xAxis);
            fieldMap.put("yAxis", yAxis);
            PluginViewParam pluginViewParam = buildPluginParam(fieldMap, fieldCustomFilter, extFilterList, ds, table, view);
            String sql = pluginViewSql(pluginViewParam, view);
            if (StringUtils.isBlank(sql)) {
                return emptyChartViewDTO(view);
            }
            datasourceRequest.setQuery(sql);
            data = datasourceProvider.getData(datasourceRequest);

            Map<String, Object> mapChart = pluginViewResult(pluginViewParam, view, data, isDrill);
            Map<String, Object> mapTableNormal = ChartDataBuild.transTableNormal(fieldMap, view, data, desensitizationList);

            return uniteViewResult(datasourceRequest.getQuery(), mapChart, mapTableNormal, view, isDrill, drillFilters, dynamicAssistFields, assistData);
            // 如果是插件到此结束
        }

        //如果不是插件视图 走原生逻辑
        if (table.getMode() == 0) {// 直连
            if (ObjectUtils.isEmpty(ds)) {
                throw new RuntimeException(Translator.get("i18n_datasource_delete"));
            }
            if (StringUtils.isNotEmpty(ds.getStatus()) && ds.getStatus().equalsIgnoreCase("Error")) {
                throw new Exception(Translator.get("i18n_invalid_ds"));
            }
            datasourceRequest.setDatasource(ds);
            DataTableInfoDTO dataTableInfoDTO = gson.fromJson(table.getInfo(), DataTableInfoDTO.class);
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            if (StringUtils.equalsIgnoreCase(table.getType(), DatasetType.DB.name())) {
                datasourceRequest.setTable(dataTableInfoDTO.getTable());
                if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType()) || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLSummary(dataTableInfoDTO.getTable(), yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view, ds));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    datasourceRequest.setQuery(qp.getSQLStack(dataTableInfoDTO.getTable(), xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, ds, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                    datasourceRequest.setQuery(qp.getSQLScatter(dataTableInfoDTO.getTable(), xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, ds, view));
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLTableInfo(dataTableInfoDTO.getTable(), xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
                } else {
                    datasourceRequest.setQuery(qp.getSQL(dataTableInfoDTO.getTable(), xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
                }
            } else if (StringUtils.equalsIgnoreCase(table.getType(), DatasetType.SQL.name())) {
                String sql = dataTableInfoDTO.isBase64Encryption() ? new String(java.util.Base64.getDecoder().decode(dataTableInfoDTO.getSql())) : dataTableInfoDTO.getSql();
                sql = handleVariable(sql, requestList, qp, table, ds);
                if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType()) || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLSummaryAsTmp(sql, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpStack(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpScatter(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, view));
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
                } else {
                    datasourceRequest.setQuery(qp.getSQLAsTmp(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view));
                }
            } else if (StringUtils.equalsIgnoreCase(table.getType(), DatasetType.CUSTOM.name())) {
                DataTableInfoDTO dt = gson.fromJson(table.getInfo(), DataTableInfoDTO.class);
                List<DataSetTableUnionDTO> list = dataSetTableUnionService.listByTableId(dt.getList().get(0).getTableId());
                String sql = dataSetTableService.getCustomSQLDatasource(dt, list, ds);
                if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType()) || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLSummaryAsTmp(sql, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpStack(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpScatter(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, view));
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
                } else {
                    datasourceRequest.setQuery(qp.getSQLAsTmp(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view));
                }
            } else if (StringUtils.equalsIgnoreCase(table.getType(), DatasetType.UNION.name())) {
                DataTableInfoDTO dt = gson.fromJson(table.getInfo(), DataTableInfoDTO.class);
                Map<String, Object> sqlMap = dataSetTableService.getUnionSQLDatasource(dt, ds);
                String sql = (String) sqlMap.get("sql");

                if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType()) || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLSummaryAsTmp(sql, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpStack(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, view));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpScatter(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, view));
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
                } else {
                    datasourceRequest.setQuery(qp.getSQLAsTmp(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view));
                }
            }
            data = datasourceProvider.getData(datasourceRequest);
            if (CollectionUtils.isNotEmpty(assistFields)) {
                datasourceAssistRequest.setQuery(assistSQL(datasourceRequest.getQuery(), assistFields));
                logger.info(datasourceAssistRequest.getQuery());
                assistData = datasourceProvider.getData(datasourceAssistRequest);
            }
        } else if (table.getMode() == 1) {// 抽取
            // 连接doris，构建doris数据源查询
            datasourceRequest.setDatasource(ds);
            String tableName = "ds_" + table.getId().replaceAll("-", "_");
            datasourceRequest.setTable(tableName);
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType()) || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                datasourceRequest.setQuery(qp.getSQLSummary(tableName, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view, ds));
            } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                datasourceRequest.setQuery(qp.getSQLStack(tableName, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, ds, view));
            } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                datasourceRequest.setQuery(qp.getSQLScatter(tableName, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, ds, view));
            } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                datasourceRequest.setQuery(qp.getSQLTableInfo(tableName, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
            } else {
                datasourceRequest.setQuery(qp.getSQL(tableName, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
            }
            if (CollectionUtils.isNotEmpty(assistFields)) {
                datasourceAssistRequest.setQuery(assistSQL(datasourceRequest.getQuery(), assistFields));
                logger.info(datasourceAssistRequest.getQuery());
                assistData = datasourceProvider.getData(datasourceAssistRequest);
            }
            // 仪表板有参数不使用缓存
            if (!cache || CollectionUtils.isNotEmpty(requestList.getFilter())
                    || CollectionUtils.isNotEmpty(requestList.getLinkageFilters())
                    || CollectionUtils.isNotEmpty(requestList.getOuterParamsFilters())
                    || CollectionUtils.isNotEmpty(requestList.getDrill()) || CollectionUtils.isNotEmpty(rowPermissionsTree) || fields.size() != columnPermissionFields.size()) {
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
                                            .divide(new BigDecimal(lastValue), 8, RoundingMode.HALF_UP)
                                            .subtract(new BigDecimal(1))
                                            .setScale(8, RoundingMode.HALF_UP)
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
            if (StringUtils.equalsIgnoreCase(view.getType(), "bar-group")) {
                mapChart = ChartDataBuild.transBaseGroupDataAntV(xAxisBase, xAxis, xAxisExt, yAxis, view, data, isDrill);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "bar-stack")) {
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
        return uniteViewResult(datasourceRequest.getQuery(), mapChart, mapTableNormal, view, isDrill, drillFilters, dynamicAssistFields, assistData);
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
                            if (CollectionUtils.isEmpty(map.get(stringBuilder.toString()))) {
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

    public ChartViewDTO uniteViewResult(String sql, Map<String, Object> chartData, Map<String, Object> tabelData, ChartViewDTO view, Boolean isDrill, List<ChartExtFilterRequest> drillFilters, List<ChartSeniorAssistDTO> dynamicAssistFields, List<String[]> assistData) {

        Map<String, Object> map = new HashMap<>();
        map.putAll(chartData);
        map.putAll(tabelData);

        List<DatasetTableField> sourceFields = dataSetTableFieldsService.getFieldsByTableId(view.getTableId());
        map.put("sourceFields", sourceFields);
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

    private PluginViewParam buildPluginParam(Map<String, List<ChartViewFieldDTO>> fieldMap, List<ChartFieldCustomFilterDTO> customFilters, List<ChartExtFilterRequest> extFilters, Datasource ds, DatasetTable table, ChartViewDTO view) {
        PluginViewParam pluginViewParam = new PluginViewParam();
        PluginViewSetImpl pluginViewSet = BeanUtils.copyBean(new PluginViewSetImpl(), table);
        pluginViewSet.setDsType(ds.getType());
        pluginViewSet.setTabelId(table.getId());
        pluginViewSet.setDs(ds);
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
    public List<String[]> cacheViewData(Provider datasourceProvider, DatasourceRequest datasourceRequest, String viewId) throws Exception {
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

    public String chartCopy(String sourceViewId, String newViewId, String panelId) {
        extChartViewMapper.chartCopy(newViewId, sourceViewId, panelId);
        extChartViewMapper.copyCache(sourceViewId, newViewId);
        extPanelGroupExtendDataMapper.copyExtendData(sourceViewId, newViewId, panelId);
        chartViewCacheService.refreshCache(newViewId);
        chartViewFieldService.copyField(sourceViewId, newViewId);
        return newViewId;
    }

    public String chartCopy(String sourceViewId, String panelId) {
        String newChartId = UUID.randomUUID().toString();
        return chartCopy(sourceViewId, newChartId, panelId);
    }


    /**
     * @param request
     * @param panelId
     * @return
     * @Description Copy a set of views with a given source ID and target ID
     */
    public Map<String, String> chartBatchCopy(ChartCopyBatchRequest request, String panelId) {
        Assert.notNull(panelId, "panelId should not be null");
        Map<String, String> sourceAndTargetIds = request.getSourceAndTargetIds();
        if (sourceAndTargetIds != null && !sourceAndTargetIds.isEmpty()) {
            for (Map.Entry<String, String> entry : sourceAndTargetIds.entrySet()) {
                chartCopy(entry.getKey(), entry.getValue(), panelId);
            }
        }
        return request.getSourceAndTargetIds();
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

    /**
     * @param panelId
     * @Description 初始化仪表板内部视图的cache表
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initViewCache(String panelId) {
        extChartViewMapper.deleteCacheWithPanel(null, panelId);
        extChartViewMapper.initPanelChartViewCache(panelId);
    }

    public List<String> getFieldData(String id, ChartExtRequest requestList, boolean cache, String fieldId, String fieldType) throws Exception {
        ChartViewDTO view = getOne(id, requestList.getQueryFrom());
        List<String[]> sqlData = sqlData(view, requestList, cache, fieldId);
        List<ChartViewFieldDTO> fieldList = new ArrayList<>();
        if (StringUtils.equalsIgnoreCase(fieldType, "xAxis")) {
            fieldList = gson.fromJson(view.getXAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
            }.getType());
        } else if (StringUtils.equalsIgnoreCase(fieldType, "extStack")) {
            fieldList = gson.fromJson(view.getExtStack(), new TypeToken<List<ChartViewFieldDTO>>() {
            }.getType());
        }
        DatasetTableField field = dataSetTableFieldsService.get(fieldId);

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
                if (StringUtils.equalsIgnoreCase(item.getId(), field.getId())) {// 获得当前自定义的字段
                    getIndex = i;
                }
            }
            if (StringUtils.equalsIgnoreCase(fieldType, "extStack")) {
                List<ChartViewFieldDTO> stack = gson.fromJson(view.getXAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
                }.getType());
                index += stack.size();
                getIndex += stack.size();
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

    /**
     * @param chartView
     * @Description Save the properties and style information of the view
     */
    public void viewPropsSave(ChartViewWithBLOBs chartView) {
        long timestamp = System.currentTimeMillis();
        chartView.setUpdateTime(timestamp);
        chartViewMapper.updateByPrimaryKeySelective(chartView);
    }

    private String handleVariable(String sql, ChartExtRequest requestList, QueryProvider qp, DataSetTableDTO table, Datasource ds) throws Exception {
        List<SqlVariableDetails> sqlVariables = new Gson().fromJson(table.getSqlVariableDetails(), new TypeToken<List<SqlVariableDetails>>() {}.getType());
        if (requestList != null && CollectionUtils.isNotEmpty(requestList.getFilter())) {
            for (ChartExtFilterRequest chartExtFilterRequest : requestList.getFilter()) {
                if (CollectionUtils.isEmpty(chartExtFilterRequest.getValue())) {
                    continue;
                }
                if (CollectionUtils.isEmpty(chartExtFilterRequest.getParameters())) {
                    continue;
                }

                for (String parameter : chartExtFilterRequest.getParameters()) {
                    if(parameter.contains("|DE|")){
                        if(!parameter.split("\\|DE\\|")[0].equals(table.getId())){
                            continue;
                        }
                        List<SqlVariableDetails> parameters = sqlVariables.stream().filter(item -> item.getVariableName().equalsIgnoreCase(parameter.split("\\|DE\\|")[1])).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(parameters)) {
                            String filter = qp.transFilter(chartExtFilterRequest, parameters.get(0));
                            sql = sql.replace("${" + parameter.split("\\|DE\\|")[1] + "}", filter);
                        }
                    }else {
                        List<SqlVariableDetails> parameters = sqlVariables.stream().filter(item -> item.getVariableName().equalsIgnoreCase(parameter)).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(parameters)) {
                            String filter = qp.transFilter(chartExtFilterRequest, parameters.get(0));
                            sql = sql.replace("${" + parameter + "}", filter);
                        }
                    }
                }
            }
        }
        sql = dataSetTableService.removeVariables(sql, ds.getType());
        return sql;
    }

    private String getDrillSort(List<ChartViewFieldDTO> xAxis, ChartViewFieldDTO field) {
        String res = "";
        for (ChartViewFieldDTO f : xAxis) {
            if (StringUtils.equalsIgnoreCase(f.getId(), field.getId())) {
                if (StringUtils.equalsIgnoreCase(f.getSort(), "asc") || StringUtils.equalsIgnoreCase(f.getSort(), "desc")) {
                    res = f.getSort();
                    break;
                }
            }
        }
        return res;
    }

    public List<ViewOption> viewOptions(String panelId) {
        return extChartViewMapper.chartOptions(panelId);
    }

    private List<ChartViewFieldDTO> getSizeField(ChartViewDTO view) {
        List<ChartViewFieldDTO> list = new ArrayList<>();
        String customAttr = view.getCustomAttr();
        JSONObject jsonObject = JSONObject.parseObject(customAttr);
        JSONObject size = jsonObject.getJSONObject("size");

        String gaugeMinType = size.getString("gaugeMinType");
        if (StringUtils.equalsIgnoreCase("dynamic", gaugeMinType)) {
            JSONObject gaugeMinField = size.getJSONObject("gaugeMinField");
            String id = gaugeMinField.getString("id");
            String summary = gaugeMinField.getString("summary");
            DatasetTableField datasetTableField = dataSetTableFieldsService.get(id);
            if (ObjectUtils.isNotEmpty(datasetTableField)) {
                if (datasetTableField.getDeType() == 0 || datasetTableField.getDeType() == 1 || datasetTableField.getDeType() == 5) {
                    if (!StringUtils.containsIgnoreCase(summary, "count")) {
                        DEException.throwException(Translator.get("i18n_gauge_field_change"));
                    }
                }
                ChartViewFieldDTO dto = new ChartViewFieldDTO();
                BeanUtils.copyBean(dto, datasetTableField);
                dto.setSummary(summary);
                list.add(dto);
            } else {
                DEException.throwException(Translator.get("i18n_gauge_field_delete"));
            }
        }
        String gaugeMaxType = size.getString("gaugeMaxType");
        if (StringUtils.equalsIgnoreCase("dynamic", gaugeMaxType)) {
            JSONObject gaugeMaxField = size.getJSONObject("gaugeMaxField");
            String id = gaugeMaxField.getString("id");
            String summary = gaugeMaxField.getString("summary");
            DatasetTableField datasetTableField = dataSetTableFieldsService.get(id);
            if (ObjectUtils.isNotEmpty(datasetTableField)) {
                if (datasetTableField.getDeType() == 0 || datasetTableField.getDeType() == 1 || datasetTableField.getDeType() == 5) {
                    if (!StringUtils.containsIgnoreCase(summary, "count")) {
                        DEException.throwException(Translator.get("i18n_gauge_field_change"));
                    }
                }
                ChartViewFieldDTO dto = new ChartViewFieldDTO();
                BeanUtils.copyBean(dto, datasetTableField);
                dto.setSummary(summary);
                list.add(dto);
            } else {
                DEException.throwException(Translator.get("i18n_gauge_field_delete"));
            }
        }
        return list;
    }

    private List<ChartSeniorAssistDTO> getDynamicAssistFields(ChartViewDTO view) {
        String senior = view.getSenior();
        JSONObject jsonObject = JSONObject.parseObject(senior);
        JSONArray assistLine = jsonObject.getJSONArray("assistLine");
        List<ChartSeniorAssistDTO> list = new ArrayList<>();
        if (ObjectUtils.isEmpty(assistLine) || StringUtils.isBlank(assistLine.toJSONString())) {
            return list;
        }
        List<ChartSeniorAssistDTO> assistLines = gson.fromJson(assistLine.toJSONString(), new TypeToken<List<ChartSeniorAssistDTO>>() {
        }.getType());


        for (ChartSeniorAssistDTO dto : assistLines) {
            if (StringUtils.equalsIgnoreCase(dto.getField(), "0")) {
                continue;
            }
            String fieldId = dto.getFieldId();
            String summary = dto.getSummary();
            if (StringUtils.isEmpty(fieldId) || StringUtils.isEmpty(summary)) {
                continue;
            }
            DatasetTableFieldExample datasetTableFieldExample = new DatasetTableFieldExample();
            datasetTableFieldExample.createCriteria().andTableIdEqualTo(view.getTableId()).andIdEqualTo(fieldId);
            List<DatasetTableField> fieldList = datasetTableFieldMapper.selectByExample(datasetTableFieldExample);
            if (CollectionUtils.isEmpty(fieldList)) {
                continue;
            }
            dto.setCurField(fieldList.get(0));
            list.add(dto);
        }
        return list;
    }

    private List<ChartViewFieldDTO> getAssistFields(List<ChartSeniorAssistDTO> list, List<ChartViewFieldDTO> yAxis) {
        List<ChartViewFieldDTO> res = new ArrayList<>();
        for (ChartSeniorAssistDTO dto : list) {
            DatasetTableField curField = dto.getCurField();
            ChartViewFieldDTO yField = null;
            String alias = "";
            for (int i = 0; i < yAxis.size(); i++) {
                ChartViewFieldDTO field = yAxis.get(i);
                if (StringUtils.equalsIgnoreCase(field.getId(), curField.getId())) {
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

    private void mergeAssistField(List<ChartSeniorAssistDTO> dynamicAssistFields, List<String[]> assistData) {
        if (CollectionUtils.isEmpty(assistData)) {
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
}

package io.dataease.service.chart;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.service.AuthUserService;
import io.dataease.commons.constants.CommonConstants;
import io.dataease.commons.constants.JdbcConstants;
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
import io.dataease.plugins.common.constants.DatasourceTypes;
import io.dataease.plugins.common.constants.datasource.SQLConstants;
import io.dataease.plugins.common.dto.chart.ChartFieldCompareDTO;
import io.dataease.plugins.common.dto.chart.ChartFieldCustomFilterDTO;
import io.dataease.plugins.common.dto.chart.ChartViewFieldDTO;
import io.dataease.plugins.common.dto.dataset.SqlVariableDetails;
import io.dataease.plugins.common.exception.DataEaseException;
import io.dataease.plugins.common.request.chart.ChartExtFilterRequest;
import io.dataease.plugins.common.request.chart.filter.FilterTreeObj;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.common.request.permission.DataSetRowPermissionsTreeDTO;
import io.dataease.plugins.common.util.SpringContextUtil;
import io.dataease.plugins.datasource.entity.PageInfo;
import io.dataease.plugins.datasource.provider.Provider;
import io.dataease.plugins.datasource.query.QueryProvider;
import io.dataease.plugins.view.entity.*;
import io.dataease.plugins.view.entity.filter.PluginFilterTreeObj;
import io.dataease.plugins.view.service.ViewPluginService;
import io.dataease.plugins.xpack.auth.dto.request.ColumnPermissionItem;
import io.dataease.provider.ProviderFactory;
import io.dataease.provider.query.SQLUtils;
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
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Resource
    private ChartFilterTreeService chartFilterTreeService;
    @Resource
    private ChartViewOldDataMergeService chartViewOldDataMergeService;

    private static final Logger logger = LoggerFactory.getLogger(ChartViewService.class);

    private static final String START_END_SEPARATOR = "_START_END_SPLIT";


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
            // trans chart filter
            tranChartFilter(result);

            DatasetTable datasetTable = dataSetTableService.get(result.getTableId());
            if (ObjectUtils.isNotEmpty(datasetTable)) {
                result.setDatasetMode(datasetTable.getMode());
                Datasource datasource = datasourceService.get(datasetTable.getDataSourceId());
                buildDsType(datasource, result);
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
            DatasetTable datasetTable = dataSetTableService.get(view.getTableId());
            if (ObjectUtils.isNotEmpty(datasetTable)) {
                view.setDatasetMode(datasetTable.getMode());
                Datasource datasource = datasourceService.get(datasetTable.getDataSourceId());
                buildDsType(datasource, view);
            }
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
        if (StringUtils.equalsAnyIgnoreCase(view.getType(), "table-pivot", "bar-time-range")
                || StringUtils.containsIgnoreCase(view.getType(), "group")
                || ("antv".equalsIgnoreCase(view.getRender()) && "line".equalsIgnoreCase(view.getType()))) {
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
        if (StringUtils.equalsIgnoreCase(view.getType(), "scatter") && StringUtils.equalsIgnoreCase(view.getRender(), "antv")) {
            xAxis.addAll(extStack);
        }

        if (CollectionUtils.isNotEmpty(xAxis) && StringUtils.equals(xAxis.get(0).getGroupType(), "q") && StringUtils.equalsIgnoreCase(view.getRender(), "antv")) {
            List<ChartViewFieldDTO> xAxisExt = gson.fromJson(view.getXAxisExt(), new TypeToken<List<ChartViewFieldDTO>>() {
            }.getType());
            extStack.addAll(xAxisExt);
        }
        List<ChartViewFieldDTO> extBubble = gson.fromJson(view.getExtBubble(), new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType());
        FilterTreeObj fieldCustomFilter = new FilterTreeObj();// todo 等前端过滤组件传递过滤配置
        List<ChartViewFieldDTO> drill = new ArrayList<ChartViewFieldDTO>();


        DatasetTableField datasetTableFieldObj = DatasetTableField.builder().tableId(view.getTableId()).checked(Boolean.TRUE).build();
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableFieldObj);
        // 获取数据集,需校验权限
        DataSetTableDTO table = dataSetTableService.getWithPermission(view.getTableId(), requestList.getUser());
        checkPermission("use", table, requestList.getUser());

        //列权限
        Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
        List<DatasetTableField> columnPermissionFields = permissionService.filterColumnPermissions(fields, desensitizationList, table.getId(), requestList.getUser());
        //将没有权限的列删掉
        List<String> dataeaseNames = columnPermissionFields.stream().map(DatasetTableField::getDataeaseName).collect(Collectors.toList());
        dataeaseNames.add("*");
//        fieldCustomFilter = fieldCustomFilter.stream().filter(item -> !desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
        extStack = extStack.stream().filter(item -> !desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
        extBubble = extBubble.stream().filter(item -> !desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
        drill = drill.stream().filter(item -> !desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());


        //行权限
        List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = permissionsTreeService.getRowPermissionsTree(fields, table, requestList.getUser());

        chartFilterTreeService.searchFieldAndSet(fieldCustomFilter);

        if (CollectionUtils.isEmpty(xAxis) && CollectionUtils.isEmpty(yAxis)) {
            return new ArrayList<String[]>();
        }

        switch (view.getType()) {
            case "label":
                xAxis = xAxis.stream().filter(item -> !desensitizationList.containsKey(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                yAxis = new ArrayList<>();
                if (CollectionUtils.isEmpty(xAxis)) {
                    return new ArrayList<String[]>();
                }
                break;
            case "text":
            case "gauge":
            case "liquid":
                xAxis = new ArrayList<>();
                yAxis = yAxis.stream().filter(item -> !desensitizationList.containsKey(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
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
                xAxis = xAxis.stream().filter(item -> !desensitizationList.containsKey(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                yAxis = yAxis.stream().filter(item -> !desensitizationList.containsKey(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
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

            if (StringUtils.equals(view.getType(), "race-bar")) {
                List<ChartViewFieldDTO> xAxisExtList = gson.fromJson(view.getXAxisExt(), new TypeToken<List<ChartViewFieldDTO>>() {
                }.getType());
                xAxisExtList.forEach((x) -> {
                    x.setBusiType("race-bar");
                });
                xAxis.addAll(xAxisExtList);
            } else {
                List<ChartViewFieldDTO> xAxisExt = gson.fromJson(view.getXAxisExt(), new TypeToken<List<ChartViewFieldDTO>>() {
                }.getType());
                fieldMap.put("xAxisExt", xAxisExt);
            }
            fieldMap.put("xAxis", xAxis);
            fieldMap.put("yAxis", yAxis);
            fieldMap.put("extStack", extStack);
            fieldMap.put("extBubble", extBubble);
            PluginViewParam pluginViewParam = buildPluginParam(fieldMap, fieldCustomFilter, extFilterList, ds, table, view, rowPermissionsTree, requestList);
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
                    datasourceRequest.setQuery(qp.getSQLScatter(dataTableInfoDTO.getTable(), xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, extStack, ds, view));
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLTableInfo(dataTableInfoDTO.getTable(), xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
                } else if (StringUtils.equalsIgnoreCase("bar-time-range", view.getType())) {
                    List<ChartViewFieldDTO> xAxisBase = gson.fromJson(view.getXAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
                    }.getType());
                    datasourceRequest.setQuery(qp.getSQLRangeBar(dataTableInfoDTO.getTable(), xAxisBase, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, ds, view));

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
                    datasourceRequest.setQuery(qp.getSQLAsTmpScatter(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, extStack, view));
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
                } else if (StringUtils.equalsIgnoreCase("bar-time-range", view.getType())) {
                    List<ChartViewFieldDTO> xAxisBase = gson.fromJson(view.getXAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
                    }.getType());
                    datasourceRequest.setQuery(qp.getSQLAsTmpRangeBar(sql, xAxisBase, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, view));

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
                    datasourceRequest.setQuery(qp.getSQLAsTmpScatter(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, extStack, view));
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
                } else if (StringUtils.equalsIgnoreCase("bar-time-range", view.getType())) {
                    List<ChartViewFieldDTO> xAxisBase = gson.fromJson(view.getXAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
                    }.getType());
                    datasourceRequest.setQuery(qp.getSQLAsTmpRangeBar(sql, xAxisBase, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, view));

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
                    datasourceRequest.setQuery(qp.getSQLAsTmpScatter(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, extStack, view));
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
                } else if (StringUtils.equalsIgnoreCase("bar-time-range", view.getType())) {
                    List<ChartViewFieldDTO> xAxisBase = gson.fromJson(view.getXAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
                    }.getType());
                    datasourceRequest.setQuery(qp.getSQLAsTmpRangeBar(sql, xAxisBase, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, view));

                } else {
                    datasourceRequest.setQuery(qp.getSQLAsTmp(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view));
                }
            }
            List<ChartViewFieldDTO> xAxisForRequest = new ArrayList<>();
            xAxisForRequest.addAll(xAxis);
            xAxisForRequest.addAll(extStack);
            datasourceRequest.setXAxis(xAxisForRequest);
            List<ChartViewFieldDTO> yAxisForRequest = new ArrayList<>();
            yAxisForRequest.addAll(yAxis);
            datasourceRequest.setYAxis(yAxisForRequest);
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
                datasourceRequest.setQuery(qp.getSQLScatter(tableName, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, extStack, ds, view));
            } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                datasourceRequest.setQuery(qp.getSQLTableInfo(tableName, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
            } else if (StringUtils.equalsIgnoreCase("bar-time-range", view.getType())) {
                List<ChartViewFieldDTO> xAxisBase = gson.fromJson(view.getXAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
                }.getType());
                datasourceRequest.setQuery(qp.getSQLRangeBar(tableName, xAxisBase, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, ds, view));

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

    public Boolean containDetailField(ChartViewDTO view) {
        List<String> detailFieldViewTypes = new ArrayList<>();
        detailFieldViewTypes.add("map");
        return detailFieldViewTypes.contains(view.getType());
    }

    public ChartViewDTO calcData(ChartViewDTO view, ChartExtRequest chartExtRequest, boolean cache) throws Exception {
        ChartViewDTO chartViewDTO = new ChartViewDTO();
        if (ObjectUtils.isEmpty(view)) {
            throw new RuntimeException(Translator.get("i18n_chart_delete"));
        }
        Type tokenType = new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType();

        List<ChartViewFieldDTO> viewFields = gson.fromJson(view.getViewFields(), tokenType);
        final Map<String, List<ChartViewFieldDTO>> extFieldsMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(viewFields)) {
            String[] busiFlagArray = new String[]{"daxis", "locationXaxis", "locationYaxis"};
            Map<String, Boolean> flagMap = new HashMap<>();
            for (String s : busiFlagArray) {
                flagMap.put(s, false);
            }
            for (ChartViewFieldDTO field : viewFields) {
                flagMap.put(field.getBusiType(), true);
                String busiType = field.getBusiType();
                List<ChartViewFieldDTO> list = extFieldsMap.containsKey(busiType) ? extFieldsMap.get(busiType) : new ArrayList<>();
                list.add(field);
                extFieldsMap.put(field.getBusiType(), list);
            }
            if (flagMap.get("daxis") && (!flagMap.get("locationXaxis") || !flagMap.get("locationYaxis"))) {
                viewFields = viewFields.stream().filter(field -> !StringUtils.equals("daxis", field.getBusiType())).collect(Collectors.toList());
            }
        }

        List<ChartViewFieldDTO> xAxisBase = gson.fromJson(view.getXAxis(), tokenType);
        List<ChartViewFieldDTO> xAxis = gson.fromJson(view.getXAxis(), tokenType);
        List<ChartViewFieldDTO> xAxisExt = gson.fromJson(view.getXAxisExt(), tokenType);
        if (StringUtils.equalsIgnoreCase(view.getType(), "table-pivot")
                || StringUtils.containsIgnoreCase(view.getType(), "group")
                || ("antv".equalsIgnoreCase(view.getRender()) && "line".equalsIgnoreCase(view.getType()))
                || StringUtils.equalsIgnoreCase(view.getType(), "flow-map")
                || StringUtils.equalsIgnoreCase(view.getType(), "bar-time-range")
        ) {
            xAxis.addAll(xAxisExt);
        }
        List<ChartViewFieldDTO> yAxis = gson.fromJson(view.getYAxis(), tokenType);
        if (StringUtils.equalsAnyIgnoreCase(view.getType(), "chart-mix", "bidirectional-bar")) {
            List<ChartViewFieldDTO> yAxisExt = gson.fromJson(view.getYAxisExt(), tokenType);
            yAxis.addAll(yAxisExt);
        }
        if (StringUtils.equalsIgnoreCase(view.getRender(), "antv") && StringUtils.equalsAnyIgnoreCase(view.getType(), "gauge", "liquid")) {
            List<ChartViewFieldDTO> sizeField = getSizeField(view);
            yAxis.addAll(sizeField);
        }
        List<ChartViewFieldDTO> extStack = gson.fromJson(view.getExtStack(), tokenType);
        if (CollectionUtils.isNotEmpty(xAxis) && StringUtils.equals(xAxis.get(0).getGroupType(), "q") && StringUtils.equalsIgnoreCase(view.getRender(), "antv")) {
            extStack.addAll(xAxisExt);
        }
        List<ChartViewFieldDTO> extBubble = gson.fromJson(view.getExtBubble(), tokenType);
        FilterTreeObj fieldCustomFilter = gson.fromJson(view.getCustomFilter(), FilterTreeObj.class);
        List<ChartViewFieldDTO> drill = gson.fromJson(view.getDrillFields(), tokenType);

        // 视图计算字段，用dataeaseName作为唯一标识
        ChartViewField chartViewField = new ChartViewField();
        chartViewField.setChartId(view.getId());
        List<ChartViewField> chartViewFields = chartViewFieldService.list(chartViewField);
        List<String> chartViewFieldNameList = chartViewFields.stream().map(ChartViewField::getDataeaseName).collect(Collectors.toList());


        DatasetTableField datasetTableFieldObj = DatasetTableField.builder().tableId(view.getTableId()).checked(Boolean.TRUE).build();
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableFieldObj);
        // 获取数据集,需校验权限
        DataSetTableDTO table = dataSetTableService.getWithPermission(view.getTableId(), chartExtRequest.getUser());
        checkPermission("use", table, chartExtRequest.getUser());

        Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
        //列权限
        List<DatasetTableField> columnPermissionFields = permissionService.filterColumnPermissions(fields, desensitizationList, table.getId(), chartExtRequest.getUser());
        //将没有权限的列删掉
        List<String> dataeaseNames = columnPermissionFields.stream().map(DatasetTableField::getDataeaseName).collect(Collectors.toList());
        dataeaseNames.add("*");
        extStack = extStack.stream().filter(item -> chartViewFieldNameList.contains(item.getDataeaseName()) || (!desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
        extBubble = extBubble.stream().filter(item -> chartViewFieldNameList.contains(item.getDataeaseName()) || (!desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
        drill = drill.stream().filter(item -> chartViewFieldNameList.contains(item.getDataeaseName()) || (!desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());

        //行权限
        List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = permissionsTreeService.getRowPermissionsTree(fields, table, chartExtRequest.getUser());

        chartFilterTreeService.searchFieldAndSet(fieldCustomFilter);

        if (CollectionUtils.isEmpty(xAxis) && CollectionUtils.isEmpty(yAxis)) {
            return emptyChartViewDTO(view);
        }

        // 直连明细表分页
        Map<String, Object> mapAttr = gson.fromJson(view.getCustomAttr(), Map.class);
        Map<String, Object> mapSize = (Map<String, Object>) mapAttr.get("size");
        if (StringUtils.equalsIgnoreCase(view.getType(), "table-info") && table.getMode() == 0) {
            if (StringUtils.equalsIgnoreCase((String) mapSize.get("tablePageMode"), "page") && !chartExtRequest.getExcelExportFlag()) {
                if (chartExtRequest.getGoPage() == null) {
                    chartExtRequest.setGoPage(1L);
                }
                if (chartExtRequest.getPageSize() == null) {
                    String pageSize = (String) mapSize.get("tablePageSize");
                    chartExtRequest.setPageSize(Math.min(Long.parseLong(pageSize), view.getResultCount().longValue()));
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
                xAxis = xAxis.stream().filter(item -> chartViewFieldNameList.contains(item.getDataeaseName()) || (!desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
                yAxis = new ArrayList<>();
                if (CollectionUtils.isEmpty(xAxis)) {
                    return emptyChartViewDTO(view);
                }
                break;
            case "text":
            case "gauge":
            case "liquid":
                xAxis = new ArrayList<>();
                yAxis = yAxis.stream().filter(item -> chartViewFieldNameList.contains(item.getDataeaseName()) || (!desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(yAxis)) {
                    return emptyChartViewDTO(view);
                }
                break;
            case "table-info":
                yAxis = new ArrayList<>();
                xAxis = xAxis.stream().filter(item -> chartViewFieldNameList.contains(item.getDataeaseName()) || dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(xAxis)) {
                    return emptyChartViewDTO(view);
                }
                break;
            case "table-normal":
                xAxis = xAxis.stream().filter(item -> chartViewFieldNameList.contains(item.getDataeaseName()) || dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                yAxis = yAxis.stream().filter(item -> chartViewFieldNameList.contains(item.getDataeaseName()) || dataeaseNames.contains(item.getDataeaseName())).collect(Collectors.toList());
                break;
            case "bar-group":
            case "bar-group-stack":
            case "flow-map":
                xAxis = xAxis.stream().filter(item -> chartViewFieldNameList.contains(item.getDataeaseName()) || (!desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
                yAxis = yAxis.stream().filter(item -> chartViewFieldNameList.contains(item.getDataeaseName()) || (!desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
                xAxisBase = xAxisBase.stream().filter(item -> chartViewFieldNameList.contains(item.getDataeaseName()) || (!desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
                xAxisExt = xAxisExt.stream().filter(item -> chartViewFieldNameList.contains(item.getDataeaseName()) || (!desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
                break;
            default:
                xAxis = xAxis.stream().filter(item -> chartViewFieldNameList.contains(item.getDataeaseName()) || (!desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
                yAxis = yAxis.stream().filter(item -> chartViewFieldNameList.contains(item.getDataeaseName()) || (!desensitizationList.keySet().contains(item.getDataeaseName()) && dataeaseNames.contains(item.getDataeaseName()))).collect(Collectors.toList());
        }
        Map<String, ChartViewFieldDTO> chartFieldMap = Stream.of(xAxisBase, xAxisExt, extStack)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(ChartViewFieldDTO::getId, o -> o, ((p, n) -> p)));
        // 过滤来自仪表板的条件
        List<ChartExtFilterRequest> extFilterList = new ArrayList<>();
        //组件过滤条件
        if (ObjectUtils.isNotEmpty(chartExtRequest.getFilter())) {
            for (ChartExtFilterRequest request : chartExtRequest.getFilter()) {
                // 解析多个fieldId,fieldId是一个逗号分隔的字符串
                String fieldId = request.getFieldId();
                if (request.getIsTree() == null) {
                    request.setIsTree(false);
                }
                boolean hasParameters = false;
                if (StringUtils.isNotEmpty(table.getSqlVariableDetails())) {
                    List<SqlVariableDetails> sqlVariables = new Gson().fromJson(table.getSqlVariableDetails(), new TypeToken<List<SqlVariableDetails>>() {
                    }.getType());
                    for (String parameter : Optional.ofNullable(request.getParameters()).orElse(new ArrayList<>())) {
                        if (StringUtils.endsWith(parameter, START_END_SEPARATOR)) {
                            parameter = parameter.split(START_END_SEPARATOR)[0];
                        }
                        if (sqlVariables.stream().map(SqlVariableDetails::getVariableName).collect(Collectors.toList()).contains(parameter)) {
                            hasParameters = true;
                        }
                        if (parameter.contains("|DE|")
                                && table.getId().equals(parameter.split("\\|DE\\|")[0])
                                && sqlVariables
                                .stream()
                                .map(SqlVariableDetails::getVariableName)
                                .collect(Collectors.toList())
                                .contains(parameter.split("\\|DE\\|")[1])) {
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
                            if (!desensitizationList.keySet().contains(datasetTableField.getDataeaseName()) && dataeaseNames.contains(datasetTableField.getDataeaseName())) {
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
                            if (!desensitizationList.keySet().contains(datasetTableField.getDataeaseName()) && dataeaseNames.contains(datasetTableField.getDataeaseName())) {
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
        if (ObjectUtils.isNotEmpty(chartExtRequest.getLinkageFilters())) {
            filters.addAll(chartExtRequest.getLinkageFilters());
        }

        // 外部参数条件
        if (ObjectUtils.isNotEmpty(chartExtRequest.getOuterParamsFilters())) {
            filters.addAll(chartExtRequest.getOuterParamsFilters());
        }

        //联动过滤条件和外部参数过滤条件全部加上
        if (ObjectUtils.isNotEmpty(filters)) {
            for (ChartExtFilterRequest request : filters) {
                DatasetTableField datasetTableField = dataSetTableFieldsService.get(request.getFieldId());
                if (!desensitizationList.keySet().contains(datasetTableField.getDataeaseName()) && dataeaseNames.contains(datasetTableField.getDataeaseName())) {
                    request.setDatasetTableField(datasetTableField);
                    if (StringUtils.equalsIgnoreCase(datasetTableField.getTableId(), view.getTableId())) {
//                         设置日期格式，以视图字段设置的格式为准，先不处理组件的条件，因为格式无法统一。
                        if (request.getDatasetTableField() != null) {
                            ChartViewFieldDTO chartViewFieldDTO = chartFieldMap.get(request.getDatasetTableField().getId());
                            if (chartViewFieldDTO != null) {
                                request.setDatePattern(chartViewFieldDTO.getDatePattern());
                                request.setDateStyle(chartViewFieldDTO.getDateStyle());
                            }
                        }
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
        List<ChartDrillRequest> drillRequestList = chartExtRequest.getDrill();
        if (CollectionUtils.isNotEmpty(drillRequestList) && (drill.size() > drillRequestList.size())) {
            ArrayList<ChartViewFieldDTO> fieldsToFilter = new ArrayList<>();
//            如果是从子维度开始下钻，那么其他维度的条件要先加上去
//            分组和堆叠
            if (StringUtils.containsIgnoreCase(view.getType(), "group")) {
//              分组堆叠
                if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
//                  分组和堆叠字段都有才有效
                    if (CollectionUtils.isNotEmpty(xAxisExt) && CollectionUtils.isNotEmpty(extStack)) {
//                      从分组字段下钻，就加上堆叠字段的条件
                        if (StringUtils.equalsIgnoreCase(drill.get(0).getId(), xAxisExt.get(0).getId())) {
                            fieldsToFilter.addAll(xAxisBase);
                            fieldsToFilter.addAll(extStack);
                        }
//                      从堆叠字段下钻，就加上分组字段的条件
                        if (StringUtils.equalsIgnoreCase(drill.get(0).getId(), extStack.get(0).getId())) {
                            fieldsToFilter.addAll(xAxisBase);
                            fieldsToFilter.addAll(xAxisExt);
                        }
                    }
                } else if (CollectionUtils.isNotEmpty(xAxisExt) &&
                        StringUtils.equalsIgnoreCase(drill.get(0).getId(), xAxisExt.get(0).getId())) {
                    fieldsToFilter.addAll(xAxisBase);
                }
            } else if (StringUtils.containsIgnoreCase(view.getType(), "stack") &&
                    CollectionUtils.isNotEmpty(extStack) &&
                    StringUtils.equalsIgnoreCase(drill.get(0).getId(), extStack.get(0).getId())) {
//              堆叠
                fieldsToFilter.addAll(xAxisBase);
            }
            ChartDrillRequest head = drillRequestList.get(0);
            Map<String, String> dimValMap = new HashMap<>();
            head.getDimensionList().forEach(item -> dimValMap.put(item.getId(), item.getValue()));

            boolean isAntVScatterNumberXAxis = CollectionUtils.isNotEmpty(xAxis) && StringUtils.equals(xAxis.get(0).getGroupType(), "q") && StringUtils.equalsIgnoreCase(view.getRender(), "antv");

            for (int i = 0; i < drillRequestList.size(); i++) {
                ChartDrillRequest request = drillRequestList.get(i);
                ChartViewFieldDTO chartViewFieldDTO = drill.get(i);
                for (ChartDimensionDTO requestDimension : request.getDimensionList()) {
                    // 将钻取值作为条件传递，将所有钻取字段作为xAxis并加上下一个钻取字段
                    if (StringUtils.equalsIgnoreCase(requestDimension.getId(), chartViewFieldDTO.getId())) {
                        isDrill = true;
                        fieldsToFilter.add(chartViewFieldDTO);
                        dimValMap.put(requestDimension.getId(), requestDimension.getValue());
                        if (!checkDrillExist(xAxis, extStack, requestDimension.getId(), view)) {
                            chartFieldMap.put(chartViewFieldDTO.getId(), chartViewFieldDTO);

                            if (isAntVScatterNumberXAxis) {
                                extStack.add(chartViewFieldDTO);
                            } else {
                                xAxis.add(chartViewFieldDTO);
                            }
                        }
                        if (i == drillRequestList.size() - 1) {
                            ChartViewFieldDTO nextDrillField = drill.get(i + 1);
                            if (!checkDrillExist(xAxis, extStack, nextDrillField.getId(), view)) {
                                // get drill list first element's sort,then assign to nextDrillField
                                nextDrillField.setSort(getDrillSort(xAxis, drill.get(0)));
                                nextDrillField.setDrill(true);

                                if (isAntVScatterNumberXAxis) {
                                    extStack.add(nextDrillField);
                                } else {
                                    xAxis.add(nextDrillField);
                                }
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < fieldsToFilter.size(); i++) {
                ChartViewFieldDTO tmpField = fieldsToFilter.get(i);
                ChartExtFilterRequest tmpFilter = new ChartExtFilterRequest();
                DatasetTableField datasetTableField = dataSetTableFieldsService.get(tmpField.getId());
                tmpFilter.setDatasetTableField(datasetTableField);
                tmpFilter.setOperator("in");
                tmpFilter.setDateStyle(chartFieldMap.get(tmpField.getId()).getDateStyle());
                tmpFilter.setDatePattern(chartFieldMap.get(tmpField.getId()).getDatePattern());
                tmpFilter.setFieldId(tmpField.getId());
                tmpFilter.setValue(Collections.singletonList(dimValMap.get(tmpField.getId())));
                extFilterList.add(tmpFilter);
                drillFilters.add(tmpFilter);
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
        List<ChartSeniorAssistDTO> dynamicAssistFields = new ArrayList<>();
        List<ChartViewFieldDTO> assistFields = null;
        if (StringUtils.containsIgnoreCase(view.getType(), "bar")
                || StringUtils.containsIgnoreCase(view.getType(), "line")
                || StringUtils.containsIgnoreCase(view.getType(), "area")
                || StringUtils.containsIgnoreCase(view.getType(), "scatter")
                || StringUtils.containsIgnoreCase(view.getType(), "mix")
        ) {
            // 动态辅助线
            dynamicAssistFields = getDynamicAssistFields(view);
            assistFields = getAssistFields(dynamicAssistFields, yAxis, null);
        } else if (StringUtils.containsIgnoreCase(view.getType(), "table-info")
                || StringUtils.containsIgnoreCase(view.getType(), "table-normal")
                || StringUtils.containsIgnoreCase(view.getType(), "table-pivot")) {
            // 动态阈值
            dynamicAssistFields = getDynamicThresholdFields(view);
            assistFields = getAssistFields(dynamicAssistFields, yAxis, xAxis);
        }

        fieldCustomFilter = chartFilterTreeService.charReplace(fieldCustomFilter);

        extFilterList = extFilterList.stream().peek(ele -> {
            if (CollectionUtils.isNotEmpty(ele.getValue())) {
                List<String> collect = ele.getValue().stream().map(SQLUtils::transKeyword).collect(Collectors.toList());
                ele.setValue(collect);
            }
        }).collect(Collectors.toList());

        // 如果是插件视图 走插件内部的逻辑
        if (ObjectUtils.isNotEmpty(view.getIsPlugin()) && view.getIsPlugin()) {
            Map<String, List<ChartViewFieldDTO>> fieldMap = ObjectUtils.isEmpty(extFieldsMap) ? new LinkedHashMap<>() : extFieldsMap;

            if (StringUtils.equals(view.getType(), "race-bar")) {
                List<ChartViewFieldDTO> xAxisExtList = gson.fromJson(view.getXAxisExt(), new TypeToken<List<ChartViewFieldDTO>>() {
                }.getType());
                xAxisExtList.forEach((x) -> {
                    x.setBusiType("race-bar");
                });
                xAxis.addAll(xAxisExtList);
            }
            fieldMap.put("xAxis", xAxis);
            fieldMap.put("xAxisExt", xAxisExt);
            fieldMap.put("extStack", extStack);
            fieldMap.put("extBubble", extBubble);
            fieldMap.put("yAxis", yAxis);
            PluginViewParam pluginViewParam = buildPluginParam(fieldMap, fieldCustomFilter, extFilterList, ds, table, view, rowPermissionsTree, chartExtRequest);
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

        String querySql = null;
        long totalPage = 0l;
        long totalItems = 0l;
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
            if (ObjectUtils.isEmpty(ds)) {
                throw new RuntimeException(Translator.get("i18n_datasource_delete"));
            }
            if (StringUtils.isNotEmpty(ds.getStatus()) && "Error".equalsIgnoreCase(ds.getStatus())) {
                throw new Exception(Translator.get("i18n_invalid_ds"));
            }
            pageInfo.setDsVersion(datasourceProvider.dsVersion(ds));
            datasourceRequest.setDatasource(ds);
            DataTableInfoDTO dataTableInfoDTO = gson.fromJson(table.getInfo(), DataTableInfoDTO.class);
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            if (StringUtils.equalsIgnoreCase(table.getType(), DatasetType.DB.name())) {
                datasourceRequest.setTable(dataTableInfoDTO.getTable());
                if (StringUtils.equalsAnyIgnoreCase(view.getType(), "text", "gauge", "liquid")) {
                    querySql = qp.getSQLSummary(dataTableInfoDTO.getTable(), yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view, ds);
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    querySql = qp.getSQLStack(dataTableInfoDTO.getTable(), xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, ds, view);
                } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                    querySql = qp.getSQLScatter(dataTableInfoDTO.getTable(), xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, extStack, ds, view);
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    querySql = qp.getSQLWithPage(true, dataTableInfoDTO.getTable(), xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view, pageInfo);
                    totalPageSql = qp.getResultCount(true, dataTableInfoDTO.getTable(), xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view);
                } else if (StringUtils.equalsIgnoreCase("bar-time-range", view.getType())) {
                    querySql = qp.getSQLRangeBar(dataTableInfoDTO.getTable(), xAxisBase, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, ds, view);
                } else {
                    querySql = qp.getSQL(dataTableInfoDTO.getTable(), xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view);
                    if (containDetailField(view) && CollectionUtils.isNotEmpty(viewFields)) {
                        detailFieldList.addAll(xAxis);
                        detailFieldList.addAll(viewFields);
                        String resultMode = view.getResultMode();
                        view.setResultMode(null);
                        detailFieldSql = qp.getSQLWithPage(true, dataTableInfoDTO.getTable(), detailFieldList, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view, pageInfo);
                        view.setResultMode(resultMode);
                    }
                }
            } else if (StringUtils.equalsIgnoreCase(table.getType(), DatasetType.SQL.name())) {
                String sql = dataTableInfoDTO.isBase64Encryption() ? new String(java.util.Base64.getDecoder().decode(dataTableInfoDTO.getSql())) : dataTableInfoDTO.getSql();
                sql = handleVariable(sql, chartExtRequest, qp, table, ds);
                if (StringUtils.equalsAnyIgnoreCase(view.getType(), "text", "gauge", "liquid")) {
                    querySql = qp.getSQLSummaryAsTmp(sql, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view);
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    querySql = qp.getSQLAsTmpStack(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, view);
                } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                    querySql = qp.getSQLAsTmpScatter(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, extStack, view);
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    querySql = qp.getSQLWithPage(false, sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view, pageInfo);
                    totalPageSql = qp.getResultCount(false, sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view);
                } else if (StringUtils.equalsIgnoreCase("bar-time-range", view.getType())) {

                    querySql = qp.getSQLAsTmpRangeBar(sql, xAxisBase, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, view);

                } else {
                    querySql = qp.getSQLAsTmp(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view);
                    if (containDetailField(view) && CollectionUtils.isNotEmpty(viewFields)) {
                        detailFieldList.addAll(xAxis);
                        detailFieldList.addAll(viewFields);
                        String resultMode = view.getResultMode();
                        view.setResultMode(null);
                        detailFieldSql = qp.getSQLWithPage(false, sql, detailFieldList, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view, pageInfo);
                        view.setResultMode(resultMode);
                    }
                }
            } else if (StringUtils.equalsIgnoreCase(table.getType(), DatasetType.CUSTOM.name())) {
                DataTableInfoDTO dt = gson.fromJson(table.getInfo(), DataTableInfoDTO.class);
                List<DataSetTableUnionDTO> list = dataSetTableUnionService.listByTableId(dt.getList().get(0).getTableId());
                String sql = dataSetTableService.getCustomSQLDatasource(dt, list, ds);
                if (StringUtils.equalsAnyIgnoreCase(view.getType(), "text", "gauge", "liquid")) {
                    querySql = qp.getSQLSummaryAsTmp(sql, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view);
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    querySql = qp.getSQLAsTmpStack(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, view);
                } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                    querySql = qp.getSQLAsTmpScatter(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, extStack, view);
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    querySql = qp.getSQLWithPage(false, sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view, pageInfo);
                    totalPageSql = qp.getResultCount(false, sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view);
                } else if (StringUtils.equalsIgnoreCase("bar-time-range", view.getType())) {
                    querySql = qp.getSQLAsTmpRangeBar(sql, xAxisBase, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, view);

                } else {
                    querySql = qp.getSQLAsTmp(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view);
                    if (containDetailField(view) && CollectionUtils.isNotEmpty(viewFields)) {
                        detailFieldList.addAll(xAxis);
                        detailFieldList.addAll(viewFields);
                        String resultMode = view.getResultMode();
                        view.setResultMode(null);
                        detailFieldSql = qp.getSQLWithPage(false, sql, detailFieldList, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view, pageInfo);
                        view.setResultMode(resultMode);
                    }
                }
            } else if (StringUtils.equalsIgnoreCase(table.getType(), DatasetType.UNION.name())) {
                DataTableInfoDTO dt = gson.fromJson(table.getInfo(), DataTableInfoDTO.class);
                Map<String, Object> sqlMap = dataSetTableService.getUnionSQLDatasource(dt, ds);
                String sql = (String) sqlMap.get("sql");
                if (StringUtils.equalsAnyIgnoreCase(view.getType(), "text", "gauge", "liquid")) {
                    querySql = qp.getSQLSummaryAsTmp(sql, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view);
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    querySql = qp.getSQLAsTmpStack(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, view);
                } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                    querySql = qp.getSQLAsTmpScatter(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, extStack, view);
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    querySql = qp.getSQLWithPage(false, sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view, pageInfo);
                    totalPageSql = qp.getResultCount(false, sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view);
                } else if (StringUtils.equalsIgnoreCase("bar-time-range", view.getType())) {

                    querySql = qp.getSQLAsTmpRangeBar(sql, xAxisBase, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, view);

                } else {
                    querySql = qp.getSQLAsTmp(sql, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view);
                    if (containDetailField(view) && CollectionUtils.isNotEmpty(viewFields)) {
                        detailFieldList.addAll(xAxis);
                        detailFieldList.addAll(viewFields);
                        String resultMode = view.getResultMode();
                        view.setResultMode(null);
                        detailFieldSql = qp.getSQLWithPage(false, sql, detailFieldList, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view, pageInfo);
                        view.setResultMode(resultMode);
                    }
                }
            }
            if (StringUtils.isNotEmpty(totalPageSql) && StringUtils.equalsIgnoreCase((String) mapSize.get("tablePageMode"), "page")) {
                datasourceRequest.setQuery(totalPageSql);
                datasourceRequest.setTotalPageFlag(true);
                java.util.List<java.lang.String[]> tmpData = datasourceProvider.getData(datasourceRequest);
                totalItems = CollectionUtils.isEmpty(tmpData) ? 0 : Long.valueOf(tmpData.get(0)[0]);
                totalPage = (totalItems / pageInfo.getPageSize()) + (totalItems % pageInfo.getPageSize() > 0 ? 1 : 0);
            }

            datasourceRequest.setQuery(querySql);
            List<ChartViewFieldDTO> xAxisForRequest = new ArrayList<>();
            xAxisForRequest.addAll(xAxis);
            xAxisForRequest.addAll(extStack);
            datasourceRequest.setXAxis(xAxisForRequest);
            List<ChartViewFieldDTO> yAxisForRequest = new ArrayList<>();
            yAxisForRequest.addAll(yAxis);
            datasourceRequest.setYAxis(yAxisForRequest);
            datasourceRequest.setTotalPageFlag(false);
            data = datasourceProvider.getData(datasourceRequest);
            if (CollectionUtils.isNotEmpty(assistFields)) {
                datasourceAssistRequest.setQuery(assistSQL(datasourceRequest.getQuery(), assistFields, ds));
                logger.info(datasourceAssistRequest.getQuery());
                assistData = datasourceProvider.getData(datasourceAssistRequest);
            }

            if (StringUtils.isNotBlank(detailFieldSql)) {
                datasourceRequest.setQuery(detailFieldSql);
                detailData = datasourceProvider.getData(datasourceRequest);
            }
        } else if (table.getMode() == 1) {// 抽取
            // 连接doris，构建doris数据源查询
            datasourceRequest.setDatasource(ds);
            String tableName = "ds_" + table.getId().replaceAll("-", "_");
            datasourceRequest.setTable(tableName);
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            if (StringUtils.equalsAnyIgnoreCase(view.getType(), "text", "gauge", "liquid")) {
                datasourceRequest.setQuery(qp.getSQLSummary(tableName, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, view, ds));
            } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                datasourceRequest.setQuery(qp.getSQLStack(tableName, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, ds, view));
            } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                datasourceRequest.setQuery(qp.getSQLScatter(tableName, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extBubble, extStack, ds, view));
            } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                datasourceRequest.setQuery(qp.getSQLTableInfo(tableName, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
            } else if (StringUtils.equalsIgnoreCase("bar-time-range", view.getType())) {

                datasourceRequest.setQuery(qp.getSQLRangeBar(tableName, xAxisBase, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, extStack, ds, view));

            } else {
                datasourceRequest.setQuery(qp.getSQL(tableName, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view));
                if (containDetailField(view) && CollectionUtils.isNotEmpty(viewFields)) {
                    detailFieldList.addAll(xAxis);
                    detailFieldList.addAll(viewFields);
                    String resultMode = view.getResultMode();
                    view.setResultMode(null);
                    detailFieldSql = qp.getSQLTableInfo(tableName, detailFieldList, fieldCustomFilter, rowPermissionsTree, extFilterList, ds, view);
                    view.setResultMode(resultMode);
                }
            }
            if (CollectionUtils.isNotEmpty(assistFields)) {
                datasourceAssistRequest.setQuery(assistSQL(datasourceRequest.getQuery(), assistFields, ds));
                logger.info(datasourceAssistRequest.getQuery());
                assistData = datasourceProvider.getData(datasourceAssistRequest);
            }
            // 仪表板有参数不使用缓存
            if (!cache || CollectionUtils.isNotEmpty(chartExtRequest.getFilter())
                    || CollectionUtils.isNotEmpty(chartExtRequest.getLinkageFilters())
                    || CollectionUtils.isNotEmpty(chartExtRequest.getOuterParamsFilters())
                    || CollectionUtils.isNotEmpty(chartExtRequest.getDrill())
                    || CollectionUtils.isNotEmpty(rowPermissionsTree)
                    || fields.size() != columnPermissionFields.size()) {
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
            if (StringUtils.isNotBlank(detailFieldSql)) {
                datasourceRequest.setQuery(detailFieldSql);
                detailData = datasourceProvider.getData(datasourceRequest);
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
        List<ChartViewFieldDTO> tempYAxis = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(xAxis) && StringUtils.equals(xAxis.get(0).getGroupType(), "q") && StringUtils.equalsIgnoreCase(view.getRender(), "antv")) {
            //针对散点图scatter处理
            tempYAxis.add(xAxis.get(0));
        }
        tempYAxis.addAll(yAxis);

        for (int i = 0; i < tempYAxis.size(); i++) {
            ChartViewFieldDTO chartViewFieldDTO = tempYAxis.get(i);
            ChartFieldCompareDTO compareCalc = chartViewFieldDTO.getCompareCalc();
            if (ObjectUtils.isEmpty(compareCalc)) {
                continue;
            }
            if (StringUtils.isNotEmpty(compareCalc.getType())
                    && !StringUtils.equalsIgnoreCase(compareCalc.getType(), "none")) {
                String compareFieldId = compareCalc.getField();// 选中字段
                // 计算指标对应的下标
                int dataIndex = 0;// 数据字段下标
                if (CollectionUtils.isNotEmpty(xAxis) && StringUtils.equals(xAxis.get(0).getGroupType(), "q") && StringUtils.equalsIgnoreCase(view.getRender(), "antv")) {
                    dataIndex = extStack.size() + i;
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
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

        // 构建结果
        Map<String, Object> map = new TreeMap<>();
        // 图表组件可再扩展
        Map<String, Object> mapChart = new HashMap<>();
        if (StringUtils.equalsIgnoreCase(view.getRender(), "echarts")) {
            // reverse data
            if (StringUtils.containsIgnoreCase(view.getType(), "horizontal")) {
                Collections.reverse(data);
            }
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
            if (StringUtils.equalsAnyIgnoreCase(view.getType(), "bar-group", "line")) {
                mapChart = ChartDataBuild.transBaseGroupDataAntV(xAxisBase, xAxis, xAxisExt, yAxis, view, data, isDrill);
            } else if (StringUtils.equalsIgnoreCase(view.getType(), "bar-group-stack")) {
                mapChart = ChartDataBuild.transGroupStackDataAntV(xAxisBase, xAxis, xAxisExt, yAxis, extStack, data, view, isDrill);
            } else if (StringUtils.equalsIgnoreCase(view.getType(), "bar-time-range")) {
                mapChart = ChartDataBuild.transTimeBarDataAntV(xAxisBase, xAxis, xAxisExt, yAxis, extStack, data, view, isDrill, drillRequestList);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "bar-stack")) {
                mapChart = ChartDataBuild.transStackChartDataAntV(xAxis, yAxis, view, data, extStack, isDrill);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "line-stack")) {
                mapChart = ChartDataBuild.transStackChartDataAntV(xAxis, yAxis, view, data, extStack, isDrill);
            } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                mapChart = ChartDataBuild.transScatterDataAntV(xAxis, yAxis, view, data, extBubble, extStack, isDrill);
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
            } else if (StringUtils.containsIgnoreCase(view.getType(), "bidirectional-bar")) {
                mapChart = ChartDataBuild.transBidirectionalBarData(xAxis, yAxis, view, data, isDrill);
            } else {
                mapChart = ChartDataBuild.transChartDataAntV(xAxis, yAxis, view, data, isDrill);
            }
        }
        // table组件，明细表，也用于导出数据
        Map<String, Object> mapTableNormal = null;
        if (CollectionUtils.isNotEmpty(detailData)) {
            mapTableNormal = ChartDataBuild.transTableNormalWithDetail(xAxis, yAxis, data, detailFieldList, detailData, desensitizationList);
        } else {
            if (StringUtils.equalsIgnoreCase(view.getType(), "bar-time-range") && CollectionUtils.isNotEmpty(drillRequestList) && CollectionUtils.isNotEmpty(xAxisExt)) { // 针对区间条形图，还需要给xAxis排个序, 把xAxisExt放到最后
                int count = 0;
                for (int i = xAxis.size() - drillRequestList.size() - 1; i >= 0; i--) {
                    xAxis.remove(i);
                    count++;
                    if (xAxisExt.size() == count) {
                        break;
                    }
                }
                xAxis.addAll(xAxisExt);
            }

            mapTableNormal = ChartDataBuild.transTableNormal(xAxis, yAxis, view, data, extStack, desensitizationList);
        }
        chartViewDTO = uniteViewResult(datasourceRequest.getQuery(), mapChart, mapTableNormal, view, isDrill, drillFilters, dynamicAssistFields, assistData);
        chartViewDTO.setTotalPage(totalPage);
        chartViewDTO.setTotalItems(totalItems);
        return chartViewDTO;
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

    public String assistSQL(String sql, List<ChartViewFieldDTO> assistFields, Datasource ds) {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(ds.getType());
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < assistFields.size(); i++) {
            ChartViewFieldDTO dto = assistFields.get(i);
            if (i == (assistFields.size() - 1)) {
                stringBuilder.append(dto.getSummary() + "(" + datasourceType.getKeywordPrefix() + dto.getOriginName() + datasourceType.getKeywordSuffix() + ")");
            } else {
                stringBuilder.append(dto.getSummary() + "(" + datasourceType.getKeywordPrefix() + dto.getOriginName() + datasourceType.getKeywordSuffix() + "),");
            }
        }
        return "SELECT " + stringBuilder + " FROM (" + sql + ") tmp";
    }

    public ChartViewDTO uniteViewResult(String sql, Map<String, Object> chartData, Map<String, Object> tableData, ChartViewDTO view, Boolean isDrill, List<ChartExtFilterRequest> drillFilters, List<ChartSeniorAssistDTO> dynamicAssistFields, List<String[]> assistData) {

        Map<String, Object> map = new HashMap<>();
        map.putAll(chartData);
        map.putAll(tableData);

        List<DatasetTableField> sourceFields = dataSetTableFieldsService.getFieldsByTableId(view.getTableId());
        map.put("sourceFields", sourceFields);
        // merge assist result
        mergeAssistField(dynamicAssistFields, assistData);
        map.put("dynamicAssistData", dynamicAssistFields);

        ChartViewDTO dto = new ChartViewDTO();
        BeanUtils.copyBean(dto, view);
        dto.setData(map);
        dto.setSql(java.util.Base64.getEncoder().encodeToString(sql.getBytes()));
        dto.setDrill(isDrill);
        dto.setDrillFilters(drillFilters);
        return dto;
    }

    private PluginViewParam buildPluginParam(Map<String, List<ChartViewFieldDTO>> fieldMap, FilterTreeObj customFilters, List<ChartExtFilterRequest> extFilters, Datasource ds, DatasetTable table, ChartViewDTO view, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, ChartExtRequest chartExtRequest) {
        PluginViewParam pluginViewParam = new PluginViewParam();
        PluginViewSetImpl pluginViewSet = BeanUtils.copyBean(new PluginViewSetImpl(), table);
        pluginViewSet.setDsType(ds.getType());
        pluginViewSet.setTableId(table.getId());
        pluginViewSet.setDs(ds);
        pluginViewSet.setChartExtRequest(gson.toJson(chartExtRequest));
        PluginViewLimit pluginViewLimit = BeanUtils.copyBean(new PluginViewLimit(), view);


        PluginFilterTreeObj fieldFilters = gson.fromJson(gson.toJson(customFilters), PluginFilterTreeObj.class);
        List<PluginChartExtFilter> panelFilters = extFilters.stream().map(filter -> gson.fromJson(gson.toJson(filter), PluginChartExtFilter.class)).collect(Collectors.toList());

        List<PluginViewField> pluginViewFields = fieldMap.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(field -> {
            PluginViewField pluginViewField = BeanUtils.copyBean(new PluginViewField(), field);
            pluginViewField.setFilter(gson.fromJson(gson.toJson(field.getFilter()), new TypeToken<List<PluginChartCustomFilterItem>>() {
            }.getType()));
            pluginViewField.setTypeField(entry.getKey());
            return pluginViewField;
        })).collect(Collectors.toList());
        pluginViewParam.setPluginViewSet(pluginViewSet);
        pluginViewParam.setPluginViewFields(pluginViewFields);
        pluginViewParam.setPluginChartFieldCustomFilters(fieldFilters);
        pluginViewParam.setPluginChartExtFilters(panelFilters);
        pluginViewParam.setPluginViewLimit(pluginViewLimit);
        pluginViewParam.setRowPermissionsTree(rowPermissionsTree);
        return pluginViewParam;
    }

    private ViewPluginService getPluginService(String viewType) {
        Map<String, ViewPluginService> beanMap = SpringContextUtil.getApplicationContext().getBeansOfType(ViewPluginService.class);

        if (beanMap.keySet().size() == 0) {
            DataEaseException.throwException("没有此插件");

        }
        ViewPluginService viewPluginService = null;
        for (Map.Entry<String, ViewPluginService> entry : beanMap.entrySet()) {
            if (StringUtils.equals(entry.getValue().viewType().getValue(), viewType)) {
                viewPluginService = entry.getValue();
                return viewPluginService;
            }
        }
        if (null == viewPluginService) DataEaseException.throwException("没有此插件");
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

    private boolean checkDrillExist(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> extStack, String fieldId, ChartViewWithBLOBs view) {

        if (CollectionUtils.isNotEmpty(xAxis)) {
            //针对判断 antv 散点图
            if (StringUtils.equals(xAxis.get(0).getGroupType(), "q") && StringUtils.equalsIgnoreCase(view.getRender(), "antv")) {
                if (CollectionUtils.isNotEmpty(extStack)) {
                    for (ChartViewFieldDTO x : extStack) {
                        if (StringUtils.equalsIgnoreCase(x.getId(), fieldId)) {
                            return true;
                        }
                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(xAxis)) {
            for (ChartViewFieldDTO x : xAxis) {
                if (StringUtils.equalsIgnoreCase(x.getId(), fieldId)) {
                    return true;
                }
            }
        }
        if (StringUtils.containsIgnoreCase(view.getType(), "stack") && CollectionUtils.isNotEmpty(extStack)) {
            for (ChartViewFieldDTO x : extStack) {
                if (StringUtils.equalsIgnoreCase(x.getId(), fieldId)) {
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
        switch (fieldType) {
            case "xaxis": {
                fieldList = gson.fromJson(view.getXAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
                }.getType());
                break;
            }
            case "extStack": {
                fieldList = gson.fromJson(view.getExtStack(), new TypeToken<List<ChartViewFieldDTO>>() {
                }.getType());
                break;
            }
            case "xaxisExt": {
                fieldList = gson.fromJson(view.getXAxisExt(), new TypeToken<List<ChartViewFieldDTO>>() {
                }.getType());
                break;
            }
            default:
                return Collections.emptyList();
        }
        DatasetTableField field = dataSetTableFieldsService.get(fieldId);

        List<String> res = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(field) && !fieldList.isEmpty()) {
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
            boolean skipAddIndex = false;
            if (StringUtils.equalsIgnoreCase(fieldType, "extStack") && StringUtils.equalsIgnoreCase("antv", view.getRender()) && StringUtils.equalsIgnoreCase("scatter", view.getType())) {
                List<ChartViewFieldDTO> xAxis = gson.fromJson(view.getXAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
                }.getType());
                if (CollectionUtils.isNotEmpty(xAxis) && StringUtils.equalsIgnoreCase(xAxis.get(0).getGroupType(), "q")) {
                    skipAddIndex = true;
                }
            }
            if (StringUtils.equalsIgnoreCase(fieldType, "extStack") && !skipAddIndex) {
                List<ChartViewFieldDTO> xaxis = gson.fromJson(view.getXAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
                }.getType());
                index += xaxis.size();
                getIndex += xaxis.size();
                if (StringUtils.containsIgnoreCase(view.getType(), "group")) {
                    List<ChartViewFieldDTO> ext = gson.fromJson(view.getXAxisExt(), new TypeToken<List<ChartViewFieldDTO>>() {
                    }.getType());
                    index += ext.size();
                    getIndex += ext.size();
                }
            }
            if (StringUtils.equalsIgnoreCase(fieldType, "xaxisExt") && !skipAddIndex) {
                List<ChartViewFieldDTO> xaxis = gson.fromJson(view.getXAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
                }.getType());
                index += xaxis.size();
                getIndex += xaxis.size();
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

    public String preHandleVariable(String sql, ChartExtRequest requestList, QueryProvider qp, DataSetTableDTO table) {
        List<SqlVariableDetails> sqlVariables = gson.fromJson(table.getSqlVariableDetails(), new TypeToken<List<SqlVariableDetails>>() {
        }.getType());
        if (requestList != null && CollectionUtils.isNotEmpty(requestList.getFilter())) {
            for (ChartExtFilterRequest chartExtFilterRequest : requestList.getFilter()) {
                if (CollectionUtils.isEmpty(chartExtFilterRequest.getValue())) {
                    continue;
                }
                if (CollectionUtils.isEmpty(chartExtFilterRequest.getParameters())) {
                    continue;
                }

                boolean isEndParam = false;
                for (String parameter : chartExtFilterRequest.getParameters()) {
                    if (parameter.contains("|DE|")) {
                        String[] parameterArray = parameter.split("\\|DE\\|");
                        if (!parameterArray[0].equals(table.getId())) {
                            continue;
                        }
                        String paramName = null;
                        if (parameterArray.length > 1) {
                            paramName = parameterArray[1];
                            if (paramName.contains(START_END_SEPARATOR)) {
                                String[] paramNameArray = paramName.split(START_END_SEPARATOR);
                                paramName = paramNameArray[0];
                                isEndParam = true;
                            }
                        } else {
                            continue;
                        }
                        final String finalParamName = paramName;
                        List<SqlVariableDetails> parameters = sqlVariables.stream().filter(item -> item.getVariableName().equalsIgnoreCase(finalParamName)).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(parameters)) {
                            String filter = null;
                            if (isEndParam) {
                                filter = transEndParamSql(chartExtFilterRequest, parameters.get(0));
                            } else {
                                filter = qp.transFilter(chartExtFilterRequest, parameters.get(0));
                            }
                            sql = sql.replace("${" + finalParamName + "}", filter);
                        }
                    } else {
                        List<SqlVariableDetails> parameters = sqlVariables.stream().filter(item -> item.getVariableName().equalsIgnoreCase(parameter)).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(parameters)) {
                            String filter = qp.transFilter(chartExtFilterRequest, parameters.get(0));
                            sql = sql.replace("${" + parameter + "}", filter);
                        }
                    }
                }
            }
        }
        return sql;
    }

    private String handleVariable(String sql, ChartExtRequest requestList, QueryProvider qp, DataSetTableDTO table, Datasource ds) throws Exception {
        sql = preHandleVariable(sql, requestList, qp, table);
        sql = dataSetTableService.handleVariableDefaultValue(sql, null, ds.getType(), false);
        return sql;
    }

    public String transEndParamSql(ChartExtFilterRequest chartExtFilterRequest, SqlVariableDetails sqlVariableDetails) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(sqlVariableDetails.getType().size() > 1 ? sqlVariableDetails.getType().get(1) : "YYYY");
        return simpleDateFormat.format(new Date(Long.parseLong(chartExtFilterRequest.getValue().get(1))));
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

    private ChartViewFieldDTO getDynamicField(JSONObject sizeObj, String type, String field) {
        String maxType = sizeObj.getString(type);
        if (StringUtils.equalsIgnoreCase("dynamic", maxType)) {
            JSONObject maxField = sizeObj.getJSONObject(field);
            String id = maxField.getString("id");
            String summary = maxField.getString("summary");
            DatasetTableField datasetTableField = dataSetTableFieldsService.get(id);
            if (ObjectUtils.isNotEmpty(datasetTableField)) {
                if (datasetTableField.getDeType() == 0 || datasetTableField.getDeType() == 1 || datasetTableField.getDeType() == 5) {
                    if (!StringUtils.containsIgnoreCase(summary, "count")) {
                        DataEaseException.throwException(Translator.get("i18n_gauge_field_change"));
                    }
                }
                ChartViewFieldDTO dto = new ChartViewFieldDTO();
                BeanUtils.copyBean(dto, datasetTableField);
                dto.setSummary(summary);
                return dto;
            } else {
                DataEaseException.throwException(Translator.get("i18n_gauge_field_delete"));
            }
        }
        return null;
    }

    private List<ChartSeniorAssistDTO> getDynamicAssistFields(ChartViewDTO view) {
        String senior = view.getSenior();
        List<ChartSeniorAssistDTO> list = new ArrayList<>();
        if (StringUtils.isEmpty(senior)) {
            return list;
        }
        JSONObject jsonObject = JSONObject.parseObject(senior);
        JSONArray assistLine = jsonObject.getJSONArray("assistLine");
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

    private List<ChartViewFieldDTO> getAssistFields(List<ChartSeniorAssistDTO> list, List<ChartViewFieldDTO> yAxis, List<ChartViewFieldDTO> xAxis) {
        List<ChartViewFieldDTO> res = new ArrayList<>();
        for (ChartSeniorAssistDTO dto : list) {
            DatasetTableField curField = dto.getCurField();
            ChartViewFieldDTO field = null;
            String alias = "";
            for (int i = 0; i < yAxis.size(); i++) {
                ChartViewFieldDTO yField = yAxis.get(i);
                if (StringUtils.equalsIgnoreCase(yField.getId(), curField.getId())) {
                    field = yField;
                    alias = String.format(SQLConstants.FIELD_ALIAS_Y_PREFIX, i);
                    break;
                }
            }

            if (ObjectUtils.isEmpty(field) && CollectionUtils.isNotEmpty(xAxis)) {
                for (int i = 0; i < xAxis.size(); i++) {
                    ChartViewFieldDTO xField = xAxis.get(i);
                    if (StringUtils.equalsIgnoreCase(xField.getId(), curField.getId())) {
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
            chartViewFieldDTO.setOriginName(alias);// 字段别名，就是查找的字段名
            res.add(chartViewFieldDTO);
        }
        return res;
    }

    private void mergeAssistField(List<ChartSeniorAssistDTO> dynamicAssistFields, List<String[]> assistData) {
        if (CollectionUtils.isEmpty(assistData)) {
            return;
        }
        String[] strings = assistData.get(0);
        for (int i = 0, size = Math.min(dynamicAssistFields.size(), strings.length); i < size; i++) {
            ChartSeniorAssistDTO chartSeniorAssistDTO = dynamicAssistFields.get(i);
            chartSeniorAssistDTO.setValue(strings[i]);
        }
    }

    private List<ChartSeniorAssistDTO> getDynamicThresholdFields(ChartViewDTO view) {
        String senior = view.getSenior();
        JSONObject jsonObject = JSONObject.parseObject(senior);
        List<ChartSeniorAssistDTO> list = new ArrayList<>();
        JSONObject threshold = jsonObject.getJSONObject("threshold");
        if (ObjectUtils.isEmpty(threshold) || StringUtils.isBlank(threshold.toJSONString())) {
            return list;
        }
        JSONArray tableThreshold = threshold.getJSONArray("tableThreshold");
        if (ObjectUtils.isEmpty(tableThreshold) || StringUtils.isBlank(tableThreshold.toJSONString())) {
            return list;
        }

        for (int i = 0; i < tableThreshold.size(); i++) {
            JSONObject item = tableThreshold.getJSONObject(i);
            JSONArray itemConditions = item.getJSONArray("conditions");
            if (ObjectUtils.isEmpty(itemConditions) || StringUtils.isBlank(itemConditions.toJSONString())) {
                continue;
            }
            List<ChartSeniorThresholdDTO> conditions = gson.fromJson(itemConditions.toJSONString(), new TypeToken<List<ChartSeniorThresholdDTO>>() {
            }.getType());
            for (ChartSeniorThresholdDTO condition : conditions) {
                if (StringUtils.equalsIgnoreCase(condition.getField(), "0")) {
                    continue;
                }

                for (ChartSeniorAssistDTO fieldDTO : getConditionFields(condition)) {
                    if (solveThresholdCondition(fieldDTO, view.getTableId())) {
                        list.add(fieldDTO);
                    }
                }
            }
        }

        return list;
    }

    private boolean solveThresholdCondition(ChartSeniorAssistDTO fieldDTO, String tableId) {
        String fieldId = fieldDTO.getFieldId();
        String summary = fieldDTO.getSummary();
        if (StringUtils.isEmpty(fieldId) || StringUtils.isEmpty(summary)) {
            return false;
        }
        DatasetTableFieldExample datasetTableFieldExample = new DatasetTableFieldExample();
        datasetTableFieldExample.createCriteria().andTableIdEqualTo(tableId).andIdEqualTo(fieldId);
        List<DatasetTableField> fieldList = datasetTableFieldMapper.selectByExample(datasetTableFieldExample);
        if (CollectionUtils.isEmpty(fieldList)) {
            return false;
        }
        fieldDTO.setCurField(fieldList.get(0));
        return true;
    }

    private List<ChartSeniorAssistDTO> getConditionFields(ChartSeniorThresholdDTO condition) {
        List<ChartSeniorAssistDTO> list = new ArrayList<>();
        if (StringUtils.equalsIgnoreCase(condition.getField(), "0")) {
            return list;
        }
        if ("between".equals(condition.getTerm())) {
            if (!StringUtils.equalsIgnoreCase(condition.getMaxField().getSummary(), "value")) {
                list.add(condition.getMaxField());
            }
            if (!StringUtils.equalsIgnoreCase(condition.getMinField().getSummary(), "value")) {
                list.add(condition.getMinField());
            }
        } else {
            if (!StringUtils.equalsIgnoreCase(condition.getTargetField().getSummary(), "value")) {
                list.add(condition.getTargetField());
            }
        }
        return list;
    }

    public void tranChartFilter(ChartViewDTO view) {
        Type filterTokenType = new TypeToken<List<ChartFieldCustomFilterDTO>>() {
        }.getType();

        List<ChartFieldCustomFilterDTO> fieldCustomFilter;
        // 尝试将历史数据转成list，如果转换出现异常，则忽略该视图继续执行下一个
        try {
            fieldCustomFilter = gson.fromJson(view.getCustomFilter(), filterTokenType);
        } catch (Exception e) {
            return;
        }

        if (CollectionUtils.isEmpty(fieldCustomFilter)) {
            // 将 '[]' 转换成 '{}'
            view.setCustomFilter("{}");
        } else {
            // array -> tree
            FilterTreeObj tree = chartViewOldDataMergeService.transArr2Obj(fieldCustomFilter);
            view.setCustomFilter(gson.toJson(tree));
        }
    }

    public void buildDsType(Datasource datasource, ChartViewDTO result) {
        if (datasource != null) {
            if (StringUtils.equalsIgnoreCase(datasource.getType(), "sqlServer")) {
                if (datasource.getVersion() == null) {
                    result.setDatasourceType(datasource.getType());
                } else {
                    if (Integer.parseInt(datasource.getVersion()) < 11) {
                        result.setDatasourceType(datasource.getType() + "_all");
                    } else {
                        result.setDatasourceType(datasource.getType());
                    }
                }
            } else {
                result.setDatasourceType(datasource.getType());
            }
        } else {
            result.setDatasourceType(null);
        }
    }
}

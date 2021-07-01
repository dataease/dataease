package io.dataease.service.chart;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.dataease.base.domain.*;
import io.dataease.base.mapper.ChartViewMapper;
import io.dataease.base.mapper.ext.ExtChartGroupMapper;
import io.dataease.base.mapper.ext.ExtChartViewMapper;
import io.dataease.commons.constants.JdbcConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.controller.request.chart.ChartExtFilterRequest;
import io.dataease.controller.request.chart.ChartExtRequest;
import io.dataease.controller.request.chart.ChartGroupRequest;
import io.dataease.controller.request.chart.ChartViewRequest;
import io.dataease.datasource.provider.DatasourceProvider;
import io.dataease.datasource.provider.ProviderFactory;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.datasource.service.DatasourceService;
import io.dataease.dto.chart.*;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.i18n.Translator;
import io.dataease.listener.util.CacheUtils;
import io.dataease.provider.QueryProvider;
import io.dataease.service.dataset.DataSetTableFieldsService;
import io.dataease.service.dataset.DataSetTableService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @Author gin
 * @Date 2021/3/1 12:34 下午
 */
@Service
public class ChartViewService {
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

    //默认使用非公平
    private ReentrantLock lock = new ReentrantLock();

    public ChartViewWithBLOBs save(ChartViewWithBLOBs chartView) {
        checkName(chartView);
        long timestamp = System.currentTimeMillis();
        chartView.setUpdateTime(timestamp);
        int i = chartViewMapper.updateByPrimaryKeySelective(chartView);
        if (i == 0) {
            chartView.setId(UUID.randomUUID().toString());
            chartView.setCreateBy(AuthUtils.getUser().getUsername());
            chartView.setCreateTime(timestamp);
            chartView.setUpdateTime(timestamp);
            chartViewMapper.insertSelective(chartView);
        }
        Optional.ofNullable(chartView.getId()).ifPresent(id -> {
            CacheUtils.remove(JdbcConstants.VIEW_CACHE_KEY, id);
        });

        return chartView;
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

    public ChartViewWithBLOBs get(String id) {
        return chartViewMapper.selectByPrimaryKey(id);
    }

    public void delete(String id) {
        chartViewMapper.deleteByPrimaryKey(id);
    }

    public void deleteBySceneId(String sceneId) {
        ChartViewExample chartViewExample = new ChartViewExample();
        chartViewExample.createCriteria().andSceneIdEqualTo(sceneId);
        chartViewMapper.deleteByExample(chartViewExample);
    }

    public ChartViewDTO getData(String id, ChartExtRequest requestList) throws Exception {
        ChartViewWithBLOBs view = chartViewMapper.selectByPrimaryKey(id);
        if (ObjectUtils.isEmpty(view)) {
            throw new RuntimeException(Translator.get("i18n_chart_delete"));
        }
        List<ChartViewFieldDTO> xAxis = new Gson().fromJson(view.getXAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType());
        List<ChartViewFieldDTO> yAxis = new Gson().fromJson(view.getYAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType());
        List<ChartCustomFilterDTO> customFilter = new Gson().fromJson(view.getCustomFilter(), new TypeToken<List<ChartCustomFilterDTO>>() {
        }.getType());
        customFilter.forEach(ele -> ele.setField(dataSetTableFieldsService.get(ele.getFieldId())));

        if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType())) {
            xAxis = new ArrayList<>();
            if (CollectionUtils.isEmpty(yAxis)) {
                ChartViewDTO dto = new ChartViewDTO();
                BeanUtils.copyBean(dto, view);
                return dto;
            }
        } else {
            if (CollectionUtils.isEmpty(xAxis) || CollectionUtils.isEmpty(yAxis)) {
                ChartViewDTO dto = new ChartViewDTO();
                BeanUtils.copyBean(dto, view);
                return dto;
            }
        }

        // 过滤来自仪表板的条件
        List<ChartExtFilterRequest> extFilterList = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(requestList.getFilter())) {
            for (ChartExtFilterRequest request : requestList.getFilter()) {
                DatasetTableField datasetTableField = dataSetTableFieldsService.get(request.getFieldId());
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

        // 获取数据集
        DatasetTable table = dataSetTableService.get(view.getTableId());
        if (ObjectUtils.isEmpty(table)) {
            throw new RuntimeException(Translator.get("i18n_dataset_delete"));
        }
        // 判断连接方式，直连或者定时抽取 table.mode
        List<String[]> data = new ArrayList<>();
        if (table.getMode() == 0) {// 直连
            Datasource ds = datasourceService.get(table.getDataSourceId());
            if (ObjectUtils.isEmpty(ds)) {
                throw new RuntimeException(Translator.get("i18n_datasource_delete"));
            }
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(table.getInfo(), DataTableInfoDTO.class);
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            if (StringUtils.equalsIgnoreCase(table.getType(), "db")) {
                datasourceRequest.setTable(dataTableInfoDTO.getTable());
                if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLSummary(dataTableInfoDTO.getTable(), yAxis, customFilter, extFilterList));
                } else {
                    datasourceRequest.setQuery(qp.getSQL(dataTableInfoDTO.getTable(), xAxis, yAxis, customFilter, extFilterList));
                }
            } else if (StringUtils.equalsIgnoreCase(table.getType(), "sql")) {
                if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLSummaryAsTmp(dataTableInfoDTO.getSql(), yAxis, customFilter, extFilterList));
                } else {
                    datasourceRequest.setQuery(qp.getSQLAsTmp(dataTableInfoDTO.getSql(), xAxis, yAxis, customFilter, extFilterList));
                }
            }
            data = datasourceProvider.getData(datasourceRequest);
            /**
             * 直连不实用缓存
            String key = "provider_sql_"+datasourceRequest.getDatasource().getId() + "_" + datasourceRequest.getTable() + "_" +datasourceRequest.getQuery();
            Object cache;
            if ((cache = CacheUtils.get(JdbcConstants.JDBC_PROVIDER_KEY, key)) == null) {
                data = datasourceProvider.getData(datasourceRequest);
                CacheUtils.put(JdbcConstants.JDBC_PROVIDER_KEY,key ,data, null, null);
            }else {
                data = (List<String[]>) cache;
            }
             */
        } else if (table.getMode() == 1) {// 抽取
            // 连接doris，构建doris数据源查询
            Datasource ds = (Datasource) CommonBeanFactory.getBean("DorisDatasource");
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            String tableName = "ds_" + table.getId().replaceAll("-", "_");
            datasourceRequest.setTable(tableName);
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType())) {
                datasourceRequest.setQuery(qp.getSQLSummary(tableName, yAxis, customFilter, extFilterList));
            } else {
                datasourceRequest.setQuery(qp.getSQL(tableName, xAxis, yAxis, customFilter, extFilterList));
            }
            /*// 定时抽取使用缓存
            Object cache;
            // 仪表板有参数不实用缓存
            if (CollectionUtils.isNotEmpty(requestList.getFilter())) {
                data = datasourceProvider.getData(datasourceRequest);
            }
            // 仪表板无参数 且 未缓存过该视图 则查询后缓存
            else if ((cache = CacheUtils.get(JdbcConstants.VIEW_CACHE_KEY, id)) == null) {
                lock.lock();
                data = datasourceProvider.getData(datasourceRequest);
                CacheUtils.put(JdbcConstants.VIEW_CACHE_KEY, id, data, null, null);
            }
            // 仪表板有缓存 使用缓存
            else {
                data = (List<String[]>) cache;
            }*/
            data = cacheViewData(datasourceProvider, datasourceRequest, id);
        }
        if (StringUtils.containsIgnoreCase(view.getType(), "pie") && data.size() > 1000) {
            data = data.subList(0, 1000);
        }

        // 图表组件可再扩展
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
            for (int i = 0; i < xAxis.size(); i++) {
                if (i == xAxis.size() - 1) {
                    a.append(d[i]);
                } else {
                    a.append(d[i]).append("\n");
                }
            }
            x.add(a.toString());
            for (int i = xAxis.size(); i < xAxis.size() + yAxis.size(); i++) {
                int j = i - xAxis.size();
                try {
                    series.get(j).getData().add(new BigDecimal(StringUtils.isEmpty(d[i]) ? "0" : d[i]));
                } catch (Exception e) {
                    series.get(j).getData().add(new BigDecimal(0));
                }
            }
        }
        // table组件
        List<ChartViewFieldDTO> fields = new ArrayList<>();
        List<Map<String, Object>> tableRow = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(xAxis)) {
            fields.addAll(xAxis);
        }
        fields.addAll(yAxis);
        data.forEach(ele -> {
            Map<String, Object> d = new HashMap<>();
            for (int i = 0; i < fields.size(); i++) {
                ChartViewFieldDTO chartViewFieldDTO = fields.get(i);
                if (chartViewFieldDTO.getDeType() == 0 || chartViewFieldDTO.getDeType() == 1) {
                    d.put(fields.get(i).getDataeaseName(), StringUtils.isEmpty(ele[i]) ? "" : ele[i]);
                } else if (chartViewFieldDTO.getDeType() == 2 || chartViewFieldDTO.getDeType() == 3) {
                    d.put(fields.get(i).getDataeaseName(), new BigDecimal(StringUtils.isEmpty(ele[i]) ? "0" : ele[i]).setScale(2, RoundingMode.HALF_UP));
                }
            }
            tableRow.add(d);
        });

        Map<String, Object> map = new HashMap<>();
        map.put("x", x);
        map.put("series", series);
        map.put("fields", fields);
        map.put("tableRow", tableRow);

        ChartViewDTO dto = new ChartViewDTO();
        BeanUtils.copyBean(dto, view);
        dto.setData(map);
        return dto;
    }

    /**
     * 避免缓存击穿
     * 虽然流量不一定能够达到击穿的水平
     * @param datasourceProvider
     * @param datasourceRequest
     * @param viewId
     * @return
     * @throws Exception
     */
    public List<String[]> cacheViewData(DatasourceProvider datasourceProvider, DatasourceRequest datasourceRequest, String viewId) throws Exception{
        List<String[]> result ;
        Object cache = CacheUtils.get(JdbcConstants.VIEW_CACHE_KEY, viewId);
        if (cache == null) {
            if (lock.tryLock()) {// 获取锁成功
                result = datasourceProvider.getData(datasourceRequest);
                if (result != null) {
                    CacheUtils.put(JdbcConstants.VIEW_CACHE_KEY, viewId, result, null, null);
                }
                lock.unlock();
            }else {//获取锁失败
                Thread.sleep(100);//避免CAS自旋频率过大 占用cpu资源过高
                result = cacheViewData(datasourceProvider, datasourceRequest, viewId);
            }
        }else {
            result = (List<String[]>)cache;
        }
        return result;
    }

    private void checkName(ChartViewWithBLOBs chartView) {
//        if (StringUtils.isEmpty(chartView.getId())) {
//            return;
//        }
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
        List<ChartViewWithBLOBs> list = chartViewMapper.selectByExampleWithBLOBs(chartViewExample);
        if (list.size() > 0) {
            throw new RuntimeException(Translator.get("i18n_name_cant_repeat_same_group"));
        }
    }

    public Map<String, Object> getChartDetail(String id) {
        Map<String, Object> map = new HashMap<>();
        ChartViewWithBLOBs chartViewWithBLOBs = chartViewMapper.selectByPrimaryKey(id);
        map.put("chart", chartViewWithBLOBs);
        if (ObjectUtils.isNotEmpty(chartViewWithBLOBs)) {
            Map<String, Object> datasetDetail = dataSetTableService.getDatasetDetail(chartViewWithBLOBs.getTableId());
            map.putAll(datasetDetail);
        }
        return map;
    }

    public List<ChartView> viewsByIds(List<String> viewIds) {
        ChartViewExample example = new ChartViewExample();
        example.createCriteria().andIdIn(viewIds);
        return chartViewMapper.selectByExample(example);
    }

    public ChartViewWithBLOBs findOne(String id) {
        return chartViewMapper.selectByPrimaryKey(id);
    }

    public String chartCopy(String id) {
        String newChartId = UUID.randomUUID().toString();
       extChartViewMapper.chartCopy(newChartId,id);
        return newChartId;
    }
}

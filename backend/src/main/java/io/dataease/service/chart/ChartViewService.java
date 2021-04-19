package io.dataease.service.chart;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.dataease.base.domain.*;
import io.dataease.base.mapper.ChartViewMapper;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.request.chart.ChartExtFilterRequest;
import io.dataease.controller.request.chart.ChartExtRequest;
import io.dataease.controller.request.chart.ChartViewRequest;
import io.dataease.datasource.constants.DatasourceTypes;
import io.dataease.datasource.provider.DatasourceProvider;
import io.dataease.datasource.provider.ProviderFactory;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.datasource.service.DatasourceService;
import io.dataease.dto.chart.ChartViewDTO;
import io.dataease.dto.chart.ChartViewFieldDTO;
import io.dataease.dto.chart.Series;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.service.dataset.DataSetTableFieldsService;
import io.dataease.service.dataset.DataSetTableService;
import io.dataease.service.spark.SparkCalc;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;

/**
 * @Author gin
 * @Date 2021/3/1 12:34 下午
 */
@Service
public class ChartViewService {
    @Resource
    private ChartViewMapper chartViewMapper;
    @Resource
    private DataSetTableService dataSetTableService;
    @Resource
    private DatasourceService datasourceService;
    @Resource
    private SparkCalc sparkCalc;
    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;

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
            chartViewMapper.insert(chartView);
        }
        return chartView;
    }

    public List<ChartViewWithBLOBs> list(ChartViewRequest chartViewRequest) {
        ChartViewExample chartViewExample = new ChartViewExample();
        ChartViewExample.Criteria criteria = chartViewExample.createCriteria();
        criteria.andCreateByEqualTo(AuthUtils.getUser().getUsername());
        if (StringUtils.isNotEmpty(chartViewRequest.getSceneId())) {
            criteria.andSceneIdEqualTo(chartViewRequest.getSceneId());
        }
        if (StringUtils.isNotEmpty(chartViewRequest.getSort())) {
            chartViewExample.setOrderByClause(chartViewRequest.getSort());
        }
        return chartViewMapper.selectByExampleWithBLOBs(chartViewExample);
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
        List<ChartViewFieldDTO> xAxis = new Gson().fromJson(view.getXAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType());
        List<ChartViewFieldDTO> yAxis = new Gson().fromJson(view.getYAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType());

        List<String> x = new ArrayList<>();
        List<Series> series = new ArrayList<>();
        if (CollectionUtils.isEmpty(xAxis) || CollectionUtils.isEmpty(yAxis)) {
            ChartViewDTO dto = new ChartViewDTO();
            BeanUtils.copyBean(dto, view);
            return dto;
        }
//        List<String> xIds = xAxis.stream().map(DatasetTableField::getId).collect(Collectors.toList());
//        List<String> yIds = yAxis.stream().map(DatasetTableField::getId).collect(Collectors.toList());
//        List<DatasetTableField> xList = dataSetTableFieldsService.getListByIds(xIds);
//        List<DatasetTableField> yList = dataSetTableFieldsService.getListByIds(yIds);

        // 过滤来自仪表盘的条件
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
        // 判断连接方式，直连或者定时抽取 table.mode
        List<String[]> data = new ArrayList<>();
        if (table.getMode() == 0) {// 直连
            Datasource ds = datasourceService.get(table.getDataSourceId());
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(table.getInfo(), DataTableInfoDTO.class);
            if (StringUtils.equalsIgnoreCase(table.getType(), "db")) {
                datasourceRequest.setTable(dataTableInfoDTO.getTable());
                datasourceRequest.setQuery(getSQL(ds.getType(), dataTableInfoDTO.getTable(), xAxis, yAxis, extFilterList));
            } else if (StringUtils.equalsIgnoreCase(table.getType(), "sql")) {
                datasourceRequest.setQuery(getSQL(ds.getType(), " (" + dataTableInfoDTO.getSql() + ") AS tmp ", xAxis, yAxis, extFilterList));
            }
            data = datasourceProvider.getData(datasourceRequest);
        } else if (table.getMode() == 1) {// 抽取
            // 获取数据集de字段
            List<DatasetTableField> fields = dataSetTableFieldsService.getFieldsByTableId(table.getId());
            data = sparkCalc.getData(table.getId(), fields, xAxis, yAxis, "tmp_" + view.getId().split("-")[0], extFilterList);
        }

        // 图表组件可再扩展
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
        Map<String, Object> map = new HashMap<>();
        map.put("x", x);
        map.put("series", series);

        ChartViewDTO dto = new ChartViewDTO();
        BeanUtils.copyBean(dto, view);
        dto.setData(map);
        return dto;
    }

    public String transMysqlExtFilter(List<ChartExtFilterRequest> requestList) {
        if (CollectionUtils.isEmpty(requestList)) {
            return "";
        }
        StringBuilder filter = new StringBuilder();
        for (ChartExtFilterRequest request : requestList) {
            List<String> value = request.getValue();
            if (CollectionUtils.isEmpty(value)) {
                continue;
            }
            DatasetTableField field = request.getDatasetTableField();
            filter.append(" AND ")
                    .append(field.getOriginName())
                    .append(" ")
                    .append(transMysqlFilterTerm(request.getOperator()))
                    .append(" ");
            if (StringUtils.containsIgnoreCase(request.getOperator(), "in")) {
                filter.append("('").append(StringUtils.join(value, "','")).append("')");
            } else if (StringUtils.containsIgnoreCase(request.getOperator(), "like")) {
                filter.append("'%").append(value.get(0)).append("%'");
            } else {
                filter.append("'").append(value.get(0)).append("'");
            }
        }
        return filter.toString();
    }

    public String getSQL(String type, String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartExtFilterRequest> extFilterRequestList) {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(type);
        switch (datasourceType) {
            case mysql:
                return transMysqlSQL(table, xAxis, yAxis, extFilterRequestList);
            case sqlServer:
            default:
                return "";
        }
    }

    public String transMysqlSQL(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartExtFilterRequest> extFilterRequestList) {
        // 字段汇总 排序等
        String[] field = yAxis.stream().map(y -> "CAST(" + y.getSummary() + "(" + y.getOriginName() + ") AS DECIMAL(20,2)) AS _" + y.getSummary() + "_" + y.getOriginName()).toArray(String[]::new);
        String[] group = xAxis.stream().map(ChartViewFieldDTO::getOriginName).toArray(String[]::new);
        String[] order = yAxis.stream().filter(y -> StringUtils.isNotEmpty(y.getSort()) && !StringUtils.equalsIgnoreCase(y.getSort(), "none"))
                .map(y -> "_" + y.getSummary() + "_" + y.getOriginName() + " " + y.getSort()).toArray(String[]::new);

        String sql = MessageFormat.format("SELECT {0},{1} FROM {2} WHERE 1=1 {3} GROUP BY {4} ORDER BY null,{5}",
                StringUtils.join(group, ","),
                StringUtils.join(field, ","),
                table,
                transMysqlExtFilter(extFilterRequestList),// origin field filter and panel field filter
                StringUtils.join(group, ","),
                StringUtils.join(order, ","));
        if (sql.endsWith(",")) {
            sql = sql.substring(0, sql.length() - 1);
        }
        // 如果是对结果字段过滤，则再包裹一层sql
        String[] resultFilter = yAxis.stream().filter(y -> CollectionUtils.isNotEmpty(y.getFilter()) && y.getFilter().size() > 0)
                .map(y -> {
                    String[] s = y.getFilter().stream().map(f -> "AND _" + y.getSummary() + "_" + y.getOriginName() + transMysqlFilterTerm(f.getTerm()) + f.getValue()).toArray(String[]::new);
                    return StringUtils.join(s, " ");
                }).toArray(String[]::new);
        if (resultFilter.length == 0) {
            return sql;
        } else {
            String filterSql = MessageFormat.format("SELECT * FROM {0} WHERE 1=1 {1}",
                    "(" + sql + ") AS tmp",
                    StringUtils.join(resultFilter, " "));
            return filterSql;
        }
    }

    public String transMysqlFilterTerm(String term) {
        switch (term) {
            case "eq":
                return " = ";
            case "not_eq":
                return " <> ";
            case "lt":
                return " < ";
            case "le":
                return " <= ";
            case "gt":
                return " > ";
            case "ge":
                return " >= ";
            case "in":
                return " IN ";
            case "not in":
                return " NOT IN ";
            case "like":
                return " LIKE ";
            case "not like":
                return " NOT LIKE ";
            case "null":
                return " IS NULL ";
            case "not_null":
                return " IS NOT NULL ";
            default:
                return "";
        }
    }

    private void checkName(ChartViewWithBLOBs chartView) {
        if (StringUtils.isEmpty(chartView.getId())) {
            return;
        }
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
            throw new RuntimeException("Name can't repeat in same group.");
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
}

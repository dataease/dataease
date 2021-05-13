package io.dataease.service.chart;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.dataease.base.domain.*;
import io.dataease.base.mapper.ChartViewMapper;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.commons.utils.LogUtil;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    //    @Resource
//    private SparkCalc sparkCalc;
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
//            List<DatasetTableField> fields = dataSetTableFieldsService.getFieldsByTableId(table.getId());
//            data = sparkCalc.getData(table.getId(), fields, xAxis, yAxis, "tmp_" + view.getId().split("-")[0], extFilterList);

            // 连接doris，构建doris数据源查询
            Datasource ds = (Datasource) CommonBeanFactory.getBean("DorisDatasource");
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            String tableName = "ds_" + table.getId().replaceAll("-", "_");
            datasourceRequest.setTable(tableName);
            datasourceRequest.setQuery(transDorisSQL(tableName, xAxis, yAxis, extFilterList));
            data = datasourceProvider.getData(datasourceRequest);
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
        fields.addAll(xAxis);
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
                    .append(field.getDataeaseName())
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

    public String transDorisSQL(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartExtFilterRequest> extFilterRequestList) {
        // 字段汇总 排序等
        String[] field = yAxis.stream().map(y -> {
            StringBuilder f = new StringBuilder();
            if (StringUtils.equalsIgnoreCase(y.getDataeaseName(), "*")) {
                f.append(y.getSummary()).append("(").append(y.getDataeaseName()).append(")");
            } else {
                f.append("ROUND(")
                        .append(y.getSummary()).append("(")
                        .append("CAST(").append(y.getDataeaseName()).append(" AS ").append(y.getDeType() == 2 ? "DECIMAL(20,0)" : "DECIMAL(20,2)").append(")")
                        .append("),2").append(")");
            }
            f.append(" AS _").append(y.getSummary()).append("_").append(StringUtils.equalsIgnoreCase(y.getDataeaseName(), "*") ? "" : y.getDataeaseName());
            return f.toString();
        }).toArray(String[]::new);
        String[] groupField = xAxis.stream().map(x -> {
            StringBuilder stringBuilder = new StringBuilder();
            // 如果原始类型为时间
            if (x.getDeExtractType() == 1) {
                if (x.getDeType() == 2 || x.getDeType() == 3) {
                    stringBuilder.append("unix_timestamp(").append(x.getDataeaseName()).append(")*1000 as ").append(x.getDataeaseName());
                } else {
                    stringBuilder.append(x.getDataeaseName());
                }
            } else {
                if (x.getDeType() == 1) {
                    stringBuilder.append("FROM_UNIXTIME(cast(").append(x.getDataeaseName()).append(" as decimal(20,0))/1000,'%Y-%m-%d %H:%i:%S') as ").append(x.getDataeaseName());
                } else {
                    stringBuilder.append(x.getDataeaseName());
                }
            }
            return stringBuilder.toString();
        }).toArray(String[]::new);
        String[] group = xAxis.stream().map(ChartViewFieldDTO::getDataeaseName).toArray(String[]::new);
        String[] xOrder = xAxis.stream().filter(f -> StringUtils.isNotEmpty(f.getSort()) && !StringUtils.equalsIgnoreCase(f.getSort(), "none"))
                .map(f -> f.getDataeaseName() + " " + f.getSort()).toArray(String[]::new);
        String[] yOrder = yAxis.stream().filter(f -> StringUtils.isNotEmpty(f.getSort()) && !StringUtils.equalsIgnoreCase(f.getSort(), "none"))
                .map(f -> "_" + f.getSummary() + "_" + (StringUtils.equalsIgnoreCase(f.getDataeaseName(), "*") ? "" : f.getDataeaseName()) + " " + f.getSort()).toArray(String[]::new);
        String[] order = Arrays.copyOf(xOrder, xOrder.length + yOrder.length);
        System.arraycopy(yOrder, 0, order, xOrder.length, yOrder.length);

        String[] xFilter = xAxis.stream().filter(x -> CollectionUtils.isNotEmpty(x.getFilter()) && x.getFilter().size() > 0)
                .map(x -> {
                    String[] s = x.getFilter().stream().map(f -> {
                        StringBuilder filter = new StringBuilder();
                        filter.append(" AND ").append(x.getDataeaseName()).append(transMysqlFilterTerm(f.getTerm()));
                        if (StringUtils.containsIgnoreCase(f.getTerm(), "null")) {
                        } else if (StringUtils.containsIgnoreCase(f.getTerm(), "in")) {
                            filter.append("('").append(StringUtils.join(f.getValue(), "','")).append("')");
                        } else if (StringUtils.containsIgnoreCase(f.getTerm(), "like")) {
                            filter.append("%").append(f.getValue()).append("%");
                        } else {
                            filter.append("'" + f.getValue() + "'");
                        }
                        return filter.toString();
                    }).toArray(String[]::new);
                    return StringUtils.join(s, " ");
                }).toArray(String[]::new);

        String sql = MessageFormat.format("SELECT {0},{1} FROM {2} WHERE 1=1 {3} GROUP BY {4} ORDER BY null,{5}",
                StringUtils.join(groupField, ","),
                StringUtils.join(field, ","),
                table,
                xFilter.length > 0 ? StringUtils.join(xFilter, " ") : "" + transMysqlExtFilter(extFilterRequestList),// origin field filter and panel field filter
                StringUtils.join(group, ","),
                StringUtils.join(order, ","));
        if (sql.endsWith(",")) {
            sql = sql.substring(0, sql.length() - 1);
        }
        // 如果是对结果字段过滤，则再包裹一层sql
        String[] resultFilter = yAxis.stream().filter(y -> CollectionUtils.isNotEmpty(y.getFilter()) && y.getFilter().size() > 0)
                .map(y -> {
                    String[] s = y.getFilter().stream().map(f -> {
                        StringBuilder filter = new StringBuilder();
                        filter.append(" AND _").append(y.getSummary()).append("_").append(StringUtils.equalsIgnoreCase(y.getDataeaseName(), "*") ? "" : y.getDataeaseName()).append(transMysqlFilterTerm(f.getTerm()));
                        if (StringUtils.containsIgnoreCase(f.getTerm(), "null")) {
                        } else if (StringUtils.containsIgnoreCase(f.getTerm(), "in")) {
                            filter.append("('").append(StringUtils.join(f.getValue(), "','")).append("')");
                        } else if (StringUtils.containsIgnoreCase(f.getTerm(), "like")) {
                            filter.append("%").append(f.getValue()).append("%");
                        } else {
                            filter.append("'" + f.getValue() + "'");
                        }
                        return filter.toString();
                    }).toArray(String[]::new);
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

    public String transMysqlSQL(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartExtFilterRequest> extFilterRequestList) {
        // 字段汇总 排序等
        String[] field = yAxis.stream().map(y -> {
            StringBuilder f = new StringBuilder();
            if (StringUtils.equalsIgnoreCase(y.getDataeaseName(), "*")) {
                f.append(y.getSummary()).append("(").append(y.getDataeaseName()).append(")");
            } else {
                f.append("CAST(")
                        .append(y.getSummary()).append("(")
                        .append("CAST(").append(y.getDataeaseName()).append(" AS ").append(y.getDeType() == 2 ? "DECIMAL(20,0)" : "DECIMAL(20,2)").append(")")
                        .append(") AS DECIMAL(20,2)").append(")");
            }
            f.append(" AS _").append(y.getSummary()).append("_").append(StringUtils.equalsIgnoreCase(y.getDataeaseName(), "*") ? "" : y.getDataeaseName());
            return f.toString();
        }).toArray(String[]::new);
        String[] groupField = xAxis.stream().map(x -> {
            StringBuilder stringBuilder = new StringBuilder();
            // 如果原始类型为时间
            if (x.getDeExtractType() == 1) {
                if (x.getDeType() == 2 || x.getDeType() == 3) {
                    stringBuilder.append("unix_timestamp(").append(x.getDataeaseName()).append(")*1000 as ").append(x.getDataeaseName());
                } else {
                    stringBuilder.append(x.getDataeaseName());
                }
            } else {
                if (x.getDeType() == 1) {
                    stringBuilder.append("FROM_UNIXTIME(cast(").append(x.getDataeaseName()).append(" as decimal(20,0))/1000,'%Y-%m-%d %H:%i:%S') as ").append(x.getDataeaseName());
                } else {
                    stringBuilder.append(x.getDataeaseName());
                }
            }
            return stringBuilder.toString();
        }).toArray(String[]::new);
        String[] group = xAxis.stream().map(ChartViewFieldDTO::getDataeaseName).toArray(String[]::new);
        String[] xOrder = xAxis.stream().filter(f -> StringUtils.isNotEmpty(f.getSort()) && !StringUtils.equalsIgnoreCase(f.getSort(), "none"))
                .map(f -> f.getDataeaseName() + " " + f.getSort()).toArray(String[]::new);
        String[] yOrder = yAxis.stream().filter(f -> StringUtils.isNotEmpty(f.getSort()) && !StringUtils.equalsIgnoreCase(f.getSort(), "none"))
                .map(f -> "_" + f.getSummary() + "_" + (StringUtils.equalsIgnoreCase(f.getDataeaseName(), "*") ? "" : f.getDataeaseName()) + " " + f.getSort()).toArray(String[]::new);
        String[] order = Arrays.copyOf(xOrder, xOrder.length + yOrder.length);
        System.arraycopy(yOrder, 0, order, xOrder.length, yOrder.length);

        String[] xFilter = xAxis.stream().filter(x -> CollectionUtils.isNotEmpty(x.getFilter()) && x.getFilter().size() > 0)
                .map(x -> {
                    String[] s = x.getFilter().stream().map(f -> {
                        StringBuilder filter = new StringBuilder();
                        filter.append(" AND ").append(x.getDataeaseName()).append(transMysqlFilterTerm(f.getTerm()));
                        if (StringUtils.containsIgnoreCase(f.getTerm(), "null")) {
                        } else if (StringUtils.containsIgnoreCase(f.getTerm(), "in")) {
                            filter.append("('").append(StringUtils.join(f.getValue(), "','")).append("')");
                        } else if (StringUtils.containsIgnoreCase(f.getTerm(), "like")) {
                            filter.append("%").append(f.getValue()).append("%");
                        } else {
                            filter.append("'" + f.getValue() + "'");
                        }
                        return filter.toString();
                    }).toArray(String[]::new);
                    return StringUtils.join(s, " ");
                }).toArray(String[]::new);

        String sql = MessageFormat.format("SELECT {0},{1} FROM {2} WHERE 1=1 {3} GROUP BY {4} ORDER BY null,{5}",
                StringUtils.join(groupField, ","),
                StringUtils.join(field, ","),
                table,
                xFilter.length > 0 ? StringUtils.join(xFilter, " ") : "" + transMysqlExtFilter(extFilterRequestList),// origin field filter and panel field filter
                StringUtils.join(group, ","),
                StringUtils.join(order, ","));
        if (sql.endsWith(",")) {
            sql = sql.substring(0, sql.length() - 1);
        }
        // 如果是对结果字段过滤，则再包裹一层sql
        String[] resultFilter = yAxis.stream().filter(y -> CollectionUtils.isNotEmpty(y.getFilter()) && y.getFilter().size() > 0)
                .map(y -> {
                    String[] s = y.getFilter().stream().map(f -> {
                        StringBuilder filter = new StringBuilder();
                        filter.append(" AND _").append(y.getSummary()).append("_").append(StringUtils.equalsIgnoreCase(y.getDataeaseName(), "*") ? "" : y.getDataeaseName()).append(transMysqlFilterTerm(f.getTerm()));
                        if (StringUtils.containsIgnoreCase(f.getTerm(), "null")) {
                        } else if (StringUtils.containsIgnoreCase(f.getTerm(), "in")) {
                            filter.append("('").append(StringUtils.join(f.getValue(), "','")).append("')");
                        } else if (StringUtils.containsIgnoreCase(f.getTerm(), "like")) {
                            filter.append("%").append(f.getValue()).append("%");
                        } else {
                            filter.append("'" + f.getValue() + "'");
                        }
                        return filter.toString();
                    }).toArray(String[]::new);
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

    public List<ChartView> viewsByIds(List<String> viewIds) {
        ChartViewExample example = new ChartViewExample();
        example.createCriteria().andIdIn(viewIds);
        return chartViewMapper.selectByExample(example);
    }

    public ChartViewWithBLOBs findOne(String id) {
        return chartViewMapper.selectByPrimaryKey(id);
    }
}

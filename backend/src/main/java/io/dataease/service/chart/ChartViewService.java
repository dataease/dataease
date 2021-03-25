package io.dataease.service.chart;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.dataease.base.domain.*;
import io.dataease.base.mapper.ChartViewMapper;
import io.dataease.commons.utils.BeanUtils;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
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
    private DataSetTableService dataSetTableService;
    @Resource
    private DatasourceService datasourceService;
    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;

    public ChartViewWithBLOBs save(ChartViewWithBLOBs chartView) {
        long timestamp = System.currentTimeMillis();
        chartView.setUpdateTime(timestamp);
        int i = chartViewMapper.updateByPrimaryKeyWithBLOBs(chartView);
        if (i == 0) {
            chartView.setId(UUID.randomUUID().toString());
            chartView.setCreateTime(timestamp);
            chartView.setUpdateTime(timestamp);
            chartViewMapper.insert(chartView);
        }
        return chartView;
    }

    public List<ChartViewWithBLOBs> list(ChartViewRequest chartViewRequest) {
        ChartViewExample chartViewExample = new ChartViewExample();
        ChartViewExample.Criteria criteria = chartViewExample.createCriteria();
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

    public ChartViewDTO getData(String id) throws Exception {
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

        // 获取数据集
        DatasetTable table = dataSetTableService.get(view.getTableId());
        // todo 判断连接方式，直连或者定时抽取 table.mode
        Datasource ds = datasourceService.get(table.getDataSourceId());
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(table.getInfo(), DataTableInfoDTO.class);
        if (StringUtils.equalsIgnoreCase(table.getType(), "db")) {
            datasourceRequest.setTable(dataTableInfoDTO.getTable());
            datasourceRequest.setQuery(getSQL(ds.getType(), dataTableInfoDTO.getTable(), xAxis, yAxis));
        } else if (StringUtils.equalsIgnoreCase(table.getType(), "sql")) {
            datasourceRequest.setQuery(getSQL(ds.getType(), " (" + dataTableInfoDTO.getSql() + ") AS tmp ", xAxis, yAxis));
        }

        List<String[]> data = datasourceProvider.getData(datasourceRequest);

        // todo 处理结果,目前做一个单系列图表，后期图表组件再扩展
        for (ChartViewFieldDTO y : yAxis) {
            Series series1 = new Series();
            series1.setName(y.getName());
            series1.setType(view.getType());
            series1.setData(new ArrayList<>());
            series.add(series1);
        }
        for (String[] d : data) {
            StringBuilder a = new StringBuilder();
            BigDecimal b = new BigDecimal("0");
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
                series.get(j).getData().add(new BigDecimal(d[i]));
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

    public String getSQL(String type, String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis) {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(type);
        switch (datasourceType) {
            case mysql:
                return transMysqlSQL(table, xAxis, yAxis);
            case sqlServer:
            default:
                return "";
        }
    }

    public String transMysqlSQL(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis) {
        // TODO 字段汇总 排序等
        String[] field = yAxis.stream().map(y -> "CAST(" + y.getSummary() + "(" + y.getOriginName() + ") AS DECIMAL(20,2)) AS _" + y.getSummary() + "_" + y.getOriginName()).toArray(String[]::new);
        String[] group = xAxis.stream().map(ChartViewFieldDTO::getOriginName).toArray(String[]::new);
        String[] order = yAxis.stream().filter(y -> StringUtils.isNotEmpty(y.getSort()) && !StringUtils.equalsIgnoreCase(y.getSort(), "none"))
                .map(y -> "_" + y.getSummary() + "_" + y.getOriginName() + " " + y.getSort()).toArray(String[]::new);

        String sql = MessageFormat.format("SELECT {0},{1} FROM {2} WHERE 1=1 {3} GROUP BY {4} ORDER BY null,{5}",
                StringUtils.join(group, ","),
                StringUtils.join(field, ","),
                table,
                "",
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
                return "=";
            case "not_eq":
                return "<>";
            case "lt":
                return "<";
            case "le":
                return "<=";
            case "gt":
                return ">";
            case "ge":
                return ">=";
            case "null":
                return "IS NULL";
            case "not_null":
                return "IS NOT NULL";
            default:
                return "";
        }
    }
}

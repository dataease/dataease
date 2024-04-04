package io.dataease.plugins.datasource.kylin.query;

import com.google.gson.Gson;
import io.dataease.plugins.common.base.domain.ChartViewWithBLOBs;
import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.plugins.common.base.domain.DatasetTableFieldExample;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.base.mapper.DatasetTableFieldMapper;
import io.dataease.plugins.common.constants.DeTypeConstants;
import io.dataease.plugins.common.constants.datasource.SQLConstants;
import io.dataease.plugins.common.constants.datasource.SqlServerSQLConstants;
import io.dataease.plugins.common.dto.chart.ChartCustomFilterItemDTO;
import io.dataease.plugins.common.dto.chart.ChartFieldCustomFilterDTO;
import io.dataease.plugins.common.dto.chart.ChartViewFieldDTO;
import io.dataease.plugins.common.dto.datasource.DeSortField;
import io.dataease.plugins.common.dto.sqlObj.SQLObj;
import io.dataease.plugins.common.request.chart.ChartExtFilterRequest;
import io.dataease.plugins.common.request.chart.filter.FilterTreeItem;
import io.dataease.plugins.common.request.chart.filter.FilterTreeObj;
import io.dataease.plugins.common.request.permission.DataSetRowPermissionsTreeDTO;
import io.dataease.plugins.common.request.permission.DatasetRowPermissionsTreeItem;
import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import io.dataease.plugins.datasource.query.QueryProvider;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.dataease.plugins.common.constants.datasource.SQLConstants.TABLE_ALIAS_PREFIX;


@Component()
public class KylinQueryProvider extends QueryProvider {
    @Resource
    private DatasetTableFieldMapper datasetTableFieldMapper;

    private static final Gson json = new Gson();

    @Override
    public Integer transFieldType(String field) {
        field = field.contains("(") ? field.split("\\(")[0] : field;
        field = field.toUpperCase();
        switch (field) {
            case "DATE":
            case "DATETIME":
            case "TIMESTAMP":
            case "TIME":
                return DeTypeConstants.DE_TIME;// 时间
            case "TINYINT":
            case "SMALLINT":
            case "INT":
            case "BIGINT":
            case "INTEGER":
                return DeTypeConstants.DE_INT;// 整型
            case "FLOAT":
            case "DOUBLE":
            case "DECIMAL":
            case "NUMERIC":
                return DeTypeConstants.DE_FLOAT;// 浮点
            case "BOOLEAN":
                return DeTypeConstants.DE_BOOL;// 布尔
            case "BINARY":
                return DeTypeConstants.DE_BINARY;// 二进制
            default:
                return DeTypeConstants.DE_STRING;
        }
    }

    @Override
    public String createSQLPreview(String sql, String orderBy) {
        return "SELECT * FROM (" + sqlFix(sql) + ") AS DE_TMP   " + " LIMIT 1000";
    }

    @Override
    public String createQuerySQL(String table, List<DatasetTableField> fields, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return createQuerySQL(table, fields, isGroup, ds, fieldCustomFilter, rowPermissionsTree, null, null, null);
    }

    @Override
    public String createQuerySQL(String table, List<DatasetTableField> fields, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<DeSortField> sortFields, Long limit, String keyword) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(KylinConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 0))
                .build();
        List<SQLObj> xFields = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(fields)) {
            for (int i = 0; i < fields.size(); i++) {
                DatasetTableField f = fields.get(i);
                String originField;
                if (ObjectUtils.isNotEmpty(f.getExtField()) && f.getExtField() == 2) {
                    // 解析origin name中有关联的字段生成sql表达式
                    originField = calcFieldRegex(f.getOriginName(), tableObj);
                } else if (ObjectUtils.isNotEmpty(f.getExtField()) && f.getExtField() == 1) {
                    originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), f.getOriginName());
                } else {
                    originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), f.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);
                String fieldName = "";
                // 处理横轴字段
                if (f.getDeExtractType() == DeTypeConstants.DE_TIME) {
//                    if (f.getDeType() == DeTypeConstants.DE_INT || f.getDeType() == DeTypeConstants.DE_FLOAT) {
//                        fieldName = String.format(KylinConstants.UNIX_TIMESTAMP, originField);
//                    } else {
//                        fieldName = originField;
//                    }
                    fieldName = originField;
                } else if (f.getDeExtractType() == DeTypeConstants.DE_STRING) {
                    if (f.getDeType() == DeTypeConstants.DE_INT) {
                        fieldName = String.format(KylinConstants.CAST, originField, KylinConstants.DEFAULT_INT_FORMAT);
                    } else if (f.getDeType() == DeTypeConstants.DE_FLOAT) {
                        fieldName = String.format(KylinConstants.CAST, originField, KylinConstants.DEFAULT_FLOAT_FORMAT);
                    } else if (f.getDeType() == DeTypeConstants.DE_TIME) {
                        fieldName = String.format(KylinConstants.CAST, originField, KylinConstants.DEFAULT_DATE_TYPE);
//                        fieldName = String.format(KylinConstants.date_parse, originField, StringUtils.isNotEmpty(f.getDateFormat()) ? f.getDateFormat() : KylinConstants.DEFAULT_DATE_FORMAT);
                    } else {
                        fieldName = originField;
                    }
                } else {
                    fieldName = originField;
//                    if (f.getDeType() == DeTypeConstants.DE_TIME) {
//                        fieldName = String.format(KylinConstants.FORMAT_DATETIME, String.format(KylinConstants.FROM_UNIXTIME, originField + "/1000"), KylinConstants.DEFAULT_DATE_FORMAT);
//                    } else if (f.getDeType() == DeTypeConstants.DE_INT) {
//                        fieldName = String.format(KylinConstants.CAST, originField, KylinConstants.DEFAULT_INT_FORMAT);
//                    } else {
//                        fieldName = originField;
//                    }
                }
                xFields.add(SQLObj.builder()
                        .fieldOriginName(originField)
                        .fieldName(fieldName)
                        .fieldAlias(fieldAlias)
                        .build());
            }
        }

        STGroup stg = new STGroupFile(SQLConstants.SQL_TEMPLATE);
        ST st_sql = stg.getInstanceOf("previewSql");
        st_sql.add("isGroup", isGroup);
        if (CollectionUtils.isNotEmpty(xFields)) st_sql.add("groups", xFields);
        if (ObjectUtils.isNotEmpty(tableObj)) st_sql.add("table", tableObj);
        String customWheres = transChartFilterTrees(tableObj, fieldCustomFilter);
        // row permissions tree
        String whereTrees = transFilterTrees(tableObj, rowPermissionsTree);
        List<String> wheres = new ArrayList<>();
        if (customWheres != null) wheres.add(customWheres);
        if (whereTrees != null) wheres.add(whereTrees);
        if (StringUtils.isNotBlank(keyword)) {
            String keyWhere = "(" + transKeywordFilterList(tableObj, xFields, keyword) + ")";
            wheres.add(keyWhere);
        }
        if (CollectionUtils.isNotEmpty(wheres)) st_sql.add("filters", wheres);

        List<SQLObj> xOrders = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sortFields)) {
            int step = fields.size();
            for (int i = step; i < (step + sortFields.size()); i++) {
                DeSortField deSortField = sortFields.get(i - step);
                SQLObj order = buildSortField(deSortField, tableObj, i);
                xOrders.add(order);
            }
        }
        if (ObjectUtils.isNotEmpty(xOrders)) {
            st_sql.add("orders", xOrders);
        }
        if (ObjectUtils.isNotEmpty(limit)) {
            ChartViewWithBLOBs view = new ChartViewWithBLOBs();
            view.setResultMode("custom");
            view.setResultCount(Integer.parseInt(limit.toString()));
            return sqlLimit(st_sql.render(), view);
        }
        return st_sql.render();
    }

    private SQLObj buildSortField(DeSortField f, SQLObj tableObj, int i) {
        String originField;
        if (ObjectUtils.isNotEmpty(f.getExtField()) && f.getExtField() == 2) {
            // 解析origin name中有关联的字段生成sql表达式
            originField = calcFieldRegex(f.getOriginName(), tableObj);
        } else if (ObjectUtils.isNotEmpty(f.getExtField()) && f.getExtField() == 1) {
            originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), f.getOriginName());
        } else {
            originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), f.getOriginName());
        }
        String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);
        String fieldName = "";
        // 处理横轴字段
        if (f.getDeExtractType() == DeTypeConstants.DE_TIME) {
            if (f.getDeType() == DeTypeConstants.DE_INT || f.getDeType() == DeTypeConstants.DE_FLOAT) {
                fieldName = String.format(KylinConstants.CAST, originField, KylinConstants.DEFAULT_INT_FORMAT);
            } else {
                fieldName = originField;
            }
        } else if (f.getDeExtractType() == DeTypeConstants.DE_STRING) {
            if (f.getDeType() == DeTypeConstants.DE_INT) {
                fieldName = String.format(KylinConstants.CAST, originField, KylinConstants.DEFAULT_INT_FORMAT);
            } else if (f.getDeType() == DeTypeConstants.DE_FLOAT) {
                fieldName = String.format(KylinConstants.CAST, originField, KylinConstants.DEFAULT_FLOAT_FORMAT);
            } else if (f.getDeType() == DeTypeConstants.DE_TIME) {
                fieldName = String.format(KylinConstants.CAST, originField, KylinConstants.DEFAULT_DATE_TYPE);
            } else {
                fieldName = originField;
            }
        } else {
            if (f.getDeType() == DeTypeConstants.DE_TIME) {
                fieldName = String.format(KylinConstants.CAST, originField, KylinConstants.DEFAULT_DATE_TYPE);
            } else if (f.getDeType() == DeTypeConstants.DE_INT) {
                fieldName = String.format(KylinConstants.CAST, originField, KylinConstants.DEFAULT_INT_FORMAT);
            } else {
                fieldName = originField;
            }
        }
        SQLObj result = SQLObj.builder().orderField(originField).orderAlias(originField).orderDirection(f.getOrderDirection()).build();
        return result;
    }

    @Override
    public String createQuerySQLAsTmp(String sql, List<DatasetTableField> fields, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<DeSortField> sortFields, Long limit, String keyword) {
        return createQuerySQL("(" + sqlFix(sql) + ")", fields, isGroup, null, fieldCustomFilter, rowPermissionsTree, sortFields, limit, keyword);
    }

    @Override
    public String createQuerySQLAsTmp(String sql, List<DatasetTableField> fields, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return createQuerySQL("(" + sqlFix(sql) + ")", fields, isGroup, null, fieldCustomFilter, rowPermissionsTree);
    }

    @Override
    public String createQueryTableWithPage(String table, List<DatasetTableField> fields, Integer page, Integer pageSize, Integer realSize, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return createQuerySQL(table, fields, isGroup, ds, fieldCustomFilter, rowPermissionsTree) + " LIMIT " + realSize;
    }

    @Override
    public String createQuerySQLWithPage(String sql, List<DatasetTableField> fields, Integer page, Integer pageSize, Integer realSize, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return createQuerySQLAsTmp(sql, fields, isGroup, fieldCustomFilter, rowPermissionsTree) + " LIMIT " + realSize;
    }

    @Override
    public String createQueryTableWithLimit(String table, List<DatasetTableField> fields, Integer limit, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return createQuerySQL(table, fields, isGroup, ds, fieldCustomFilter, rowPermissionsTree) + " LIMIT " + limit;
    }

    @Override
    public String createQuerySqlWithLimit(String sql, List<DatasetTableField> fields, Integer limit, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return createQuerySQLAsTmp(sql, fields, isGroup, fieldCustomFilter, rowPermissionsTree) + " LIMIT " + limit;
    }

    @Override
    public String getSQL(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(KylinConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 0))
                .build();
        setSchema(tableObj, ds);
        List<SQLObj> xFields = new ArrayList<>();
        List<SQLObj> xOrders = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(xAxis)) {
            for (int i = 0; i < xAxis.size(); i++) {
                ChartViewFieldDTO x = xAxis.get(i);
                String originField;
                if (ObjectUtils.isNotEmpty(x.getExtField()) && x.getExtField() == 2) {
                    // 解析origin name中有关联的字段生成sql表达式
                    originField = calcFieldRegex(x.getOriginName(), tableObj);
                } else if (ObjectUtils.isNotEmpty(x.getExtField()) && x.getExtField() == 1) {
                    originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);
                // 处理横轴字段
                xFields.add(getXFields(x, originField, fieldAlias));
                // 处理横轴排序
                if (StringUtils.isNotEmpty(x.getSort()) && !StringUtils.equalsIgnoreCase(x.getSort(), "none")) {
                    xOrders.add(SQLObj.builder()
                            .orderField(originField)
                            .orderAlias(fieldAlias)
                            .orderDirection(x.getSort())
                            .build());
                }
            }
        }
        List<SQLObj> yFields = new ArrayList<>();
        List<String> yWheres = new ArrayList<>();
        List<SQLObj> yOrders = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(yAxis)) {
            for (int i = 0; i < yAxis.size(); i++) {
                ChartViewFieldDTO y = yAxis.get(i);
                String originField;
                if (ObjectUtils.isNotEmpty(y.getExtField()) && y.getExtField() == 2) {
                    // 解析origin name中有关联的字段生成sql表达式
                    originField = calcFieldRegex(y.getOriginName(), tableObj);
                } else if (ObjectUtils.isNotEmpty(y.getExtField()) && y.getExtField() == 1) {
                    originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_Y_PREFIX, i);
                // 处理纵轴字段
                yFields.add(getYFields(y, originField, fieldAlias));
                // 处理纵轴过滤
                yWheres.add(getYWheres(y, originField, fieldAlias));
                // 处理纵轴排序
                if (StringUtils.isNotEmpty(y.getSort()) && !StringUtils.equalsIgnoreCase(y.getSort(), "none")) {
                    yOrders.add(SQLObj.builder()
                            .orderField(originField)
                            .orderAlias(fieldAlias)
                            .orderDirection(y.getSort())
                            .build());
                }
            }
        }
        // 处理视图中字段过滤
        String customWheres = transChartFilterTrees(tableObj, fieldCustomFilter);
        // 处理仪表板字段过滤
        String extWheres = transExtFilterList(tableObj, extFilterRequestList);
        // row permissions tree
        String whereTrees = transFilterTrees(tableObj, rowPermissionsTree);
        // 构建sql所有参数
        List<SQLObj> fields = new ArrayList<>();
        fields.addAll(xFields);
        fields.addAll(yFields);
        List<String> wheres = new ArrayList<>();
        if (customWheres != null) wheres.add(customWheres);
        if (extWheres != null) wheres.add(extWheres);
        if (whereTrees != null) wheres.add(whereTrees);
        List<SQLObj> groups = new ArrayList<>();
        groups.addAll(xFields);
        // 外层再次套sql
        List<SQLObj> orders = new ArrayList<>();
        orders.addAll(xOrders);
        orders.addAll(yOrders);
        List<String> aggWheres = new ArrayList<>();
        aggWheres.addAll(yWheres.stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList()));

        STGroup stg = new STGroupFile(SQLConstants.SQL_TEMPLATE);
        ST st_sql = stg.getInstanceOf("querySql");
        if (CollectionUtils.isNotEmpty(xFields)) st_sql.add("groups", xFields);
        if (CollectionUtils.isNotEmpty(yFields)) st_sql.add("aggregators", yFields);
        if (CollectionUtils.isNotEmpty(wheres)) st_sql.add("filters", wheres);
        if (ObjectUtils.isNotEmpty(tableObj)) st_sql.add("table", tableObj);
        String sql = st_sql.render();

        ST st = stg.getInstanceOf("querySql");
        SQLObj tableSQL = SQLObj.builder()
                .tableName(String.format(KylinConstants.BRACKETS, sql))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 1))
                .build();
        if (CollectionUtils.isNotEmpty(aggWheres)) st.add("filters", aggWheres);
        if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        return sqlLimit(st.render(), view);
    }

    @Override
    public String getSQLRangeBar(String table, List<ChartViewFieldDTO> baseXAxis, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack, Datasource ds, ChartViewWithBLOBs view) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(KylinConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 0))
                .build();
        setSchema(tableObj, ds);
        List<SQLObj> xFields = new ArrayList<>();
        List<SQLObj> xFields2Tail = new ArrayList<>();
        List<SQLObj> xOrders = new ArrayList<>();

        List<SQLObj> yFields = new ArrayList<>(); // 要把两个时间字段放进y里面
        List<String> yWheres = new ArrayList<>();
        List<SQLObj> yOrders = new ArrayList<>();

        boolean ifAggregate = BooleanUtils.isTrue(view.getAggregate());


        if (CollectionUtils.isNotEmpty(xAxis)) {
            for (int i = 0; i < xAxis.size(); i++) {
                ChartViewFieldDTO x = xAxis.get(i);
                String originField;

                if (StringUtils.equalsIgnoreCase(x.getGroupType(), "q")) {
                    if (ObjectUtils.isNotEmpty(x.getExtField()) && x.getExtField() == 2) {
                        // 解析origin name中有关联的字段生成sql表达式
                        originField = calcFieldRegex(x.getOriginName(), tableObj);
                    } else if (ObjectUtils.isNotEmpty(x.getExtField()) && x.getExtField() == 1) {
                        originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                    } else {
                        originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                    }
                    String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_Y_PREFIX, i);
                    // 处理纵轴字段
                    yFields.add(getYFields(x, originField, fieldAlias));
                    // 处理纵轴过滤
                    yWheres.add(getYWheres(x, originField, fieldAlias));
                    // 处理纵轴排序
                    if (StringUtils.isNotEmpty(x.getSort()) && !StringUtils.equalsIgnoreCase(x.getSort(), "none")) {
                        yOrders.add(SQLObj.builder()
                                .orderField(originField)
                                .orderAlias(fieldAlias)
                                .orderDirection(x.getSort())
                                .build());
                    }
                    continue;
                }

                if (ObjectUtils.isNotEmpty(x.getExtField()) && x.getExtField() == 2) {
                    // 解析origin name中有关联的字段生成sql表达式
                    originField = calcFieldRegex(x.getOriginName(), tableObj);
                } else if (ObjectUtils.isNotEmpty(x.getExtField()) && x.getExtField() == 1) {
                    originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);

                if (ifAggregate) {
                    if (i == baseXAxis.size()) {// 起止时间
                        String fieldName = String.format(KylinConstants.AGG_FIELD, "min", originField);
                        yFields.add(getXFields(x, fieldName, fieldAlias));

                        yWheres.add(getYWheres(x, originField, fieldAlias));

                    } else if (i == baseXAxis.size() + 1) {
                        String fieldName = String.format(KylinConstants.AGG_FIELD, "max", originField);

                        yFields.add(getXFields(x, fieldName, fieldAlias));

                        yWheres.add(getYWheres(x, originField, fieldAlias));
                    } else {
                        // 处理横轴字段
                        xFields.add(getXFields(x, originField, fieldAlias));
                    }
                } else {
                    if (i == baseXAxis.size() || i == baseXAxis.size() + 1) {// 起止时间
                        xFields2Tail.add(getXFields(x, originField, fieldAlias));
                    } else {
                        xFields.add(getXFields(x, originField, fieldAlias));
                    }
                }

                // 处理横轴排序
                if (StringUtils.isNotEmpty(x.getSort()) && !StringUtils.equalsIgnoreCase(x.getSort(), "none")) {
                    xOrders.add(SQLObj.builder()
                            .orderField(originField)
                            .orderAlias(fieldAlias)
                            .orderDirection(x.getSort())
                            .build());
                }
            }
            if (!ifAggregate) { //把起止时间放到数组最后
                xFields.addAll(xFields2Tail);
            }
        }


        // 处理视图中字段过滤
        String customWheres = transChartFilterTrees(tableObj, fieldCustomFilter);
        // 处理仪表板字段过滤
        String extWheres = transExtFilterList(tableObj, extFilterRequestList);
        // row permissions tree
        String whereTrees = transFilterTrees(tableObj, rowPermissionsTree);
        // 构建sql所有参数
        List<SQLObj> fields = new ArrayList<>();
        fields.addAll(xFields);
        fields.addAll(yFields);
        List<String> wheres = new ArrayList<>();
        if (customWheres != null) wheres.add(customWheres);
        if (extWheres != null) wheres.add(extWheres);
        if (whereTrees != null) wheres.add(whereTrees);
        List<SQLObj> groups = new ArrayList<>();
        groups.addAll(xFields);
        // 外层再次套sql
        List<SQLObj> orders = new ArrayList<>();
        orders.addAll(xOrders);
        orders.addAll(yOrders);
        List<String> aggWheres = new ArrayList<>();
        aggWheres.addAll(yWheres.stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList()));

        STGroup stg = new STGroupFile(SQLConstants.SQL_TEMPLATE);
        ST st_sql = stg.getInstanceOf("querySql");
        if (CollectionUtils.isNotEmpty(xFields)) st_sql.add("groups", xFields);
        if (CollectionUtils.isNotEmpty(yFields)) st_sql.add("aggregators", yFields);
        if (CollectionUtils.isNotEmpty(wheres)) st_sql.add("filters", wheres);
        if (ObjectUtils.isNotEmpty(tableObj)) st_sql.add("table", tableObj);
        String sql = st_sql.render();

        ST st = stg.getInstanceOf("querySql");
        SQLObj tableSQL = SQLObj.builder()
                .tableName(String.format(KylinConstants.BRACKETS, sql))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 1))
                .build();
        if (CollectionUtils.isNotEmpty(aggWheres)) st.add("filters", aggWheres);
        if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        return sqlLimit(st.render(), view);
    }

    @Override
    public String getSQLAsTmpRangeBar(String table, List<ChartViewFieldDTO> baseXAxis, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack, ChartViewWithBLOBs view) {
        return getSQLRangeBar("(" + table + ")", baseXAxis, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, extStack, null, view);
    }

    @Override
    public String getSQLTableInfo(String table, List<ChartViewFieldDTO> xAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(KylinConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 0))
                .build();
        setSchema(tableObj, ds);
        List<SQLObj> xFields = new ArrayList<>();
        List<SQLObj> xOrders = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(xAxis)) {
            for (int i = 0; i < xAxis.size(); i++) {
                ChartViewFieldDTO x = xAxis.get(i);
                String originField;
                if (ObjectUtils.isNotEmpty(x.getExtField()) && x.getExtField() == 2) {
                    // 解析origin name中有关联的字段生成sql表达式
                    originField = calcFieldRegex(x.getOriginName(), tableObj);
                } else if (ObjectUtils.isNotEmpty(x.getExtField()) && x.getExtField() == 1) {
                    originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    if (x.getDeType() == 2 || x.getDeType() == 3) {
                        originField = String.format(KylinConstants.CAST, String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName()), KylinConstants.DEFAULT_FLOAT_FORMAT);
                    } else {
                        originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                    }
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);
                // 处理横轴字段
                xFields.add(getXFields(x, originField, fieldAlias));
                // 处理横轴排序
                if (StringUtils.isNotEmpty(x.getSort()) && !StringUtils.equalsIgnoreCase(x.getSort(), "none")) {
                    xOrders.add(SQLObj.builder()
                            .orderField(originField)
                            .orderAlias(fieldAlias)
                            .orderDirection(x.getSort())
                            .build());
                }
            }
        }
        // 处理视图中字段过滤
        String customWheres = transChartFilterTrees(tableObj, fieldCustomFilter);
        // 处理仪表板字段过滤
        String extWheres = transExtFilterList(tableObj, extFilterRequestList);
        // row permissions tree
        String whereTrees = transFilterTrees(tableObj, rowPermissionsTree);
        // 构建sql所有参数
        List<SQLObj> fields = new ArrayList<>();
        fields.addAll(xFields);
        List<String> wheres = new ArrayList<>();
        if (customWheres != null) wheres.add(customWheres);
        if (extWheres != null) wheres.add(extWheres);
        if (whereTrees != null) wheres.add(whereTrees);
        List<SQLObj> groups = new ArrayList<>();
        groups.addAll(xFields);
        // 外层再次套sql
        List<SQLObj> orders = new ArrayList<>();
        orders.addAll(xOrders);

        STGroup stg = new STGroupFile(SQLConstants.SQL_TEMPLATE);
        ST st_sql = stg.getInstanceOf("previewSql");
        st_sql.add("isGroup", false);
        if (CollectionUtils.isNotEmpty(xFields)) st_sql.add("groups", xFields);
        if (CollectionUtils.isNotEmpty(wheres)) st_sql.add("filters", wheres);
        if (ObjectUtils.isNotEmpty(tableObj)) st_sql.add("table", tableObj);
        String sql = st_sql.render();

        ST st = stg.getInstanceOf("previewSql");
        st.add("isGroup", false);
        SQLObj tableSQL = SQLObj.builder()
                .tableName(String.format(KylinConstants.BRACKETS, sql))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 1))
                .build();
        if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        return sqlLimit(st.render(), view);
    }

    @Override
    public String getSQLAsTmpTableInfo(String sql, List<ChartViewFieldDTO> xAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view) {
        return getSQLTableInfo("(" + sqlFix(sql) + ")", xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view);
    }


    @Override
    public String getSQLAsTmp(String sql, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, ChartViewWithBLOBs view) {
        return getSQL("(" + sqlFix(sql) + ")", xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, null, view);
    }

    @Override
    public String getSQLStack(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack, Datasource ds, ChartViewWithBLOBs view) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(KylinConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 0))
                .build();
        setSchema(tableObj, ds);
        List<SQLObj> xFields = new ArrayList<>();
        List<SQLObj> xOrders = new ArrayList<>();
        List<ChartViewFieldDTO> xList = new ArrayList<>();
        xList.addAll(xAxis);
        xList.addAll(extStack);
        if (CollectionUtils.isNotEmpty(xList)) {
            for (int i = 0; i < xList.size(); i++) {
                ChartViewFieldDTO x = xList.get(i);
                String originField;
                if (ObjectUtils.isNotEmpty(x.getExtField()) && x.getExtField() == 2) {
                    // 解析origin name中有关联的字段生成sql表达式
                    originField = calcFieldRegex(x.getOriginName(), tableObj);
                } else if (ObjectUtils.isNotEmpty(x.getExtField()) && x.getExtField() == 1) {
                    originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);
                // 处理横轴字段
                xFields.add(getXFields(x, originField, fieldAlias));
                // 处理横轴排序
                if (StringUtils.isNotEmpty(x.getSort()) && !StringUtils.equalsIgnoreCase(x.getSort(), "none")) {
                    xOrders.add(SQLObj.builder()
                            .orderField(originField)
                            .orderAlias(fieldAlias)
                            .orderDirection(x.getSort())
                            .build());
                }
            }
        }
        List<SQLObj> yFields = new ArrayList<>();
        List<String> yWheres = new ArrayList<>();
        List<SQLObj> yOrders = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(yAxis)) {
            for (int i = 0; i < yAxis.size(); i++) {
                ChartViewFieldDTO y = yAxis.get(i);
                String originField;
                if (ObjectUtils.isNotEmpty(y.getExtField()) && y.getExtField() == 2) {
                    // 解析origin name中有关联的字段生成sql表达式
                    originField = calcFieldRegex(y.getOriginName(), tableObj);
                } else if (ObjectUtils.isNotEmpty(y.getExtField()) && y.getExtField() == 1) {
                    originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_Y_PREFIX, i);
                // 处理纵轴字段
                yFields.add(getYFields(y, originField, fieldAlias));
                // 处理纵轴过滤
                yWheres.add(getYWheres(y, originField, fieldAlias));
                // 处理纵轴排序
                if (StringUtils.isNotEmpty(y.getSort()) && !StringUtils.equalsIgnoreCase(y.getSort(), "none")) {
                    yOrders.add(SQLObj.builder()
                            .orderField(originField)
                            .orderAlias(fieldAlias)
                            .orderDirection(y.getSort())
                            .build());
                }
            }
        }
        // 处理视图中字段过滤
        String customWheres = transChartFilterTrees(tableObj, fieldCustomFilter);
        // 处理仪表板字段过滤
        String extWheres = transExtFilterList(tableObj, extFilterRequestList);
        // row permissions tree
        String whereTrees = transFilterTrees(tableObj, rowPermissionsTree);
        // 构建sql所有参数
        List<SQLObj> fields = new ArrayList<>();
        fields.addAll(xFields);
        fields.addAll(yFields);
        List<String> wheres = new ArrayList<>();
        if (customWheres != null) wheres.add(customWheres);
        if (extWheres != null) wheres.add(extWheres);
        if (whereTrees != null) wheres.add(whereTrees);
        List<SQLObj> groups = new ArrayList<>();
        groups.addAll(xFields);
        // 外层再次套sql
        List<SQLObj> orders = new ArrayList<>();
        orders.addAll(xOrders);
        orders.addAll(yOrders);
        List<String> aggWheres = new ArrayList<>();
        aggWheres.addAll(yWheres.stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList()));

        STGroup stg = new STGroupFile(SQLConstants.SQL_TEMPLATE);
        ST st_sql = stg.getInstanceOf("querySql");
        if (CollectionUtils.isNotEmpty(xFields)) st_sql.add("groups", xFields);
        if (CollectionUtils.isNotEmpty(yFields)) st_sql.add("aggregators", yFields);
        if (CollectionUtils.isNotEmpty(wheres)) st_sql.add("filters", wheres);
        if (ObjectUtils.isNotEmpty(tableObj)) st_sql.add("table", tableObj);
        String sql = st_sql.render();

        ST st = stg.getInstanceOf("querySql");
        SQLObj tableSQL = SQLObj.builder()
                .tableName(String.format(KylinConstants.BRACKETS, sql))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 1))
                .build();
        if (CollectionUtils.isNotEmpty(aggWheres)) st.add("filters", aggWheres);
        if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        return sqlLimit(st.render(), view);
    }

    @Override
    public String getSQLAsTmpStack(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack, ChartViewWithBLOBs view) {
        return getSQLStack("(" + sqlFix(table) + ")", xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, extStack, null, view);
    }

    @Override
    public String getSQLScatter(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extBubble, List<ChartViewFieldDTO> extGroup, Datasource ds, ChartViewWithBLOBs view) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(KylinConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 0))
                .build();
        setSchema(tableObj, ds);
        List<SQLObj> xFields = new ArrayList<>();
        List<SQLObj> xOrders = new ArrayList<>();

        boolean xIsNumber = false;
        List<ChartViewFieldDTO> xAxisList = new ArrayList<>();

        //先判断x轴内是不是数值格式的
        if (CollectionUtils.isNotEmpty(xAxis)) {
            if (StringUtils.equals(xAxis.get(0).getGroupType(), "q") && StringUtils.equalsIgnoreCase(view.getRender(), "antv")) {
                xIsNumber = true;
            } else {
                xAxisList.addAll(xAxis);
            }
        }

        //然后是数值格式的情况还需要传extGroup
        if (xIsNumber && CollectionUtils.isNotEmpty(extGroup)) {
            xAxisList.addAll(extGroup);
        }

        if (CollectionUtils.isNotEmpty(xAxisList)) {
            for (int i = 0; i < xAxisList.size(); i++) {
                ChartViewFieldDTO x = xAxisList.get(i);
                String originField;
                if (ObjectUtils.isNotEmpty(x.getExtField()) && x.getExtField() == 2) {
                    // 解析origin name中有关联的字段生成sql表达式
                    originField = calcFieldRegex(x.getOriginName(), tableObj);
                } else if (ObjectUtils.isNotEmpty(x.getExtField()) && x.getExtField() == 1) {
                    originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);
                // 处理横轴字段
                xFields.add(getXFields(x, originField, fieldAlias));
                // 处理横轴排序
                if (StringUtils.isNotEmpty(x.getSort()) && !StringUtils.equalsIgnoreCase(x.getSort(), "none")) {
                    xOrders.add(SQLObj.builder()
                            .orderField(originField)
                            .orderAlias(fieldAlias)
                            .orderDirection(x.getSort())
                            .build());
                }
            }
        }
        List<SQLObj> yFields = new ArrayList<>();
        List<String> yWheres = new ArrayList<>();
        List<SQLObj> yOrders = new ArrayList<>();
        List<ChartViewFieldDTO> yList = new ArrayList<>();
        if (xIsNumber) {
            yList.add(xAxis.get(0));
        }
        yList.addAll(yAxis);
        yList.addAll(extBubble);
        if (CollectionUtils.isNotEmpty(yList)) {
            for (int i = 0; i < yList.size(); i++) {
                ChartViewFieldDTO y = yList.get(i);
                String originField;
                if (ObjectUtils.isNotEmpty(y.getExtField()) && y.getExtField() == 2) {
                    // 解析origin name中有关联的字段生成sql表达式
                    originField = calcFieldRegex(y.getOriginName(), tableObj);
                } else if (ObjectUtils.isNotEmpty(y.getExtField()) && y.getExtField() == 1) {
                    originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_Y_PREFIX, i);
                // 处理纵轴字段
                yFields.add(getYFields(y, originField, fieldAlias));
                // 处理纵轴过滤
                yWheres.add(getYWheres(y, originField, fieldAlias));
                // 处理纵轴排序
                if (StringUtils.isNotEmpty(y.getSort()) && !StringUtils.equalsIgnoreCase(y.getSort(), "none")) {
                    yOrders.add(SQLObj.builder()
                            .orderField(originField)
                            .orderAlias(fieldAlias)
                            .orderDirection(y.getSort())
                            .build());
                }
            }
        }
        // 处理视图中字段过滤
        String customWheres = transChartFilterTrees(tableObj, fieldCustomFilter);
        // 处理仪表板字段过滤
        String extWheres = transExtFilterList(tableObj, extFilterRequestList);
        // row permissions tree
        String whereTrees = transFilterTrees(tableObj, rowPermissionsTree);
        // 构建sql所有参数
        List<SQLObj> fields = new ArrayList<>();
        fields.addAll(xFields);
        fields.addAll(yFields);
        List<String> wheres = new ArrayList<>();
        if (customWheres != null) wheres.add(customWheres);
        if (extWheres != null) wheres.add(extWheres);
        if (whereTrees != null) wheres.add(whereTrees);
        List<SQLObj> groups = new ArrayList<>();
        groups.addAll(xFields);
        // 外层再次套sql
        List<SQLObj> orders = new ArrayList<>();
        orders.addAll(xOrders);
        orders.addAll(yOrders);
        List<String> aggWheres = new ArrayList<>();
        aggWheres.addAll(yWheres.stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList()));

        STGroup stg = new STGroupFile(SQLConstants.SQL_TEMPLATE);
        ST st_sql = stg.getInstanceOf("querySql");
        if (CollectionUtils.isNotEmpty(xFields)) st_sql.add("groups", xFields);
        if (CollectionUtils.isNotEmpty(yFields)) st_sql.add("aggregators", yFields);
        if (CollectionUtils.isNotEmpty(wheres)) st_sql.add("filters", wheres);
        if (ObjectUtils.isNotEmpty(tableObj)) st_sql.add("table", tableObj);
        String sql = st_sql.render();

        ST st = stg.getInstanceOf("querySql");
        SQLObj tableSQL = SQLObj.builder()
                .tableName(String.format(KylinConstants.BRACKETS, sql))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 1))
                .build();
        if (CollectionUtils.isNotEmpty(aggWheres)) st.add("filters", aggWheres);
        if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        return sqlLimit(st.render(), view);
    }

    @Override
    public String getSQLAsTmpScatter(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extBubble, List<ChartViewFieldDTO> extGroup, ChartViewWithBLOBs view) {
        return getSQLScatter("(" + sqlFix(table) + ")", xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, extBubble, extGroup, null, view);
    }

    @Override
    public String searchTable(String table) {
        return "SELECT table_name FROM information_schema.TABLES WHERE table_name ='" + table + "'";
    }

    @Override
    public String getSQLSummary(String table, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, ChartViewWithBLOBs view, Datasource ds) {
        // 字段汇总 排序等
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(KylinConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 0))
                .build();
        setSchema(tableObj, ds);
        List<SQLObj> yFields = new ArrayList<>();
        List<String> yWheres = new ArrayList<>();
        List<SQLObj> yOrders = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(yAxis)) {
            for (int i = 0; i < yAxis.size(); i++) {
                ChartViewFieldDTO y = yAxis.get(i);
                String originField;
                if (ObjectUtils.isNotEmpty(y.getExtField()) && y.getExtField() == 2) {
                    // 解析origin name中有关联的字段生成sql表达式
                    originField = calcFieldRegex(y.getOriginName(), tableObj);
                } else if (ObjectUtils.isNotEmpty(y.getExtField()) && y.getExtField() == 1) {
                    originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_Y_PREFIX, i);
                // 处理纵轴字段
                yFields.add(getYFields(y, originField, fieldAlias));
                // 处理纵轴过滤
                yWheres.add(getYWheres(y, originField, fieldAlias));
                // 处理纵轴排序
                if (StringUtils.isNotEmpty(y.getSort()) && !StringUtils.equalsIgnoreCase(y.getSort(), "none")) {
                    yOrders.add(SQLObj.builder()
                            .orderField(originField)
                            .orderAlias(fieldAlias)
                            .orderDirection(y.getSort())
                            .build());
                }
            }
        }
        // 处理视图中字段过滤
        String customWheres = transChartFilterTrees(tableObj, fieldCustomFilter);
        // 处理仪表板字段过滤
        String extWheres = transExtFilterList(tableObj, extFilterRequestList);
        // row permissions tree
        String whereTrees = transFilterTrees(tableObj, rowPermissionsTree);
        // 构建sql所有参数
        List<SQLObj> fields = new ArrayList<>();
        fields.addAll(yFields);
        List<String> wheres = new ArrayList<>();
        if (customWheres != null) wheres.add(customWheres);
        if (extWheres != null) wheres.add(extWheres);
        if (whereTrees != null) wheres.add(whereTrees);
        List<SQLObj> groups = new ArrayList<>();
        // 外层再次套sql
        List<SQLObj> orders = new ArrayList<>();
        orders.addAll(yOrders);
        List<String> aggWheres = new ArrayList<>();
        aggWheres.addAll(yWheres.stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList()));

        STGroup stg = new STGroupFile(SQLConstants.SQL_TEMPLATE);
        ST st_sql = stg.getInstanceOf("querySql");
        if (CollectionUtils.isNotEmpty(yFields)) st_sql.add("aggregators", yFields);
        if (CollectionUtils.isNotEmpty(wheres)) st_sql.add("filters", wheres);
        if (ObjectUtils.isNotEmpty(tableObj)) st_sql.add("table", tableObj);
        String sql = st_sql.render();

        ST st = stg.getInstanceOf("querySql");
        SQLObj tableSQL = SQLObj.builder()
                .tableName(String.format(KylinConstants.BRACKETS, sql))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 1))
                .build();
        if (CollectionUtils.isNotEmpty(aggWheres)) st.add("filters", aggWheres);
        if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        return sqlLimit(st.render(), view);
    }

    @Override
    public String getSQLSummaryAsTmp(String sql, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, ChartViewWithBLOBs view) {
        return getSQLSummary("(" + sqlFix(sql) + ")", yAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, view, null);
    }

    @Override
    public String wrapSql(String sql) {
        sql = sql.trim();
        if (sql.lastIndexOf(";") == (sql.length() - 1)) {
            sql = sql.substring(0, sql.length() - 1);
        }
        String tmpSql = "SELECT * FROM (" + sql + ") AS tmp " + " LIMIT 0 ";
        return tmpSql;
    }

    @Override
    public String createRawQuerySQL(String table, List<DatasetTableField> fields, Datasource ds) {
        String[] array = fields.stream().map(f -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\"").append(f.getOriginName()).append("\" AS ").append(f.getDataeaseName());
            return stringBuilder.toString();
        }).toArray(String[]::new);
        if (ds != null) {
            String schema = new Gson().fromJson(ds.getConfiguration(), JdbcConfiguration.class).getSchema();
            String tableWithSchema = String.format(SqlServerSQLConstants.KEYWORD_TABLE, schema) + "." + String.format(SqlServerSQLConstants.KEYWORD_TABLE, table);
            return MessageFormat.format("SELECT {0} FROM {1}  ", StringUtils.join(array, ","), tableWithSchema);
        } else {
            return MessageFormat.format("SELECT {0} FROM {1}  ", StringUtils.join(array, ","), table);
        }
    }

    @Override
    public String createRawQuerySQLAsTmp(String sql, List<DatasetTableField> fields) {
        return createRawQuerySQL(" (" + sqlFix(sql) + ") AS tmp ", fields, null);
    }

    @Override
    public String transTreeItem(SQLObj tableObj, DatasetRowPermissionsTreeItem item) {
        String res = null;
        DatasetTableField field = item.getField();
        if (ObjectUtils.isEmpty(field)) {
            return null;
        }
        String whereName = "";
        String originName;
        if (ObjectUtils.isNotEmpty(field.getExtField()) && field.getExtField() == 2) {
            // 解析origin name中有关联的字段生成sql表达式
            originName = calcFieldRegex(field.getOriginName(), tableObj);
        } else if (ObjectUtils.isNotEmpty(field.getExtField()) && field.getExtField() == 1) {
            originName = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
        } else {
            originName = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
        }
        if (field.getDeType() == DeTypeConstants.DE_TIME) {
            if (field.getDeExtractType() == DeTypeConstants.DE_STRING || field.getDeExtractType() == 5) {
                whereName = String.format(KylinConstants.CAST, originName, KylinConstants.DEFAULT_DATE_TYPE);
            }
            if (field.getDeExtractType() == DeTypeConstants.DE_INT || field.getDeExtractType() == DeTypeConstants.DE_FLOAT || field.getDeExtractType() == 4) {
                String cast = String.format(KylinConstants.CAST, originName, KylinConstants.DEFAULT_INT_FORMAT);
                whereName = String.format(KylinConstants.CAST, originName, KylinConstants.DEFAULT_DATE_TYPE);
            }
            if (field.getDeExtractType() == DeTypeConstants.DE_STRING) {
                whereName = originName;
            }
        } else if (field.getDeType() == DeTypeConstants.DE_INT || field.getDeType() == DeTypeConstants.DE_FLOAT) {
            if (field.getDeExtractType() == DeTypeConstants.DE_STRING || field.getDeExtractType() == 5) {
                whereName = String.format(KylinConstants.CAST, originName, KylinConstants.DEFAULT_FLOAT_FORMAT);
            }
            if (field.getDeExtractType() == DeTypeConstants.DE_TIME) {
                whereName = String.format(KylinConstants.CAST, originName, KylinConstants.DEFAULT_DATE_TYPE);
            }
            if (field.getDeExtractType() == DeTypeConstants.DE_INT || field.getDeExtractType() == DeTypeConstants.DE_FLOAT || field.getDeExtractType() == 4) {
                whereName = originName;
            }
        } else {
            whereName = originName;
        }

        if (StringUtils.equalsIgnoreCase(item.getFilterType(), "enum")) {
            if (CollectionUtils.isNotEmpty(item.getEnumValue())) {
                res = "(" + whereName + " IN ('" + String.join("','", item.getEnumValue()) + "'))";
            }
        } else {
            String value = item.getValue();
            String whereTerm = transMysqlFilterTerm(item.getTerm());
            String whereValue = "";

            if (StringUtils.equalsIgnoreCase(item.getTerm(), "null")) {
                whereValue = "";
            } else if (StringUtils.equalsIgnoreCase(item.getTerm(), "not_null")) {
                whereValue = "";
            } else if (StringUtils.equalsIgnoreCase(item.getTerm(), "empty")) {
                whereValue = "''";
            } else if (StringUtils.equalsIgnoreCase(item.getTerm(), "not_empty")) {
                whereValue = "''";
            } else if (StringUtils.equalsIgnoreCase(item.getTerm(), "in") || StringUtils.equalsIgnoreCase(item.getTerm(), "not in")) {
                whereValue = "('" + String.join("','", value.split(",")) + "')";
            } else if (StringUtils.containsIgnoreCase(item.getTerm(), "like")) {
                whereValue = "'%" + value + "%'";
            } else if (StringUtils.equalsIgnoreCase(item.getTerm(), "begin_with")) {
                whereValue = "'" + value + "%'";
            } else if (StringUtils.containsIgnoreCase(item.getTerm(), "end_with")) {
                whereValue = "'%" + value + "'";
            } else {
                whereValue = String.format(KylinConstants.WHERE_VALUE_VALUE, value);
            }
            SQLObj build = SQLObj.builder()
                    .whereField(whereName)
                    .whereTermAndValue(whereTerm + whereValue)
                    .build();
            res = build.getWhereField() + " " + build.getWhereTermAndValue();
        }
        return res;
    }

    @Override
    public String transTreeItem(SQLObj tableObj, FilterTreeItem item) {
        String res = null;
        DatasetTableField field = item.getField();
        if (ObjectUtils.isEmpty(field)) {
            return null;
        }
        String whereName = "";
        String originName;
        if (ObjectUtils.isNotEmpty(field.getExtField()) && field.getExtField() == 2) {
            // 解析origin name中有关联的字段生成sql表达式
            originName = calcFieldRegex(field.getOriginName(), tableObj);
        } else if (ObjectUtils.isNotEmpty(field.getExtField()) && field.getExtField() == 1) {
            originName = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
        } else {
            originName = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
        }
        if (field.getDeType() == DeTypeConstants.DE_TIME) {
            if (field.getDeExtractType() == DeTypeConstants.DE_STRING || field.getDeExtractType() == 5) {
                whereName = String.format(KylinConstants.CAST, originName, KylinConstants.DEFAULT_DATE_TYPE);
            }
            if (field.getDeExtractType() == DeTypeConstants.DE_INT || field.getDeExtractType() == DeTypeConstants.DE_FLOAT || field.getDeExtractType() == 4) {
                String cast = String.format(KylinConstants.CAST, originName, KylinConstants.DEFAULT_INT_FORMAT);
                whereName = String.format(KylinConstants.CAST, originName, KylinConstants.DEFAULT_DATE_TYPE);
            }
            if (field.getDeExtractType() == DeTypeConstants.DE_STRING) {
                whereName = originName;
            }
        } else if (field.getDeType() == DeTypeConstants.DE_INT || field.getDeType() == DeTypeConstants.DE_FLOAT) {
            if (field.getDeExtractType() == DeTypeConstants.DE_STRING || field.getDeExtractType() == 5) {
                whereName = String.format(KylinConstants.CAST, originName, KylinConstants.DEFAULT_FLOAT_FORMAT);
            }
            if (field.getDeExtractType() == DeTypeConstants.DE_TIME) {
                whereName = String.format(KylinConstants.CAST, originName, KylinConstants.DEFAULT_DATE_TYPE);
            }
            if (field.getDeExtractType() == DeTypeConstants.DE_INT || field.getDeExtractType() == DeTypeConstants.DE_FLOAT || field.getDeExtractType() == 4) {
                whereName = originName;
            }
        } else {
            whereName = originName;
        }

        if (StringUtils.equalsIgnoreCase(item.getFilterType(), "enum")) {
            if (CollectionUtils.isNotEmpty(item.getEnumValue())) {
                res = "(" + whereName + " IN ('" + String.join("','", item.getEnumValue()) + "'))";
            }
        } else {
            String value = item.getValue();
            String whereTerm = transMysqlFilterTerm(item.getTerm());
            String whereValue = "";

            if (StringUtils.equalsIgnoreCase(item.getTerm(), "null")) {
                whereValue = "";
            } else if (StringUtils.equalsIgnoreCase(item.getTerm(), "not_null")) {
                whereValue = "";
            } else if (StringUtils.equalsIgnoreCase(item.getTerm(), "empty")) {
                whereValue = "''";
            } else if (StringUtils.equalsIgnoreCase(item.getTerm(), "not_empty")) {
                whereValue = "''";
            } else if (StringUtils.equalsIgnoreCase(item.getTerm(), "in") || StringUtils.equalsIgnoreCase(item.getTerm(), "not in")) {
                whereValue = "('" + String.join("','", value.split(",")) + "')";
            } else if (StringUtils.containsIgnoreCase(item.getTerm(), "like")) {
                whereValue = "'%" + value + "%'";
            } else if (StringUtils.equalsIgnoreCase(item.getTerm(), "begin_with")) {
                whereValue = "'" + value + "%'";
            } else if (StringUtils.containsIgnoreCase(item.getTerm(), "end_with")) {
                whereValue = "'%" + value + "'";
            } else {
                whereValue = String.format(KylinConstants.WHERE_VALUE_VALUE, value);
            }
            SQLObj build = SQLObj.builder()
                    .whereField(whereName)
                    .whereTermAndValue(whereTerm + whereValue)
                    .build();
            res = build.getWhereField() + " " + build.getWhereTermAndValue();
        }
        return res;
    }

    @Override
    public String convertTableToSql(String tableName, Datasource ds) {
        String schema = new Gson().fromJson(ds.getConfiguration(), JdbcConfiguration.class).getSchema();
        schema = String.format(KylinConstants.KEYWORD_TABLE, schema);
        return createSQLPreview("SELECT * FROM " + schema + "." + String.format(KylinConstants.KEYWORD_TABLE, tableName), null);
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
            case "begin_with":
            case "end_with":
                return " LIKE ";
            case "not like":
                return " NOT LIKE ";
            case "null":
                return " IS NULL ";
            case "not_null":
                return " IS NOT NULL ";
            case "empty":
                return " = ";
            case "not_empty":
                return " <> ";
            case "between":
                return " BETWEEN ";
            default:
                return "";
        }
    }

    @Deprecated
    public String transCustomFilterList(SQLObj tableObj, List<ChartFieldCustomFilterDTO> requestList) {
        if (CollectionUtils.isEmpty(requestList)) {
            return null;
        }
        List<String> res = new ArrayList<>();
        for (ChartFieldCustomFilterDTO request : requestList) {
            List<SQLObj> list = new ArrayList<>();
            DatasetTableField field = request.getField();

            if (ObjectUtils.isEmpty(field)) {
                continue;
            }
            String whereName = "";
            String originName;
            if (ObjectUtils.isNotEmpty(field.getExtField()) && field.getExtField() == 2) {
                // 解析origin name中有关联的字段生成sql表达式
                originName = calcFieldRegex(field.getOriginName(), tableObj);
            } else if (ObjectUtils.isNotEmpty(field.getExtField()) && field.getExtField() == 1) {
                originName = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
            } else {
                originName = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
            }
            if (field.getDeType() == DeTypeConstants.DE_TIME) {
                if (field.getDeExtractType() == DeTypeConstants.DE_STRING || field.getDeExtractType() == 5) {
                    whereName = String.format(KylinConstants.CAST, originName, KylinConstants.DEFAULT_DATE_TYPE);
                }
                if (field.getDeExtractType() == DeTypeConstants.DE_INT || field.getDeExtractType() == DeTypeConstants.DE_FLOAT || field.getDeExtractType() == DeTypeConstants.DE_BOOL) {
                    whereName = String.format(KylinConstants.CAST, originName, KylinConstants.DEFAULT_DATE_TYPE);
                }
                if (field.getDeExtractType() == DeTypeConstants.DE_STRING) {
                    whereName = originName;
                }
            } else if (field.getDeType() == DeTypeConstants.DE_INT || field.getDeType() == DeTypeConstants.DE_FLOAT) {
                if (field.getDeExtractType() == DeTypeConstants.DE_STRING || field.getDeExtractType() == 5) {
                    whereName = String.format(KylinConstants.CAST, originName, KylinConstants.DEFAULT_FLOAT_FORMAT);
                }
                if (field.getDeExtractType() == DeTypeConstants.DE_TIME) {
                    whereName = String.format(KylinConstants.CAST, originName, KylinConstants.DEFAULT_INT_FORMAT);
                }
                if (field.getDeExtractType() == DeTypeConstants.DE_INT || field.getDeExtractType() == DeTypeConstants.DE_FLOAT || field.getDeExtractType() == 4) {
                    whereName = originName;
                }
            } else {
                whereName = originName;
            }

            if (StringUtils.equalsIgnoreCase(request.getFilterType(), "enum")) {
                if (CollectionUtils.isNotEmpty(request.getEnumCheckField())) {
                    res.add("(" + whereName + " IN ('" + String.join("','", request.getEnumCheckField()) + "'))");
                }
            } else {
                List<ChartCustomFilterItemDTO> filter = request.getFilter();
                for (ChartCustomFilterItemDTO filterItemDTO : filter) {
                    String value = filterItemDTO.getValue();
                    String whereTerm = transMysqlFilterTerm(filterItemDTO.getTerm());
                    String whereValue = "";

                    if (StringUtils.equalsIgnoreCase(filterItemDTO.getTerm(), "null")) {
                        whereValue = "";
                    } else if (StringUtils.equalsIgnoreCase(filterItemDTO.getTerm(), "not_null")) {
                        whereValue = "";
                    } else if (StringUtils.equalsIgnoreCase(filterItemDTO.getTerm(), "empty")) {
                        whereValue = "''";
                    } else if (StringUtils.equalsIgnoreCase(filterItemDTO.getTerm(), "not_empty")) {
                        whereValue = "''";
                    } else if (StringUtils.containsIgnoreCase(filterItemDTO.getTerm(), "in") || StringUtils.containsIgnoreCase(filterItemDTO.getTerm(), "not in")) {
                        whereValue = "('" + String.join("','", value.split(",")) + "')";
                    } else if (StringUtils.containsIgnoreCase(filterItemDTO.getTerm(), "like")) {
                        whereValue = "'%" + value + "%'";
                    } else {
                        whereValue = String.format(KylinConstants.WHERE_VALUE_VALUE, value);
                    }
                    list.add(SQLObj.builder()
                            .whereField(whereName)
                            .whereTermAndValue(whereTerm + whereValue)
                            .build());
                }

                List<String> strList = new ArrayList<>();
                list.forEach(ele -> strList.add(ele.getWhereField() + " " + ele.getWhereTermAndValue()));
                if (CollectionUtils.isNotEmpty(list)) {
                    res.add("(" + String.join(" " + getLogic(request.getLogic()) + " ", strList) + ")");
                }
            }
        }
        return CollectionUtils.isNotEmpty(res) ? "(" + String.join(" AND ", res) + ")" : null;
    }

    public String transExtFilterList(SQLObj tableObj, List<ChartExtFilterRequest> requestList) {
        if (CollectionUtils.isEmpty(requestList)) {
            return null;
        }
        List<SQLObj> list = new ArrayList<>();
        for (ChartExtFilterRequest request : requestList) {
            List<String> value = request.getValue();

            List<String> whereNameList = new ArrayList<>();
            List<DatasetTableField> fieldList = new ArrayList<>();
            if (request.getIsTree()) {
                fieldList.addAll(request.getDatasetTableFieldList());
            } else {
                fieldList.add(request.getDatasetTableField());
            }

            for (DatasetTableField field : fieldList) {
                if (CollectionUtils.isEmpty(value) || ObjectUtils.isEmpty(field)) {
                    continue;
                }
                String whereName = "";

                String originName;
                if (ObjectUtils.isNotEmpty(field.getExtField()) && field.getExtField() == 2) {
                    // 解析origin name中有关联的字段生成sql表达式
                    originName = calcFieldRegex(field.getOriginName(), tableObj);
                } else if (ObjectUtils.isNotEmpty(field.getExtField()) && field.getExtField() == 1) {
                    originName = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
                } else {
                    originName = String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
                }

                if (field.getDeType() == DeTypeConstants.DE_TIME) {
                    if (field.getDeExtractType() == DeTypeConstants.DE_STRING || field.getDeExtractType() == 5) {
                        whereName = String.format(KylinConstants.CAST, originName, KylinConstants.DEFAULT_DATE_TYPE);
                    }
                    if (field.getDeExtractType() == DeTypeConstants.DE_INT || field.getDeExtractType() == DeTypeConstants.DE_FLOAT || field.getDeExtractType() == 4) {
                        whereName = String.format(KylinConstants.CAST, originName, KylinConstants.DEFAULT_DATE_TYPE);
                    }
                    if (field.getDeExtractType() == 1) {
                        whereName = originName;
                    }
                } else if (field.getDeType() == DeTypeConstants.DE_INT || field.getDeType() == DeTypeConstants.DE_FLOAT) {
                    if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                        whereName = String.format(KylinConstants.CAST, originName, KylinConstants.DEFAULT_FLOAT_FORMAT);
                    }
                    if (field.getDeExtractType() == DeTypeConstants.DE_TIME) {
                        whereName = String.format(KylinConstants.CAST, originName, KylinConstants.DEFAULT_FLOAT_FORMAT);
                    }
                    if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                        whereName = originName;
                    }
                } else {
                    whereName = originName;
                }
                whereNameList.add(whereName);
            }

            String whereName = "";
            if (request.getIsTree()) {
                whereName = "CONCAT(" + StringUtils.join(whereNameList, ",',',") + ")";
            } else {
                whereName = whereNameList.get(0);
            }
            String whereTerm = transMysqlFilterTerm(request.getOperator());
            String whereValue = "";

            if (StringUtils.containsIgnoreCase(request.getOperator(), "in")) {
                // 过滤空数据
                if (value.contains(SQLConstants.EMPTY_SIGN)) {
                    whereValue = "('" + StringUtils.join(value, "','") + "', '')" + " or " + whereName + " is null ";
                } else {
                    whereValue = "('" + StringUtils.join(value, "','") + "')";
                }
            } else if (StringUtils.containsIgnoreCase(request.getOperator(), "like")) {
                String keyword = value.get(0).toUpperCase();
                whereValue = formatLikeValue(keyword);
                whereName = "upper(" + whereName + ")";
            } else if (StringUtils.containsIgnoreCase(request.getOperator(), "between")) {
                if (request.getDatasetTableField().getDeType() == 1) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String startTime = simpleDateFormat.format(new Date(Long.parseLong(value.get(0))));
                    String endTime = simpleDateFormat.format(new Date(Long.parseLong(value.get(1))));
                    whereValue = String.format(KylinConstants.WHERE_BETWEEN, startTime, endTime);
                } else {
                    whereValue = String.format(KylinConstants.WHERE_BETWEEN, value.get(0), value.get(1));
                }
            } else {
                // 过滤空数据
                if (StringUtils.equals(value.get(0), SQLConstants.EMPTY_SIGN)) {
                    whereValue = String.format(KylinConstants.WHERE_VALUE_VALUE, "") + " or " + whereName + " is null ";
                } else {
                    whereValue = String.format(KylinConstants.WHERE_VALUE_VALUE, value.get(0));
                }
            }
            list.add(SQLObj.builder()
                    .whereField(whereName)
                    .whereTermAndValue(whereTerm + whereValue)
                    .build());
        }
        List<String> strList = new ArrayList<>();
        list.forEach(ele -> strList.add("(" + ele.getWhereField() + " " + ele.getWhereTermAndValue() + ")"));
        return CollectionUtils.isNotEmpty(list) ? "(" + String.join(" AND ", strList) + ")" : null;
    }

    private String sqlFix(String sql) {
        if (sql.lastIndexOf(";") == (sql.length() - 1)) {
            sql = sql.substring(0, sql.length() - 1);
        }
        return sql;
    }

    private String transDateFormat(String dateStyle, String datePattern) {
        String split = "-";
        if (StringUtils.equalsIgnoreCase(datePattern, "date_sub")) {
            split = "-";
        } else if (StringUtils.equalsIgnoreCase(datePattern, "date_split")) {
            split = "/";
        } else {
            split = "-";
        }

        if (StringUtils.isEmpty(dateStyle)) {
            return "yyyy-MM-dd HH:mm:ss";
        }

        switch (dateStyle) {
            case "y":
                return "yyyy";
            case "y_M":
                return "yyyy" + split + "MM";
            case "y_M_d":
                return "yyyy" + split + "MM" + split + "dd";
            case "H_m_s":
                return "hh:mi:ss";
            case "y_M_d_H_m":
                return "yyyy" + split + "MM" + split + "dd" + " HH:mm";
            case "y_M_d_H_m_s":
                return "yyyy" + split + "MM" + split + "dd" + " HH:mm:ss";
            default:
                return "yyyy-MM-dd HH:mm:s";
        }
    }

    private SQLObj getXFields(ChartViewFieldDTO x, String originField, String fieldAlias) {
        String fieldName = "";
        if (x.getDeExtractType() == DeTypeConstants.DE_TIME) {
//            if (x.getDeType() == DeTypeConstants.DE_INT || x.getDeType() == DeTypeConstants.DE_FLOAT) {
//                fieldName = String.format(KylinConstants.UNIX_TIMESTAMP, originField);
//            } else if (x.getDeType() == DeTypeConstants.DE_TIME) {
//                String format = transDateFormat(x.getDateStyle(), x.getDatePattern());
//                fieldName = String.format(KylinConstants.FORMAT_DATETIME, originField, format);
//            } else {
//                fieldName = originField;
//            }
            fieldName = originField;
        } else {
            if (x.getDeType() == DeTypeConstants.DE_TIME) {
                String format = transDateFormat(x.getDateStyle(), x.getDatePattern());
                if (x.getDeExtractType() == DeTypeConstants.DE_STRING) {
                    fieldName = String.format(KylinConstants.CAST, originField, KylinConstants.DEFAULT_DATE_TYPE);
                } else {
                    fieldName = originField;
                }
            } else {
                if (x.getDeType() == DeTypeConstants.DE_INT) {
                    fieldName = String.format(KylinConstants.CAST, originField, KylinConstants.DEFAULT_INT_FORMAT);
                } else if (x.getDeType() == DeTypeConstants.DE_FLOAT) {
                    fieldName = String.format(KylinConstants.CAST, originField, KylinConstants.DEFAULT_FLOAT_FORMAT);
                } else {
                    fieldName = originField;
                }
            }
        }
        return SQLObj.builder()
                .fieldName(fieldName)
                .fieldAlias(fieldAlias)
                .build();
    }

    private SQLObj getYFields(ChartViewFieldDTO y, String originField, String fieldAlias) {
        String fieldName = "";
        if (StringUtils.equalsIgnoreCase(y.getOriginName(), "*")) {
            fieldName = KylinConstants.AGG_COUNT;
        } else if (SQLConstants.DIMENSION_TYPE.contains(y.getDeType())) {
            fieldName = String.format(KylinConstants.AGG_FIELD, y.getSummary(), originField);
        } else {
            if (StringUtils.equalsIgnoreCase(y.getSummary(), "avg") || StringUtils.containsIgnoreCase(y.getSummary(), "pop")) {
                String cast = String.format(KylinConstants.CAST, originField, y.getDeType() == DeTypeConstants.DE_INT ? KylinConstants.DEFAULT_INT_FORMAT : KylinConstants.DEFAULT_FLOAT_FORMAT);
                String agg = String.format(KylinConstants.AGG_FIELD, y.getSummary(), cast);
                fieldName = String.format(KylinConstants.CAST, agg, KylinConstants.DEFAULT_FLOAT_FORMAT);
            } else {
                String cast = String.format(KylinConstants.CAST, originField, y.getDeType() == DeTypeConstants.DE_INT ? KylinConstants.DEFAULT_INT_FORMAT : KylinConstants.DEFAULT_FLOAT_FORMAT);
                fieldName = String.format(KylinConstants.AGG_FIELD, y.getSummary(), cast);
            }
        }
        return SQLObj.builder()
                .fieldName(fieldName)
                .fieldAlias(fieldAlias)
                .build();
    }

    private String getYWheres(ChartViewFieldDTO y, String originField, String fieldAlias) {
        List<SQLObj> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(y.getFilter()) && y.getFilter().size() > 0) {
            y.getFilter().forEach(f -> {
                String whereTerm = transMysqlFilterTerm(f.getTerm());
                String whereValue = "";
                // 原始类型不是时间，在de中被转成时间的字段做处理
                if (StringUtils.equalsIgnoreCase(f.getTerm(), "null")) {
                    whereValue = "";
                } else if (StringUtils.equalsIgnoreCase(f.getTerm(), "not_null")) {
                    whereValue = "";
                } else if (StringUtils.equalsIgnoreCase(f.getTerm(), "empty")) {
                    whereValue = "''";
                } else if (StringUtils.equalsIgnoreCase(f.getTerm(), "not_empty")) {
                    whereValue = "''";
                } else if (StringUtils.containsIgnoreCase(f.getTerm(), "in")) {
                    whereValue = "('" + StringUtils.join(f.getValue(), "','") + "')";
                } else if (StringUtils.containsIgnoreCase(f.getTerm(), "like")) {
                    whereValue = "'%" + f.getValue() + "%'";
                } else {
                    whereValue = String.format(KylinConstants.WHERE_VALUE_VALUE, f.getValue());
                }
                list.add(SQLObj.builder()
                        .whereField(fieldAlias)
                        .whereAlias(fieldAlias)
                        .whereTermAndValue(whereTerm + whereValue)
                        .build());
            });
        }
        List<String> strList = new ArrayList<>();
        list.forEach(ele -> strList.add(ele.getWhereField() + " " + ele.getWhereTermAndValue()));
        return CollectionUtils.isNotEmpty(list) ? "(" + String.join(" " + getLogic(y.getLogic()) + " ", strList) + ")" : null;
    }

    private String calcFieldRegex(String originField, SQLObj tableObj) {
        originField = originField.replaceAll("[\\t\\n\\r]]", "");
        // 正则提取[xxx]
        String regex = "\\[(.*?)]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(originField);
        Set<String> ids = new HashSet<>();
        while (matcher.find()) {
            String id = matcher.group(1);
            ids.add(id);
        }
        if (CollectionUtils.isEmpty(ids)) {
            return originField;
        }
        DatasetTableFieldExample datasetTableFieldExample = new DatasetTableFieldExample();
        datasetTableFieldExample.createCriteria().andIdIn(new ArrayList<>(ids));
        List<DatasetTableField> calcFields = datasetTableFieldMapper.selectByExample(datasetTableFieldExample);
        for (DatasetTableField ele : calcFields) {
            originField = originField.replaceAll("\\[" + ele.getId() + "]",
                    String.format(KylinConstants.KEYWORD_FIX, tableObj.getTableAlias(), ele.getOriginName()));
        }
        return originField;
    }

    private String sqlLimit(String sql, ChartViewWithBLOBs view) {
        if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom")) {
            return sql + " LIMIT " + view.getResultCount();
        } else {
            return sql;
        }
    }

    public String getResultCount(boolean isTable, String sql, List<ChartViewFieldDTO> xAxis, List<ChartFieldCustomFilterDTO> fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view) {
        return null;
    }

}

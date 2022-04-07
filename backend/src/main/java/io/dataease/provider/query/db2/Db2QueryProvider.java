package io.dataease.provider.query.db2;

import com.google.gson.Gson;
import io.dataease.base.domain.ChartViewWithBLOBs;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.base.domain.DatasetTableFieldExample;
import io.dataease.base.domain.Datasource;
import io.dataease.base.mapper.DatasetTableFieldMapper;
import io.dataease.commons.constants.DeTypeConstants;
import io.dataease.controller.request.chart.ChartExtFilterRequest;
import io.dataease.dto.chart.ChartCustomFilterItemDTO;
import io.dataease.dto.chart.ChartFieldCustomFilterDTO;
import io.dataease.dto.chart.ChartViewFieldDTO;
import io.dataease.dto.datasource.Db2Configuration;
import io.dataease.dto.sqlObj.SQLObj;
import io.dataease.plugins.common.constants.Db2Constants;
import io.dataease.provider.QueryProvider;
import io.dataease.plugins.common.constants.SQLConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
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

import static io.dataease.plugins.common.constants.SQLConstants.TABLE_ALIAS_PREFIX;

@Service("db2Query")
public class Db2QueryProvider extends QueryProvider {
    @Resource
    private DatasetTableFieldMapper datasetTableFieldMapper;

    @Override
    public Integer transFieldType(String field) {
        field = field.toUpperCase();
        switch (field) {
            case "BINARY":
            case "BLOB":
            case "CHAR":
            case "CLOB":
            case "DBCLOB":
            case "GRAPHIC":
            case "VARCHAR":
            case "VARGRAPHIC":
            case "XML":
                return 0;// 文本
            case "TIME":
            case "TIMESTAMP":
            case "DATE":
                return 1;// 时间
            case "BIGINT":
            case "INTEGER":
            case "SMALLINT":
            case "INT":
                return 2;// 整型
            case "DECFLOAT":
            case "DECIMAL":
            case "DOUBLE":
            case "NUMERIC":
            case "REAL":
                return 3;// 浮点
            case "BOOLEAN":
                return 4;// 布尔
            default:
                return 0;
        }
    }

    @Override
    public String createSQLPreview(String sql, String orderBy) {
        return "SELECT * FROM (" + sqlFix(sql) + ") DE_TMP " + " fetch first 1000 rows only;";
    }

    @Override
    public String createQuerySQL(String table, List<DatasetTableField> fields, boolean isGroup, Datasource ds, List<ChartFieldCustomFilterDTO> fieldCustomFilter) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(Db2Constants.KEYWORD_TABLE, table))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 0))
                .build();
        setSchema(tableObj, ds);
        List<SQLObj> xFields = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(fields)) {
            for (int i = 0; i < fields.size(); i++) {
                DatasetTableField f = fields.get(i);
                String originField;
                if (ObjectUtils.isNotEmpty(f.getExtField()) && f.getExtField() == 2) {
                    // 解析origin name中有关联的字段生成sql表达式
                    originField = calcFieldRegex(f.getOriginName(), tableObj);
                } else if (ObjectUtils.isNotEmpty(f.getExtField()) && f.getExtField() == 1) {
                    originField = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), f.getOriginName());
                } else {
                    originField = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), f.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);
                String fieldName = "";
                // 处理横轴字段
                if (f.getDeExtractType() == DeTypeConstants.DE_TIME) {
                    if (f.getDeType() == DeTypeConstants.DE_INT || f.getDeType() == DeTypeConstants.DE_FLOAT) {
                        fieldName = String.format(Db2Constants.UNIX_TIMESTAMP, originField);
                    } else {
                        fieldName = originField;
                    }
                } else if (f.getDeExtractType() == DeTypeConstants.DE_STRING) {
                    if (f.getDeType() == DeTypeConstants.DE_INT) {
                        fieldName = String.format(Db2Constants.CAST, originField, Db2Constants.DEFAULT_INT_FORMAT);
                    } else if (f.getDeType() == DeTypeConstants.DE_FLOAT) {
                        fieldName = String.format(Db2Constants.CAST, originField, Db2Constants.DEFAULT_FLOAT_FORMAT);
                    } else if (f.getDeType() == DeTypeConstants.DE_TIME) {
                        fieldName = String.format(Db2Constants.DATE_FORMAT, originField, Db2Constants.DEFAULT_DATE_FORMAT);
                    } else {
                        fieldName = originField;
                    }
                } else {
                    if (f.getDeType() == DeTypeConstants.DE_TIME) {
                        String cast = String.format(Db2Constants.CAST, originField, Db2Constants.DEFAULT_INT_FORMAT);
                        fieldName = String.format(Db2Constants.FROM_UNIXTIME, cast, Db2Constants.DEFAULT_DATE_FORMAT);
                    } else if (f.getDeType() == DeTypeConstants.DE_INT) {
                        fieldName = String.format(Db2Constants.CAST, originField, Db2Constants.DEFAULT_INT_FORMAT);
                    } else {
                        fieldName = originField;
                    }
                }
                xFields.add(SQLObj.builder()
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

        String customWheres = transCustomFilterList(tableObj, fieldCustomFilter);
        List<String> wheres = new ArrayList<>();
        if (customWheres != null) wheres.add(customWheres);
        if (CollectionUtils.isNotEmpty(wheres)) st_sql.add("filters", wheres);

        return st_sql.render();
    }

    @Override
    public String createQuerySQLAsTmp(String sql, List<DatasetTableField> fields, boolean isGroup, List<ChartFieldCustomFilterDTO> fieldCustomFilter) {
        return createQuerySQL("(" + sqlFix(sql) + ")", fields, isGroup, null, fieldCustomFilter);
    }

    @Override
    public String createQueryTableWithPage(String table, List<DatasetTableField> fields, Integer page, Integer pageSize, Integer realSize, boolean isGroup, Datasource ds, List<ChartFieldCustomFilterDTO> fieldCustomFilter) {
        Integer size = (page - 1) * pageSize + realSize;
        return createQuerySQL(table, fields, isGroup, ds, fieldCustomFilter) + String.format(" fetch first %s rows only; ", size);
    }

    @Override
    public String createQueryTableWithLimit(String table, List<DatasetTableField> fields, Integer limit, boolean isGroup, Datasource ds, List<ChartFieldCustomFilterDTO> fieldCustomFilter) {
        return createQuerySQL(table, fields, isGroup, ds, fieldCustomFilter) + String.format(" fetch first %s rows only; ", limit);
    }

    @Override
    public String createQuerySqlWithLimit(String sql, List<DatasetTableField> fields, Integer limit, boolean isGroup, List<ChartFieldCustomFilterDTO> fieldCustomFilter) {
        return createQuerySQLAsTmp(sql, fields, isGroup, fieldCustomFilter) + String.format(" fetch first %s rows only; ", limit);
    }

    @Override
    public String createQuerySQLWithPage(String sql, List<DatasetTableField> fields, Integer page, Integer pageSize, Integer realSize, boolean isGroup, List<ChartFieldCustomFilterDTO> fieldCustomFilter) {
        Integer size = (page - 1) * pageSize + realSize;
        return createQuerySQLAsTmp(sql, fields, isGroup, fieldCustomFilter) + String.format(" fetch first %s rows only; ", size);
    }

    @Override
    public String getSQL(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartFieldCustomFilterDTO> fieldCustomFilter, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(Db2Constants.KEYWORD_TABLE, table))
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
                    originField = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    originField = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
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
                    originField = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
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
        String customWheres = transCustomFilterList(tableObj, fieldCustomFilter);
        // 处理仪表板字段过滤
        String extWheres = transExtFilterList(tableObj, extFilterRequestList);
        // 构建sql所有参数
        List<SQLObj> fields = new ArrayList<>();
        fields.addAll(xFields);
        fields.addAll(yFields);
        List<String> wheres = new ArrayList<>();
        if (customWheres != null) wheres.add(customWheres);
        if (extWheres != null) wheres.add(extWheres);
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
                .tableName(String.format(Db2Constants.BRACKETS, sql))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 1))
                .build();
        if (CollectionUtils.isNotEmpty(aggWheres)) st.add("filters", aggWheres);
        if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        return sqlLimit(st.render(), view);
    }

    @Override
    public String getSQLTableInfo(String table, List<ChartViewFieldDTO> xAxis, List<ChartFieldCustomFilterDTO> fieldCustomFilter, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(Db2Constants.KEYWORD_TABLE, table))
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
                    originField = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    if (x.getDeType() == 2 || x.getDeType() == 3) {
                        originField = String.format(Db2Constants.CAST, String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName()), Db2Constants.DEFAULT_FLOAT_FORMAT);
                    } else {
                        originField = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
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
        String customWheres = transCustomFilterList(tableObj, fieldCustomFilter);
        // 处理仪表板字段过滤
        String extWheres = transExtFilterList(tableObj, extFilterRequestList);
        // 构建sql所有参数
        List<SQLObj> fields = new ArrayList<>();
        fields.addAll(xFields);
        List<String> wheres = new ArrayList<>();
        if (customWheres != null) wheres.add(customWheres);
        if (extWheres != null) wheres.add(extWheres);
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
                .tableName(String.format(Db2Constants.BRACKETS, sql))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 1))
                .build();
        if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        return sqlLimit(st.render(), view);
    }

    @Override
    public String getSQLAsTmpTableInfo(String sql, List<ChartViewFieldDTO> xAxis, List<ChartFieldCustomFilterDTO> fieldCustomFilter, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view) {
        return getSQLTableInfo("(" + sqlFix(sql) + ")", xAxis, fieldCustomFilter, extFilterRequestList, null, view);
    }


    @Override
    public String getSQLAsTmp(String sql, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartFieldCustomFilterDTO> fieldCustomFilter, List<ChartExtFilterRequest> extFilterRequestList, ChartViewWithBLOBs view) {
        return getSQL("(" + sqlFix(sql) + ")", xAxis, yAxis, fieldCustomFilter, extFilterRequestList, null, view);
    }

    @Override
    public String getSQLStack(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartFieldCustomFilterDTO> fieldCustomFilter, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack, Datasource ds, ChartViewWithBLOBs view) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(Db2Constants.KEYWORD_TABLE, table))
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
                    originField = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    originField = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
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
                    originField = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
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
        String customWheres = transCustomFilterList(tableObj, fieldCustomFilter);
        // 处理仪表板字段过滤
        String extWheres = transExtFilterList(tableObj, extFilterRequestList);
        // 构建sql所有参数
        List<SQLObj> fields = new ArrayList<>();
        fields.addAll(xFields);
        fields.addAll(yFields);
        List<String> wheres = new ArrayList<>();
        if (customWheres != null) wheres.add(customWheres);
        if (extWheres != null) wheres.add(extWheres);
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
                .tableName(String.format(Db2Constants.BRACKETS, sql))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 1))
                .build();
        if (CollectionUtils.isNotEmpty(aggWheres)) st.add("filters", aggWheres);
        if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        return sqlLimit(st.render(), view);
    }

    @Override
    public String getSQLAsTmpStack(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartFieldCustomFilterDTO> fieldCustomFilter, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack, ChartViewWithBLOBs view) {
        return getSQLStack("(" + sqlFix(table) + ")", xAxis, yAxis, fieldCustomFilter, extFilterRequestList, extStack, null, view);
    }

    @Override
    public String getSQLScatter(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartFieldCustomFilterDTO> fieldCustomFilter, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extBubble, Datasource ds, ChartViewWithBLOBs view) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(Db2Constants.KEYWORD_TABLE, table))
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
                    originField = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    originField = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
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
                    originField = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
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
        String customWheres = transCustomFilterList(tableObj, fieldCustomFilter);
        // 处理仪表板字段过滤
        String extWheres = transExtFilterList(tableObj, extFilterRequestList);
        // 构建sql所有参数
        List<SQLObj> fields = new ArrayList<>();
        fields.addAll(xFields);
        fields.addAll(yFields);
        List<String> wheres = new ArrayList<>();
        if (customWheres != null) wheres.add(customWheres);
        if (extWheres != null) wheres.add(extWheres);
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
                .tableName(String.format(Db2Constants.BRACKETS, sql))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 1))
                .build();
        if (CollectionUtils.isNotEmpty(aggWheres)) st.add("filters", aggWheres);
        if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        return sqlLimit(st.render(), view);
    }

    @Override
    public String getSQLAsTmpScatter(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartFieldCustomFilterDTO> fieldCustomFilter, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extBubble, ChartViewWithBLOBs view) {
        return getSQLScatter("(" + sqlFix(table) + ")", xAxis, yAxis, fieldCustomFilter, extFilterRequestList, extBubble, null, view);
    }

    @Override
    public String searchTable(String table) {
        return "SELECT table_name FROM information_schema.TABLES WHERE table_name ='" + table + "'";
    }

    @Override
    public String getSQLSummary(String table, List<ChartViewFieldDTO> yAxis, List<ChartFieldCustomFilterDTO> fieldCustomFilter, List<ChartExtFilterRequest> extFilterRequestList, ChartViewWithBLOBs view, Datasource ds) {
        // 字段汇总 排序等
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(Db2Constants.KEYWORD_TABLE, table))
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
                    originField = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
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
        String customWheres = transCustomFilterList(tableObj, fieldCustomFilter);
        // 处理仪表板字段过滤
        String extWheres = transExtFilterList(tableObj, extFilterRequestList);
        // 构建sql所有参数
        List<SQLObj> fields = new ArrayList<>();
        fields.addAll(yFields);
        List<String> wheres = new ArrayList<>();
        if (customWheres != null) wheres.add(customWheres);
        if (extWheres != null) wheres.add(extWheres);
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
                .tableName(String.format(Db2Constants.BRACKETS, sql))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 1))
                .build();
        if (CollectionUtils.isNotEmpty(aggWheres)) st.add("filters", aggWheres);
        if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        return sqlLimit(st.render(), view);
    }

    @Override
    public String getSQLSummaryAsTmp(String sql, List<ChartViewFieldDTO> yAxis, List<ChartFieldCustomFilterDTO> fieldCustomFilter, List<ChartExtFilterRequest> extFilterRequestList, ChartViewWithBLOBs view) {
        return getSQLSummary("(" + sqlFix(sql) + ")", yAxis, fieldCustomFilter, extFilterRequestList, view, null);
    }

    @Override
    public String wrapSql(String sql) {
        sql = sql.trim();
        if (sql.lastIndexOf(";") == (sql.length() - 1)) {
            sql = sql.substring(0, sql.length() - 1);
        }
        String tmpSql = "SELECT * FROM (" + sql + ") AS de_tmp fetch first 0 rows only";
        return tmpSql;
    }

    @Override
    public String createRawQuerySQL(String table, List<DatasetTableField> fields, Datasource ds) {
        String[] array = fields.stream().map(f -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(" \"").append(f.getOriginName()).append("\"");
            return stringBuilder.toString();
        }).toArray(String[]::new);
        if (ds != null) {
            Db2Configuration db2Configuration = new Gson().fromJson(ds.getConfiguration(), Db2Configuration.class);
            return MessageFormat.format("SELECT {0} FROM {1}", StringUtils.join(array, ","), db2Configuration.getSchema() + ".\"" + table + "\"");
        } else {
            return MessageFormat.format("SELECT {0} FROM {1}", StringUtils.join(array, ","), table);
        }
    }

    @Override
    public String createRawQuerySQLAsTmp(String sql, List<DatasetTableField> fields) {
        return createRawQuerySQL(" (" + sqlFix(sql) + ") AS de_tmp ", fields, null);
    }

    @Override
    public String convertTableToSql(String tableName, Datasource ds) {
        return createSQLPreview("SELECT * FROM " + String.format(Db2Constants.KEYWORD_TABLE, tableName), null);
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
                originName = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
            } else {
                originName = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
            }
            if (field.getDeType() == DeTypeConstants.DE_TIME) {
                if (field.getDeExtractType() == DeTypeConstants.DE_STRING || field.getDeExtractType() == 5) {
                    originName = String.format(Db2Constants.STR_TO_DATE, originName);
                    whereName = String.format(Db2Constants.DATE_FORMAT, originName, Db2Constants.DEFAULT_DATE_FORMAT);
                }
                if (field.getDeExtractType() == DeTypeConstants.DE_INT || field.getDeExtractType() == DeTypeConstants.DE_FLOAT) {
                    String cast = String.format(Db2Constants.CAST, originName, Db2Constants.DEFAULT_INT_FORMAT);
                    whereName = String.format(Db2Constants.FROM_UNIXTIME, cast, Db2Constants.DEFAULT_DATE_FORMAT);
                }
                if (field.getDeExtractType() == DeTypeConstants.DE_TIME) {
                    whereName = originName;
                }
            } else if (field.getDeType() == 2 || field.getDeType() == 3) {
                if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                    whereName = String.format(Db2Constants.CAST, originName, Db2Constants.DEFAULT_FLOAT_FORMAT);
                }
                if (field.getDeExtractType() == 1) {
                    whereName = String.format(Db2Constants.UNIX_TIMESTAMP, originName);
                }
                if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
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
                        if (field.getDeType().equals(DeTypeConstants.DE_TIME)) {
                            whereValue = String.format(Db2Constants.DATE_FORMAT, "'" + value + "'", Db2Constants.DEFAULT_DATE_FORMAT);
                        } else {
                            whereValue = String.format(Db2Constants.WHERE_VALUE_VALUE, value);
                        }
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
            DatasetTableField field = request.getDatasetTableField();
            if (CollectionUtils.isEmpty(value) || ObjectUtils.isEmpty(field)) {
                continue;
            }
            String whereName = "";
            String whereTerm = transMysqlFilterTerm(request.getOperator());
            String whereValue = "";

            String originName;
            if (ObjectUtils.isNotEmpty(field.getExtField()) && field.getExtField() == 2) {
                // 解析origin name中有关联的字段生成sql表达式
                originName = calcFieldRegex(field.getOriginName(), tableObj);
            } else if (ObjectUtils.isNotEmpty(field.getExtField()) && field.getExtField() == 1) {
                originName = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
            } else {
                originName = String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
            }

            if (field.getDeType() == DeTypeConstants.DE_TIME) {
                if (field.getDeExtractType() == DeTypeConstants.DE_STRING || field.getDeExtractType() == 5) {
                    originName = String.format(Db2Constants.STR_TO_DATE, originName);
                    whereName = String.format(Db2Constants.DATE_FORMAT, originName, Db2Constants.DEFAULT_DATE_FORMAT);
                }
                if (field.getDeExtractType() == DeTypeConstants.DE_INT || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                    String cast = String.format(Db2Constants.CAST, originName, Db2Constants.DEFAULT_INT_FORMAT);
                    whereName = String.format(Db2Constants.FROM_UNIXTIME, cast, Db2Constants.DEFAULT_DATE_FORMAT);
                }
                if (field.getDeExtractType() == DeTypeConstants.DE_TIME) {
                    whereName = originName;
                }
            } else if (field.getDeType() == 2 || field.getDeType() == 3) {
                if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                    whereName = String.format(Db2Constants.CAST, originName, Db2Constants.DEFAULT_FLOAT_FORMAT);
                }
                if (field.getDeExtractType() == 1) {
                    whereName = String.format(Db2Constants.UNIX_TIMESTAMP, originName);
                }
                if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                    whereName = originName;
                }
            } else {
                whereName = originName;
            }


            if (StringUtils.containsIgnoreCase(request.getOperator(), "in")) {
                whereValue = "('" + StringUtils.join(value, "','") + "')";
            } else if (StringUtils.containsIgnoreCase(request.getOperator(), "like")) {
                whereValue = "'%" + value.get(0) + "%'";
            } else if (StringUtils.containsIgnoreCase(request.getOperator(), "between")) {
                if (request.getDatasetTableField().getDeType() == DeTypeConstants.DE_TIME) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String startTime = simpleDateFormat.format(new Date(Long.parseLong(value.get(0))));
                    String endTime = simpleDateFormat.format(new Date(Long.parseLong(value.get(1))));
                    whereValue = String.format(Db2Constants.WHERE_BETWEEN, startTime, endTime);
                } else {
                    whereValue = String.format(Db2Constants.WHERE_BETWEEN, value.get(0), value.get(1));
                }
            } else {
                whereValue = String.format(Db2Constants.WHERE_VALUE_VALUE, value.get(0));
            }
            list.add(SQLObj.builder()
                    .whereField(whereName)
                    .whereTermAndValue(whereTerm + whereValue)
                    .build());
        }
        List<String> strList = new ArrayList<>();
        list.forEach(ele -> strList.add(ele.getWhereField() + " " + ele.getWhereTermAndValue()));
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
            return "YYYY-MM-DD HH24:MI:SS";
        }

        switch (dateStyle) {
            case "y":
                return "YYYY";
            case "y_M":
                return "YYYY" + split + "MM";
            case "y_M_d":
                return "YYYY" + split + "MM" + split + "DD";
            case "H_m_s":
                return "HH24:MI:SS";
            case "y_M_d_H_m":
                return "YYYY" + split + "MM" + split + "DD" + " HH24:MI";
            case "y_M_d_H_m_s":
                return "YYYY" + split + "MM" + split + "DD" + " HH24:MI:SS";
            default:
                return "YYYY-MM-DD HH24:MI:SS";
        }
    }

    private SQLObj getXFields(ChartViewFieldDTO x, String originField, String fieldAlias) {
        String fieldName = "";
        if (x.getDeExtractType() == DeTypeConstants.DE_TIME) {
            if (x.getDeType() == 2 || x.getDeType() == 3) {
                fieldName = String.format(Db2Constants.UNIX_TIMESTAMP, originField) + "*1000";
            } else if (x.getDeType() == DeTypeConstants.DE_TIME) {
                String format = transDateFormat(x.getDateStyle(), x.getDatePattern());
                fieldName = String.format(Db2Constants.DATE_FORMAT, originField, format);
            } else {
                fieldName = originField;
            }
        } else {
            if (x.getDeType() == DeTypeConstants.DE_TIME) {
                String format = transDateFormat(x.getDateStyle(), x.getDatePattern());
                if (x.getDeExtractType() == DeTypeConstants.DE_STRING) {
                    fieldName = String.format(Db2Constants.DATE_FORMAT, originField, format);
                } else {
                    String cast = String.format(Db2Constants.CAST, originField, Db2Constants.DEFAULT_INT_FORMAT);
                    String from_unixtime = String.format(Db2Constants.FROM_UNIXTIME, cast, Db2Constants.DEFAULT_DATE_FORMAT);
                    fieldName = String.format(Db2Constants.DATE_FORMAT, from_unixtime, format);
                }
            } else {
                fieldName = originField;
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
            fieldName = Db2Constants.AGG_COUNT;
        } else if (SQLConstants.DIMENSION_TYPE.contains(y.getDeType())) {
            fieldName = String.format(Db2Constants.AGG_FIELD, y.getSummary(), originField);
        } else {
            if (StringUtils.equalsIgnoreCase(y.getSummary(), "avg") || StringUtils.containsIgnoreCase(y.getSummary(), "pop")) {
                String cast = String.format(Db2Constants.CAST, originField, y.getDeType() == 2 ? Db2Constants.DEFAULT_INT_FORMAT : Db2Constants.DEFAULT_FLOAT_FORMAT);
                String agg = String.format(Db2Constants.AGG_FIELD, y.getSummary(), cast);
                fieldName = String.format(Db2Constants.CAST, agg, Db2Constants.DEFAULT_FLOAT_FORMAT);
            } else {
                String cast = String.format(Db2Constants.CAST, originField, y.getDeType() == 2 ? Db2Constants.DEFAULT_INT_FORMAT : Db2Constants.DEFAULT_FLOAT_FORMAT);
                fieldName = String.format(Db2Constants.AGG_FIELD, y.getSummary(), cast);
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
                    if (y.getDeType().equals(DeTypeConstants.DE_TIME)) {
                        whereValue = String.format(Db2Constants.DATE_FORMAT, "'" + f.getValue() + "'", Db2Constants.DEFAULT_DATE_FORMAT);
                        ;
                    } else {
                        whereValue = String.format(Db2Constants.WHERE_VALUE_VALUE, f.getValue());
                    }
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
                    String.format(Db2Constants.KEYWORD_FIX, tableObj.getTableAlias(), ele.getOriginName()));
        }
        return originField;
    }

    private String sqlLimit(String sql, ChartViewWithBLOBs view) {
        if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom")) {
            return sql + String.format(" fetch first %s rows only; ", view.getResultCount());
        } else {
            return sql;
        }
    }
}

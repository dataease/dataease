package io.dataease.provider.query.oracle;

import com.google.gson.Gson;
import io.dataease.base.domain.ChartViewWithBLOBs;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.base.domain.DatasetTableFieldExample;
import io.dataease.base.domain.Datasource;
import io.dataease.base.mapper.DatasetTableFieldMapper;
import io.dataease.controller.request.chart.ChartExtFilterRequest;
import io.dataease.dto.chart.ChartCustomFilterItemDTO;
import io.dataease.dto.chart.ChartFieldCustomFilterDTO;
import io.dataease.dto.chart.ChartViewFieldDTO;
import io.dataease.dto.datasource.JdbcConfiguration;
import io.dataease.dto.datasource.OracleConfiguration;
import io.dataease.dto.sqlObj.SQLObj;
import io.dataease.plugins.common.constants.OracleConstants;
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

/**
 * @Author gin
 * @Date 2021/5/17 2:43 下午
 */
@Service("oracleQuery")
public class OracleQueryProvider extends QueryProvider {

    private static final Integer STRING = 0;
    private static final Integer TIME = 1;
    private static final Integer INT = 2;
    private static final Integer FLOAT = 3;
    private static final Integer BOOLEAN = 4;

    @Resource
    private DatasetTableFieldMapper datasetTableFieldMapper;

    @Override
    public Integer transFieldType(String field) {
        switch (field) {
            case "CHAR":
            case "VARCHAR2":
            case "VARCHAR":
            case "TEXT":
            case "TINYTEXT":
            case "MEDIUMTEXT":
            case "LONGTEXT":
            case "ENUM":
            case "LONG":
            case "NVARCHAR2":
            case "NCHAR":
                return 0;// 文本
            case "DATE":
            case "TIME":
            case "YEAR":
            case "DATETIME":
            case "TIMESTAMP":
                return 1;// 时间
            case "INT":
            case "SMALLINT":
            case "MEDIUMINT":
            case "INTEGER":
            case "BIGINT":
                return 2;// 整型
            case "NUMBER":
            case "FLOAT":
            case "DOUBLE":
            case "DECIMAL":
                return 3;// 浮点
            case "BIT":
            case "TINYINT":
                return 4;// 布尔
            default:
                return 0;
        }
    }

    @Override
    public String createSQLPreview(String sql, String orderBy) {
        return "SELECT * FROM (" + sqlFix(sql) + ") DE_TMP " + " WHERE rownum <= 1000";
    }

    @Override
    public String createQuerySQL(String table, List<DatasetTableField> fields, boolean isGroup, Datasource ds, List<ChartFieldCustomFilterDTO> fieldCustomFilter) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(OracleConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(OracleConstants.ALIAS_FIX, String.format(TABLE_ALIAS_PREFIX, 0)))
                .build();

        setSchema(tableObj, ds);
        List<SQLObj> xFields = xFields(table, fields);

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

    public void setSchema(SQLObj tableObj, Datasource ds) {
        if (ds != null && !tableObj.getTableName().startsWith("(") && !tableObj.getTableName().endsWith(")")) {
            String schema = new Gson().fromJson(ds.getConfiguration(), JdbcConfiguration.class).getSchema();
            schema = String.format(OracleConstants.KEYWORD_TABLE, schema);
            tableObj.setTableName(schema + "." + tableObj.getTableName());
        }
    }

    private List<SQLObj> xFields(String table, List<DatasetTableField> fields) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(OracleConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(OracleConstants.ALIAS_FIX, String.format(TABLE_ALIAS_PREFIX, 0)))
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
                    originField = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), f.getOriginName());
                } else {
                    originField = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), f.getOriginName());
                }
                String fieldAlias = String.format(OracleConstants.ALIAS_FIX, String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i));
                String fieldName = "";
                // 处理横轴字段
                if (f.getDeExtractType() == 1) {
                    if (f.getDeType() == 2 || f.getDeType() == 3) {
                        fieldName = String.format(OracleConstants.UNIX_TIMESTAMP, originField) + "*1000";
                    } else {
                        fieldName = originField;
                    }
                } else if (f.getDeExtractType() == 0) {
                    if (f.getDeType() == 2) {
                        fieldName = String.format(OracleConstants.CAST, originField, OracleConstants.DEFAULT_INT_FORMAT);
                    } else if (f.getDeType() == 3) {
                        fieldName = String.format(OracleConstants.CAST, originField, OracleConstants.DEFAULT_FLOAT_FORMAT);
                    } else if (f.getDeType() == 1) {
                        fieldName = String.format(OracleConstants.DATE_FORMAT, originField, OracleConstants.DEFAULT_DATE_FORMAT);
                    } else {
                        fieldName = originField;
                    }
                } else {
                    if (f.getDeType() == 1) {
                        String cast = String.format(OracleConstants.CAST, originField, OracleConstants.DEFAULT_INT_FORMAT) + "/1000";
                        fieldName = String.format(OracleConstants.FROM_UNIXTIME, cast, OracleConstants.DEFAULT_DATE_FORMAT);
                    } else if (f.getDeType() == 2) {
                        fieldName = String.format(OracleConstants.CAST, originField, OracleConstants.DEFAULT_INT_FORMAT);
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
        return xFields;
    }

    private String sqlColumn(List<SQLObj> xFields) {
        String[] array = xFields.stream().map(f -> {
            return f.getFieldAlias();
        }).toArray(String[]::new);
        return StringUtils.join(array, ",");
    }

    @Override
    public String createQuerySQLAsTmp(String sql, List<DatasetTableField> fields, boolean isGroup, List<ChartFieldCustomFilterDTO> fieldCustomFilter) {
        return createQuerySQL("(" + sqlFix(sql) + ")", fields, isGroup, null, fieldCustomFilter);
    }

    @Override
    public String createQueryTableWithPage(String table, List<DatasetTableField> fields, Integer page, Integer pageSize, Integer realSize, boolean isGroup, Datasource ds, List<ChartFieldCustomFilterDTO> fieldCustomFilter) {
        List<SQLObj> xFields = xFields(table, fields);

        return MessageFormat.format("SELECT {0} FROM ( SELECT DE_TMP.*, rownum r FROM ( {1} ) DE_TMP WHERE rownum <= {2} ) WHERE r > {3} ",
                sqlColumn(xFields), createQuerySQL(table, fields, isGroup, ds, fieldCustomFilter), Integer.valueOf(page * realSize).toString(), Integer.valueOf((page - 1) * pageSize).toString());
    }

    @Override
    public String createQuerySQLWithPage(String sql, List<DatasetTableField> fields, Integer page, Integer pageSize, Integer realSize, boolean isGroup, List<ChartFieldCustomFilterDTO> fieldCustomFilter) {
        List<SQLObj> xFields = xFields("(" + sqlFix(sql) + ")", fields);
        return MessageFormat.format("SELECT {0} FROM ( SELECT DE_TMP.*, rownum r FROM ( {1} ) DE_TMP WHERE rownum <= {2} ) WHERE r > {3} ",
                sqlColumn(xFields), createQuerySQLAsTmp(sql, fields, isGroup, fieldCustomFilter), Integer.valueOf(page * realSize).toString(), Integer.valueOf((page - 1) * pageSize).toString());
    }

    @Override
    public String createQueryTableWithLimit(String table, List<DatasetTableField> fields, Integer limit, boolean isGroup, Datasource ds, List<ChartFieldCustomFilterDTO> fieldCustomFilter) {
        String schema = new Gson().fromJson(ds.getConfiguration(), JdbcConfiguration.class).getSchema();
        return String.format("SELECT *  from %s  WHERE rownum <= %s ", schema + "." + String.format(OracleConstants.KEYWORD_TABLE, table), limit.toString());
    }

    @Override
    public String createQuerySqlWithLimit(String sql, List<DatasetTableField> fields, Integer limit, boolean isGroup, List<ChartFieldCustomFilterDTO> fieldCustomFilter) {
        return String.format("SELECT * from %s  WHERE rownum <= %s ", "(" + sqlFix(sql) + ")", limit.toString());
    }

    @Override
    public String getSQL(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartFieldCustomFilterDTO> fieldCustomFilter, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(OracleConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(OracleConstants.ALIAS_FIX, String.format(TABLE_ALIAS_PREFIX, 0)))
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
                    originField = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    originField = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                }
                String fieldAlias = String.format(OracleConstants.ALIAS_FIX, String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i));
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
                    originField = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                }
                String fieldAlias = String.format(OracleConstants.ALIAS_FIX, String.format(SQLConstants.FIELD_ALIAS_Y_PREFIX, i));
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
                .tableName(String.format(OracleConstants.BRACKETS, sql))
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
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(OracleConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(OracleConstants.ALIAS_FIX, String.format(TABLE_ALIAS_PREFIX, 0)))
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
                    originField = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    if (x.getDeType() == 2 || x.getDeType() == 3) {
                        originField = String.format(OracleConstants.CAST, String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName()), OracleConstants.DEFAULT_FLOAT_FORMAT);
                    } else {
                        originField = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                    }
                }
                String fieldAlias = String.format(OracleConstants.ALIAS_FIX, String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i));
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
                .tableName(String.format(OracleConstants.BRACKETS, sql))
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
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(OracleConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(OracleConstants.ALIAS_FIX, String.format(TABLE_ALIAS_PREFIX, 0)))
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
                    originField = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    originField = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                }
                String fieldAlias = String.format(OracleConstants.ALIAS_FIX, String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i));
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
                    originField = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                }
                String fieldAlias = String.format(OracleConstants.ALIAS_FIX, String.format(SQLConstants.FIELD_ALIAS_Y_PREFIX, i));
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
                .tableName(String.format(OracleConstants.BRACKETS, sql))
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
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(OracleConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(OracleConstants.ALIAS_FIX, String.format(TABLE_ALIAS_PREFIX, 0)))
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
                    originField = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    originField = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                }
                String fieldAlias = String.format(OracleConstants.ALIAS_FIX, String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i));
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
                    originField = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                }
                String fieldAlias = String.format(OracleConstants.ALIAS_FIX, String.format(SQLConstants.FIELD_ALIAS_Y_PREFIX, i));
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
                .tableName(String.format(OracleConstants.BRACKETS, sql))
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
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(OracleConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(OracleConstants.ALIAS_FIX, String.format(TABLE_ALIAS_PREFIX, 0)))
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
                    originField = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
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
                .tableName(String.format(OracleConstants.BRACKETS, sql))
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
        String tmpSql = "SELECT * FROM (" + sql + ") DE_TMP " + " where rownum <= 0";
        return tmpSql;
    }

    @Override
    public String createRawQuerySQL(String table, List<DatasetTableField> fields, Datasource ds) {
        String[] array = fields.stream().map(f -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(" \"").append(f.getOriginName()).append("\"");
            return stringBuilder.toString();
        }).toArray(String[]::new);
        OracleConfiguration oracleConfiguration = new Gson().fromJson(ds.getConfiguration(), OracleConfiguration.class);
        return MessageFormat.format("SELECT {0} FROM {1}", StringUtils.join(array, ","), oracleConfiguration.getSchema() + ".\"" + table + "\"");
    }

    @Override
    public String createRawQuerySQLAsTmp(String sql, List<DatasetTableField> fields) {
        String[] array = fields.stream().map(f -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(" \"").append(f.getOriginName()).append("\"");
            return stringBuilder.toString();
        }).toArray(String[]::new);
        return MessageFormat.format("SELECT {0} FROM {1}", StringUtils.join(array, ","), " (" + sqlFix(sql) + ") DE_TMP ");
    }

    @Override
    public String convertTableToSql(String tableName, Datasource ds) {
        String schema = new Gson().fromJson(ds.getConfiguration(), JdbcConfiguration.class).getSchema();
        schema = String.format(OracleConstants.KEYWORD_TABLE, schema);
        return createSQLPreview("SELECT * FROM " + schema + "." + String.format(OracleConstants.KEYWORD_TABLE, tableName), null);
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
                originName = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
            } else {
                originName = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
            }

            if (field.getDeType() == 1) {
                if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                    whereName = String.format(OracleConstants.TO_DATE, originName, OracleConstants.DEFAULT_DATE_FORMAT);
                }
                if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                    String cast = String.format(OracleConstants.CAST, originName, OracleConstants.DEFAULT_INT_FORMAT) + "/1000";
                    whereName = String.format(OracleConstants.FROM_UNIXTIME, cast, OracleConstants.DEFAULT_DATE_FORMAT);
                }
                if (field.getDeExtractType() == 1) {
                    whereName = originName;
                }
            } else if (field.getDeType() == 2 || field.getDeType() == 3) {
                if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                    whereName = String.format(OracleConstants.CAST, originName, OracleConstants.DEFAULT_FLOAT_FORMAT);
                }
                if (field.getDeExtractType() == 1) {
                    whereName = String.format(OracleConstants.UNIX_TIMESTAMP, originName) + "*1000";
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
                        if (field.getDeType() == 1) {
                            whereValue = String.format(OracleConstants.TO_DATE, "'" + value + "'", OracleConstants.DEFAULT_DATE_FORMAT);
                        } else {
                            whereValue = String.format(OracleConstants.WHERE_VALUE_VALUE, value);
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
                originName = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
            } else {
                originName = String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
            }

            if (field.getDeType() == 1) {
                if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                    whereName = String.format(OracleConstants.TO_DATE, originName, OracleConstants.DEFAULT_DATE_FORMAT);
                }
                if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                    String cast = String.format(OracleConstants.CAST, originName, OracleConstants.DEFAULT_INT_FORMAT) + "/1000";
                    whereName = String.format(OracleConstants.FROM_UNIXTIME, cast, OracleConstants.DEFAULT_DATE_FORMAT);
                }
                if (field.getDeExtractType() == 1) {
                    whereName = originName;
                }
            } else if (field.getDeType() == 2 || field.getDeType() == 3) {
                if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                    whereName = String.format(OracleConstants.CAST, originName, OracleConstants.DEFAULT_FLOAT_FORMAT);
                }
                if (field.getDeExtractType() == 1) {
                    whereName = String.format(OracleConstants.UNIX_TIMESTAMP, originName) + "*1000";
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
                if (request.getDatasetTableField().getDeType() == 1) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String startTime = simpleDateFormat.format(new Date(Long.parseLong(value.get(0))));
                    String endTime = simpleDateFormat.format(new Date(Long.parseLong(value.get(1))));
                    String st = String.format(OracleConstants.TO_DATE, "'" + startTime + "'", OracleConstants.DEFAULT_DATE_FORMAT);
                    String et = String.format(OracleConstants.TO_DATE, "'" + endTime + "'", OracleConstants.DEFAULT_DATE_FORMAT);
                    whereValue = st + " AND " + et;
                } else {
                    whereValue = String.format(OracleConstants.WHERE_BETWEEN, value.get(0), value.get(1));
                }
            } else {
                whereValue = String.format(OracleConstants.WHERE_VALUE_VALUE, value.get(0));
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
            return OracleConstants.DEFAULT_DATE_FORMAT;
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
                return OracleConstants.DEFAULT_DATE_FORMAT;
        }
    }

    private SQLObj getXFields(ChartViewFieldDTO x, String originField, String fieldAlias) {
        String fieldName = "";
        if (x.getDeExtractType() == TIME) {
            if (x.getDeType() == INT || x.getDeType() == FLOAT) { //时间转数值
                if (x.getType().equalsIgnoreCase("DATE")) {
                    String date = String.format(OracleConstants.CALC_SUB, originField, String.format(OracleConstants.TO_DATE, OracleConstants.DEFAULT_START_DATE, OracleConstants.DEFAULT_DATE_FORMAT));
                    fieldName = String.format(OracleConstants.TO_NUMBER, date) + OracleConstants.TO_MS;
                } else {
                    String toChar = String.format(OracleConstants.TO_CHAR, originField, OracleConstants.DEFAULT_DATE_FORMAT);
                    String toDate = String.format(OracleConstants.TO_DATE, toChar, OracleConstants.DEFAULT_DATE_FORMAT);
                    String toDate1 = String.format(OracleConstants.TO_DATE, OracleConstants.DEFAULT_START_DATE, OracleConstants.DEFAULT_DATE_FORMAT);
                    fieldName = String.format(OracleConstants.TO_NUMBER, String.format(OracleConstants.CALC_SUB, toDate, toDate1)) + OracleConstants.TO_MS;
                }
            } else if (x.getDeType() == TIME) { //格式化显示时间
                String format = transDateFormat(x.getDateStyle(), x.getDatePattern());
                if (x.getType().equalsIgnoreCase("DATE")) {
                    fieldName = String.format(OracleConstants.TO_CHAR, originField, format);
                } else {
                    String toChar = String.format(OracleConstants.TO_CHAR, originField, OracleConstants.DEFAULT_DATE_FORMAT);
                    String toDate = String.format(OracleConstants.TO_DATE, toChar, OracleConstants.DEFAULT_DATE_FORMAT);
                    fieldName = String.format(OracleConstants.TO_CHAR, toDate, format);
                }
            } else {
                fieldName = originField;
            }
        } else {
            if (x.getDeType() == TIME) {
                String format = transDateFormat(x.getDateStyle(), x.getDatePattern());
                if (x.getDeExtractType() == STRING) { //字符串转时间
                    String toDate = String.format(OracleConstants.TO_DATE, originField, OracleConstants.DEFAULT_DATE_FORMAT);
                    fieldName = String.format(OracleConstants.TO_CHAR, toDate, format);
                } else {                            //数值转时间
                    String date = originField + "/(1000 * 60 * 60 * 24)+" + String.format(OracleConstants.TO_DATE, OracleConstants.DEFAULT_START_DATE, OracleConstants.DEFAULT_DATE_FORMAT);
                    fieldName = String.format(OracleConstants.TO_CHAR, date, format);
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

    private List<SQLObj> getXWheres(ChartViewFieldDTO x, String originField, String fieldAlias) {
        List<SQLObj> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(x.getFilter()) && x.getFilter().size() > 0) {
            x.getFilter().forEach(f -> {
                String whereName = "";
                String whereTerm = transMysqlFilterTerm(f.getTerm());
                String whereValue = "";
                if (x.getDeType() == 1 && x.getDeExtractType() != 1) {
                    String cast = String.format(OracleConstants.CAST, originField, OracleConstants.DEFAULT_INT_FORMAT) + "/1000";
                    whereName = String.format(OracleConstants.FROM_UNIXTIME, cast, OracleConstants.DEFAULT_DATE_FORMAT);
                } else {
                    whereName = originField;
                }
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
                    whereValue = String.format(OracleConstants.WHERE_VALUE_VALUE, f.getValue());
                }
                list.add(SQLObj.builder()
                        .whereField(whereName)
                        .whereAlias(fieldAlias)
                        .whereTermAndValue(whereTerm + whereValue)
                        .build());
            });
        }
        return list;
    }

    private SQLObj getYFields(ChartViewFieldDTO y, String originField, String fieldAlias) {
        String fieldName = "";
        if (StringUtils.equalsIgnoreCase(y.getOriginName(), "*")) {
            fieldName = OracleConstants.AGG_COUNT;
        } else if (SQLConstants.DIMENSION_TYPE.contains(y.getDeType())) {
            fieldName = String.format(OracleConstants.AGG_FIELD, y.getSummary(), originField);
        } else {
            if (StringUtils.equalsIgnoreCase(y.getSummary(), "avg") || StringUtils.containsIgnoreCase(y.getSummary(), "pop")) {
                String cast = String.format(OracleConstants.CAST, originField, y.getDeType() == 2 ? OracleConstants.DEFAULT_INT_FORMAT : OracleConstants.DEFAULT_FLOAT_FORMAT);
                String agg = String.format(OracleConstants.AGG_FIELD, y.getSummary(), cast);
                fieldName = String.format(OracleConstants.CAST, agg, OracleConstants.DEFAULT_FLOAT_FORMAT);
            } else {
                String cast = String.format(OracleConstants.CAST, originField, y.getDeType() == 2 ? OracleConstants.DEFAULT_INT_FORMAT : OracleConstants.DEFAULT_FLOAT_FORMAT);
                fieldName = String.format(OracleConstants.AGG_FIELD, y.getSummary(), cast);
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
                    whereValue = String.format(OracleConstants.WHERE_VALUE_VALUE, f.getValue());
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
                    String.format(OracleConstants.KEYWORD_FIX, tableObj.getTableAlias(), ele.getOriginName()));
        }
        return originField;
    }

    private String sqlLimit(String sql, ChartViewWithBLOBs view) {
        if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom")) {
            return "SELECT * FROM (" + sqlFix(sql) + ") DE_RESULT_TMP " + " WHERE rownum <= " + view.getResultCount();
        } else {
            return sql;
        }
    }
}

package io.dataease.provider.query.sqlserver;

import com.alibaba.fastjson.JSONArray;
import com.google.gson.Gson;
import io.dataease.i18n.Translator;
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
import io.dataease.plugins.common.exception.DataEaseException;
import io.dataease.plugins.common.request.chart.ChartExtFilterRequest;
import io.dataease.plugins.common.request.chart.filter.FilterTreeItem;
import io.dataease.plugins.common.request.chart.filter.FilterTreeObj;
import io.dataease.plugins.common.request.permission.DataSetRowPermissionsTreeDTO;
import io.dataease.plugins.common.request.permission.DatasetRowPermissionsTreeItem;
import io.dataease.plugins.datasource.entity.Dateformat;
import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import io.dataease.plugins.datasource.entity.PageInfo;
import io.dataease.plugins.datasource.query.QueryProvider;
import io.dataease.plugins.datasource.query.Utils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
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

import static io.dataease.plugins.common.constants.datasource.SQLConstants.TABLE_ALIAS_PREFIX;

@Service("sqlServerQueryProvider")
public class SqlserverQueryProvider extends QueryProvider {
    @Resource
    private DatasetTableFieldMapper datasetTableFieldMapper;

    @Override
    public Integer transFieldType(String field) {
        field = field.toUpperCase();
        switch (field) {
            case "CHAR":
            case "NCHAR":
            case "NTEXT":
            case "VARCHAR":
            case "NVARCHAR":
            case "TEXT":
            case "TINYTEXT":
            case "MEDIUMTEXT":
            case "LONGTEXT":
            case "ENUM":
            case "XML":
            case "TIME":
                return DeTypeConstants.DE_STRING;// 文本
            case "DATE":
            case "YEAR":
            case "DATETIME":
            case "DATETIME2":
            case "DATETIMEOFFSET":
            case "SMALLDATATIME":
                return DeTypeConstants.DE_TIME;// 时间
            case "INT":
            case "MEDIUMINT":
            case "INTEGER":
            case "BIGINT":
            case "SMALLINT":
                return DeTypeConstants.DE_INT;// 整型
            case "FLOAT":
            case "DOUBLE":
            case "DECIMAL":
            case "MONEY":
            case "REAL":
            case "NUMERIC":
                return DeTypeConstants.DE_FLOAT;// 浮点
            case "BIT":
            case "TINYINT":
                return DeTypeConstants.DE_BOOL;// 布尔
            case "TIMESTAMP":
                return DeTypeConstants.DE_BINARY;// 二进制
            default:
                return DeTypeConstants.DE_STRING;
        }
    }

    @Override
    public String createSQLPreview(String sql, String orderBy) {
        return "SELECT top 1000 * FROM (" + sqlFix(sql) + ") AS tmp";
    }

    @Override
    public String createQuerySQL(String table, List<DatasetTableField> fields, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return createQuerySQL(table, fields, isGroup, ds, fieldCustomFilter, rowPermissionsTree, null, null, null);
    }

    @Override
    public String createQuerySQLAsTmp(String sql, List<DatasetTableField> fields, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return createQuerySQL("(" + sqlFix(sql) + ")", fields, isGroup, null, fieldCustomFilter, rowPermissionsTree);
    }


    public String createQuerySQLAsTmp(String sql, List<DatasetTableField> fields, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<DeSortField> sortFields, Long limit, String keyword) {
        return createQuerySQL("(" + sqlFix(sql) + ")", fields, isGroup, null, fieldCustomFilter, rowPermissionsTree, sortFields, limit, keyword);
    }

    private boolean anyFieldExceed(List<DatasetTableField> fields) {
        if (CollectionUtils.isEmpty(fields)) return false;
        return fields.stream().anyMatch(field -> field.getDeExtractType().equals(DeTypeConstants.DE_STRING) && field.getSize() > 8000);
    }

    private boolean anySortFieldExceed(List<DeSortField> fields) {
        if (CollectionUtils.isEmpty(fields)) return false;
        return fields.stream().anyMatch(field -> field.getDeExtractType().equals(DeTypeConstants.DE_STRING) && field.getSize() > 8000);
    }

    @Override
    public String createQuerySQL(String table, List<DatasetTableField> fields, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<DeSortField> sortFields, Long limit, String keyword) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(SqlServerSQLConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 0))
                .build();
        setSchema(tableObj, ds);
        List<String> exceedList = null;
        if (anyFieldExceed(fields) || anySortFieldExceed(sortFields)) {
            exceedList = new ArrayList<>();
            List<DatasetTableField> calcFieldList = new ArrayList<>(fields);
            if (CollectionUtils.isNotEmpty(sortFields)) {
                calcFieldList.addAll(sortFields);
            }
            List<SQLObj> sqlObjList = new ArrayList<>();
            sqlObjList.add(SQLObj.builder().fieldName("*").build());
            for (DatasetTableField f : calcFieldList) {
                boolean exceed = f.getDeExtractType().equals(DeTypeConstants.DE_STRING) && f.getSize() > 8000;
                if (!exceedList.contains(f.getOriginName()) && exceed) {
                    exceedList.add(f.getOriginName());
                    String originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), f.getOriginName());
                    String newOriginName = f.getOriginName() + "_exceed";
                    String format = String.format(SqlServerSQLConstants.TO_STRING, originField, newOriginName);
                    sqlObjList.add(SQLObj.builder().fieldName(format).build());
                }
            }
            STGroup tabStg = new STGroupFile(SQLConstants.SQL_TEMPLATE);
            ST st_sql = tabStg.getInstanceOf("previewSql");
            st_sql.add("isGroup", false);
            st_sql.add("notUseAs", true);
            st_sql.add("table", tableObj);
            st_sql.add("groups", sqlObjList);
            String render = st_sql.render();
            tableObj = SQLObj.builder()
                    .tableName(" (" + render + ") ")
                    .tableAlias(String.format(TABLE_ALIAS_PREFIX, "_exceed"))
                    .build();
        }


        List<SQLObj> xFields = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(fields)) {
            for (int i = 0; i < fields.size(); i++) {
                DatasetTableField f = fields.get(i);
                if (CollectionUtils.isNotEmpty(exceedList) && exceedList.contains(f.getOriginName())) {
                    f.setOriginName(f.getOriginName() + "_exceed");
                }
                String originField;
                if (ObjectUtils.isNotEmpty(f.getExtField()) && f.getExtField() == 2) {
                    // 解析origin name中有关联的字段生成sql表达式
                    originField = calcFieldRegex(f.getOriginName(), tableObj);
                } else if (ObjectUtils.isNotEmpty(f.getExtField()) && f.getExtField() == 1) {
                    originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), f.getOriginName());
                } else {
                    originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), f.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);
                String fieldName;
                // 处理横轴字段
                if (f.getDeExtractType() == DeTypeConstants.DE_TIME) { // 时间 转为 数值
                    if (f.getDeType() == DeTypeConstants.DE_INT || f.getDeType() == DeTypeConstants.DE_FLOAT) {
                        fieldName = String.format(SqlServerSQLConstants.UNIX_TIMESTAMP, originField);
                    } else {
                        fieldName = originField;
                    }
                } else if (f.getDeExtractType() == DeTypeConstants.DE_STRING) {
                    if (f.getDeType() == DeTypeConstants.DE_INT) {
                        fieldName = String.format(SqlServerSQLConstants.CONVERT, SqlServerSQLConstants.DEFAULT_INT_FORMAT, originField);
                    } else if (f.getDeType() == DeTypeConstants.DE_FLOAT) {
                        fieldName = String.format(SqlServerSQLConstants.CONVERT, SqlServerSQLConstants.DEFAULT_FLOAT_FORMAT, originField);
                    } else if (f.getDeType() == DeTypeConstants.DE_TIME) { //字符串转时间
                        fieldName = String.format(SqlServerSQLConstants.STRING_TO_DATE, originField, StringUtils.isNotEmpty(f.getDateFormat()) ? f.getDateFormat() : SqlServerSQLConstants.DEFAULT_DATE_FORMAT);
                    } else {
                        fieldName = originField;
                    }
                } else {
                    if (f.getDeType() == DeTypeConstants.DE_TIME) { // 数值转时间
                        String cast = String.format(SqlServerSQLConstants.LONG_TO_DATE, originField + "/1000");
                        fieldName = String.format(SqlServerSQLConstants.FROM_UNIXTIME, cast);
                    } else if (f.getDeType() == DeTypeConstants.DE_INT) {
                        fieldName = String.format(SqlServerSQLConstants.CONVERT, SqlServerSQLConstants.DEFAULT_INT_FORMAT, originField);
                    } else {
                        fieldName = originField;
                    }
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
                if (CollectionUtils.isNotEmpty(exceedList) && exceedList.contains(deSortField.getOriginName())) {
                    deSortField.setOriginName(deSortField.getOriginName() + "_exceed");
                }
                SQLObj order = buildSortField(deSortField, tableObj, i);
                xOrders.add(order);
            }
        }
        if (ObjectUtils.isNotEmpty(limit)) {
            SQLObj limitFiled = SQLObj.builder().limitFiled(" top " + limit + " ").build();
            st_sql.add("limitFiled", limitFiled);
        }

        if (ObjectUtils.isNotEmpty(xOrders)) {
            st_sql.add("orders", xOrders);
        }
        return st_sql.render();
    }

    private SQLObj buildSortField(DeSortField f, SQLObj tableObj, int index) {
        String originField;
        if (ObjectUtils.isNotEmpty(f.getExtField()) && f.getExtField() == 2) {
            // 解析origin name中有关联的字段生成sql表达式
            originField = calcFieldRegex(f.getOriginName(), tableObj);
        } else if (ObjectUtils.isNotEmpty(f.getExtField()) && f.getExtField() == 1) {
            originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), f.getOriginName());
        } else {
            originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), f.getOriginName());
        }
        String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, index);
        String fieldName;
        // 处理横轴字段
        if (f.getDeExtractType() == DeTypeConstants.DE_TIME) { // 时间 转为 数值
            if (f.getDeType() == DeTypeConstants.DE_INT || f.getDeType() == DeTypeConstants.DE_FLOAT) {
                fieldName = String.format(SqlServerSQLConstants.UNIX_TIMESTAMP, originField);
            } else {
                fieldName = originField;
            }
        } else if (f.getDeExtractType() == DeTypeConstants.DE_STRING) {
            if (f.getDeType() == DeTypeConstants.DE_INT) {
                fieldName = String.format(SqlServerSQLConstants.CONVERT, SqlServerSQLConstants.DEFAULT_INT_FORMAT, originField);
            } else if (f.getDeType() == DeTypeConstants.DE_FLOAT) {
                fieldName = String.format(SqlServerSQLConstants.CONVERT, SqlServerSQLConstants.DEFAULT_FLOAT_FORMAT, originField);
            } else if (f.getDeType() == DeTypeConstants.DE_TIME) { //字符串转时间
                fieldName = String.format(SqlServerSQLConstants.STRING_TO_DATE, originField, StringUtils.isNotEmpty(f.getDateFormat()) ? f.getDateFormat() : SqlServerSQLConstants.DEFAULT_DATE_FORMAT);
            } else {
                fieldName = originField;
            }
        } else {
            if (f.getDeType() == DeTypeConstants.DE_TIME) { // 数值转时间
                String cast = String.format(SqlServerSQLConstants.LONG_TO_DATE, originField + "/1000");
                fieldName = String.format(SqlServerSQLConstants.FROM_UNIXTIME, cast);
            } else if (f.getDeType() == DeTypeConstants.DE_INT) {
                fieldName = String.format(SqlServerSQLConstants.CONVERT, SqlServerSQLConstants.DEFAULT_INT_FORMAT, originField);
            } else {
                fieldName = originField;
            }
        }
        SQLObj result = SQLObj.builder().orderField(originField).orderAlias(originField).orderDirection(f.getOrderDirection()).build();
        return result;
    }

    @Override
    public String createQueryTableWithPage(String table, List<DatasetTableField> fields, Integer page, Integer pageSize, Integer realSize, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        Integer size = (page - 1) * pageSize + realSize;
        return String.format("SELECT top %s * from ( %s ) AS DE_SQLSERVER_TMP ", size.toString(), createQuerySQL(table, fields, isGroup, ds, fieldCustomFilter, rowPermissionsTree));

    }

    @Override
    public String createQueryTableWithLimit(String table, List<DatasetTableField> fields, Integer limit, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        String schema = new Gson().fromJson(ds.getConfiguration(), JdbcConfiguration.class).getSchema();
        return String.format("SELECT top %s * from %s ", limit.toString(), schema + "." + String.format(SqlServerSQLConstants.KEYWORD_TABLE, table));
    }

    @Override
    public String createQuerySQLWithPage(String sql, List<DatasetTableField> fields, Integer page, Integer pageSize, Integer realSize, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        Integer size = (page - 1) * pageSize + realSize;
        return String.format("SELECT top %s * from ( %s ) AS DE_SQLSERVER_TMP ", size.toString(), createQuerySQLAsTmp(sql, fields, isGroup, fieldCustomFilter, rowPermissionsTree));
    }

    @Override
    public String createQuerySqlWithLimit(String sql, List<DatasetTableField> fields, Integer limit, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return String.format("SELECT top %s * from ( %s ) as DE_SQLSERVER_TMP ", limit.toString(), sqlFix(sql));
    }

    @Override
    public String getSQL(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(SqlServerSQLConstants.KEYWORD_TABLE, table))
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
                    originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);
                // 处理横轴字段
                xFields.add(getXFields(x, originField, fieldAlias));
                // 处理横轴排序
                if (StringUtils.isNotEmpty(x.getSort()) && Utils.joinSort(x.getSort())) {
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
                    originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_Y_PREFIX, i);
                // 处理纵轴字段
                yFields.add(getYFields(y, originField, fieldAlias));
                // 处理纵轴过滤
                yWheres.add(getYWheres(y, originField, fieldAlias));
                // 处理纵轴排序
                if (StringUtils.isNotEmpty(y.getSort()) && Utils.joinSort(y.getSort())) {
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
                .tableName(String.format(SqlServerSQLConstants.BRACKETS, sql))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 1))
                .build();
        if (CollectionUtils.isNotEmpty(aggWheres)) st.add("filters", aggWheres);
        if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom")) {
            SQLObj limitFiled = SQLObj.builder().limitFiled("top " + view.getResultCount() + " ").build();
            st.add("limitFiled", limitFiled);
        }
        return st.render();
    }

    @Override
    public String getSQLTableInfo(String table, List<ChartViewFieldDTO> xAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view) {
        return originTableInfo(table, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view, true, true, false);
    }

    public String originTableInfo(String table, List<ChartViewFieldDTO> xAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view, boolean needOrder, boolean needResultCount, boolean ignoreOrder) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(SqlServerSQLConstants.KEYWORD_TABLE, table))
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
                    originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    if (x.getDeType() == 2 || x.getDeType() == 3) {
                        originField = String.format(SqlServerSQLConstants.CONVERT, SqlServerSQLConstants.DEFAULT_FLOAT_FORMAT, String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName()));
                    } else {
                        originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                    }
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);
                // 处理横轴字段
                xFields.add(getXFields(x, originField, fieldAlias));
                // 处理横轴排序
                if (StringUtils.isNotEmpty(x.getSort()) && Utils.joinSort(x.getSort())) {
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
        if (!ignoreOrder) {
            orders.addAll(xOrders);
            if (needOrder && CollectionUtils.isEmpty(xOrders)) {
                orders.add(SQLObj.builder()
                        .orderField(String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, 0))
                        .orderAlias(String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, 0))
                        .orderDirection("ASC")
                        .build());
            }
        }

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
                .tableName(String.format(SqlServerSQLConstants.BRACKETS, sql))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 1))
                .build();
        if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom") && needResultCount) {
            SQLObj limitFiled = SQLObj.builder().limitFiled("top " + view.getResultCount() + " ").build();
            st.add("limitFiled", limitFiled);
        }
        return st.render();
    }

    public String originSQLAsTmpTableInfo(String sql, List<ChartViewFieldDTO> xAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view, boolean needOrder, boolean ignoreOrder) {
        return originTableInfo("(" + sqlFix(sql) + ")", xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, null, view, needOrder, true, ignoreOrder);
    }

    public String getSQLWithPage(boolean isTable, String sql, List<ChartViewFieldDTO> xAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view, PageInfo pageInfo) {
        if (Integer.valueOf(ds.getVersion()) < 11) {
            if (isTable) {
                return getSQLTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view);
            } else {
                return getSQLAsTmpTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view);
            }
        } else {
            boolean isPage = (pageInfo.getGoPage() != null && pageInfo.getPageSize() != null);
            String limit = (isPage ? " OFFSET " + (pageInfo.getGoPage() - 1) * pageInfo.getPageSize() + " ROW FETCH NEXT " + pageInfo.getPageSize() + " ROW ONLY " : "");
            if (isTable) {
                return originTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view, true, !isPage, false) + limit;
            } else {
                return originTableInfo("(" + sqlFix(sql) + ")", xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view, true, !isPage, false) + limit;
            }
        }
    }

    @Override
    public String getSQLAsTmpTableInfo(String sql, List<ChartViewFieldDTO> xAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view) {
        return getSQLTableInfo("(" + sqlFix(sql) + ")", xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, null, view);
    }


    @Override
    public String getSQLAsTmp(String sql, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, ChartViewWithBLOBs view) {
        return getSQL("(" + sqlFix(sql) + ")", xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, null, view);
    }

    @Override
    public String getSQLStack(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack, Datasource ds, ChartViewWithBLOBs view) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(SqlServerSQLConstants.KEYWORD_TABLE, table))
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
                    originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);
                // 处理横轴字段
                xFields.add(getXFields(x, originField, fieldAlias));
                // 处理横轴排序
                if (StringUtils.isNotEmpty(x.getSort()) && Utils.joinSort(x.getSort())) {
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
                    originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_Y_PREFIX, i);
                // 处理纵轴字段
                yFields.add(getYFields(y, originField, fieldAlias));
                // 处理纵轴过滤
                yWheres.add(getYWheres(y, originField, fieldAlias));
                // 处理纵轴排序
                if (StringUtils.isNotEmpty(y.getSort()) && Utils.joinSort(y.getSort())) {
                    yOrders.add(SQLObj.builder()
                            .orderField(originField)
                            .orderAlias(fieldAlias)
                            .orderDirection(y.getSort())
                            .build());
                }
            }
        }
        List<SQLObj> stackFields = new ArrayList<>();
        List<SQLObj> stackOrders = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(extStack)) {
            for (int i = 0; i < extStack.size(); i++) {
                ChartViewFieldDTO stack = extStack.get(i);
                String originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), stack.getOriginName());
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);
                // 处理横轴字段
                stackFields.add(getXFields(stack, originField, fieldAlias));
                // 处理横轴排序
                if (StringUtils.isNotEmpty(stack.getSort()) && Utils.joinSort(stack.getSort())) {
                    stackOrders.add(SQLObj.builder()
                            .orderField(originField)
                            .orderAlias(fieldAlias)
                            .orderDirection(stack.getSort())
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
                .tableName(String.format(SqlServerSQLConstants.BRACKETS, sql))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 1))
                .build();
        if (CollectionUtils.isNotEmpty(aggWheres)) st.add("filters", aggWheres);
        if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom")) {
            SQLObj limitFiled = SQLObj.builder().limitFiled("top " + view.getResultCount() + " ").build();
            st.add("limitFiled", limitFiled);
        }
        return st.render();
    }

    @Override
    public String getSQLAsTmpStack(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack, ChartViewWithBLOBs view) {
        return getSQLStack("(" + sqlFix(table) + ")", xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, extStack, null, view);
    }

    @Override
    public String getSQLScatter(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extBubble, List<ChartViewFieldDTO> extGroup, Datasource ds, ChartViewWithBLOBs view) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(SqlServerSQLConstants.KEYWORD_TABLE, table))
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
                    originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);
                // 处理横轴字段
                xFields.add(getXFields(x, originField, fieldAlias));
                // 处理横轴排序
                if (StringUtils.isNotEmpty(x.getSort()) && Utils.joinSort(x.getSort())) {
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
                    originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_Y_PREFIX, i);
                // 处理纵轴字段
                yFields.add(getYFields(y, originField, fieldAlias));
                // 处理纵轴过滤
                yWheres.add(getYWheres(y, originField, fieldAlias));
                // 处理纵轴排序
                if (StringUtils.isNotEmpty(y.getSort()) && Utils.joinSort(y.getSort())) {
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
                .tableName(String.format(SqlServerSQLConstants.BRACKETS, sql))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 1))
                .build();
        if (CollectionUtils.isNotEmpty(aggWheres)) st.add("filters", aggWheres);
        if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom")) {
            SQLObj limitFiled = SQLObj.builder().limitFiled("top " + view.getResultCount() + " ").build();
            st.add("limitFiled", limitFiled);
        }
        return st.render();
    }

    @Override
    public String getSQLAsTmpScatter(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extBubble, List<ChartViewFieldDTO> extGroup, ChartViewWithBLOBs view) {
        return getSQLScatter("(" + sqlFix(table) + ")", xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, extBubble, extGroup, null, view);
    }

    @Override
    public String getSQLRangeBar(String table, List<ChartViewFieldDTO> baseXAxis, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack, Datasource ds, ChartViewWithBLOBs view) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(SqlServerSQLConstants.KEYWORD_TABLE, table))
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
                        originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                    } else {
                        originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                    }
                    String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_Y_PREFIX, i);
                    // 处理纵轴字段
                    yFields.add(getYFields(x, originField, fieldAlias));
                    // 处理纵轴过滤
                    yWheres.add(getYWheres(x, originField, fieldAlias));
                    // 处理纵轴排序
                    if (StringUtils.isNotEmpty(x.getSort()) && Utils.joinSort(x.getSort())) {
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
                    originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);

                if (ifAggregate) {
                    if (i == baseXAxis.size()) {// 起止时间
                        String fieldName = String.format(SqlServerSQLConstants.AGG_FIELD, "min", originField);
                        yFields.add(getXFields(x, fieldName, fieldAlias));

                        yWheres.add(getYWheres(x, originField, fieldAlias));

                    } else if (i == baseXAxis.size() + 1) {
                        String fieldName = String.format(SqlServerSQLConstants.AGG_FIELD, "max", originField);

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
                if (StringUtils.isNotEmpty(x.getSort()) && Utils.joinSort(x.getSort())) {
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
                .tableName(String.format(SqlServerSQLConstants.BRACKETS, sql))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 1))
                .build();
        if (CollectionUtils.isNotEmpty(aggWheres)) st.add("filters", aggWheres);
        if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom")) {
            SQLObj limitFiled = SQLObj.builder().limitFiled("top " + view.getResultCount() + " ").build();
            st.add("limitFiled", limitFiled);
        }
        return st.render();
    }

    @Override
    public String getSQLAsTmpRangeBar(String table, List<ChartViewFieldDTO> baseXAxis, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack, ChartViewWithBLOBs view) {
        return getSQLRangeBar("(" + table + ")", baseXAxis, xAxis, yAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, extStack, null, view);
    }

    @Override
    public String searchTable(String table) {
        return "SELECT table_name FROM information_schema.TABLES WHERE table_name ='" + table + "'";
    }

    @Override
    public String getSQLSummary(String table, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, ChartViewWithBLOBs view, Datasource ds) {
        // 字段汇总 排序等
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(SqlServerSQLConstants.KEYWORD_TABLE, table))
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
                    originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_Y_PREFIX, i);
                // 处理纵轴字段
                yFields.add(getYFields(y, originField, fieldAlias));
                // 处理纵轴过滤
                yWheres.add(getYWheres(y, originField, fieldAlias));
                // 处理纵轴排序
                if (StringUtils.isNotEmpty(y.getSort()) && Utils.joinSort(y.getSort())) {
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
                .tableName(String.format(SqlServerSQLConstants.BRACKETS, sql))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 1))
                .build();
        if (CollectionUtils.isNotEmpty(aggWheres)) st.add("filters", aggWheres);
        if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom")) {
            SQLObj limitFiled = SQLObj.builder().limitFiled("top " + view.getResultCount() + " ").build();
            st.add("limitFiled", limitFiled);
        }
        return st.render();
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
        String tmpSql = "SELECT TOP 0 * FROM (" + sql + ") AS DE_SQLSERVER_TMP ";
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
            originName = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
        } else {
            originName = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
        }
        if (field.getDeType() == 1) {
            if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                whereName = String.format(SqlServerSQLConstants.STRING_TO_DATE, originName, StringUtils.isNotEmpty(field.getDateFormat()) ? field.getDateFormat() : SqlServerSQLConstants.DEFAULT_DATE_FORMAT);
            }
            if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                String cast = String.format(SqlServerSQLConstants.LONG_TO_DATE, originName + "/1000");
                whereName = String.format(SqlServerSQLConstants.FROM_UNIXTIME, cast);
            }
            if (field.getDeExtractType() == 1) {
                whereName = originName;
            }
        } else if (field.getDeType() == 2 || field.getDeType() == 3) {
            if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                whereName = String.format(SqlServerSQLConstants.CONVERT, SqlServerSQLConstants.DEFAULT_FLOAT_FORMAT, originName);
            }
            if (field.getDeExtractType() == 1) {
                whereName = String.format(SqlServerSQLConstants.UNIX_TIMESTAMP, originName);
            }
            if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
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
            } else if (StringUtils.containsIgnoreCase(item.getTerm(), "in") || StringUtils.containsIgnoreCase(item.getTerm(), "not in")) {
                if (field.getType().equalsIgnoreCase("NVARCHAR")) {
                    whereValue = "(" + Arrays.asList(value.split(",")).stream().map(str -> {
                        return "N" + "'" + str + "'";
                    }).collect(Collectors.joining(",")) + ")";
                } else {
                    whereValue = "('" + String.join("','", value.split(",")) + "')";
                }
            } else if (StringUtils.containsIgnoreCase(item.getTerm(), "like")) {
                whereValue = "'%" + value + "%'";
            } else {
                if (field.getType().equalsIgnoreCase("NVARCHAR")) {
                    whereValue = String.format(SqlServerSQLConstants.WHERE_VALUE_VALUE_CH, value);
                } else {
                    whereValue = String.format(SqlServerSQLConstants.WHERE_VALUE_VALUE, value);
                }
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
            originName = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
        } else {
            originName = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
        }
        if (field.getDeType() == 1) {
            if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                whereName = String.format(SqlServerSQLConstants.STRING_TO_DATE, originName, StringUtils.isNotEmpty(field.getDateFormat()) ? field.getDateFormat() : SqlServerSQLConstants.DEFAULT_DATE_FORMAT);
            }
            if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                String cast = String.format(SqlServerSQLConstants.LONG_TO_DATE, originName + "/1000");
                whereName = String.format(SqlServerSQLConstants.FROM_UNIXTIME, cast);
            }
            if (field.getDeExtractType() == 1) {
                whereName = originName;
            }
        } else if (field.getDeType() == 2 || field.getDeType() == 3) {
            if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                whereName = String.format(SqlServerSQLConstants.CONVERT, SqlServerSQLConstants.DEFAULT_FLOAT_FORMAT, originName);
            }
            if (field.getDeExtractType() == 1) {
                whereName = String.format(SqlServerSQLConstants.UNIX_TIMESTAMP, originName);
            }
            if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
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
            } else if (StringUtils.containsIgnoreCase(item.getTerm(), "in") || StringUtils.containsIgnoreCase(item.getTerm(), "not in")) {
                if (field.getType().equalsIgnoreCase("NVARCHAR")) {
                    whereValue = "(" + Arrays.asList(value.split(",")).stream().map(str -> {
                        return "N" + "'" + str + "'";
                    }).collect(Collectors.joining(",")) + ")";
                } else {
                    whereValue = "('" + String.join("','", value.split(",")) + "')";
                }
            } else if (StringUtils.containsIgnoreCase(item.getTerm(), "like")) {
                whereValue = "'%" + value + "%'";
            } else {
                if (field.getType().equalsIgnoreCase("NVARCHAR")) {
                    whereValue = String.format(SqlServerSQLConstants.WHERE_VALUE_VALUE_CH, value);
                } else {
                    whereValue = String.format(SqlServerSQLConstants.WHERE_VALUE_VALUE, value);
                }
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
        schema = String.format(SqlServerSQLConstants.KEYWORD_TABLE, schema);
        return createSQLPreview("SELECT * FROM " + schema + "." + String.format(SqlServerSQLConstants.KEYWORD_TABLE, tableName), null);
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
                originName = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
            } else {
                originName = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
            }
            if (field.getDeType() == 1) {
                if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                    whereName = String.format(SqlServerSQLConstants.STRING_TO_DATE, originName, StringUtils.isNotEmpty(field.getDateFormat()) ? field.getDateFormat() : SqlServerSQLConstants.DEFAULT_DATE_FORMAT);
                }
                if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                    String cast = String.format(SqlServerSQLConstants.LONG_TO_DATE, originName + "/1000");
                    whereName = String.format(SqlServerSQLConstants.FROM_UNIXTIME, cast);
                }
                if (field.getDeExtractType() == 1) {
                    whereName = originName;
                }
            } else if (field.getDeType() == 2 || field.getDeType() == 3) {
                if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                    whereName = String.format(SqlServerSQLConstants.CONVERT, SqlServerSQLConstants.DEFAULT_FLOAT_FORMAT, originName);
                }
                if (field.getDeExtractType() == 1) {
                    whereName = String.format(SqlServerSQLConstants.UNIX_TIMESTAMP, originName);
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
                        if (field.getType().equalsIgnoreCase("NVARCHAR")) {
                            whereValue = "(" + Arrays.asList(value.split(",")).stream().map(str -> {
                                return "N" + "'" + str + "'";
                            }).collect(Collectors.joining(",")) + ")";
                        } else {
                            whereValue = "('" + String.join("','", value.split(",")) + "')";
                        }
                    } else if (StringUtils.containsIgnoreCase(filterItemDTO.getTerm(), "like")) {
                        whereValue = "'%" + value + "%'";
                    } else {
                        if (field.getType().equalsIgnoreCase("NVARCHAR")) {
                            whereValue = String.format(SqlServerSQLConstants.WHERE_VALUE_VALUE_CH, value);
                        } else {
                            whereValue = String.format(SqlServerSQLConstants.WHERE_VALUE_VALUE, value);
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
                    originName = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
                } else {
                    originName = String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
                }

                if (field.getDeType() == 1) {
                    if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                        if (!StringUtils.containsIgnoreCase(request.getOperator(), "between")) {
                            whereName = transDateFormat(request.getDateStyle(), request.getDatePattern(), String.format(SqlServerSQLConstants.STRING_TO_DATE, originName, StringUtils.isNotEmpty(field.getDateFormat()) ? field.getDateFormat() : SqlServerSQLConstants.DEFAULT_DATE_FORMAT));
                        } else {
                            whereName = String.format(SqlServerSQLConstants.STRING_TO_DATE, originName, StringUtils.isNotEmpty(field.getDateFormat()) ? field.getDateFormat() : SqlServerSQLConstants.DEFAULT_DATE_FORMAT);
                        }
                    }
                    if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                        if (!StringUtils.containsIgnoreCase(request.getOperator(), "between")) {
                            String cast = String.format(SqlServerSQLConstants.LONG_TO_DATE, originName + "/1000");
                            whereName = transDateFormat(request.getDateStyle(), request.getDatePattern(), cast);
                        } else {
                            String cast = String.format(SqlServerSQLConstants.LONG_TO_DATE, originName + "/1000");
                            whereName = String.format(SqlServerSQLConstants.FROM_UNIXTIME, cast);
                        }
                    }
                    if (field.getDeExtractType() == 1) {
                        if (!StringUtils.containsIgnoreCase(request.getOperator(), "between")) {
                            whereName = transDateFormat(request.getDateStyle(), request.getDatePattern(), originName);
                        } else {
                            whereName = originName;
                        }
                    }
                } else if (field.getDeType() == 2 || field.getDeType() == 3) {
                    if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                        whereName = String.format(SqlServerSQLConstants.CONVERT, SqlServerSQLConstants.DEFAULT_FLOAT_FORMAT, originName);
                    }
                    if (field.getDeExtractType() == 1) {
                        whereName = String.format(SqlServerSQLConstants.UNIX_TIMESTAMP, originName);
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
            if (request.getIsTree() && whereNameList.size() > 1) {
                whereName = "CONCAT(" + StringUtils.join(whereNameList, ",',',") + ")";
            } else {
                whereName = whereNameList.get(0);
            }
            String whereTerm = transMysqlFilterTerm(request.getOperator());
            String whereValue = "";

            if (StringUtils.containsIgnoreCase(request.getOperator(), "in")) {
                if ((request.getDatasetTableField() != null && request.getDatasetTableField().getType().equalsIgnoreCase("NVARCHAR")) || (request.getDatasetTableFieldList() != null && request.getDatasetTableFieldList().stream().map(DatasetTableField::getType).collect(Collectors.toList()).contains("nvarchar"))) {
                    whereValue = "(" + value.stream().map(str -> {
                        return "N" + "'" + str + "'";
                    }).collect(Collectors.joining(",")) + ")";
                } else {
                    whereValue = "('" + StringUtils.join(value, "','") + "')";
                }
            } else if (StringUtils.containsIgnoreCase(request.getOperator(), "like")) {
                String keyword = value.get(0).toUpperCase();
                whereValue = "'%" + keyword + "%'";
                whereName = "upper(" + whereName + ")";
            } else if (StringUtils.containsIgnoreCase(request.getOperator(), "between")) {
                if (request.getDatasetTableField().getDeType() == DeTypeConstants.DE_TIME) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String startTime = simpleDateFormat.format(new Date(Long.parseLong(value.get(0))));
                    String endTime = simpleDateFormat.format(new Date(Long.parseLong(value.get(1))));
                    whereValue = String.format(SqlServerSQLConstants.WHERE_BETWEEN, startTime, endTime);
                } else {
                    whereValue = String.format(SqlServerSQLConstants.WHERE_BETWEEN, value.get(0), value.get(1));
                }
            } else {

                if ((request.getDatasetTableField() != null && request.getDatasetTableField().getType().equalsIgnoreCase("NVARCHAR")) || (request.getDatasetTableFieldList() != null && request.getDatasetTableFieldList().stream().map(DatasetTableField::getType).collect(Collectors.toList()).contains("nvarchar"))) {
                    whereValue = String.format(SqlServerSQLConstants.WHERE_VALUE_VALUE_CH, value.get(0));
                } else {
                    whereValue = String.format(SqlServerSQLConstants.WHERE_VALUE_VALUE, value.get(0));
                }

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

    //日期格式化
    private String transDateFormat(String dateStyle, String datePattern, String originField) {
        String split = "-";
        if (StringUtils.equalsIgnoreCase(datePattern, "date_sub")) {
            split = "-";
        } else if (StringUtils.equalsIgnoreCase(datePattern, "date_split")) {
            split = "/";
        } else {
            split = "-";
        }

        if (StringUtils.isEmpty(dateStyle)) {
            return "convert(varchar," + originField + ",120)";
        }

        switch (dateStyle) {
            case "y":
                return "CONVERT(varchar(100), datepart(yy, " + originField + "))";
            case "y_M":
                if (split.equalsIgnoreCase("-")) {
                    return "substring( convert(varchar," + originField + ",120),1,7)";
                } else {
                    return "replace(" + "substring( convert(varchar," + originField + ",120),1,7), '-','/')";
                }
            case "y_M_d":
                if (split.equalsIgnoreCase("-")) {
                    return "CONVERT(varchar(100), " + originField + ", 23)";
                } else {
                    return "CONVERT(varchar(100), " + originField + ", 111)";
                }
            case "H_m_s":
                return "CONVERT(varchar(100), " + originField + ", 8)";
            case "y_M_d_H":
                if (split.equalsIgnoreCase("-")) {
                    return "substring( convert(varchar," + originField + ",120),1,13)";
                } else {
                    return "replace(" + "substring( convert(varchar," + originField + ",120),1,13), '-','/')";
                }
            case "y_M_d_H_m":
                if (split.equalsIgnoreCase("-")) {
                    return "substring( convert(varchar," + originField + ",120),1,16)";
                } else {
                    return "replace(" + "substring( convert(varchar," + originField + ",120),1,16), '-','/')";
                }
            case "y_M_d_H_m_s":
                if (split.equalsIgnoreCase("-")) {
                    return "convert(varchar," + originField + ",120)";
                } else {
                    return "replace(" + "convert(varchar," + originField + ",120), '-','/')";
                }
            default:
                return "convert(varchar," + originField + ",120)";
        }
    }

    private SQLObj getXFields(ChartViewFieldDTO x, String originField, String fieldAlias) {
        String fieldName = "";
        if (x.getDeExtractType() == DeTypeConstants.DE_TIME) {
            if (x.getDeType() == DeTypeConstants.DE_INT || x.getDeType() == DeTypeConstants.DE_FLOAT) { //时间转数值
                fieldName = String.format(SqlServerSQLConstants.UNIX_TIMESTAMP, originField);
            } else if (x.getDeType() == DeTypeConstants.DE_TIME) { //时间格式化
                fieldName = transDateFormat(x.getDateStyle(), x.getDatePattern(), originField);
            } else {
                fieldName = originField;
            }
        } else {
            if (x.getDeType() == DeTypeConstants.DE_TIME) {
                if (x.getDeExtractType() == DeTypeConstants.DE_STRING) {// 字符串转时间
                    String cast = String.format(SqlServerSQLConstants.STRING_TO_DATE, originField, StringUtils.isNotEmpty(x.getDateFormat()) ? x.getDateFormat() : SqlServerSQLConstants.DEFAULT_DATE_FORMAT);
                    fieldName = transDateFormat(x.getDateStyle(), x.getDatePattern(), cast);
                } else {// 数值转时间
                    String cast = String.format(SqlServerSQLConstants.LONG_TO_DATE, originField + "/1000");
                    fieldName = transDateFormat(x.getDateStyle(), x.getDatePattern(), cast);
                }
            } else {
                if (x.getDeType() == DeTypeConstants.DE_INT) {
                    fieldName = String.format(SqlServerSQLConstants.CONVERT, SqlServerSQLConstants.DEFAULT_INT_FORMAT, originField);
                } else if (x.getDeType() == DeTypeConstants.DE_FLOAT) {
                    fieldName = String.format(SqlServerSQLConstants.CONVERT, SqlServerSQLConstants.DEFAULT_FLOAT_FORMAT, originField);
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
            fieldName = SqlServerSQLConstants.AGG_COUNT;
        } else if (SQLConstants.DIMENSION_TYPE.contains(y.getDeType())) {
            if (StringUtils.equalsIgnoreCase(y.getSummary(), "count_distinct")) {
                fieldName = String.format(SqlServerSQLConstants.AGG_FIELD, "COUNT", "DISTINCT " + originField);
            } else if (StringUtils.equalsIgnoreCase(y.getSummary(), "group_concat")) {
                fieldName = String.format(SqlServerSQLConstants.GROUP_CONCAT, originField);
            } else {
                fieldName = String.format(SqlServerSQLConstants.AGG_FIELD, y.getSummary(), originField);
            }
        } else {
            if (StringUtils.equalsIgnoreCase(y.getSummary(), "avg") || StringUtils.containsIgnoreCase(y.getSummary(), "pop")) {
                String convert = String.format(SqlServerSQLConstants.CONVERT, y.getDeType() == DeTypeConstants.DE_INT ? SqlServerSQLConstants.DEFAULT_INT_FORMAT : SqlServerSQLConstants.DEFAULT_FLOAT_FORMAT, originField);
                String summary = y.getSummary();
                if (StringUtils.equalsIgnoreCase(y.getSummary(), "stddev_pop")) {
                    summary = "STDEVP";
                } else if (StringUtils.equalsIgnoreCase(y.getSummary(), "var_pop")) {
                    summary = "VARP";
                }
                String agg = String.format(SqlServerSQLConstants.AGG_FIELD, summary, convert);
                fieldName = String.format(SqlServerSQLConstants.CONVERT, SqlServerSQLConstants.DEFAULT_FLOAT_FORMAT, agg);
            } else {
                String convert = String.format(SqlServerSQLConstants.CONVERT, y.getDeType() == 2 ? SqlServerSQLConstants.DEFAULT_INT_FORMAT : SqlServerSQLConstants.DEFAULT_FLOAT_FORMAT, originField);
                if (StringUtils.equalsIgnoreCase(y.getSummary(), "count_distinct")) {
                    fieldName = String.format(SqlServerSQLConstants.AGG_FIELD, "COUNT", "DISTINCT " + convert);
                } else {
                    fieldName = String.format(SqlServerSQLConstants.AGG_FIELD, y.getSummary(), convert);
                }
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
                    if (y.getType().equalsIgnoreCase("NVARCHAR")) {
                        whereValue = "(" + Arrays.asList(f.getValue().split(",")).stream().map(str -> {
                            return "N" + "'" + str + "'";
                        }).collect(Collectors.joining(",")) + ")";
                    } else {
                        whereValue = "('" + String.join("','", f.getValue().split(",")) + "')";
                    }
                } else if (StringUtils.containsIgnoreCase(f.getTerm(), "like")) {
                    whereValue = "'%" + f.getValue() + "%'";
                } else {
                    if (y.getType().equalsIgnoreCase("NVARCHAR")) {
                        whereValue = String.format(SqlServerSQLConstants.WHERE_VALUE_VALUE_CH, f.getValue());
                    } else {
                        whereValue = String.format(SqlServerSQLConstants.WHERE_VALUE_VALUE, f.getValue());
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
        try {
            int i = 0;
            return buildCalcField(originField, tableObj, i);
        } catch (Exception e) {
            DataEaseException.throwException(Translator.get("i18n_field_circular_ref"));
        }
        return null;
    }

    private String buildCalcField(String originField, SQLObj tableObj, int i) throws Exception {
        try {
            i++;
            if (i > 100) {
                DataEaseException.throwException(Translator.get("i18n_field_circular_error"));
            }
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
                if (StringUtils.containsIgnoreCase(originField, ele.getId() + "")) {
                    // 计算字段允许二次引用，这里递归查询完整引用链
                    if (Objects.equals(ele.getExtField(), 0)) {
                        originField = originField.replaceAll("\\[" + ele.getId() + "]",
                                String.format(SqlServerSQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), ele.getOriginName()));
                    } else {
                        originField = originField.replaceAll("\\[" + ele.getId() + "]", ele.getOriginName());
                        originField = buildCalcField(originField, tableObj, i);
                    }
                }
            }
            return originField;
        } catch (Exception e) {
            DataEaseException.throwException(Translator.get("i18n_field_circular_error"));
        }
        return null;
    }

    @Override
    public String sqlForPreview(String table, Datasource ds) {
        String schema = new Gson().fromJson(ds.getConfiguration(), JdbcConfiguration.class).getSchema();
        schema = String.format(SqlServerSQLConstants.KEYWORD_TABLE, schema);
        return "SELECT * FROM " + schema + "." + String.format(SqlServerSQLConstants.KEYWORD_TABLE, table);
    }

    public List<Dateformat> dateformat() {
        return JSONArray.parseArray("[\n" +
                "{\"dateformat\": \"102\", \"desc\": \"yyyy.mm.dd\"},\n" +
                "{\"dateformat\": \"111\", \"desc\": \"yyyy/mm/dd\"},\n" +
                "{\"dateformat\": \"112\", \"desc\": \"yyyymmdd\"},\n" +
                "{\"dateformat\": \"120\", \"desc\": \"yyyy-mm-dd hh:mi:ss\"}\n" +
                "]", Dateformat.class);
    }

    @Override
    public String getResultCount(boolean isTable, String sql, List<ChartViewFieldDTO> xAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view) {
        if (isTable) {
            return "SELECT COUNT(*) AS count from (" + originTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view, false, true, true) + ") COUNT_TEMP";
        } else {
            return "SELECT COUNT(*) AS count from (" + originSQLAsTmpTableInfo(sql, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view, false, true) + ") COUNT_TEMP";
        }
    }
}

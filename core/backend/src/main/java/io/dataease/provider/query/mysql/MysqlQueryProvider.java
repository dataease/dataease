package io.dataease.provider.query.mysql;

import com.alibaba.fastjson.JSONArray;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.ChartViewWithBLOBs;
import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.plugins.common.base.domain.DatasetTableFieldExample;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.base.mapper.DatasetTableFieldMapper;
import io.dataease.plugins.common.constants.DeTypeConstants;
import io.dataease.plugins.common.constants.datasource.DorisConstants;
import io.dataease.plugins.common.constants.datasource.MySQLConstants;
import io.dataease.plugins.common.constants.datasource.SQLConstants;
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


/**
 * @Author gin
 * @Date 2021/5/17 2:43 下午
 */
@Service("mysqlQueryProvider")
public class MysqlQueryProvider extends QueryProvider {
    @Resource
    private DatasetTableFieldMapper datasetTableFieldMapper;

    @Override
    public Integer transFieldType(String field) {
        switch (field) {
            case "CHAR":
            case "VARCHAR":
            case "TEXT":
            case "TINYTEXT":
            case "MEDIUMTEXT":
            case "LONGTEXT":
            case "ENUM":
                return 0;// 文本
            case "DATE":
            case "TIME":
            case "YEAR":
            case "DATETIME":
            case "DATEV2":
            case "DATETIMEV2":
            case "TIMESTAMP":
                return 1;// 时间
            case "INT":
            case "SMALLINT":
            case "MEDIUMINT":
            case "INTEGER":
            case "BIGINT":
            case "BIGINT UNSIGNED":
            case "LONG": //增加了LONG类型
                return 2;// 整型
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
        return "SELECT * FROM (" + sqlFix(sql) + ") AS tmp " + " LIMIT 0,1000";
    }

    @Override
    public String createQuerySQL(String table, List<DatasetTableField> fields, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return createQuerySQL(table, fields, isGroup, ds, fieldCustomFilter, rowPermissionsTree, null, null, null);
    }

    @Override
    public String createQuerySQLAsTmp(String sql, List<DatasetTableField> fields, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return createQuerySQL("(" + sqlFix(sql) + ")", fields, isGroup, null, fieldCustomFilter, rowPermissionsTree, null, null, null);
    }

    private SQLObj buildSortField(DeSortField f, SQLObj tableObj, int index) {
        String originField;
        if (ObjectUtils.isNotEmpty(f.getExtField()) && f.getExtField() == 2) {
            // 解析origin name中有关联的字段生成sql表达式
            originField = calcFieldRegex(f.getOriginName(), tableObj);
        } else if (ObjectUtils.isNotEmpty(f.getExtField()) && f.getExtField() == 1) {
            originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), f.getOriginName());
        } else {
            originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), f.getOriginName());
        }
        String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, index);
        String fieldName = "";
        // 处理横轴字段
        if (f.getDeExtractType() == 1) {
            if (f.getDeType() == 2 || f.getDeType() == 3) {
                fieldName = String.format(MySQLConstants.UNIX_TIMESTAMP, originField) + "*1000";
            } else {
                if (f.getType().equalsIgnoreCase("YEAR")) {
                    fieldName = String.format(MySQLConstants.DATE_FORMAT, "CONCAT(" + originField + ",'-01-01')", MySQLConstants.DEFAULT_DATE_FORMAT);
                } else if (f.getType().equalsIgnoreCase("TIME")) {
                    fieldName = String.format(MySQLConstants.DATE_FORMAT, "CONCAT('1970-01-01', " + originField + ")", MySQLConstants.DEFAULT_DATE_FORMAT);
                } else {
                    fieldName = String.format(MySQLConstants.DATE_FORMAT, originField, MySQLConstants.DEFAULT_DATE_FORMAT);
                }
            }
        } else if (f.getDeExtractType() == 0) {
            if (f.getDeType() == 2) {
                fieldName = String.format(MySQLConstants.CAST, originField, MySQLConstants.DEFAULT_INT_FORMAT);
            } else if (f.getDeType() == 3) {
                fieldName = String.format(MySQLConstants.CAST, originField, MySQLConstants.DEFAULT_FLOAT_FORMAT);
            } else if (f.getDeType() == 1) {
                fieldName = StringUtils.isEmpty(f.getDateFormat()) ? String.format(MySQLConstants.STR_TO_DATE, originField, MySQLConstants.DEFAULT_DATE_FORMAT) :
                        String.format(MySQLConstants.DATE_FORMAT, String.format(MySQLConstants.STR_TO_DATE, originField, f.getDateFormat()), MySQLConstants.DEFAULT_DATE_FORMAT);
            } else {
                fieldName = originField;
            }
        } else {
            if (f.getDeType() == 1) {
                String cast = String.format(MySQLConstants.CAST, originField, MySQLConstants.DEFAULT_INT_FORMAT) + "/1000";
                fieldName = String.format(MySQLConstants.FROM_UNIXTIME, cast, MySQLConstants.DEFAULT_DATE_FORMAT);
            } else if (f.getDeType() == 2) {
                fieldName = String.format(MySQLConstants.CAST, originField, MySQLConstants.DEFAULT_INT_FORMAT);
            } else {
                fieldName = originField;
            }
        }
        SQLObj result = SQLObj.builder().orderField(originField).orderAlias(originField).orderDirection(f.getOrderDirection()).build();
        return result;

    }

    @Override
    public String createQuerySQL(String table, List<DatasetTableField> fields, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<DeSortField> sortFields, Long limit, String keyword) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(MySQLConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 0))
                .build();
        List<SQLObj> xFields = new ArrayList<>();
        List<String> fieldList = fields.stream().map(DatasetTableField::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(sortFields)) {
            sortFields.forEach(item -> {
                int indexOf = fieldList.indexOf(item.getId());
                if (indexOf == -1) {
                    fields.add(item);
                } else {
                    fields.set(indexOf, item);
                }
            });
        }
        List<SQLObj> xOrders = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(fields)) {
            for (int i = 0; i < fields.size(); i++) {
                DatasetTableField f = fields.get(i);
                String originField;
                if (ObjectUtils.isNotEmpty(f.getExtField()) && f.getExtField() == 2) {
                    // 解析origin name中有关联的字段生成sql表达式
                    originField = calcFieldRegex(f.getOriginName(), tableObj);
                } else if (ObjectUtils.isNotEmpty(f.getExtField()) && f.getExtField() == 1) {
                    originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), f.getOriginName());
                } else {
                    originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), f.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);
                String fieldName = "";
                // 处理横轴字段
                if (f.getDeExtractType() == 1) {
                    if (f.getDeType() == 2 || f.getDeType() == 3) {
                        fieldName = String.format(MySQLConstants.UNIX_TIMESTAMP, originField) + "*1000";
                    } else {
                        if (f.getType().equalsIgnoreCase("YEAR")) {
                            fieldName = String.format(MySQLConstants.DATE_FORMAT, "CONCAT(" + originField + ",'-01-01')", MySQLConstants.DEFAULT_DATE_FORMAT);
                        } else if (f.getType().equalsIgnoreCase("TIME")) {
                            fieldName = String.format(MySQLConstants.DATE_FORMAT, "CONCAT('1970-01-01', " + originField + ")", MySQLConstants.DEFAULT_DATE_FORMAT);
                        } else {
                            fieldName = String.format(MySQLConstants.DATE_FORMAT, originField, MySQLConstants.DEFAULT_DATE_FORMAT);
                        }
                    }
                } else if (f.getDeExtractType() == 0) {
                    if (f.getDeType() == 2) {
                        fieldName = String.format(MySQLConstants.CAST, originField, MySQLConstants.DEFAULT_INT_FORMAT);
                    } else if (f.getDeType() == 3) {
                        fieldName = String.format(MySQLConstants.CAST, originField, MySQLConstants.DEFAULT_FLOAT_FORMAT);
                    } else if (f.getDeType() == 1) {
                        fieldName = StringUtils.isEmpty(f.getDateFormat()) ? String.format(MySQLConstants.STR_TO_DATE, originField, MySQLConstants.DEFAULT_DATE_FORMAT) :
                                String.format(MySQLConstants.DATE_FORMAT, String.format(MySQLConstants.STR_TO_DATE, originField, f.getDateFormat()), MySQLConstants.DEFAULT_DATE_FORMAT);
                    } else {
                        fieldName = originField;
                    }
                } else {
                    if (f.getDeType() == 1) {
                        String cast = String.format(MySQLConstants.CAST, originField, MySQLConstants.DEFAULT_INT_FORMAT) + "/1000";
                        fieldName = String.format(MySQLConstants.FROM_UNIXTIME, cast, MySQLConstants.DEFAULT_DATE_FORMAT);
                    } else if (f.getDeType() == 2) {
                        fieldName = String.format(MySQLConstants.CAST, originField, MySQLConstants.DEFAULT_INT_FORMAT);
                    } else {
                        fieldName = originField;
                    }
                }
                xFields.add(SQLObj.builder()
                        .fieldOriginName(originField)
                        .fieldName(fieldName)
                        .fieldAlias(fieldAlias)
                        .build());
                if (f instanceof DeSortField) {
                    DeSortField x = (DeSortField) f;
                    xOrders.add(SQLObj.builder()
                            .orderField(originField)
                            .orderAlias(fieldAlias)
                            .orderDirection(x.getOrderDirection())
                            .build());
                }
            }
        }


        STGroup stg = new STGroupFile(SQLConstants.SQL_TEMPLATE);
        ST st_sql = stg.getInstanceOf("previewSql");
        st_sql.add("isGroup", isGroup);
        if (CollectionUtils.isNotEmpty(xFields)) st_sql.add("groups", xFields);
        if (ObjectUtils.isNotEmpty(tableObj)) st_sql.add("table", tableObj);

        String customWheres = transChartFilterTrees(tableObj, fieldCustomFilter);
        String whereTrees = transFilterTrees(tableObj, rowPermissionsTree);
        List<String> wheres = new ArrayList<>();
        if (customWheres != null) wheres.add(customWheres);
        if (whereTrees != null) wheres.add(whereTrees);

        if (StringUtils.isNotBlank(keyword)) {
            String keyWhere = "(" + transKeywordFilterList(tableObj, xFields, keyword) + ")";
            wheres.add(keyWhere);
        }
        if (CollectionUtils.isNotEmpty(wheres)) st_sql.add("filters", wheres);
        String sql = st_sql.render();
        ST st = stg.getInstanceOf("previewSql");
        st.add("isGroup", false);
        SQLObj tableSQL = SQLObj.builder()
                .tableName(String.format(DorisConstants.BRACKETS, sql))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 1))
                .build();
        st.add("table", tableSQL);

        if (CollectionUtils.isNotEmpty(xOrders)) {
            st.add("orders", xOrders);
        }
        if (ObjectUtils.isNotEmpty(limit)) {
            ChartViewWithBLOBs view = new ChartViewWithBLOBs();
            view.setResultMode("custom");
            view.setResultCount(Integer.parseInt(limit.toString()));
            return sqlLimit(st.render(), view);
        }
        return st.render();
    }


    @Override
    public String createQuerySQLAsTmp(String sql, List<DatasetTableField> fields, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<DeSortField> sortFields, Long limit, String keyword) {
        return createQuerySQL("(" + sqlFix(sql) + ")", fields, isGroup, null, fieldCustomFilter, rowPermissionsTree, sortFields, limit, keyword);
    }

    @Override
    public String createQueryTableWithPage(String table, List<DatasetTableField> fields, Integer page, Integer pageSize, Integer realSize, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return createQuerySQL(table, fields, isGroup, null, fieldCustomFilter, rowPermissionsTree) + " LIMIT " + (page - 1) * pageSize + "," + realSize;
    }

    @Override
    public String createQueryTableWithLimit(String table, List<DatasetTableField> fields, Integer limit, boolean isGroup, Datasource ds, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return createQuerySQL(table, fields, isGroup, null, fieldCustomFilter, rowPermissionsTree) + " LIMIT 0," + limit;
    }

    @Override
    public String createQuerySqlWithLimit(String sql, List<DatasetTableField> fields, Integer limit, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return createQuerySQLAsTmp(sql, fields, isGroup, fieldCustomFilter, rowPermissionsTree) + " LIMIT 0," + limit;
    }

    @Override
    public String createQuerySQLWithPage(String sql, List<DatasetTableField> fields, Integer page, Integer pageSize, Integer realSize, boolean isGroup, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree) {
        return createQuerySQLAsTmp(sql, fields, isGroup, fieldCustomFilter, rowPermissionsTree) + " LIMIT " + (page - 1) * pageSize + "," + realSize;
    }

    @Override
    public String getSQL(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(MySQLConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 0))
                .build();
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
                    originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
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
                    originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
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
                .tableName(String.format(MySQLConstants.BRACKETS, sql))
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
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(MySQLConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 0))
                .build();
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
                        originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                    } else {
                        originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
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
                    originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);

                if (ifAggregate) {
                    if (i == baseXAxis.size()) {// 起止时间
                        String fieldName = String.format(MySQLConstants.AGG_FIELD, "min", originField);
                        yFields.add(getXFields(x, fieldName, fieldAlias));

                        yWheres.add(getYWheres(x, originField, fieldAlias));

                    } else if (i == baseXAxis.size() + 1) {
                        String fieldName = String.format(MySQLConstants.AGG_FIELD, "max", originField);

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
                .tableName(String.format(MySQLConstants.BRACKETS, sql))
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
        return sqlLimit(originalTableInfo(table, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view, false), view);
    }

    @Override
    public String getSQLWithPage(boolean isTable, String table, List<ChartViewFieldDTO> xAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view, PageInfo pageInfo) {
        String limit = ((pageInfo.getGoPage() != null && pageInfo.getPageSize() != null) ? " LIMIT " + (pageInfo.getGoPage() - 1) * pageInfo.getPageSize() + "," + pageInfo.getPageSize() : "");
        if (isTable) {
            return originalTableInfo(table, xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view, false) + limit;
        } else {
            return originalTableInfo("(" + sqlFix(table) + ")", xAxis, fieldCustomFilter, rowPermissionsTree, extFilterRequestList, ds, view, false) + limit;
        }
    }

    public String originalTableInfo(String table, List<ChartViewFieldDTO> xAxis, FilterTreeObj fieldCustomFilter, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds, ChartViewWithBLOBs view, boolean needOrder) {
        SQLObj tableObj = SQLObj.builder()
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(MySQLConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 0))
                .build();
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
                    originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    if (x.getDeType() == 2 || x.getDeType() == 3) {
                        originField = String.format(MySQLConstants.CAST, String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName()), MySQLConstants.DEFAULT_FLOAT_FORMAT);
                    } else {
                        originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
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
        orders.addAll(xOrders);
        if (needOrder && CollectionUtils.isEmpty(xOrders)) {
            orders.add(SQLObj.builder()
                    .orderField(String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, 0))
                    .orderAlias(String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, 0))
                    .orderDirection("ASC")
                    .build());
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
                .tableName(String.format(MySQLConstants.BRACKETS, sql))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 1))
                .build();
        if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        return st.render();
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
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(MySQLConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 0))
                .build();
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
                    originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
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
                    originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
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
                .tableName(String.format(MySQLConstants.BRACKETS, sql))
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
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(MySQLConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 0))
                .build();
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
                    originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
                } else {
                    originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), x.getOriginName());
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
                    originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
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
        if (CollectionUtils.isNotEmpty(groups)) st_sql.add("groups", groups);
        if (CollectionUtils.isNotEmpty(yFields)) st_sql.add("aggregators", yFields);
        if (CollectionUtils.isNotEmpty(wheres)) st_sql.add("filters", wheres);
        if (ObjectUtils.isNotEmpty(tableObj)) st_sql.add("table", tableObj);
        String sql = st_sql.render();

        ST st = stg.getInstanceOf("querySql");
        SQLObj tableSQL = SQLObj.builder()
                .tableName(String.format(MySQLConstants.BRACKETS, sql))
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
                .tableName((table.startsWith("(") && table.endsWith(")")) ? table : String.format(MySQLConstants.KEYWORD_TABLE, table))
                .tableAlias(String.format(TABLE_ALIAS_PREFIX, 0))
                .build();
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
                    originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
                } else {
                    originField = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), y.getOriginName());
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
                .tableName(String.format(MySQLConstants.BRACKETS, sql))
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
        String tmpSql = "SELECT * FROM (" + sql + ") AS tmp " + " LIMIT 0";
        return tmpSql;
    }

    @Override
    public String createRawQuerySQL(String table, List<DatasetTableField> fields, Datasource ds) {
        String[] array = fields.stream().map(f -> {
            StringBuilder stringBuilder = new StringBuilder();
            if (f.getDeExtractType() == 4) { // 处理 tinyint
                stringBuilder.append("concat(`").append(f.getOriginName()).append("`,'') AS ").append(f.getDataeaseName());
            } else if (f.getDeExtractType() == 1 && f.getType().equalsIgnoreCase("YEAR")) { // 处理 YEAR
                stringBuilder.append("").append(String.format(MySQLConstants.DATE_FORMAT, "CONCAT(" + f.getOriginName() + ",'-01-01')", MySQLConstants.DEFAULT_DATE_FORMAT)).append(" AS ").append(f.getDataeaseName());
            } else {
                stringBuilder.append("`").append(f.getOriginName()).append("` AS ").append(f.getDataeaseName());
            }
            return stringBuilder.toString();
        }).toArray(String[]::new);
        table = table.trim().startsWith("(") ? table : String.format(MySQLConstants.KEYWORD_TABLE, table);
        return MessageFormat.format("SELECT {0} FROM {1}  LIMIT DE_OFFSET, DE_PAGE_SIZE ", StringUtils.join(array, ","), table);
    }

    public String getTotalCount(boolean isTable, String sql, Datasource ds) {
        if (isTable) {
            return "SELECT COUNT(*) from " + String.format(MySQLConstants.KEYWORD_TABLE, sql);
        } else {
            return "SELECT COUNT(*) from ( " + sqlFix(sql) + " ) DE_COUNT_TEMP";
        }
    }

    @Override
    public String createRawQuerySQLAsTmp(String sql, List<DatasetTableField> fields) {
        return createRawQuerySQL("(" + sqlFix(sql) + ") AS DE_TEMP", fields, null);
    }

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
            originName = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
        } else {
            originName = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
        }
        if (field.getDeType() == 1) {
            if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                whereName = String.format(MySQLConstants.STR_TO_DATE, originName, StringUtils.isNotEmpty(field.getDateFormat()) ? field.getDateFormat() : MySQLConstants.DEFAULT_DATE_FORMAT);
            }
            if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                String cast = String.format(MySQLConstants.CAST, originName, MySQLConstants.DEFAULT_INT_FORMAT) + "/1000";
                whereName = String.format(MySQLConstants.FROM_UNIXTIME, cast, MySQLConstants.DEFAULT_DATE_FORMAT);
            }
            if (field.getDeExtractType() == 1) {
                whereName = originName;
            }
        } else if (field.getDeType() == 2 || field.getDeType() == 3) {
            if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                whereName = String.format(MySQLConstants.CAST, originName, MySQLConstants.DEFAULT_FLOAT_FORMAT);
            }
            if (field.getDeExtractType() == 1) {
                whereName = String.format(MySQLConstants.UNIX_TIMESTAMP, originName);
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
                whereValue = "('" + String.join("','", value.split(",")) + "')";
            } else if (StringUtils.containsIgnoreCase(item.getTerm(), "like")) {
                whereValue = "'%" + value + "%'";
            } else {
                whereValue = String.format(MySQLConstants.WHERE_VALUE_VALUE, value);
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
            originName = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
        } else {
            originName = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
        }
        if (field.getDeType() == 1) {
            if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                whereName = String.format(MySQLConstants.STR_TO_DATE, originName, StringUtils.isNotEmpty(field.getDateFormat()) ? field.getDateFormat() : MySQLConstants.DEFAULT_DATE_FORMAT);
            }
            if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                String cast = String.format(MySQLConstants.CAST, originName, MySQLConstants.DEFAULT_INT_FORMAT) + "/1000";
                whereName = String.format(MySQLConstants.FROM_UNIXTIME, cast, MySQLConstants.DEFAULT_DATE_FORMAT);
            }
            if (field.getDeExtractType() == 1) {
                whereName = originName;
            }
        } else if (field.getDeType() == 2 || field.getDeType() == 3) {
            if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                whereName = String.format(MySQLConstants.CAST, originName, MySQLConstants.DEFAULT_FLOAT_FORMAT);
            }
            if (field.getDeExtractType() == 1) {
                whereName = String.format(MySQLConstants.UNIX_TIMESTAMP, originName);
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
                whereValue = "('" + String.join("','", value.split(",")) + "')";
            } else if (StringUtils.containsIgnoreCase(item.getTerm(), "like")) {
                whereValue = "'%" + value + "%'";
            } else {
                whereValue = String.format(MySQLConstants.WHERE_VALUE_VALUE, value);
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
        return createSQLPreview("SELECT * FROM " + String.format(MySQLConstants.KEYWORD_TABLE, tableName), null);
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
                originName = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
            } else {
                originName = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
            }
            if (field.getDeType() == 1) {
                if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                    whereName = String.format(MySQLConstants.STR_TO_DATE, originName, StringUtils.isNotEmpty(field.getDateFormat()) ? field.getDateFormat() : MySQLConstants.DEFAULT_DATE_FORMAT);
                }
                if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                    String cast = String.format(MySQLConstants.CAST, originName, MySQLConstants.DEFAULT_INT_FORMAT) + "/1000";
                    whereName = String.format(MySQLConstants.FROM_UNIXTIME, cast, MySQLConstants.DEFAULT_DATE_FORMAT);
                }
                if (field.getDeExtractType() == 1) {
                    whereName = originName;
                }
            } else if (field.getDeType() == 2 || field.getDeType() == 3) {
                if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                    whereName = String.format(MySQLConstants.CAST, originName, MySQLConstants.DEFAULT_FLOAT_FORMAT);
                }
                if (field.getDeExtractType() == 1) {
                    whereName = String.format(MySQLConstants.UNIX_TIMESTAMP, originName);
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
                        whereValue = String.format(MySQLConstants.WHERE_VALUE_VALUE, value);
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
                    originName = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
                } else {
                    originName = String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), field.getOriginName());
                }

                if (field.getDeType() == 1) {
                    String format = transDateFormat(request.getDateStyle(), request.getDatePattern());
                    if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                        String date;
                        if (field.getType().equalsIgnoreCase("YEAR") || StringUtils.equalsIgnoreCase(field.getDateFormat(), "%Y")) {
                            date = String.format(MySQLConstants.DATE_FORMAT, "CONCAT(" + originName + ",'-01-01')", StringUtils.isNotEmpty(field.getDateFormat()) ? field.getDateFormat() : MySQLConstants.DEFAULT_DATE_FORMAT);
                        } else {
                            date = String.format(MySQLConstants.DATE_FORMAT, originName, StringUtils.isNotEmpty(field.getDateFormat()) ? field.getDateFormat() : MySQLConstants.DEFAULT_DATE_FORMAT);
                        }
                        if (request.getOperator().equals("between") && request.getDatasetTableField().getDeExtractType() != 1) {
                            whereName = String.format(MySQLConstants.UNIX_TIMESTAMP, date) + "*1000";
                        } else {
                            if (StringUtils.equalsIgnoreCase(request.getDateStyle(), "y_Q")) {
                                whereName = String.format(format,
                                        String.format(MySQLConstants.DATE_FORMAT, originName, "%Y"),
                                        String.format(MySQLConstants.QUARTER, String.format(MySQLConstants.DATE_FORMAT, originName, MySQLConstants.DEFAULT_DATE_FORMAT)));
                            } else {
                                whereName = String.format(MySQLConstants.DATE_FORMAT, date, format);
                            }
                        }

                    }
                    if (field.getDeExtractType() == 1) {
                        String date;
                        if (field.getType().equalsIgnoreCase("YEAR") || StringUtils.equalsIgnoreCase(field.getDateFormat(), "%Y")) {
                            date = String.format(MySQLConstants.DATE_FORMAT, "CONCAT(" + originName + ",'-01-01')", StringUtils.isNotEmpty(field.getDateFormat()) ? field.getDateFormat() : MySQLConstants.DEFAULT_DATE_FORMAT);
                        } else {
                            date = originName;
                        }
                        if (StringUtils.equalsIgnoreCase(request.getDateStyle(), "y_Q")) {
                            whereName = String.format(format,
                                    String.format(MySQLConstants.DATE_FORMAT, originName, "%Y"),
                                    String.format(MySQLConstants.QUARTER, String.format(MySQLConstants.DATE_FORMAT, originName, MySQLConstants.DEFAULT_DATE_FORMAT)));
                        } else {
                            whereName = date;
                        }
                    }
                    if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                        if (request.getOperator().equals("between")) {
                            whereName = originName;
                        } else {
                            String cast = String.format(MySQLConstants.CAST, originName, MySQLConstants.DEFAULT_INT_FORMAT) + "/1000";
                            if (StringUtils.equalsIgnoreCase(request.getDateStyle(), "y_Q")) {
                                whereName = String.format(format,
                                        String.format(MySQLConstants.DATE_FORMAT, cast, "%Y"),
                                        String.format(MySQLConstants.QUARTER, String.format(MySQLConstants.DATE_FORMAT, field, MySQLConstants.DEFAULT_DATE_FORMAT)));
                            } else {
                                whereName = String.format(MySQLConstants.FROM_UNIXTIME, cast, format);
                            }
                        }
                    }
                } else if (field.getDeType() == 2 || field.getDeType() == 3) {
                    if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                        whereName = String.format(MySQLConstants.CAST, originName, MySQLConstants.DEFAULT_FLOAT_FORMAT);
                    }
                    if (field.getDeExtractType() == 1) {
                        whereName = String.format(MySQLConstants.UNIX_TIMESTAMP, originName);
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
                whereValue = "('" + StringUtils.join(value, "','") + "')";
            } else if (StringUtils.containsIgnoreCase(request.getOperator(), "like")) {
                String keyword = value.get(0).toUpperCase();
                whereValue = "'%" + keyword + "%'";
                whereName = "upper(" + whereName + ")";
            } else if (StringUtils.containsIgnoreCase(request.getOperator(), "between")) {
                if (request.getDatasetTableField().getDeType() == 1) {
                    if (request.getDatasetTableField().getDeExtractType() == 1) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String startTime = simpleDateFormat.format(new Date(Long.parseLong(value.get(0)) - 1000));
                        String endTime = simpleDateFormat.format(new Date(Long.parseLong(value.get(1))));
                        whereValue = String.format(MySQLConstants.WHERE_BETWEEN, startTime, endTime);
                    } else {
                        whereValue = String.format(MySQLConstants.WHERE_BETWEEN, value.get(0), value.get(1));
                    }
                } else {
                    whereValue = String.format(MySQLConstants.WHERE_BETWEEN, value.get(0), value.get(1));
                }
            } else {
                whereValue = String.format(MySQLConstants.WHERE_VALUE_VALUE, value.get(0));
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

    public String sqlFix(String sql) {
        sql = sql.trim();
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
            return "%Y-%m-%d %H:%i:%S";
        }

        switch (dateStyle) {
            case "y":
                return "%Y";
            case "y_Q":
                return "CONCAT(%s,'" + split + "','Q',%s)";
            case "y_M":
                return "%Y" + split + "%m";
            case "y_W":
                return "%Y" + split + "W%u";
            case "y_M_d":
                return "%Y" + split + "%m" + split + "%d";
            case "H_m_s":
                return "%H:%i:%S";
            case "y_M_d_H":
                return "%Y" + split + "%m" + split + "%d" + " %H";
            case "y_M_d_H_m":
                return "%Y" + split + "%m" + split + "%d" + " %H:%i";
            case "y_M_d_H_m_s":
                return "%Y" + split + "%m" + split + "%d" + " %H:%i:%S";
            default:
                return "%Y-%m-%d %H:%i:%S";
        }
    }

    private SQLObj getXFields(ChartViewFieldDTO x, String originField, String fieldAlias) {
        String fieldName = "";
        if (x.getDeExtractType() == 1) {
            if (x.getDeType() == 2 || x.getDeType() == 3) {
                fieldName = String.format(MySQLConstants.UNIX_TIMESTAMP, originField) + "*1000";
            } else if (x.getDeType() == 1) {
                String format = transDateFormat(x.getDateStyle(), x.getDatePattern());
                if (x.getType().equalsIgnoreCase("YEAR")) {
                    if (StringUtils.equalsIgnoreCase(x.getDateStyle(), "y_Q")) {
                        fieldName = String.format(format,
                                String.format(MySQLConstants.DATE_FORMAT, "CONCAT(" + originField + ",'-01-01')", "%Y"),
                                String.format(MySQLConstants.QUARTER, "CONCAT(" + originField + ",'-01-01')"));
                    } else {
                        fieldName = String.format(MySQLConstants.DATE_FORMAT, "CONCAT(" + originField + ",'-01-01')", format);
                    }
                } else if (x.getType().equalsIgnoreCase("TIME")) {
                    fieldName = String.format(MySQLConstants.DATE_FORMAT, "CONCAT('1970-01-01', " + originField + ")", MySQLConstants.DEFAULT_DATE_FORMAT);
                } else {
                    if (StringUtils.equalsIgnoreCase(x.getDateStyle(), "y_Q")) {
                        fieldName = String.format(format,
                                String.format(MySQLConstants.DATE_FORMAT, originField, "%Y"),
                                String.format(MySQLConstants.QUARTER, originField));
                    } else {
                        fieldName = String.format(MySQLConstants.DATE_FORMAT, originField, format);
                    }
                }
            } else {
                fieldName = originField;
            }
        } else {
            if (x.getDeType() == 1) {
                String format = transDateFormat(x.getDateStyle(), x.getDatePattern());
                if (x.getDeExtractType() == 0) {
                    if (StringUtils.equalsIgnoreCase(x.getDateStyle(), "y_Q")) {
                        fieldName = String.format(format,
                                String.format(MySQLConstants.DATE_FORMAT, String.format(MySQLConstants.STR_TO_DATE, originField, StringUtils.isNotEmpty(x.getDateFormat()) ? x.getDateFormat() : MySQLConstants.DEFAULT_DATE_FORMAT), "%Y"),
                                String.format(MySQLConstants.QUARTER, String.format(MySQLConstants.STR_TO_DATE, originField, StringUtils.isNotEmpty(x.getDateFormat()) ? x.getDateFormat() : MySQLConstants.DEFAULT_DATE_FORMAT)));
                    } else {
                        fieldName = String.format(MySQLConstants.DATE_FORMAT, String.format(MySQLConstants.STR_TO_DATE, originField, StringUtils.isNotEmpty(x.getDateFormat()) ? x.getDateFormat() : MySQLConstants.DEFAULT_DATE_FORMAT), format);
                    }
                } else {
                    String cast = String.format(MySQLConstants.CAST, originField, MySQLConstants.DEFAULT_INT_FORMAT) + "/1000";
                    String from_unixtime = String.format(MySQLConstants.FROM_UNIXTIME, cast, MySQLConstants.DEFAULT_DATE_FORMAT);
                    if (StringUtils.equalsIgnoreCase(x.getDateStyle(), "y_Q")) {
                        fieldName = String.format(format,
                                String.format(MySQLConstants.DATE_FORMAT, from_unixtime, "%Y"),
                                String.format(MySQLConstants.QUARTER, from_unixtime));
                    } else {
                        fieldName = String.format(MySQLConstants.DATE_FORMAT, from_unixtime, format);
                    }
                }
            } else {
                if (x.getDeType() == DeTypeConstants.DE_INT) {
                    fieldName = String.format(MySQLConstants.CAST, originField, MySQLConstants.DEFAULT_INT_FORMAT);
                } else if (x.getDeType() == DeTypeConstants.DE_FLOAT) {
                    fieldName = String.format(MySQLConstants.CAST, originField, MySQLConstants.DEFAULT_FLOAT_FORMAT);
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

    private List<SQLObj> getXWheres(ChartViewFieldDTO x, String originField, String fieldAlias) {
        List<SQLObj> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(x.getFilter()) && x.getFilter().size() > 0) {
            x.getFilter().forEach(f -> {
                String whereName = "";
                String whereTerm = transMysqlFilterTerm(f.getTerm());
                String whereValue = "";
                if (x.getDeType() == 1 && x.getDeExtractType() != 1) {
                    String cast = String.format(MySQLConstants.CAST, originField, MySQLConstants.DEFAULT_INT_FORMAT) + "/1000";
                    whereName = String.format(MySQLConstants.FROM_UNIXTIME, cast, MySQLConstants.DEFAULT_DATE_FORMAT);
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
                    whereValue = String.format(MySQLConstants.WHERE_VALUE_VALUE, f.getValue());
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
            fieldName = MySQLConstants.AGG_COUNT;
        } else if (SQLConstants.DIMENSION_TYPE.contains(y.getDeType())) {
            if (StringUtils.equalsIgnoreCase(y.getSummary(), "count_distinct")) {
                fieldName = String.format(MySQLConstants.AGG_FIELD, "COUNT", "DISTINCT " + originField);
            } else if (StringUtils.equalsIgnoreCase(y.getSummary(), "group_concat")) {
                fieldName = String.format(MySQLConstants.GROUP_CONCAT, originField);
            } else {
                fieldName = String.format(MySQLConstants.AGG_FIELD, y.getSummary(), originField);
            }
        } else {
            if (StringUtils.equalsIgnoreCase(y.getSummary(), "avg") || StringUtils.containsIgnoreCase(y.getSummary(), "pop")) {
                String cast = String.format(MySQLConstants.CAST, originField, y.getDeType() == 2 ? MySQLConstants.DEFAULT_INT_FORMAT : MySQLConstants.DEFAULT_FLOAT_FORMAT);
                String agg = String.format(MySQLConstants.AGG_FIELD, y.getSummary(), cast);
                fieldName = String.format(MySQLConstants.CAST, agg, MySQLConstants.DEFAULT_FLOAT_FORMAT);
            } else {
                String cast = String.format(MySQLConstants.CAST, originField, y.getDeType() == 2 ? MySQLConstants.DEFAULT_INT_FORMAT : MySQLConstants.DEFAULT_FLOAT_FORMAT);
                if (StringUtils.equalsIgnoreCase(y.getSummary(), "count_distinct")) {
                    fieldName = String.format(MySQLConstants.AGG_FIELD, "COUNT", "DISTINCT " + cast);
                } else {
                    fieldName = String.format(MySQLConstants.AGG_FIELD, y.getSummary(), cast);
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
                    whereValue = "('" + StringUtils.join(f.getValue(), "','") + "')";
                } else if (StringUtils.containsIgnoreCase(f.getTerm(), "like")) {
                    whereValue = "'%" + f.getValue() + "%'";
                } else {
                    whereValue = String.format(MySQLConstants.WHERE_VALUE_VALUE, f.getValue());
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
                                String.format(MySQLConstants.KEYWORD_FIX, tableObj.getTableAlias(), ele.getOriginName()));
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

    private String sqlLimit(String sql, ChartViewWithBLOBs view) {
        if (StringUtils.equalsIgnoreCase(view.getResultMode(), "custom")) {
            return sql + " LIMIT 0," + view.getResultCount();
        } else {
            return sql;
        }
    }

    public String sqlForPreview(String table, Datasource ds) {
        return "SELECT * FROM " + String.format(MySQLConstants.KEYWORD_TABLE, table);
    }

    public List<Dateformat> dateformat() {
        return JSONArray.parseArray("[\n" +
                "{\"dateformat\": \"%Y-%m-%d\"},\n" +
                "{\"dateformat\": \"%Y/%m/%d\"},\n" +
                "{\"dateformat\": \"%Y%m%d\"},\n" +
                "{\"dateformat\": \"%Y-%m-%d %H:%i:%S\"},\n" +
                "{\"dateformat\": \"%Y/%m/%d %H:%i:%S\"},\n" +
                "{\"dateformat\": \"%Y%m%d %H:%i:%S\"}\n" +
                "]", Dateformat.class);
    }

}

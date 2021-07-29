//package io.dataease.provider.sqlserver;
//
//import io.dataease.base.domain.DatasetTableField;
//import io.dataease.controller.request.chart.ChartExtFilterRequest;
//import io.dataease.dto.chart.ChartCustomFilterDTO;
//import io.dataease.dto.chart.ChartViewFieldDTO;
//import io.dataease.provider.QueryProvider;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.lang3.ObjectUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.stereotype.Service;
//
//import java.text.MessageFormat;
//import java.text.SimpleDateFormat;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//
///**
// * @Author gin
// * @Date 2021/5/17 2:43 下午
// */
//@Service("sqlServerQuery")
//public class SqlserverQueryProvider extends QueryProvider {
//    @Override
//    public Integer transFieldType(String field) {
//        switch (field) {
//            case "CHAR":
//            case "VARCHAR":
//            case "TEXT":
//            case "TINYTEXT":
//            case "MEDIUMTEXT":
//            case "LONGTEXT":
//            case "ENUM":
//                return 0;// 文本
//            case "DATE":
//            case "TIME":
//            case "YEAR":
//            case "DATETIME":
//            case "TIMESTAMP":
//                return 1;// 时间
//            case "INT":
//            case "SMALLINT":
//            case "MEDIUMINT":
//            case "INTEGER":
//            case "BIGINT":
//                return 2;// 整型
//            case "FLOAT":
//            case "DOUBLE":
//            case "DECIMAL":
//                return 3;// 浮点
//            case "BIT":
//            case "TINYINT":
//                return 4;// 布尔
//            default:
//                return 0;
//        }
//    }
//
//    @Override
//    public String createQueryCountSQL(String table) {
//        return MessageFormat.format("SELECT COUNT(*) FROM {0}", table);
//    }
//
//    @Override
//    public String createQueryCountSQLAsTmp(String sql) {
//        return createQueryCountSQL(" (" + sqlFix(sql) + ") AS tmp ");
//    }
//
//    @Override
//    public String createSQLPreview(String sql, String orderBy) {
//        return "SELECT * FROM (" + sqlFix(sql) + ") AS tmp ORDER BY null " + " LIMIT 0,1000";
//    }
//
//    @Override
//    public String createQuerySQL(String table, List<DatasetTableField> fields) {
//        String[] array = fields.stream().map(f -> {
//            StringBuilder stringBuilder = new StringBuilder();
//            // 如果原始类型为时间
//            if (f.getDeExtractType() == 1) {
//                if (f.getDeType() == 2 || f.getDeType() == 3) {
//                    stringBuilder.append("UNIX_TIMESTAMP(`").append(f.getOriginName()).append("`)*1000 AS ").append(f.getDataeaseName());
//                } else {
//                    stringBuilder.append("`").append(f.getOriginName()).append("` AS ").append(f.getDataeaseName());
//                }
//            } else if (f.getDeExtractType() == 0) {
//                if (f.getDeType() == 2) {
//                    stringBuilder.append("CAST(`").append(f.getOriginName()).append("` AS DECIMAL(20,0)) AS ").append(f.getDataeaseName());
//                } else if (f.getDeType() == 3) {
//                    stringBuilder.append("CAST(`").append(f.getOriginName()).append("` AS DECIMAL(20,2)) AS ").append(f.getDataeaseName());
//                } else if (f.getDeType() == 1) {
//                    stringBuilder.append("DATE_FORMAT(`").append(f.getOriginName()).append("`,'%Y-%m-%d %H:%i:%S') AS _").append(f.getDataeaseName());
//                } else {
//                    stringBuilder.append("`").append(f.getOriginName()).append("` AS ").append(f.getDataeaseName());
//                }
//            } else {
//                if (f.getDeType() == 1) {
//                    stringBuilder.append("FROM_UNIXTIME(CAST(`").append(f.getOriginName()).append("` AS DECIMAL(20,0))/1000,'%Y-%m-%d %H:%i:%S') AS ").append(f.getDataeaseName());
//                } else {
//                    stringBuilder.append("`").append(f.getOriginName()).append("` AS ").append(f.getDataeaseName());
//                }
//            }
//            return stringBuilder.toString();
//        }).toArray(String[]::new);
//        return MessageFormat.format("SELECT {0} FROM {1} ORDER BY null", StringUtils.join(array, ","), table);
//    }
//
//    @Override
//    public String createQuerySQLAsTmp(String sql, List<DatasetTableField> fields) {
//        return createQuerySQL(" (" + sqlFix(sql) + ") AS tmp ", fields);
//    }
//
//    @Override
//    public String createQuerySQLWithPage(String table, List<DatasetTableField> fields, Integer page, Integer pageSize, Integer realSize) {
//        return createQuerySQL(table, fields) + " LIMIT " + (page - 1) * pageSize + "," + realSize;
//    }
//
//    @Override
//    public String createQueryTableWithLimit(String table, List<DatasetTableField> fields, Integer limit) {
//        return createQuerySQL(table, fields) + " LIMIT 0," + limit;
//    }
//
//    @Override
//    public String createQuerySqlWithLimit(String sql, List<DatasetTableField> fields, Integer limit) {
//        return createQuerySQLAsTmp(sql, fields) + " LIMIT 0," + limit;
//    }
//
//    @Override
//    public String createQuerySQLAsTmpWithPage(String sql, List<DatasetTableField> fields, Integer page, Integer pageSize, Integer realSize) {
//        return createQuerySQLAsTmp(sql, fields) + " LIMIT " + (page - 1) * pageSize + "," + realSize;
//    }
//
//    @Override
//    public String getSQL(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartCustomFilterDTO> customFilter, List<ChartExtFilterRequest> extFilterRequestList) {
//        // 字段汇总 排序等
//        String[] field = yAxis.stream().map(y -> {
//            StringBuilder f = new StringBuilder();
//            if (StringUtils.equalsIgnoreCase(y.getOriginName(), "*")) {
//                f.append(y.getSummary()).append("(").append(y.getOriginName()).append(")");
//            } else {
//                if (StringUtils.equalsIgnoreCase(y.getSummary(), "avg") || StringUtils.containsIgnoreCase(y.getSummary(), "pop")) {
//                    f.append("CAST(")
//                            .append(y.getSummary()).append("(")
//                            .append("CAST(`").append(y.getOriginName()).append("` AS ").append(y.getDeType() == 2 ? "DECIMAL(20,0)" : "DECIMAL(20,2)").append(")")
//                            .append(") AS DECIMAL(20,2)").append(")");
//                } else {
//                    f.append(y.getSummary()).append("(")
//                            .append("CAST(`").append(y.getOriginName()).append("` AS ").append(y.getDeType() == 2 ? "DECIMAL(20,0)" : "DECIMAL(20,2)").append(")")
//                            .append(")");
//                }
//            }
//            f.append(" AS `_").append(y.getSummary()).append("_").append(StringUtils.equalsIgnoreCase(y.getOriginName(), "*") ? "" : y.getOriginName()).append("`");
//            return f.toString();
//        }).toArray(String[]::new);
//        String[] groupField = xAxis.stream().map(x -> {
//            StringBuilder stringBuilder = new StringBuilder();
//            // 如果原始类型为时间
//            if (x.getDeExtractType() == 1) {
//                if (x.getDeType() == 2 || x.getDeType() == 3) {
//                    stringBuilder.append("UNIX_TIMESTAMP(`").append(x.getOriginName()).append("`)*1000 AS `_").append(x.getOriginName()).append("`");
//                } else if (x.getDeType() == 1) {
//                    String format = transDateFormat(x.getDateStyle(), x.getDatePattern());
//                    stringBuilder.append("DATE_FORMAT(`").append(x.getOriginName()).append("`,'").append(format).append("') AS `_").append(x.getOriginName()).append("`");
//                } else {
//                    stringBuilder.append("`").append(x.getOriginName()).append("` AS `_").append(x.getOriginName()).append("`");
//                }
//            } else {
//                if (x.getDeType() == 1) {
//                    String format = transDateFormat(x.getDateStyle(), x.getDatePattern());
//                    if (x.getDeExtractType() == 0) {
//                        stringBuilder.append("DATE_FORMAT(`").append(x.getOriginName()).append("`,'").append(format).append("') AS `_").append(x.getOriginName()).append("`");
//                    } else {
//                        stringBuilder.append("DATE_FORMAT(").append("FROM_UNIXTIME(CAST(`").append(x.getOriginName()).append("` AS DECIMAL(20,0))/1000,'%Y-%m-%d %H:%i:%S')").append(",'").append(format).append("') AS `_").append(x.getOriginName()).append("`");
//                    }
//                } else {
//                    stringBuilder.append("`").append(x.getOriginName()).append("` AS `_").append(x.getOriginName()).append("`");
//                }
//            }
//            return stringBuilder.toString();
//        }).toArray(String[]::new);
//        String[] group = xAxis.stream().map(x -> "`_" + x.getOriginName() + "`").toArray(String[]::new);
//        String[] xOrder = xAxis.stream().filter(f -> StringUtils.isNotEmpty(f.getSort()) && !StringUtils.equalsIgnoreCase(f.getSort(), "none"))
//                .map(f -> "`_" + f.getOriginName() + "` " + f.getSort()).toArray(String[]::new);
//        String[] yOrder = yAxis.stream().filter(f -> StringUtils.isNotEmpty(f.getSort()) && !StringUtils.equalsIgnoreCase(f.getSort(), "none"))
//                .map(f -> "`_" + f.getSummary() + "_" + (StringUtils.equalsIgnoreCase(f.getOriginName(), "*") ? "" : f.getOriginName()) + "` " + f.getSort()).toArray(String[]::new);
//        String[] order = Arrays.copyOf(xOrder, xOrder.length + yOrder.length);
//        System.arraycopy(yOrder, 0, order, xOrder.length, yOrder.length);
//
//        String[] xFilter = xAxis.stream().filter(x -> CollectionUtils.isNotEmpty(x.getFilter()) && x.getFilter().size() > 0)
//                .map(x -> {
//                    String[] s = x.getFilter().stream().map(f -> {
//                        StringBuilder filter = new StringBuilder();
//                        if (x.getDeType() == 1 && x.getDeExtractType() != 1) {
//                            filter.append(" AND FROM_UNIXTIME(cast(`")
//                                    .append(x.getOriginName())
//                                    .append("` AS DECIMAL(20,0))/1000,'%Y-%m-%d %H:%i:%S') ");
//                        } else {
//                            filter.append(" AND `").append(x.getOriginName()).append("`");
//                        }
//                        filter.append(transMysqlFilterTerm(f.getTerm()));
//                        if (StringUtils.equalsIgnoreCase(f.getTerm(), "null")) {
//                            filter.append("(null,'')");
//                        } else if (StringUtils.equalsIgnoreCase(f.getTerm(), "not_null")) {
//                            filter.append(" AND `").append(x.getOriginName()).append("`").append(" <> ''");
//                        } else if (StringUtils.containsIgnoreCase(f.getTerm(), "in")) {
//                            filter.append("('").append(StringUtils.join(f.getValue(), "','")).append("')");
//                        } else if (StringUtils.containsIgnoreCase(f.getTerm(), "like")) {
//                            filter.append("%").append(f.getValue()).append("%");
//                        } else {
//                            filter.append("'").append(f.getValue()).append("'");
//                        }
//                        return filter.toString();
//                    }).toArray(String[]::new);
//                    return StringUtils.join(s, " ");
//                }).toArray(String[]::new);
//
//        String sql = MessageFormat.format("SELECT {0},{1} FROM {2} WHERE 1=1 {3} GROUP BY {4} ORDER BY null,{5}",
//                StringUtils.join(groupField, ","),
//                StringUtils.join(field, ","),
//                table,
//                (xFilter.length > 0 ? StringUtils.join(xFilter, " ") : "") + transCustomFilter(customFilter) + transExtFilter(extFilterRequestList),// origin field filter and panel field filter
//                StringUtils.join(group, ","),
//                StringUtils.join(order, ","));
//        if (sql.endsWith(",")) {
//            sql = sql.substring(0, sql.length() - 1);
//        }
//        // 如果是对结果字段过滤，则再包裹一层sql
//        String[] resultFilter = yAxis.stream().filter(y -> CollectionUtils.isNotEmpty(y.getFilter()) && y.getFilter().size() > 0)
//                .map(y -> {
//                    String[] s = y.getFilter().stream().map(f -> {
//                        StringBuilder filter = new StringBuilder();
//                        // 原始类型不是时间，在de中被转成时间的字段做处理
//                        if (y.getDeType() == 1 && y.getDeExtractType() != 1) {
//                            filter.append(" AND FROM_UNIXTIME(CAST(`_")
//                                    .append(y.getSummary()).append("_").append(StringUtils.equalsIgnoreCase(y.getOriginName(), "*") ? "" : y.getOriginName()).append("`")
//                                    .append(" AS DECIMAL(20,0))/1000,'%Y-%m-%d %H:%i:%S') ");
//                        } else {
//                            filter.append(" AND `_").append(y.getSummary()).append("_").append(StringUtils.equalsIgnoreCase(y.getOriginName(), "*") ? "" : y.getOriginName()).append("`");
//                        }
//                        filter.append(transMysqlFilterTerm(f.getTerm()));
//                        if (StringUtils.equalsIgnoreCase(f.getTerm(), "null")) {
//                            filter.append("(null,'')");
//                        } else if (StringUtils.equalsIgnoreCase(f.getTerm(), "not_null")) {
//                            filter.append(" AND `_")
//                                    .append(y.getSummary()).append("_").append(StringUtils.equalsIgnoreCase(y.getOriginName(), "*") ? "" : y.getOriginName())
//                                    .append("`").append(" <> ''");
//                        } else if (StringUtils.containsIgnoreCase(f.getTerm(), "in")) {
//                            filter.append("('").append(StringUtils.join(f.getValue(), "','")).append("')");
//                        } else if (StringUtils.containsIgnoreCase(f.getTerm(), "like")) {
//                            filter.append("%").append(f.getValue()).append("%");
//                        } else {
//                            filter.append("'").append(f.getValue()).append("'");
//                        }
//                        return filter.toString();
//                    }).toArray(String[]::new);
//                    return StringUtils.join(s, " ");
//                }).toArray(String[]::new);
//        if (resultFilter.length == 0) {
//            return sql;
//        } else {
//            String filterSql = MessageFormat.format("SELECT * FROM {0} WHERE 1=1 {1} ORDER BY {2}",
//                    "(" + sql + ") AS tmp",
//                    StringUtils.join(resultFilter, " "),
//                    ObjectUtils.isNotEmpty(yOrder) ? StringUtils.join(yOrder, ",") : "null");
//            return filterSql;
//        }
//    }
//
//    @Override
//    public String getSQLAsTmp(String sql, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartCustomFilterDTO> customFilter, List<ChartExtFilterRequest> extFilterRequestList) {
//        return getSQL(" (" + sqlFix(sql) + ") AS tmp ", xAxis, yAxis, customFilter, extFilterRequestList);
//    }
//
//    @Override
//    public String getSQLStack(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartCustomFilterDTO> customFilter, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack) {
//        return null;
//    }
//
//    @Override
//    public String getSQLAsTmpStack(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartCustomFilterDTO> customFilter, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack) {
//        return null;
//    }
//
//    @Override
//    public String searchTable(String table) {
//        return "SELECT table_name FROM information_schema.TABLES WHERE table_name ='" + table + "'";
//    }
//
//    @Override
//    public String getSQLSummary(String table, List<ChartViewFieldDTO> yAxis, List<ChartCustomFilterDTO> customFilter, List<ChartExtFilterRequest> extFilterRequestList) {
//        // 字段汇总 排序等
//        String[] field = yAxis.stream().map(y -> {
//            StringBuilder f = new StringBuilder();
//            if (StringUtils.equalsIgnoreCase(y.getOriginName(), "*")) {
//                f.append(y.getSummary()).append("(").append(y.getOriginName()).append(")");
//            } else {
//                if (StringUtils.equalsIgnoreCase(y.getSummary(), "avg") || StringUtils.containsIgnoreCase(y.getSummary(), "pop")) {
//                    f.append("CAST(")
//                            .append(y.getSummary()).append("(")
//                            .append("CAST(`").append(y.getOriginName()).append("` AS ").append(y.getDeType() == 2 ? "DECIMAL(20,0)" : "DECIMAL(20,2)").append(")")
//                            .append(") AS DECIMAL(20,2)").append(")");
//                } else {
//                    f.append(y.getSummary()).append("(")
//                            .append("CAST(`").append(y.getOriginName()).append("` AS ").append(y.getDeType() == 2 ? "DECIMAL(20,0)" : "DECIMAL(20,2)").append(")")
//                            .append(")");
//                }
//            }
//            f.append(" AS `_").append(y.getSummary()).append("_").append(StringUtils.equalsIgnoreCase(y.getOriginName(), "*") ? "" : y.getOriginName()).append("`");
//            return f.toString();
//        }).toArray(String[]::new);
//
//        String[] order = yAxis.stream().filter(f -> StringUtils.isNotEmpty(f.getSort()) && !StringUtils.equalsIgnoreCase(f.getSort(), "none"))
//                .map(f -> "`_" + f.getSummary() + "_" + (StringUtils.equalsIgnoreCase(f.getOriginName(), "*") ? "" : f.getOriginName()) + "` " + f.getSort()).toArray(String[]::new);
//
//        String sql = MessageFormat.format("SELECT {0} FROM {1} WHERE 1=1 {2} ORDER BY null,{3}",
//                StringUtils.join(field, ","),
//                table,
//                transCustomFilter(customFilter) + transExtFilter(extFilterRequestList),// origin field filter and panel field filter
//                StringUtils.join(order, ","));
//        if (sql.endsWith(",")) {
//            sql = sql.substring(0, sql.length() - 1);
//        }
//        // 如果是对结果字段过滤，则再包裹一层sql
//        String[] resultFilter = yAxis.stream().filter(y -> CollectionUtils.isNotEmpty(y.getFilter()) && y.getFilter().size() > 0)
//                .map(y -> {
//                    String[] s = y.getFilter().stream().map(f -> {
//                        StringBuilder filter = new StringBuilder();
//                        // 原始类型不是时间，在de中被转成时间的字段做处理
//                        if (y.getDeType() == 1 && y.getDeExtractType() != 1) {
//                            filter.append(" AND FROM_UNIXTIME(CAST(`_")
//                                    .append(y.getSummary()).append("_").append(StringUtils.equalsIgnoreCase(y.getOriginName(), "*") ? "" : y.getOriginName()).append("`")
//                                    .append(" AS DECIMAL(20,0))/1000,'%Y-%m-%d %H:%i:%S') ");
//                        } else {
//                            filter.append(" AND `_").append(y.getSummary()).append("_").append(StringUtils.equalsIgnoreCase(y.getOriginName(), "*") ? "" : y.getOriginName()).append("`");
//                        }
//                        filter.append(transMysqlFilterTerm(f.getTerm()));
//                        if (StringUtils.equalsIgnoreCase(f.getTerm(), "null")) {
//                            filter.append("(null,'')");
//                        } else if (StringUtils.equalsIgnoreCase(f.getTerm(), "not_null")) {
//                            filter.append(" AND `_")
//                                    .append(y.getSummary()).append("_").append(StringUtils.equalsIgnoreCase(y.getOriginName(), "*") ? "" : y.getOriginName())
//                                    .append("`").append(" <> ''");
//                        } else if (StringUtils.containsIgnoreCase(f.getTerm(), "in")) {
//                            filter.append("('").append(StringUtils.join(f.getValue(), "','")).append("')");
//                        } else if (StringUtils.containsIgnoreCase(f.getTerm(), "like")) {
//                            filter.append("%").append(f.getValue()).append("%");
//                        } else {
//                            filter.append("'").append(f.getValue()).append("'");
//                        }
//                        return filter.toString();
//                    }).toArray(String[]::new);
//                    return StringUtils.join(s, " ");
//                }).toArray(String[]::new);
//        if (resultFilter.length == 0) {
//            return sql;
//        } else {
//            String filterSql = MessageFormat.format("SELECT * FROM {0} WHERE 1=1 {1} ORDER BY {2}",
//                    "(" + sql + ") AS tmp",
//                    StringUtils.join(resultFilter, " "),
//                    ObjectUtils.isNotEmpty(order) ? StringUtils.join(order, ",") : "null");
//            return filterSql;
//        }
//    }
//
//    @Override
//    public String getSQLSummaryAsTmp(String sql, List<ChartViewFieldDTO> yAxis, List<ChartCustomFilterDTO> customFilter, List<ChartExtFilterRequest> extFilterRequestList) {
//        return getSQLSummary(" (" + sqlFix(sql) + ") AS tmp ", yAxis, customFilter, extFilterRequestList);
//    }
//
//    @Override
//    public String wrapSql(String sql) {
//        sql = sql.trim();
//        if (sql.lastIndexOf(";") == (sql.length() - 1)) {
//            sql = sql.substring(0, sql.length() - 1);
//        }
//        String tmpSql = "SELECT * FROM (" + sql + ") AS tmp " + " LIMIT 0";
//        return tmpSql;
//    }
//
//    @Override
//    public String createRawQuerySQL(String table, List<DatasetTableField> fields) {
//        String[] array = fields.stream().map(f -> {
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append("`").append(f.getOriginName()).append("` AS ").append(f.getDataeaseName());
//            return stringBuilder.toString();
//        }).toArray(String[]::new);
//        return MessageFormat.format("SELECT {0} FROM {1} ORDER BY null", StringUtils.join(array, ","), table);
//    }
//
//    @Override
//    public String createRawQuerySQLAsTmp(String sql, List<DatasetTableField> fields) {
//        return createRawQuerySQL(" (" + sqlFix(sql) + ") AS tmp ", fields);
//    }
//
//    public String transMysqlFilterTerm(String term) {
//        switch (term) {
//            case "eq":
//                return " = ";
//            case "not_eq":
//                return " <> ";
//            case "lt":
//                return " < ";
//            case "le":
//                return " <= ";
//            case "gt":
//                return " > ";
//            case "ge":
//                return " >= ";
//            case "in":
//                return " IN ";
//            case "not in":
//                return " NOT IN ";
//            case "like":
//                return " LIKE ";
//            case "not like":
//                return " NOT LIKE ";
//            case "null":
//                return " IN ";
//            case "not_null":
//                return " IS NOT NULL ";
//            case "between":
//                return " BETWEEN ";
//            default:
//                return "";
//        }
//    }
//
//    public String transCustomFilter(List<ChartCustomFilterDTO> requestList) {
//        if (CollectionUtils.isEmpty(requestList)) {
//            return "";
//        }
//        StringBuilder filter = new StringBuilder();
//        for (ChartCustomFilterDTO request : requestList) {
//            String value = request.getValue();
//            DatasetTableField field = request.getField();
//            if (ObjectUtils.isEmpty(field)) {
//                continue;
//            }
//            if (field.getDeType() == 1 && field.getDeExtractType() != 1) {
//                filter.append(" AND FROM_UNIXTIME(CAST(`")
//                        .append(field.getOriginName())
//                        .append("` AS DECIMAL(20,0))/1000,'%Y-%m-%d %H:%i:%S') ");
//            } else {
//                filter.append(" AND `").append(field.getOriginName()).append("`");
//            }
//            filter.append(" ")
//                    .append(transMysqlFilterTerm(request.getTerm()))
//                    .append(" ");
//            if (StringUtils.equalsIgnoreCase(request.getTerm(), "null")) {
//                filter.append("(null,'')");
//            } else if (StringUtils.equalsIgnoreCase(request.getTerm(), "not_null")) {
//                filter.append(" AND `").append(field.getOriginName()).append("`").append(" <> ''");
//            } else if (StringUtils.containsIgnoreCase(request.getTerm(), "in")) {
//                filter.append("('").append(StringUtils.join(value, "','")).append("')");
//            } else if (StringUtils.containsIgnoreCase(request.getTerm(), "like")) {
//                filter.append("'%").append(value).append("%'");
//            } else {
//                filter.append("'").append(value).append("'");
//            }
//        }
//        return filter.toString();
//    }
//
//    public String transExtFilter(List<ChartExtFilterRequest> requestList) {
//        if (CollectionUtils.isEmpty(requestList)) {
//            return "";
//        }
//        StringBuilder filter = new StringBuilder();
//        for (ChartExtFilterRequest request : requestList) {
//            List<String> value = request.getValue();
//            if (CollectionUtils.isEmpty(value)) {
//                continue;
//            }
//            DatasetTableField field = request.getDatasetTableField();
//            if (field.getDeType() == 1 && field.getDeExtractType() != 1) {
//                filter.append(" AND FROM_UNIXTIME(CAST(`")
//                        .append(field.getOriginName())
//                        .append("` AS DECIMAL(20,0))/1000,'%Y-%m-%d %H:%i:%S') ");
//            } else {
//                filter.append(" AND `").append(field.getOriginName()).append("`");
//            }
//            filter.append(" ")
//                    .append(transMysqlFilterTerm(request.getOperator()))
//                    .append(" ");
//            if (StringUtils.containsIgnoreCase(request.getOperator(), "in")) {
//                filter.append("('").append(StringUtils.join(value, "','")).append("')");
//            } else if (StringUtils.containsIgnoreCase(request.getOperator(), "like")) {
//                filter.append("'%").append(value.get(0)).append("%'");
//            } else if (StringUtils.containsIgnoreCase(request.getOperator(), "between")) {
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                String startTime = simpleDateFormat.format(new Date(Long.parseLong(value.get(0))));
//                String endTime = simpleDateFormat.format(new Date(Long.parseLong(value.get(1))));
//                filter.append("'").append(startTime).append("' AND '").append(endTime).append("'");
//            } else {
//                filter.append("'").append(value.get(0)).append("'");
//            }
//        }
//        return filter.toString();
//    }
//
//    private String sqlFix(String sql) {
//        if (sql.lastIndexOf(";") == (sql.length() - 1)) {
//            sql = sql.substring(0, sql.length() - 1);
//        }
//        return sql;
//    }
//
//    private String transDateFormat(String dateStyle, String datePattern) {
//        String split = "-";
//        if (StringUtils.equalsIgnoreCase(datePattern, "date_sub")) {
//            split = "-";
//        } else if (StringUtils.equalsIgnoreCase(datePattern, "date_split")) {
//            split = "/";
//        }
//
//        switch (dateStyle) {
//            case "y":
//                return "%Y";
//            case "y_M":
//                return "%Y" + split + "%m";
//            case "y_M_d":
//                return "%Y" + split + "%m" + split + "%d";
//            case "H_m_s":
//                return "%H:%i:%S";
//            case "y_M_d_H_m":
//                return "%Y" + split + "%m" + split + "%d" + " %H:%i";
//            case "y_M_d_H_m_s":
//                return "%Y" + split + "%m" + split + "%d" + " %H:%i:%S";
//            default:
//                return "%Y-%m-%d %H:%i:%S";
//        }
//    }
//}

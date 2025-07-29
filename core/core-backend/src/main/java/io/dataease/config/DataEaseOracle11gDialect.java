package io.dataease.config;

import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.spi.Limit;
import org.hibernate.query.sqm.FetchClauseType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.SqlAstTranslatorFactory;
import org.hibernate.sql.ast.spi.AbstractSqlAstTranslator;
import org.hibernate.sql.ast.spi.StandardSqlAstTranslatorFactory;
import org.hibernate.sql.ast.tree.Statement;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.sql.exec.spi.JdbcOperation;

import java.util.*;

/**
 * Oracle 11g 以下 版本的方言实现，主要是分页不支持offset和fetch语法
 *
 * @author jianneng
 * @date 2025/7/4 16:40
 **/
public class DataEaseOracle11gDialect extends OracleDialect {

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return DataEaseOracle12cIdentityColumnSupport.INSTANCE;
    }

    /**
     * Oracle number 不允许带精度
     * 需要精度请在字段上使用 @Column(precision = 10, scale = 2)
     */
    @Override
    public String columnType(int sqlTypeCode) {
        if (sqlTypeCode == -5) {
            return "number";
        }
        return super.columnType(sqlTypeCode);
    }

    @Override
    public SqlAstTranslatorFactory getSqlAstTranslatorFactory() {
        return new StandardSqlAstTranslatorFactory() {
            @Override
            public <T extends JdbcOperation> SqlAstTranslator<T> buildTranslator(
                    SessionFactoryImplementor sessionFactory, Statement statement) {
                return new Oracle11gSqlAstTranslator<>(sessionFactory, statement);
            }
        };
    }

    public static class Oracle11gSqlAstTranslator<T extends JdbcOperation>
            extends AbstractSqlAstTranslator<T> {

        private final StringBuilder sqlBuffer = new StringBuilder();

        public Oracle11gSqlAstTranslator(
                SessionFactoryImplementor sessionFactory,
                Statement statement) {
            super(sessionFactory, statement);
        }

        @Override
        protected void renderOffsetFetchClause(Expression offsetExpression, Expression fetchExpression, FetchClauseType fetchClauseType, boolean renderOffsetRowsKeyword) {
            Limit limit = super.getLimit();
            Integer firstRow = limit.getFirstRow();
            Integer maxRows = limit.getMaxRows();
            if (firstRow != null && maxRows != null) {
                String originalSql = getSql();
                this.getSqlBuffer().setLength(0);
                String innerSql = originalSql.trim();
                String lowerSql = innerSql.toLowerCase();
                boolean hasDistinct = lowerSql.contains("select distinct");
                boolean hasGroupBy = lowerSql.contains("group by");
                int orderByIndex = lowerSql.lastIndexOf("order by");
                String orderByClause = "";
                if ((hasDistinct || hasGroupBy) && orderByIndex > 0) {
                    orderByClause = innerSql.substring(orderByIndex);
                    innerSql = innerSql.substring(0, orderByIndex);

                    Set<String> orderColumns = extractOrderByColumns(orderByClause);
                    innerSql = ensureSelectColumns(innerSql, orderColumns);
                    orderByClause = buildOrderByClause(innerSql, orderByClause);
                } else {
                    innerSql = ensureSelectColumns(innerSql, new HashSet<>());
                }
                sqlBuffer.append(buildPagingSql(innerSql, firstRow, maxRows, orderByClause));
                appendSql(sqlBuffer.toString());
            }
        }

        /**
         * 提取 order by 字段名，去除排序方式
         *
         * @param orderByClause 排序sql
         * @return 排序字段集合
         */
        private Set<String> extractOrderByColumns(String orderByClause) {
            String orderByFields = orderByClause.replaceAll("(?i)order\\s+by", "").trim();
            String[] fields = orderByFields.split(",");
            Set<String> orderColumns = new LinkedHashSet<>();
            for (String field : fields) {
                String col = field.trim();
                col = col.replaceAll("\\s+(asc|desc)", "").trim(); // 去排序方式
                orderColumns.add(col);
            }
            return orderColumns;
        }

        /**
         * 确保 select 列表中包含所有 order by 字段
         * 并且 全部 select 字段都加上别名
         *
         * @param innerSql     原始 SQL 语句
         * @param orderColumns 需要确保存在的排序字段集合
         * @return 补全后的 SQL 语句
         */
        private String ensureSelectColumns(String innerSql, Set<String> orderColumns) {
            String lowerSql = innerSql.toLowerCase();
            int selectIdx = lowerSql.indexOf("select") + 6;
            int fromIdx = lowerSql.indexOf("from");
            StringBuilder selectList = new StringBuilder(innerSql.substring(selectIdx, fromIdx).trim());
            // 将selectList按逗号分割成数组
            String[] selectFields = selectList.toString().split(",");
            // 补全 order by 字段到select中
            for (String col : orderColumns) {
                // 如果已经存在，跳过
                if (Arrays.stream(selectFields).toList().contains(col)) {
                    continue;
                }
                selectList.append(",").append(col);
            }
            // 处理 select 字段，按需加别名
            List<String> aliasedColumns = buildAliasedSelectColumns(selectList.toString());
            // 如果 select 中有 distinct，保留
            String distinct = innerSql.contains("distinct") ? " distinct " : " ";
            String newSelectList = String.join(", ", aliasedColumns);
            return innerSql.substring(0, selectIdx) + distinct + newSelectList + " " + innerSql.substring(fromIdx);
        }

        /**
         * 构建带别名的 select 列表
         * 出现重复字段时，添加别名
         *
         * @param selectList 原始 select 列表
         * @return 带别名的 select 列表
         */
        private List<String> buildAliasedSelectColumns(String selectList) {
            String[] columns = Arrays.stream(selectList.replaceAll("(?i)distinct", "").split(",")).filter(s -> !s.isEmpty()).toArray(String[]::new);
            Map<String, Integer> fieldCount = new HashMap<>();
            List<String> result = new ArrayList<>();

            // 去掉表名，统计字段出现次数
            for (String col : columns) {
                String trimmed = col.trim();
                String base = trimmed.replaceAll("(?i)\\s+as\\s+.+", "");
                base = base.replaceAll("^\\w+\\.", "");
                base = base.replaceAll("^\"|\"", "");
                fieldCount.put(base, fieldCount.getOrDefault(base, 0) + 1);
            }

            for (String col : columns) {
                String trimmed = col.trim();
                // 已有 as，跳过
                if (trimmed.matches(".*\\s+as\\s+.+")) {
                    result.add(trimmed);
                    continue;
                }
                String base = trimmed.replaceAll("^\\w+\\.", "").replaceAll("^\"|\"", "");
                // 没有重复，不加别名
                if (fieldCount.getOrDefault(base, 0) == 1) {
                    result.add(trimmed);
                } else {
                    result.add(trimmed + " as " + trimmed.replaceAll("^\"|\"", "").replaceAll("\\.", "_"));
                }
            }
            return result;
        }

        /**
         * 构建去除表别名的 order by 子句
         *
         * @param orderByClause 原始 order by 子句
         * @return 去除表别名后的 order by 子句
         */
        private String buildOrderByClause(String innerSql, String orderByClause) {
            // 获取 select 列表中的字段名和别名
            String lowerSql = innerSql.toLowerCase();
            int selectIdx = lowerSql.indexOf("select") + 6;
            int fromIdx = lowerSql.indexOf("from");
            String selectList = innerSql.substring(selectIdx, fromIdx).trim();
            // 将selectList按逗号分割成数组
            String[] selectFields = selectList.split(",");
            // 创建一个映射，存储字段名和别名
            Map<String, String> aliasMap = new HashMap<>();
            for (String f : selectFields) {
                String[] arr = f.trim().replaceAll("distinct", "").split("(?i)\\s+as\\s+");
                String col = arr[0].trim();
                if (arr.length > 1) aliasMap.put(col, arr[1].trim());
            }
            // 去除 order by 字段中的表别名
            String orderByFields = orderByClause.replaceAll("(?i)order\\s+by", "").trim();
            String[] fields = orderByFields.split(",");
            List<String> cleanFields = new ArrayList<>();
            for (String field : fields) {
                String f = field.trim();
                // 去除排序方式
                String col = f.replaceAll("\\s+(asc|desc)", "");
                // 获取排序方式
                String suffix = f.replaceAll(".*?(asc|desc)?$", "$1").trim();
                // 如果有别名，使用别名，否则使用去表名的原字段名
                if (aliasMap.containsKey(col)) {
                    cleanFields.add(aliasMap.get(col) + (suffix.isEmpty() ? "" : " " + suffix));
                } else {
                    cleanFields.add(field.trim().replaceAll("\\b\\w+\\.", ""));
                }
            }
            return "ORDER BY " + String.join(", ", cleanFields);
        }

        /**
         * 构建分页 SQL 语句
         *
         * @param innerSql      原始 SQL 语句
         * @param firstRow      起始行号
         * @param maxRows       每页最大行数
         * @param orderByClause 排序子句
         * @return 分页后的 SQL 语句
         */
        private String buildPagingSql(String innerSql, int firstRow, int maxRows, String orderByClause) {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM ( SELECT a.*, ROWNUM rnum FROM ( ");
            sb.append(innerSql);
            sb.append(" ) a WHERE ROWNUM <= ");
            sb.append(firstRow + maxRows);
            sb.append(" ) WHERE rnum > ");
            sb.append(firstRow);
            if (orderByClause != null && !orderByClause.isEmpty()) {
                sb.append(" ");
                sb.append(orderByClause);
            }
            return sb.toString();
        }
    }
}

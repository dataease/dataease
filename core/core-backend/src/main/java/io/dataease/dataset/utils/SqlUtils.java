package io.dataease.dataset.utils;

import com.google.common.collect.ImmutableList;
import io.dataease.api.dataset.union.model.SQLMeta;
import io.dataease.api.ds.vo.DatasourceConfiguration;
import io.dataease.dataset.dto.DatasourceSchemaDTO;
import io.dataease.engine.constant.SqlPlaceholderConstants;
import io.dataease.exception.DEException;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.dialect.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.calcite.sql.SqlKind.*;

/**
 * @Author Junjun
 */
public class SqlUtils {
    public static String addSchema(String sql, String schema) {
        if (sql.trim().endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }

        SqlParser.Config config =
                SqlParser.config()
                        .withLex(Lex.JAVA)
                        .withIdentifierMaxLength(256);
        // 创建解析器
        SqlParser sqlParser = SqlParser
                .create(sql, config);
        // 生成 AST 语法树
        SqlNode sqlNode = null;
        try {
            sqlNode = sqlParser.parseStmt();
            addTableSchema(sqlNode, false, schema, config);
        } catch (SqlParseException e) {
            DEException.throwException("使用 Calcite 进行语法分析发生了异常:" + e);
        }
        String sqlRender = sqlNode.toString();
        // 处理sql中多余的`都替换成1个
        sqlRender = sqlRender.replaceAll("(`+)", "`");
        return sqlRender.replaceAll("`", "");
    }

    private static void addTableSchema(SqlNode sqlNode, Boolean fromOrJoin, String schema, SqlParser.Config config) {
        try {
            if (sqlNode.getKind() == JOIN) {
                SqlJoin sqlKind = (SqlJoin) sqlNode;
                addTableSchema(sqlKind.getLeft(), true, schema, config);
                addTableSchema(sqlKind.getRight(), true, schema, config);
            } else if (sqlNode.getKind() == IDENTIFIER) {
                if (fromOrJoin) {
                    // 获取表名
                    String tableName = sqlNode.toString();
                    SqlIdentifier sqlKind = (SqlIdentifier) sqlNode;
                    sqlKind.setNames(ImmutableList.of(schema + "`.`" + tableName), null);
                }
            } else if (sqlNode.getKind() == AS) {
                SqlBasicCall sqlKind = (SqlBasicCall) sqlNode;
                if (sqlKind.getOperandList().size() >= 2) {
                    addTableSchema(sqlKind.getOperandList().get(0), fromOrJoin, schema, config);
                }
            } else if (sqlNode.getKind() == SELECT) {
                SqlSelect sqlKind = (SqlSelect) sqlNode;

                // 解析from
                addTableSchema(sqlKind.getFrom(), true, schema, config);

                // 解析where
                SqlBasicCall where = (SqlBasicCall) sqlKind.getWhere();
                if (where != null && where.getOperandList().size() >= 2) {
                    for (int i = 0; i < where.getOperandList().size(); i++) {
                        addTableSchema(where.getOperandList().get(i), false, schema, config);
                    }
                }
            } else if (sqlNode.getKind() == UNION) {
                SqlBasicCall sqlKind = (SqlBasicCall) sqlNode;
                // 使用union，至少会有2个子SQL，否则语法不正确
                if (sqlKind.getOperandList().size() >= 2) {
                    for (int i = 0; i < sqlKind.getOperandList().size(); i++) {
                        addTableSchema(sqlKind.getOperandList().get(i), fromOrJoin, schema, config);
                    }
                }
            } else if (sqlNode.getKind() == ORDER_BY) {
                SqlOrderBy sqlKind = (SqlOrderBy) sqlNode;
                List<SqlNode> operandList = sqlKind.getOperandList();
                if (operandList.size() > 0) {
                    addTableSchema(operandList.get(0), fromOrJoin, schema, config);
                }
            } else if (sqlNode.getKind() == IN
                    || sqlNode.getKind() == NOT_IN
                    || sqlNode.getKind() == AND
                    || sqlNode.getKind() == OR
                    || sqlNode.getKind() == LESS_THAN
                    || sqlNode.getKind() == GREATER_THAN
                    || sqlNode.getKind() == LESS_THAN_OR_EQUAL
                    || sqlNode.getKind() == GREATER_THAN_OR_EQUAL
                    || sqlNode.getKind() == EQUALS
                    || sqlNode.getKind() == NOT_EQUALS) {
                SqlBasicCall where = (SqlBasicCall) sqlNode;
                if (where.getOperandList().size() >= 2) {
                    for (int i = 0; i < where.getOperandList().size(); i++) {
                        addTableSchema(where.getOperandList().get(i), fromOrJoin, schema, config);
                    }
                }
            }
        } catch (Exception e) {
            DEException.throwException("使用 Calcite 进行语法分析发生了异常:" + e);
        }
    }

    public static String rebuildSQL(String sql, SQLMeta sqlMeta, boolean crossDs, Map<Long, DatasourceSchemaDTO> dsMap) {
        if (crossDs) {
            return sql;
        }

        String s = transSqlDialect(sql, dsMap);
        String tableDialect = sqlMeta.getTableDialect();
        s = replaceTablePlaceHolder(s, tableDialect);
        return replaceCalcFieldPlaceHolder(s, sqlMeta);
    }

    public static String transSqlDialect(String sql, Map<Long, DatasourceSchemaDTO> dsMap) throws DEException {
        try {
            DatasourceSchemaDTO value = dsMap.entrySet().iterator().next().getValue();

            SqlParser parser = SqlParser.create(sql, SqlParser.Config.DEFAULT.withLex(Lex.JAVA));
            SqlNode sqlNode = parser.parseStmt();
            return sqlNode.toSqlString(getDialect(value)).toString();
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        return null;
    }

    public static String replaceTablePlaceHolder(String s, String placeholder) {
        s = s.replaceAll("\r\n", " ")
                .replaceAll("\n", " ")
                .replaceAll(SqlPlaceholderConstants.TABLE_PLACEHOLDER_REGEX, placeholder)
                .replaceAll("ASYMMETRIC", "")
                .replaceAll("SYMMETRIC", "");
        return s;
    }

    public static String replaceCalcFieldPlaceHolder(String s, SQLMeta sqlMeta) {
        Map<String, String> fieldsDialect = new HashMap<>();
        if (MapUtils.isNotEmpty(sqlMeta.getXFieldsDialect())) {
            fieldsDialect.putAll(sqlMeta.getXFieldsDialect());
        }
        if (MapUtils.isNotEmpty(sqlMeta.getYFieldsDialect())) {
            fieldsDialect.putAll(sqlMeta.getYFieldsDialect());
        }
        if (MapUtils.isNotEmpty(sqlMeta.getCustomWheresDialect())) {
            fieldsDialect.putAll(sqlMeta.getCustomWheresDialect());
        }
        if (MapUtils.isNotEmpty(sqlMeta.getExtWheresDialect())) {
            fieldsDialect.putAll(sqlMeta.getExtWheresDialect());
        }
        if (MapUtils.isNotEmpty(sqlMeta.getWhereTreesDialect())) {
            fieldsDialect.putAll(sqlMeta.getWhereTreesDialect());
        }

        if (MapUtils.isNotEmpty(fieldsDialect)) {
            for (Map.Entry<String, String> ele : fieldsDialect.entrySet()) {
                s = s.replaceAll(SqlPlaceholderConstants.KEYWORD_PREFIX_REGEX + ele.getKey() + SqlPlaceholderConstants.KEYWORD_SUFFIX_REGEX, ele.getValue());
            }
        }
        return s;
    }

    private static SqlDialect getDialect(DatasourceSchemaDTO coreDatasource) {
        SqlDialect sqlDialect = null;
        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(coreDatasource.getType());
        switch (datasourceType) {
            case mysql:
            case mongo:
            case StarRocks:
            case doris:
            case TiDB:
            case mariadb:
                sqlDialect = MysqlSqlDialect.DEFAULT;
                break;
            case impala:
                sqlDialect = ImpalaSqlDialect.DEFAULT;
                break;
            case sqlServer:
                sqlDialect = MssqlSqlDialect.DEFAULT;
                break;
            case oracle:
                sqlDialect = OracleSqlDialect.DEFAULT;
                break;
            case db2:
                sqlDialect = Db2SqlDialect.DEFAULT;
                break;
            case pg:
                sqlDialect = PostgresqlSqlDialect.DEFAULT;
                break;
            case redshift:
                sqlDialect = RedshiftSqlDialect.DEFAULT;
                break;
            case ck:
                sqlDialect = ClickHouseSqlDialect.DEFAULT;
                break;
            case h2:
                sqlDialect = H2SqlDialect.DEFAULT;
                break;
            default:
                sqlDialect = MysqlSqlDialect.DEFAULT;
        }
        return sqlDialect;
    }
}

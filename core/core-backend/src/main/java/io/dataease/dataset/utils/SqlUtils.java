package io.dataease.dataset.utils;

import com.google.common.collect.ImmutableList;
import io.dataease.exception.DEException;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.apache.calcite.sql.SqlKind.*;

/**
 * @Author Junjun
 */
public class SqlUtils {
    public static Logger logger = LoggerFactory.getLogger(SqlUtils.class);

    public static String addSchema(String sql, String schema) {
        sql = sql.trim();
        if (sql.endsWith(";")) {
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

}

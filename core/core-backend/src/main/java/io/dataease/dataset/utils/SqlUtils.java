package io.dataease.dataset.utils;

import com.google.common.collect.ImmutableList;
import io.dataease.exception.DEException;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;

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
                SqlParser.configBuilder()
//                        .setParserFactory(FlinkSqlParserImpl.FACTORY)
                        .setLex(Lex.JAVA)
                        .setIdentifierMaxLength(256)
                        .build();
        // 创建解析器
        SqlParser sqlParser = SqlParser
                .create(sql, config);
        // 生成 AST 语法树
        SqlNode sqlNode = null;
        try {
            sqlNode = sqlParser.parseStmt();
        } catch (SqlParseException e) {
            DEException.throwException("使用 Calcite 进行语法分析发生了异常:" + e);
        }
        addTableSchema(sqlNode, false, schema, config);
        String sqlRender = sqlNode.toString();
        // 处理sql中多余的`都替换成1个
        sqlRender = sqlRender.replaceAll("(`+)", "`");
        return sqlRender;
    }

    private static void addTableSchema(SqlNode sqlNode, Boolean fromOrJoin, String schema, SqlParser.Config config) {
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

                try {
                    sqlKind.setOperand(0, SqlParser.create(schema + ".`" + sqlKind.getOperandList().get(0).toString() + "`", config).parseExpression());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (sqlNode.getKind() == SELECT) {
            SqlSelect sqlKind = (SqlSelect) sqlNode;
//            List<SqlNode> list = sqlKind.getSelectList().getList();
//            for (SqlNode i : list) {
//                addTableSchema(i, false, schema);// select字段解析
//            }
            addTableSchema(sqlKind.getFrom(), true, schema, config);

//            if (((SqlSelect) sqlNode).getFrom().getKind() == IDENTIFIER) {
//                try {
//                    sqlKind.setFrom(SqlParser.create(schema + ".`" + ((SqlSelect) sqlNode).getFrom().toString() + "`", config).parseExpression());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }
}

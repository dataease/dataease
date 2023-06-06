package io.dataease.dataset.utils;

import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

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
        SqlNode sqlNode;
        try {
            sqlNode = sqlParser.parseStmt();
        } catch (SqlParseException e) {
            throw new RuntimeException("使用 Calcite 进行语法分析发生了异常", e);
        }
        List<String> dependencies = new ArrayList<>();
        dependencies = getDependencies(sqlNode, false, dependencies);
        if (ObjectUtils.isNotEmpty(dependencies)) {
            for (String s : dependencies) {
                if (sql.contains("`" + s + "`")) {
                    s = "`" + s + "`";
                    sql = sql.replaceAll(s, schema + "." + s);
                } else {
                    sql = sql.replaceAll(s, schema + "." + s);
                }
            }
        }

        return sql;
    }

    private static List<String> getDependencies(SqlNode sqlNode, Boolean fromOrJoin, List<String> l) {
        if (sqlNode.getKind() == JOIN) {
            SqlJoin sqlKind = (SqlJoin) sqlNode;
            getDependencies(sqlKind.getLeft(), true, l);
            getDependencies(sqlKind.getRight(), true, l);
        } else if (sqlNode.getKind() == IDENTIFIER) {
            if (fromOrJoin) {
                // 获取 source 表名
                l.add(sqlNode.toString());
            }
        } else if (sqlNode.getKind() == AS) {
            SqlBasicCall sqlKind = (SqlBasicCall) sqlNode;
            if (sqlKind.getOperandList().size() >= 2) {
                getDependencies(sqlKind.getOperandList().get(0), fromOrJoin, l);
            }
        } else if (sqlNode.getKind() == SELECT) {
            SqlSelect sqlKind = (SqlSelect) sqlNode;
            List<SqlNode> list = sqlKind.getSelectList().getList();
            for (SqlNode i : list) {
                getDependencies(i, false, l);
            }
            getDependencies(sqlKind.getFrom(), true, l);
        }
        return l;
    }
}

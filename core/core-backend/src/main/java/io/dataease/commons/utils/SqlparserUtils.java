package io.dataease.commons.utils;

import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlShuttle;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.calcite.sql.SqlKind.*;
import static org.apache.calcite.sql.SqlKind.SELECT;

public class SqlparserUtils {
    public static final String regex = "\\$\\{(.*?)\\}";
    private static final String SubstitutedParams = "DATAEASE_PATAMS_BI";
    private static final String SubstitutedSql = " 'DE-BI' = 'DE-BI' ";
    private static final String SubstitutedSqlVirtualData = " 1 > 2 ";

    public static String removeVariables(final String sql) {
        String tmpSql = sql;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sql);
        boolean hasVariables = false;
        while (matcher.find()) {
            hasVariables = true;
            tmpSql = tmpSql.replace(matcher.group(), SubstitutedParams);
        }
        if (!hasVariables && !tmpSql.contains(SubstitutedParams)) {
            return tmpSql;
        }

        SqlParser.Config config =
                SqlParser.configBuilder()
                        .setLex(Lex.JAVA)
                        .setIdentifierMaxLength(256)
                        .build();
        SqlParser sqlParser = SqlParser.create(sql, config);
        SqlNode sqlNode;
        try {
            sqlNode = sqlParser.parseStmt();
        } catch (SqlParseException e) {
            throw new RuntimeException("使用 Calcite 进行语法分析发生了异常", e);
        }

        // 递归遍历语法树
        getDependencies(sqlNode, false);
        return sql;
    }

    private static void getDependencies(SqlNode sqlNode, Boolean fromOrJoin) {

        if (sqlNode.getKind() == JOIN) {
            SqlJoin sqlKind = (SqlJoin) sqlNode;

        } else if (sqlNode.getKind() == IDENTIFIER) {

        } else if (sqlNode.getKind() == AS) {
            SqlBasicCall sqlKind = (SqlBasicCall) sqlNode;
        } else if (sqlNode.getKind() == SELECT) {
            SqlSelect sqlKind = (SqlSelect) sqlNode;
            List<SqlNode> list = sqlKind.getSelectList().getList();
            for (SqlNode i : list) {
                getDependencies(i, false);
            }
            SqlNode newWhere = sqlKind.getWhere().accept(getSqlShuttle());
            sqlKind.setWhere(newWhere);
        } else {
            // TODO 这里可根据需求拓展处理其他类型的 sqlNode
        }
    }

    public static SqlShuttle getSqlShuttle() {
        return new SqlShuttle() {

            @Override
            public @Nullable SqlNode visit(final SqlCall call) {
                CallCopyingArgHandler argHandler = new CallCopyingArgHandler(call, false);
                call.getOperator().acceptCall(this, call, false, argHandler);
                if (argHandler.result().toString().contains(SubstitutedParams)) {
                    SqlNode sqlNode1 = null;
                    try {
                        sqlNode1 = SqlParser.create(SubstitutedSql).parseExpression();
                    } catch (Exception e) {

                    }
                    return sqlNode1;
                }
                return argHandler.result();
            }
        };
    }
}

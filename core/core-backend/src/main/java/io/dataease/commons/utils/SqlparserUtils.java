package io.dataease.commons.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.commons.exception.DataEaseException;
import io.dataease.dataset.dto.SqlVariableDetails;
import io.dataease.i18n.Translator;
import io.dataease.utils.JsonUtil;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlShuttle;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.calcite.sql.SqlKind.*;

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
        SqlParser sqlParser = SqlParser.create(tmpSql, config);
        SqlNode sqlNode;
        try {
            sqlNode = sqlParser.parseStmt();
        } catch (SqlParseException e) {
            throw new RuntimeException("使用 Calcite 进行语法分析发生了异常", e);
        }
        // 递归遍历语法树
        getDependencies(sqlNode, false);
        return sqlNode.toString();
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

    public static String handleVariableDefaultValue(String sql, String sqlVariableDetails, boolean isEdit) {
        if (StringUtils.isEmpty(sql)) {
            DataEaseException.throwException(Translator.get("i18n_sql_not_empty"));
        }
        if (StringUtils.isNotEmpty(sqlVariableDetails)) {
            TypeReference<List<SqlVariableDetails>> listTypeReference = new TypeReference<List<SqlVariableDetails>>() {
            };
            List<SqlVariableDetails> defaultsSqlVariableDetails = JsonUtil.parseList(sqlVariableDetails, listTypeReference);

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(sql);
            while (matcher.find()) {
                SqlVariableDetails defaultsSqlVariableDetail = null;
                for (SqlVariableDetails sqlVariableDetail : defaultsSqlVariableDetails) {
                    if (matcher.group().substring(2, matcher.group().length() - 1).equalsIgnoreCase(sqlVariableDetail.getVariableName())) {
                        defaultsSqlVariableDetail = sqlVariableDetail;
                        break;
                    }
                }
                if (defaultsSqlVariableDetail != null && StringUtils.isNotEmpty(defaultsSqlVariableDetail.getDefaultValue())) {
                    if (!isEdit && defaultsSqlVariableDetail.getDefaultValueScope().equals(SqlVariableDetails.DefaultValueScope.ALLSCOPE)) {
                        sql = sql.replace(matcher.group(), defaultsSqlVariableDetail.getDefaultValue());
                    }
                    if (isEdit) {
                        sql = sql.replace(matcher.group(), defaultsSqlVariableDetail.getDefaultValue());
                    }
                }
            }
        }
        try {
            sql = removeVariables(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sql;
    }
}

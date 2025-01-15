package io.dataease.commons.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.api.permissions.variable.dto.SysVariableValueDto;
import io.dataease.api.permissions.variable.dto.SysVariableValueItem;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.extensions.datasource.vo.XpackPluginsDatasourceVO;
import io.dataease.extensions.view.dto.SqlVariableDetails;
import io.dataease.i18n.Translator;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.utils.JsonUtil;
import io.dataease.utils.LogUtil;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlShuttle;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.provider.CsvSource;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.dataease.chart.manage.ChartDataManage.START_END_SEPARATOR;
import static org.apache.calcite.sql.SqlKind.*;

public class SqlparserUtils {
    public static final String regex = "\\$\\{(.*?)\\}";
    public static final String regex2 = "\\[(.*?)\\]";
    private static final String SubstitutedParams = "DATAEASE_PATAMS_BI";
    private static final String SysParamsSubstitutedParams = "DeSysParams_";
    private static final String SubstitutedSql = " 'DE-BI' = 'DE-BI' ";
    private boolean removeSysParams;
    private UserFormVO userEntity;
    private final List<Map<String, String>> sysParams = new ArrayList<>();

    public String handleVariableDefaultValue(String sql, String sqlVariableDetails, boolean isEdit, boolean isFromDataSet, List<SqlVariableDetails> parameters, boolean isCross, Map<Long, DatasourceSchemaDTO> dsMap, PluginManageApi pluginManage, UserFormVO userEntity) {
        DatasourceSchemaDTO ds = dsMap.entrySet().iterator().next().getValue();
        if (StringUtils.isEmpty(sql)) {
            DEException.throwException(Translator.get("i18n_sql_not_empty"));
        }
        this.userEntity = userEntity;
        try {
            this.removeSysParams = true;
            removeVariables(sql, ds.getType());
        } catch (Exception e) {
            DEException.throwException(e);
        }
        sql = sql.trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
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
                SqlVariableDetails filterParameter = null;
                if (ObjectUtils.isNotEmpty(parameters)) {
                    for (SqlVariableDetails parameter : parameters) {
                        if (parameter.getVariableName().equalsIgnoreCase(defaultsSqlVariableDetail.getVariableName())) {
                            filterParameter = parameter;
                        }
                    }
                }
                if (filterParameter != null) {
                    sql = sql.replace(matcher.group(), transFilter(filterParameter, dsMap));
                } else {
                    if (defaultsSqlVariableDetail != null && StringUtils.isNotEmpty(defaultsSqlVariableDetail.getDefaultValue())) {
                        if (!isEdit && isFromDataSet && defaultsSqlVariableDetail.getDefaultValueScope().equals(SqlVariableDetails.DefaultValueScope.ALLSCOPE)) {
                            sql = sql.replace(matcher.group(), defaultsSqlVariableDetail.getDefaultValue());
                        }
                        if (isEdit) {
                            sql = sql.replace(matcher.group(), defaultsSqlVariableDetail.getDefaultValue());
                        }
                    }
                }
            }
        }

        try {
            this.removeSysParams = false;
            sql = removeVariables(sql, ds.getType());
            // replace keyword '`'
            if (!isCross) {
                Map.Entry<Long, DatasourceSchemaDTO> next = dsMap.entrySet().iterator().next();
                DatasourceSchemaDTO value = next.getValue();

                String prefix = "";
                String suffix = "";
                if (Arrays.stream(DatasourceConfiguration.DatasourceType.values()).map(DatasourceConfiguration.DatasourceType::getType).toList().contains(value.getType())) {
                    DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(value.getType());
                    prefix = datasourceType.getPrefix();
                    suffix = datasourceType.getSuffix();
                } else {
                    if (LicenseUtil.licenseValid()) {
                        List<XpackPluginsDatasourceVO> xpackPluginsDatasourceVOS = pluginManage.queryPluginDs();
                        List<XpackPluginsDatasourceVO> list = xpackPluginsDatasourceVOS.stream().filter(ele -> StringUtils.equals(ele.getType(), value.getType())).toList();
                        if (ObjectUtils.isNotEmpty(list)) {
                            XpackPluginsDatasourceVO first = list.getFirst();
                            prefix = first.getPrefix();
                            suffix = first.getSuffix();
                        } else {
                            DEException.throwException("当前数据源插件不存在");
                        }
                    }
                }

                Pattern pattern = Pattern.compile("(`.*?`)");
                Matcher matcher = pattern.matcher(sql);
                while (matcher.find()) {
                    String group = matcher.group();
                    String info = group.substring(1, group.length() - 1);
                    sql = sql.replaceAll(group, prefix + info + suffix);
                }
            }
            this.removeSysParams = true;
            sql = removeVariables(sql, ds.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sql;
    }


    private String removeVariables(final String sql, String dsType) throws Exception {
        String tmpSql = sql.replaceAll("(?m)^\\s*$[\n\r]{0,}", "");
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(tmpSql);
        boolean hasVariables = false;
        while (matcher.find()) {
            hasVariables = true;
            tmpSql = tmpSql.replace(matcher.group(), SubstitutedParams);
        }
        if (removeSysParams) {
            for (Map<String, String> sysParam : sysParams) {
                tmpSql = tmpSql.replaceAll(sysParam.get("replace"), sysParam.get("origin"));
            }
            pattern = Pattern.compile(regex2);
            matcher = pattern.matcher(tmpSql);
            while (matcher.find()) {
                hasVariables = true;
                tmpSql = tmpSql.replace(matcher.group(), SubstitutedParams);
            }
        } else {
            pattern = Pattern.compile(regex2);
            matcher = pattern.matcher(tmpSql);
            sysParams.clear();
            while (matcher.find()) {
                hasVariables = true;
                tmpSql = tmpSql.replace(matcher.group(), SysParamsSubstitutedParams + matcher.group().substring(1, matcher.group().length() - 1));
                Map<String, String> sysParam = new HashMap<>();
                sysParam.put("origin", matcher.group());
                sysParam.put("replace", SysParamsSubstitutedParams + matcher.group().substring(1, matcher.group().length() - 1));
                sysParams.add(sysParam);
            }
        }
        Statement statement = CCJSqlParserUtil.parse(tmpSql);
        Select select = (Select) statement;
        if (CollectionUtils.isNotEmpty(select.getWithItemsList())) {
            for (Iterator<WithItem> iter = select.getWithItemsList().iterator(); iter.hasNext(); ) {
                WithItem withItem = iter.next();
                ParenthesedSelect parenthesedSelect = (ParenthesedSelect) withItem.getSelect();
                parenthesedSelect.setSelect((Select) CCJSqlParserUtil.parse(removeVariables(parenthesedSelect.getSelect().toString(), dsType)));
            }
        }

        if (select.getSelectBody() instanceof PlainSelect) {
            return handlePlainSelect((PlainSelect) select.getSelectBody(), select, dsType);
        } else {
            StringBuilder result = new StringBuilder();
            SetOperationList setOperationList = (SetOperationList) select.getSelectBody();
            for (int i = 0; i < setOperationList.getSelects().size(); i++) {
                result.append(handlePlainSelect((PlainSelect) setOperationList.getSelects().get(i), null, dsType));
                if (i < setOperationList.getSelects().size() - 1) {
                    result.append(" ").append(setOperationList.getOperations().get(i).toString()).append(" ");
                }
            }
            return select.toString();
        }
    }

    private String handlePlainSelect(PlainSelect plainSelect, Select statementSelect, String dsType) throws Exception {
        handleSelectItems(plainSelect, dsType);
        handleFromItems(plainSelect, dsType);
        handleJoins(plainSelect, dsType);
        handleHaving(plainSelect);
        return handleWhere(plainSelect, statementSelect, dsType);
    }

    private void handleSelectItems(PlainSelect plainSelect, String dsType) throws Exception {
        List<SelectItem<?>> selectItems = new ArrayList<>();
        for (SelectItem selectItem : plainSelect.getSelectItems()) {
            try {
                if (selectItem.getExpression() instanceof ParenthesedSelect) {
                    ParenthesedSelect parenthesedSelect = (ParenthesedSelect) selectItem.getExpression();
                    parenthesedSelect.setSelect((Select) CCJSqlParserUtil.parse(removeVariables(((Select) selectItem.getExpression()).getPlainSelect().toString(), dsType)));
                    selectItem.setExpression(parenthesedSelect);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            selectItems.add(selectItem);
        }
        plainSelect.setSelectItems(selectItems);
    }

    private void handleFromItems(PlainSelect plainSelect, String dsType) throws Exception {
        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem instanceof ParenthesedSelect) {
            handleParenthesedSelect(fromItem, dsType);
            plainSelect.setFromItem(fromItem);
        } else {
            if (fromItem instanceof ParenthesedFromItem) {
                fromItem = ((ParenthesedFromItem) fromItem).getFromItem();
                while (fromItem instanceof ParenthesedFromItem) {
                    fromItem = ((ParenthesedFromItem) fromItem).getFromItem();
                }
                handleParenthesedSelect(fromItem, dsType);
            }
            plainSelect.setFromItem(fromItem);
        }
    }

    private void handleParenthesedSelect(FromItem fromItem, String dsType) throws Exception {
        if (((ParenthesedSelect) fromItem).getSelect() instanceof SetOperationList) {
            StringBuilder result = new StringBuilder();
            SetOperationList setOperationList = (SetOperationList) ((ParenthesedSelect) fromItem).getSelect().getSelectBody();
            for (int i = 0; i < setOperationList.getSelects().size(); i++) {
                result.append(handlePlainSelect((PlainSelect) setOperationList.getSelects().get(i), null, dsType));
                if (i < setOperationList.getSelects().size() - 1) {
                    result.append(" ").append(setOperationList.getOperations().get(i).toString()).append(" ");
                }
            }
        } else {
            PlainSelect selectBody = ((ParenthesedSelect) fromItem).getSelect().getPlainSelect();
            Select subSelectTmp = (Select) CCJSqlParserUtil.parse(removeVariables(selectBody.toString(), dsType));
            ((ParenthesedSelect) fromItem).setSelect(subSelectTmp.getSelectBody());
            if (dsType.equals(DatasourceConfiguration.DatasourceType.oracle.getType())) {
                if (fromItem.getAlias() != null) {
                    fromItem.setAlias(new Alias(fromItem.getAlias().toString(), false));
                }
            } else {
                if (fromItem.getAlias() == null) {
                    throw new Exception("Failed to parse sql, Every derived table must have its own alias！");
                }
                fromItem.setAlias(new Alias(fromItem.getAlias().toString(), false));
            }
        }
    }

    private void handleJoins(PlainSelect plainSelect, String dsType) throws Exception {
        List<Join> joins = plainSelect.getJoins();
        if (joins != null) {
            List<Join> joinsList = new ArrayList<>();
            for (Join join : joins) {
                FromItem rightItem = join.getRightItem();
                Collection<Expression> exprs = join.getOnExpressions();
                Collection<Expression> exprs2 = new ArrayList<>();
                for (Expression expr : exprs) {
                    StringBuilder stringBuilder = new StringBuilder();
                    BinaryExpression binaryExpression = null;
                    try {
                        binaryExpression = (BinaryExpression) expr;
                    } catch (Exception e) {
                    }
                    if (binaryExpression != null) {
                        boolean hasSubBinaryExpression = binaryExpression instanceof AndExpression || binaryExpression instanceof OrExpression;
                        if (!hasSubBinaryExpression && !(binaryExpression.getLeftExpression() instanceof BinaryExpression) && !(binaryExpression.getLeftExpression() instanceof InExpression) && (hasVariable(binaryExpression.getLeftExpression().toString()) || hasVariable(binaryExpression.getRightExpression().toString()))) {
                            stringBuilder.append(handleSubstitutedSql(binaryExpression.toString()));
                        } else {
                            expr.accept(getExpressionDeParser(stringBuilder));
                        }
                    } else {
                        expr.accept(getExpressionDeParser(stringBuilder));
                    }
                    exprs2.add(CCJSqlParserUtil.parseCondExpression(stringBuilder.toString()));
                }
                join.setOnExpressions(exprs2);
                if (rightItem instanceof ParenthesedSelect) {
                    try {
                        PlainSelect selectBody = ((ParenthesedSelect) rightItem).getPlainSelect();
                        Select subSelectTmp = (Select) CCJSqlParserUtil.parse(removeVariables(selectBody.toString(), dsType));
                        PlainSelect subPlainSelect = ((PlainSelect) subSelectTmp.getSelectBody());
                        ((ParenthesedSelect) rightItem).setSelect(subPlainSelect);
                    } catch (Exception e) {
                        SetOperationList select = ((ParenthesedSelect) rightItem).getSetOperationList();
                        SetOperationList setOperationList = new SetOperationList();
                        setOperationList.setSelects(new ArrayList<>());
                        setOperationList.setOperations(select.getOperations());
                        for (Select selectSelect : select.getSelects()) {
                            Select subSelectTmp = (Select) CCJSqlParserUtil.parse(removeVariables(selectSelect.toString(), dsType));
                            setOperationList.getSelects().add(subSelectTmp);
                        }
                        ((ParenthesedSelect) rightItem).setSelect(setOperationList);
                    }
                    if (dsType.equals(DatasourceConfiguration.DatasourceType.oracle.getType())) {
                        rightItem.setAlias(new Alias(rightItem.getAlias().toString(), false));
                    } else {
                        if (rightItem.getAlias() == null) {
                            throw new Exception("Failed to parse sql, Every derived table must have its own alias！");
                        }
                        rightItem.setAlias(new Alias(rightItem.getAlias().toString(), false));
                    }
                    join.setRightItem(rightItem);
                }
                joinsList.add(join);
            }
            plainSelect.setJoins(joinsList);
        }
    }

    private void handleHaving(PlainSelect plainSelect) throws Exception {
        Expression expr = plainSelect.getHaving();
        if (expr == null) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        BinaryExpression binaryExpression = null;
        try {
            binaryExpression = (BinaryExpression) expr;
        } catch (Exception e) {
        }
        if (binaryExpression != null) {
            boolean hasSubBinaryExpression = binaryExpression instanceof AndExpression || binaryExpression instanceof OrExpression;
            if (!hasSubBinaryExpression && !(binaryExpression.getLeftExpression() instanceof BinaryExpression) && !(binaryExpression.getLeftExpression() instanceof InExpression) && (hasVariable(binaryExpression.getLeftExpression().toString()) || hasVariable(binaryExpression.getRightExpression().toString()))) {
                stringBuilder.append(handleSubstitutedSql(binaryExpression.toString()));
            } else {
                expr.accept(getExpressionDeParser(stringBuilder));
            }
        } else {
            expr.accept(getExpressionDeParser(stringBuilder));
        }
        plainSelect.setHaving(CCJSqlParserUtil.parseCondExpression(stringBuilder.toString()));
    }

    private String handleWhere(PlainSelect plainSelect, Select statementSelect, String dsType) throws Exception {
        Expression expr = plainSelect.getWhere();
        if (expr == null) {
            return handleWith(plainSelect, statementSelect, dsType);
        }
        StringBuilder stringBuilder = new StringBuilder();
        BinaryExpression binaryExpression = null;
        try {
            binaryExpression = (BinaryExpression) expr;
        } catch (Exception e) {
        }
        if (binaryExpression != null) {
            boolean hasSubBinaryExpression = binaryExpression instanceof AndExpression || binaryExpression instanceof OrExpression;
            if (!hasSubBinaryExpression && !(binaryExpression.getLeftExpression() instanceof BinaryExpression) && !(binaryExpression.getLeftExpression() instanceof InExpression) && (hasVariable(binaryExpression.getLeftExpression().toString()) || hasVariable(binaryExpression.getRightExpression().toString()))) {
                stringBuilder.append(handleSubstitutedSql(binaryExpression.toString()));
            } else {
                expr.accept(getExpressionDeParser(stringBuilder));
            }
        } else {
            expr.accept(getExpressionDeParser(stringBuilder));
        }
        plainSelect.setWhere(CCJSqlParserUtil.parseCondExpression(stringBuilder.toString()));
        return handleWith(plainSelect, statementSelect, dsType);
    }

    private String handleWith(PlainSelect plainSelect, Select select, String dsType) throws Exception {
        if (select != null && CollectionUtils.isNotEmpty(select.getWithItemsList())) {
            for (Iterator<WithItem> iter = select.getWithItemsList().iterator(); iter.hasNext(); ) {
                WithItem withItem = iter.next();
                ParenthesedSelect parenthesedSelect = (ParenthesedSelect) withItem.getSelect();
                parenthesedSelect.setSelect((Select) CCJSqlParserUtil.parse(removeVariables(parenthesedSelect.getSelect().toString(), dsType)));
            }
        }
        return plainSelect.toString();
    }

    private ExpressionDeParser getExpressionDeParser(StringBuilder stringBuilder) {
        ExpressionDeParser expressionDeParser = new ExpressionDeParser(null, stringBuilder) {
            @Override
            public void visit(Parenthesis parenthesis) {
                getBuffer().append("(");
                parenthesis.getExpression().accept(this);
                getBuffer().append(")");
            }

            @Override
            public void visit(OrExpression orExpression) {
                visitBinaryExpr(orExpression, "OR");
            }

            @Override
            public void visit(AndExpression andExpression) {
                visitBinaryExpr(andExpression, andExpression.isUseOperator() ? " && " : " AND ");
            }

            @Override
            public void visit(Between between) {
                if (hasVariable(between.getBetweenExpressionStart().toString()) || hasVariable(between.getBetweenExpressionEnd().toString())) {
                    getBuffer().append(handleSubstitutedSql(between.toString()));
                } else {
                    getBuffer().append(between.getLeftExpression()).append(" BETWEEN ").append(between.getBetweenExpressionStart()).append(" AND ").append(between.getBetweenExpressionEnd());
                }
            }

            @Override
            public void visit(MinorThan minorThan) {
                if (hasVariable(minorThan.getLeftExpression().toString()) || hasVariable(minorThan.getRightExpression().toString())) {
                    getBuffer().append(handleSubstitutedSql(minorThan.toString()));
                    return;
                }
                getBuffer().append(minorThan.getLeftExpression());
                getBuffer().append(" < ");
                getBuffer().append(minorThan.getRightExpression());
            }

            @Override
            public void visit(MinorThanEquals minorThan) {
                if (hasVariable(minorThan.getLeftExpression().toString()) || hasVariable(minorThan.getRightExpression().toString())) {
                    getBuffer().append(handleSubstitutedSql(minorThan.toString()));
                    return;
                }
                getBuffer().append(minorThan.getLeftExpression());
                getBuffer().append(" <= ");
                getBuffer().append(minorThan.getRightExpression());
            }

            @Override
            public void visit(GreaterThanEquals minorThan) {
                if (hasVariable(minorThan.getLeftExpression().toString()) || hasVariable(minorThan.getRightExpression().toString())) {
                    getBuffer().append(handleSubstitutedSql(minorThan.toString()));
                    return;
                }
                getBuffer().append(minorThan.getLeftExpression());
                getBuffer().append(" >= ");
                getBuffer().append(minorThan.getRightExpression());
            }

            @Override
            public void visit(GreaterThan greaterThan) {
                if (hasVariable(greaterThan.getLeftExpression().toString()) || hasVariable(greaterThan.getRightExpression().toString())) {
                    getBuffer().append(handleSubstitutedSql(greaterThan.toString()));
                    return;
                }
                getBuffer().append(greaterThan.getLeftExpression());
                getBuffer().append(" > ");
                getBuffer().append(greaterThan.getRightExpression());
            }

            @Override
            public void visit(ExpressionList expressionList) {
                for (Iterator<Expression> iter = expressionList.getExpressions().iterator(); iter.hasNext(); ) {
                    Expression expression = iter.next();
                    expression.accept(this);
                    if (iter.hasNext()) {
                        buffer.append(", ");
                    }
                }
            }

            @Override
            public void visit(LikeExpression likeExpression) {
                if (hasVariable(likeExpression.toString())) {
                    getBuffer().append(handleSubstitutedSql(likeExpression.toString()));
                    return;
                }
                visitBinaryExpression(likeExpression, (likeExpression.isNot() ? " NOT" : "") + (likeExpression.isCaseInsensitive() ? " ILIKE " : " LIKE "));
                if (likeExpression.getEscape() != null) {
                    buffer.append(" ESCAPE '").append(likeExpression.getEscape()).append('\'');
                }
            }

            @Override
            public void visit(InExpression inExpression) {
                if (inExpression.getRightExpression() != null && hasVariable(inExpression.getRightExpression().toString()) && !(inExpression.getRightExpression() instanceof ParenthesedSelect)) {
                    stringBuilder.append(handleSubstitutedSqlForIn(inExpression.toString()));
                    return;
                }
                inExpression.getLeftExpression().accept(this);
                if (inExpression.isNot()) {
                    getBuffer().append(" " + " NOT IN " + " ");
                } else {
                    getBuffer().append(" IN ");
                }
                if (inExpression.getRightExpression() != null && inExpression.getRightExpression() instanceof ParenthesedSelect) {
                    try {
                        ParenthesedSelect subSelect = (ParenthesedSelect) inExpression.getRightExpression();
                        Select select = (Select) CCJSqlParserUtil.parse(removeVariables(subSelect.getPlainSelect().toString(), ""));
                        subSelect.setSelect(select);
                        inExpression.setRightExpression(subSelect);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    inExpression.getRightExpression().accept(this);
                }
                if (inExpression.getRightExpression() instanceof ParenthesedExpressionList) {
                    buffer.append(inExpression.getRightExpression());
                }
            }

            @Override
            public void visit(ParenthesedSelect subSelect) {
                StringBuilder stringBuilder = new StringBuilder();
                Expression in = ((PlainSelect) subSelect.getSelectBody()).getWhere();
                if (in instanceof BinaryExpression && hasVariable(in.toString())) {
                    stringBuilder.append(SubstitutedParams);
                } else {
                    in.accept(getExpressionDeParser(stringBuilder));
                }

                try {
                    Expression where = CCJSqlParserUtil.parseCondExpression(stringBuilder.toString());
                    ((PlainSelect) subSelect.getSelectBody()).setWhere(where);
                    getBuffer().append(subSelect.getSelectBody());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void visit(Select selectBody) {
                getBuffer().append(selectBody.toString());
            }


            private void visitBinaryExpr(BinaryExpression expr, String operator) {
                boolean hasSubBinaryExpression = false;
                if (expr.getLeftExpression() instanceof Parenthesis) {
                    try {
                        Parenthesis parenthesis = (Parenthesis) expr.getLeftExpression();
                        BinaryExpression leftBinaryExpression = (BinaryExpression) parenthesis.getExpression();
                        hasSubBinaryExpression = leftBinaryExpression instanceof AndExpression || leftBinaryExpression instanceof OrExpression;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (expr.getLeftExpression() instanceof BinaryExpression) {
                    try {
                        BinaryExpression leftBinaryExpression = (BinaryExpression) expr.getLeftExpression();
                        hasSubBinaryExpression = leftBinaryExpression instanceof AndExpression || leftBinaryExpression instanceof OrExpression;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if ((expr.getLeftExpression() instanceof BinaryExpression || expr.getLeftExpression() instanceof Parenthesis) && !hasSubBinaryExpression && hasVariable(expr.getLeftExpression().toString())) {
                    getBuffer().append(handleSubstitutedSql(expr.getLeftExpression().toString()));
                } else {
                    expr.getLeftExpression().accept(this);
                }
                getBuffer().append(" " + operator + " ");
                hasSubBinaryExpression = false;
                if (expr.getRightExpression() instanceof Parenthesis) {
                    try {
                        Parenthesis parenthesis = (Parenthesis) expr.getRightExpression();
                        BinaryExpression rightBinaryExpression = (BinaryExpression) parenthesis.getExpression();
                        hasSubBinaryExpression = rightBinaryExpression instanceof AndExpression || rightBinaryExpression instanceof OrExpression;
                    } catch (Exception e) {
                        LogUtil.error("Failed parse sql", e);
                    }
                }
                if (expr.getRightExpression() instanceof BinaryExpression) {
                    try {
                        BinaryExpression rightBinaryExpression = (BinaryExpression) expr.getRightExpression();
                        hasSubBinaryExpression = rightBinaryExpression instanceof AndExpression || rightBinaryExpression instanceof OrExpression;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if ((expr.getRightExpression() instanceof Parenthesis || expr.getRightExpression() instanceof BinaryExpression || expr.getRightExpression() instanceof Function) && !hasSubBinaryExpression && hasVariable(expr.getRightExpression().toString())) {
                    getBuffer().append(handleSubstitutedSql(expr.getRightExpression().toString()));
                } else {
                    expr.getRightExpression().accept(this);
                }
            }
        };
        return expressionDeParser;
    }

    private boolean hasVariable(String sql) {
        return sql.contains(SubstitutedParams) || (!removeSysParams && sql.contains(SysParamsSubstitutedParams));
    }


    private void getDependencies(SqlNode sqlNode, Boolean fromOrJoin) {
        if (sqlNode == null) {
            return;
        }
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
            SqlNode from = sqlKind.getFrom().accept(getSqlShuttle());
            sqlKind.setFrom(from);
            if (sqlKind.getWhere() != null) {
                SqlNode newWhere = sqlKind.getWhere().accept(getSqlShuttle());
                sqlKind.setWhere(newWhere);
            }
        } else if (sqlNode.getKind() == ORDER_BY) {
            SqlOrderBy sqlKind = (SqlOrderBy) sqlNode;
            List<SqlNode> operandList = sqlKind.getOperandList();
            for (int i = 0; i < operandList.size(); i++) {
                getDependencies(operandList.get(i), false);
            }
        } else if (sqlNode.getKind() == UNION) {
            SqlBasicCall sqlKind = (SqlBasicCall) sqlNode;
            if (sqlKind.getOperandList().size() >= 2) {
                for (int i = 0; i < sqlKind.getOperandList().size(); i++) {
                    getDependencies(sqlKind.getOperandList().get(i), false);
                }
            }
        }
    }

    private SqlShuttle getSqlShuttle() {
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

    private String transFilter(SqlVariableDetails sqlVariableDetails, Map<Long, DatasourceSchemaDTO> dsMap) {
        if (sqlVariableDetails.getOperator().equals("in")) {
            if (StringUtils.equalsIgnoreCase(dsMap.entrySet().iterator().next().getValue().getType(), DatasourceConfiguration.DatasourceType.sqlServer.getType())
                    && sqlVariableDetails.getDeType() == 0) {
                return "N'" + String.join("', N'", sqlVariableDetails.getValue()) + "'";
            } else {
                if (sqlVariableDetails.getDeType() == 2 || sqlVariableDetails.getDeType() == 3) {
                    return String.join(",", sqlVariableDetails.getValue());
                } else {
                    return "'" + String.join("','", sqlVariableDetails.getValue()) + "'";
                }
            }
        } else if (sqlVariableDetails.getOperator().equals("between")) {
            if (sqlVariableDetails.getDeType() == 1) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(sqlVariableDetails.getType().size() > 1 ? (String) sqlVariableDetails.getType().get(1).replace("DD", "dd").replace("YYYY", "yyyy") : "yyyy");
                if (StringUtils.endsWith(sqlVariableDetails.getId(), START_END_SEPARATOR)) {
                    return simpleDateFormat.format(new Date(Long.parseLong((String) sqlVariableDetails.getValue().get(1))));
                } else {
                    return simpleDateFormat.format(new Date(Long.parseLong((String) sqlVariableDetails.getValue().get(0))));
                }
            } else {
                if (StringUtils.endsWith(sqlVariableDetails.getId(), START_END_SEPARATOR)) {
                    return sqlVariableDetails.getValue().get(1);
                } else {
                    return sqlVariableDetails.getValue().get(0);
                }
            }
        } else {
            return (String) sqlVariableDetails.getValue().get(0);
        }

    }

    private String handleSubstitutedSql(String sql) {
        if (sql.contains(SysParamsSubstitutedParams) && userEntity != null) {
            sql = sql.replace(SysParamsSubstitutedParams + "sysParams.userId", userEntity.getAccount());
            sql = sql.replace(SysParamsSubstitutedParams + "sysParams.userEmail", userEntity.getEmail());
            sql = sql.replace(SysParamsSubstitutedParams + "sysParams.userName", userEntity.getName());
            for (SysVariableValueItem variable : userEntity.getVariables()) {
                String value = null;
                if (!variable.isValid()) {
                    continue;
                }
                if (variable.getSysVariableDto().getType().equalsIgnoreCase("text")) {
                    for (SysVariableValueDto sysVariableValueDto : variable.getValueList()) {
                        if (variable.getVariableValueIds().contains(sysVariableValueDto.getId().toString())) {
                            value = sysVariableValueDto.getValue();
                            break;
                        }
                    }
                } else {
                    value = variable.getVariableValue();
                }
                if (StringUtils.isNotEmpty(value)) {
                    sql = sql.replace(SysParamsSubstitutedParams + variable.getVariableId(), value);
                }
            }
            return sql;
        } else {
            return SubstitutedSql;
        }
    }


    private String handleSubstitutedSqlForIn(String sql) {
        if (sql.contains(SysParamsSubstitutedParams) && userEntity != null) {
            for (SysVariableValueItem variable : userEntity.getVariables()) {
                List<String> values = new ArrayList<>();
                if (!variable.isValid()) {
                    continue;
                }
                if (variable.getSysVariableDto().getType().equalsIgnoreCase("text")) {

                    for (SysVariableValueDto sysVariableValueDto : variable.getValueList()) {
                        if (variable.getVariableValueIds().contains(sysVariableValueDto.getId().toString())) {
                            values.add(sysVariableValueDto.getValue());
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(values)) {
                    sql = sql.replace(SysParamsSubstitutedParams + variable.getVariableId(), "'" + String.join("','", values) + "'");
                }
            }
            return sql;
        } else {
            return SubstitutedSql;
        }
    }
}




package io.dataease.commons.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.api.permissions.variable.dto.SysVariableValueDto;
import io.dataease.api.permissions.variable.dto.SysVariableValueItem;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.dto.TableFieldWithValue;
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
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlShuttle;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.math.BigDecimal;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.dataease.chart.manage.ChartDataManage.START_END_SEPARATOR;
import static org.apache.calcite.sql.SqlKind.*;

public class SqlparserUtils {
    public static final String regex = "\\$\\{(.*?)\\}";
    public static final String regex2 = "\\$f2cde\\[(.*?)\\]";
    private static final String SubstitutedParams = "DATAEASE_PATAMS_BI";
    private static final String SysParamsSubstitutedParams = "DeSysParams_";
    private static final String SubstitutedSql = " 'DE-BI' = 'DE-BI' ";
    private boolean removeSysParams;
    boolean hasVariables = false;
    private UserFormVO userEntity;
    private final List<Map<String, String>> sysParams = new ArrayList<>();
    private static final String deVariablePattern = "\\$DE_PARAM\\{(.*?)\\}";
    private List<SqlVariableDetails> defaultsSqlVariableDetails = new ArrayList<>();

    public String handleVariableDefaultValue(String sql, String sqlVariableDetails, boolean isEdit, boolean isFromDataSet, List<SqlVariableDetails> parameters, boolean isCross, Map<Long, DatasourceSchemaDTO> dsMap, PluginManageApi pluginManage, UserFormVO userEntity) {
        return handleVariableDefaultValueWithPreparedParams(sql, sqlVariableDetails, isEdit, isFromDataSet, parameters, isCross, dsMap, pluginManage, userEntity).getSql();
    }

    public SqlVariableHandleResult handleVariableDefaultValueWithPreparedParams(String sql, String sqlVariableDetails, boolean isEdit, boolean isFromDataSet, List<SqlVariableDetails> parameters, boolean isCross, Map<Long, DatasourceSchemaDTO> dsMap, PluginManageApi pluginManage, UserFormVO userEntity) {
        Pattern r = Pattern.compile(deVariablePattern);
        Matcher m = r.matcher(sql);
        if (m.find()) {
            return new DeSqlparserUtils().handleVariableDefaultValueWithPreparedParams(sql, sqlVariableDetails, isEdit, isFromDataSet, parameters, isCross, dsMap, pluginManage, userEntity);
        }

        DatasourceSchemaDTO ds = dsMap.entrySet().iterator().next().getValue();
        if (StringUtils.isEmpty(sql)) {
            DEException.throwException(Translator.get("i18n_sql_not_empty"));
        }
        this.userEntity = userEntity;
        hasVariables = false;
        sql = sql.trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }
        List<TableFieldWithValue> tableFieldWithValues = new ArrayList<>();
        defaultsSqlVariableDetails = new ArrayList<>();
        if (StringUtils.isNotEmpty(sqlVariableDetails)) {
            TypeReference<List<SqlVariableDetails>> listTypeReference = new TypeReference<List<SqlVariableDetails>>() {
            };
            defaultsSqlVariableDetails = JsonUtil.parseList(sqlVariableDetails, listTypeReference);
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(sql);
            StringBuilder sqlBuilder = new StringBuilder();
            int lastIndex = 0;
            while (matcher.find()) {
                if (matcher.start() < lastIndex) {
                    continue;
                }
                String variableName = matcher.group().substring(2, matcher.group().length() - 1);
                QuotedLiteralContext quotedLiteralContext = findQuotedLiteralContext(sql, matcher.start(), matcher.end());
                int appendEnd = quotedLiteralContext == null ? matcher.start() : quotedLiteralContext.start();
                if (appendEnd < lastIndex) {
                    continue;
                }
                sqlBuilder.append(sql, lastIndex, appendEnd);
                boolean replaced = false;
                PreparedSqlFragment preparedSqlFragment;
                if (quotedLiteralContext != null) {
                    preparedSqlFragment = buildPreparedSqlFragmentForQuotedLiteral(quotedLiteralContext, parameters, isEdit, isFromDataSet);
                    if (preparedSqlFragment != null) {
                        sqlBuilder.append(preparedSqlFragment.replacement());
                        lastIndex = quotedLiteralContext.end() + 1;
                        tableFieldWithValues.addAll(preparedSqlFragment.tableFieldWithValues());
                        replaced = true;
                    }
                } else {
                    preparedSqlFragment = resolvePreparedSqlFragment(variableName, parameters, isEdit, isFromDataSet);
                    if (preparedSqlFragment != null) {
                        sqlBuilder.append(preparedSqlFragment.replacement());
                        lastIndex = matcher.end();
                        tableFieldWithValues.addAll(preparedSqlFragment.tableFieldWithValues());
                        replaced = true;
                    }
                }
                if (!replaced) {
                    sqlBuilder.append(matcher.group());
                    lastIndex = matcher.end();
                }
            }
            sqlBuilder.append(sql.substring(lastIndex));
            sql = sqlBuilder.toString();
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
        SqlVariableHandleResult result = new SqlVariableHandleResult(sql);
        result.setTableFieldWithValues(tableFieldWithValues);
        return result;
    }

    private static boolean isParams(String paramId) {
        if (Arrays.asList("sysParams.userId", "sysParams.userEmail", "sysParams.userName", "sysParams.userPhone").contains(paramId)) {
            return true;
        }
        boolean isLong = false;
        try {
            Long.valueOf(paramId);
            isLong = true;
        } catch (Exception e) {
            isLong = false;
        }
        if (paramId.length() >= 18 && isLong) {
            return true;
        }
        return false;
    }

    private String removeVariables(final String sql, String dsType) throws Exception {
        String tmpSql = sql.replaceAll("(?m)^\\s*$[\n\r]{0,}", "");
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(tmpSql);
        while (matcher.find()) {
            hasVariables = true;
            tmpSql = tmpSql.replace(matcher.group(), SubstitutedParams);
        }
        if (removeSysParams) {
            for (Map<String, String> sysParam : sysParams) {
                tmpSql = tmpSql.replace(sysParam.get("replace"), sysParam.get("origin"));
            }
            pattern = Pattern.compile(regex2);
            matcher = pattern.matcher(tmpSql);
            while (matcher.find()) {
                String paramId = matcher.group().substring(7, matcher.group().length() - 1);
                if (!isParams(paramId)) {
                    continue;
                }
                hasVariables = true;
                tmpSql = tmpSql.replace(matcher.group(), SubstitutedParams);
            }
        } else {
            pattern = Pattern.compile(regex2);
            matcher = pattern.matcher(tmpSql);
            while (matcher.find()) {
                String paramId = matcher.group().substring(7, matcher.group().length() - 1);
                if (!isParams(paramId)) {
                    continue;
                }
                hasVariables = true;
                tmpSql = tmpSql.replace(matcher.group(), SysParamsSubstitutedParams + matcher.group().substring(7, matcher.group().length() - 1));
                Map<String, String> sysParam = new HashMap<>();
                sysParam.put("origin", matcher.group());
                sysParam.put("replace", SysParamsSubstitutedParams + matcher.group().substring(7, matcher.group().length() - 1));
                sysParams.add(sysParam);
            }
        }
        if (!hasVariables && !sql.contains(SubstitutedParams)) {
            return sql;
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

    private SqlVariableDetails findSqlVariableDetail(List<SqlVariableDetails> sqlVariableDetails, String variableName) {
        if (CollectionUtils.isEmpty(sqlVariableDetails)) {
            return null;
        }
        for (SqlVariableDetails sqlVariableDetail : sqlVariableDetails) {
            if (StringUtils.equalsIgnoreCase(variableName, sqlVariableDetail.getVariableName())) {
                return sqlVariableDetail;
            }
        }
        return null;
    }

    private PreparedSqlFragment resolvePreparedSqlFragment(String variableName, List<SqlVariableDetails> parameters, boolean isEdit, boolean isFromDataSet) {
        SqlVariableDetails filterParameter = findSqlVariableDetail(parameters, variableName);
        if (filterParameter != null) {
            return buildPreparedSqlFragment(filterParameter);
        }
        SqlVariableDetails defaultsSqlVariableDetail = findSqlVariableDetail(defaultsSqlVariableDetails, variableName);
        if (shouldUseDefaultValue(defaultsSqlVariableDetail, isEdit, isFromDataSet)) {
            return buildPreparedSqlFragmentForDefaultValue(defaultsSqlVariableDetail);
        }
        return null;
    }

    private boolean shouldUseDefaultValue(SqlVariableDetails sqlVariableDetails, boolean isEdit, boolean isFromDataSet) {
        if (sqlVariableDetails == null || StringUtils.isEmpty(sqlVariableDetails.getDefaultValue())) {
            return false;
        }
        return isEdit || isFromDataSet && sqlVariableDetails.getDefaultValueScope() == SqlVariableDetails.DefaultValueScope.ALLSCOPE;
    }

    private boolean isQuotedVariable(String sql, int start, int end) {
        return start > 0
                && end < sql.length()
                && sql.charAt(start - 1) == '\''
                && sql.charAt(end) == '\'';
    }

    private QuotedLiteralContext findQuotedLiteralContext(String sql, int start, int end) {
        int literalStart = -1;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) != '\'') {
                continue;
            }
            if (literalStart < 0) {
                literalStart = i;
                continue;
            }
            if (i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                i++;
                continue;
            }
            if (start > literalStart && end <= i) {
                return new QuotedLiteralContext(literalStart, i, sql.substring(literalStart + 1, i));
            }
            literalStart = -1;
        }
        return null;
    }

    private PreparedSqlFragment buildPreparedSqlFragmentForQuotedLiteral(QuotedLiteralContext quotedLiteralContext, List<SqlVariableDetails> parameters, boolean isEdit, boolean isFromDataSet) {
        List<LiteralSegment> literalSegments = parseLiteralSegments(quotedLiteralContext.content());
        if (literalSegments.size() == 1 && literalSegments.get(0).variable()) {
            return resolvePreparedSqlFragment(literalSegments.get(0).content(), parameters, isEdit, isFromDataSet);
        }
        StringBuilder preparedValueBuilder = new StringBuilder();
        boolean hasVariable = false;
        for (LiteralSegment literalSegment : literalSegments) {
            if (!literalSegment.variable()) {
                preparedValueBuilder.append(unescapeQuotedLiteralText(literalSegment.content()));
                continue;
            }
            hasVariable = true;
            String preparedValue = resolveQuotedLiteralVariableValue(literalSegment.content(), parameters, isEdit, isFromDataSet);
            if (preparedValue == null) {
                return null;
            }
            preparedValueBuilder.append(preparedValue);
        }
        if (!hasVariable) {
            return null;
        }
        TableFieldWithValue tableFieldWithValue = new TableFieldWithValue();
        tableFieldWithValue.setFiledName(firstVariableName(literalSegments));
        tableFieldWithValue.setType(Types.VARCHAR);
        tableFieldWithValue.setColumnTypeName("VARCHAR");
        tableFieldWithValue.setValue(preparedValueBuilder.toString());
        return new PreparedSqlFragment("?", Collections.singletonList(tableFieldWithValue));
    }

    private String resolveQuotedLiteralVariableValue(String variableName, List<SqlVariableDetails> parameters, boolean isEdit, boolean isFromDataSet) {
        List<String> preparedValues = resolvePreparedValuesForQuotedLiteral(variableName, parameters, isEdit, isFromDataSet);
        if (CollectionUtils.isEmpty(preparedValues)) {
            return null;
        }
        if (preparedValues.size() != 1) {
            DEException.throwException("SQL模板字符串仅支持单值参数");
        }
        return preparedValues.get(0);
    }

    private List<String> resolvePreparedValuesForQuotedLiteral(String variableName, List<SqlVariableDetails> parameters, boolean isEdit, boolean isFromDataSet) {
        SqlVariableDetails filterParameter = findSqlVariableDetail(parameters, variableName);
        if (filterParameter != null) {
            return resolvePreparedValues(filterParameter);
        }
        SqlVariableDetails defaultsSqlVariableDetail = findSqlVariableDetail(defaultsSqlVariableDetails, variableName);
        if (!shouldUseDefaultValue(defaultsSqlVariableDetail, isEdit, isFromDataSet)) {
            return null;
        }
        SqlVariableDetails defaultValueDetail = new SqlVariableDetails();
        defaultValueDetail.setVariableName(defaultsSqlVariableDetail.getVariableName());
        defaultValueDetail.setType(defaultsSqlVariableDetail.getType());
        defaultValueDetail.setDeType(defaultsSqlVariableDetail.getDeType());
        defaultValueDetail.setId(defaultsSqlVariableDetail.getId());
        defaultValueDetail.setOperator(defaultsSqlVariableDetail.getOperator());
        defaultValueDetail.setValue(Collections.singletonList(defaultsSqlVariableDetail.getDefaultValue()));
        return resolvePreparedValues(defaultValueDetail);
    }

    private List<LiteralSegment> parseLiteralSegments(String literalContent) {
        List<LiteralSegment> literalSegments = new ArrayList<>();
        Matcher matcher = Pattern.compile(regex).matcher(literalContent);
        int lastIndex = 0;
        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                literalSegments.add(new LiteralSegment(false, literalContent.substring(lastIndex, matcher.start())));
            }
            literalSegments.add(new LiteralSegment(true, matcher.group().substring(2, matcher.group().length() - 1)));
            lastIndex = matcher.end();
        }
        if (lastIndex < literalContent.length()) {
            literalSegments.add(new LiteralSegment(false, literalContent.substring(lastIndex)));
        }
        return literalSegments;
    }

    private String unescapeQuotedLiteralText(String text) {
        return StringUtils.replace(text, "''", "'");
    }

    private String firstVariableName(List<LiteralSegment> literalSegments) {
        for (LiteralSegment literalSegment : literalSegments) {
            if (literalSegment.variable()) {
                return literalSegment.content();
            }
        }
        return null;
    }

    private PreparedSqlFragment buildPreparedSqlFragment(SqlVariableDetails sqlVariableDetails) {
        List<TableFieldWithValue> values = new ArrayList<>();
        List<String> replacements = new ArrayList<>();
        List<String> preparedValues = resolvePreparedValues(sqlVariableDetails);
        for (String preparedValue : preparedValues) {
            values.add(buildPreparedValue(sqlVariableDetails, preparedValue));
            replacements.add("?");
        }
        return new PreparedSqlFragment(String.join(",", replacements), values);
    }

    private PreparedSqlFragment buildPreparedSqlFragmentForDefaultValue(SqlVariableDetails sqlVariableDetails) {
        SqlVariableDetails defaultValueDetail = new SqlVariableDetails();
        defaultValueDetail.setVariableName(sqlVariableDetails.getVariableName());
        defaultValueDetail.setType(sqlVariableDetails.getType());
        defaultValueDetail.setDeType(sqlVariableDetails.getDeType());
        defaultValueDetail.setId(sqlVariableDetails.getId());
        defaultValueDetail.setOperator(sqlVariableDetails.getOperator());
        defaultValueDetail.setValue(Collections.singletonList(sqlVariableDetails.getDefaultValue()));
        return buildPreparedSqlFragment(defaultValueDetail);
    }

    private List<String> resolvePreparedValues(SqlVariableDetails sqlVariableDetails) {
        if (StringUtils.equals(sqlVariableDetails.getOperator(), "in")) {
            if (sqlVariableDetails.getDeType() == 1) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(sqlVariableDetails.getType().size() > 1 ? (String) sqlVariableDetails.getType().get(1).replace("DD", "dd").replace("YYYY", "yyyy") : "yyyy");
                if (StringUtils.endsWith(sqlVariableDetails.getId(), START_END_SEPARATOR)) {
                    return Collections.singletonList(simpleDateFormat.format(new Date(Long.parseLong((String) sqlVariableDetails.getValue().get(1)))));
                }
                return Collections.singletonList(simpleDateFormat.format(new Date(Long.parseLong((String) sqlVariableDetails.getValue().get(0)))));
            }
            return CollectionUtils.isEmpty(sqlVariableDetails.getValue()) ? Collections.emptyList() : sqlVariableDetails.getValue();
        }
        if (StringUtils.equals(sqlVariableDetails.getOperator(), "between") || StringUtils.equals(sqlVariableDetails.getOperator(), "eq")) {
            if (sqlVariableDetails.getDeType() == 1) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(sqlVariableDetails.getType().size() > 1 ? (String) sqlVariableDetails.getType().get(1).replace("DD", "dd").replace("YYYY", "yyyy") : "yyyy");
                if (StringUtils.endsWith(sqlVariableDetails.getId(), START_END_SEPARATOR)) {
                    return Collections.singletonList(simpleDateFormat.format(new Date(Long.parseLong((String) sqlVariableDetails.getValue().get(1)))));
                }
                return Collections.singletonList(simpleDateFormat.format(new Date(Long.parseLong((String) sqlVariableDetails.getValue().get(0)))));
            }
            if (StringUtils.endsWith(sqlVariableDetails.getId(), START_END_SEPARATOR)) {
                return Collections.singletonList(sqlVariableDetails.getValue().get(1));
            }
            return Collections.singletonList(sqlVariableDetails.getValue().get(0));
        }
        return CollectionUtils.isEmpty(sqlVariableDetails.getValue()) ? Collections.emptyList() : Collections.singletonList(sqlVariableDetails.getValue().get(0));
    }

    private TableFieldWithValue buildPreparedValue(SqlVariableDetails sqlVariableDetails, String value) {
        TableFieldWithValue tableFieldWithValue = new TableFieldWithValue();
        tableFieldWithValue.setFiledName(sqlVariableDetails.getVariableName());
        tableFieldWithValue.setTerm(sqlVariableDetails.getOperator());
        tableFieldWithValue.setDeExtractType(sqlVariableDetails.getDeType());
        if (sqlVariableDetails.getDeType() == 2) {
            tableFieldWithValue.setType(Types.BIGINT);
            tableFieldWithValue.setColumnTypeName("BIGINT");
            tableFieldWithValue.setValue(Long.parseLong(value));
            return tableFieldWithValue;
        }
        if (sqlVariableDetails.getDeType() == 3) {
            tableFieldWithValue.setType(Types.DECIMAL);
            tableFieldWithValue.setColumnTypeName("DECIMAL");
            tableFieldWithValue.setValue(new BigDecimal(value));
            return tableFieldWithValue;
        }
        if (sqlVariableDetails.getDeType() == 4) {
            if (StringUtils.equalsAnyIgnoreCase(value, "true", "false")) {
                tableFieldWithValue.setType(Types.BOOLEAN);
                tableFieldWithValue.setColumnTypeName("BOOLEAN");
                tableFieldWithValue.setValue(Boolean.parseBoolean(value));
            } else {
                tableFieldWithValue.setType(Types.INTEGER);
                tableFieldWithValue.setColumnTypeName("INTEGER");
                tableFieldWithValue.setValue(Integer.parseInt(value));
            }
            return tableFieldWithValue;
        }
        tableFieldWithValue.setType(Types.VARCHAR);
        tableFieldWithValue.setColumnTypeName("VARCHAR");
        tableFieldWithValue.setValue(value);
        return tableFieldWithValue;
    }

    private record PreparedSqlFragment(String replacement, List<TableFieldWithValue> tableFieldWithValues) {
    }

    private record QuotedLiteralContext(int start, int end, String content) {
    }

    private record LiteralSegment(boolean variable, String content) {
    }

    private String handleSubstitutedSql(String sql) {
        if (sql.contains(SysParamsSubstitutedParams) && userEntity != null) {
            sql = sql.replace(SysParamsSubstitutedParams + "sysParams.userId", userEntity.getAccount());
            sql = sql.replace(SysParamsSubstitutedParams + "sysParams.userEmail", userEntity.getEmail());
            sql = sql.replace(SysParamsSubstitutedParams + "sysParams.userName", Translator.get(userEntity.getName()));
            if (StringUtils.isNotEmpty(userEntity.getPhone())) {
                sql = sql.replace(SysParamsSubstitutedParams + "sysParams.userPhone", Translator.get(userEntity.getPhone()));
            }
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

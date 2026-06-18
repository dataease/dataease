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
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static io.dataease.chart.manage.ChartDataManage.START_END_SEPARATOR;

public class DeSqlparserUtils {
    private static final String deVariablePattern = "\\$DE_PARAM\\{(.*?)\\}";

    public static final String sqlParamsRegex = "\\$\\[(.*?)\\]";
    public static final String sysVariableRegex = "\\$f2cde\\[(.*?)\\]";
    private static final String SysParamsSubstitutedParams = "DeSysParams_";
    private static final String PREPARED_BINDING_TOKEN_PREFIX = "DE_BIND_";
    private UserFormVO userEntity;
    private static final String SubstitutedSql = " 'DE-BI' = 'DE-BI' ";
    private final List<Map<String, String>> sysParams = new ArrayList<>();
    private final Map<String, TableFieldWithValue> preparedBindings = new LinkedHashMap<>();
    private int preparedBindingIndex;
    TypeReference<List<SqlVariableDetails>> listTypeReference = new TypeReference<List<SqlVariableDetails>>() {
    };
    private List<SqlVariableDetails> defaultsSqlVariableDetails = new ArrayList<>();

    public String handleVariableDefaultValue(String sql, String sqlVariableDetails, boolean isEdit, boolean isFromDataSet, List<SqlVariableDetails> parameters, boolean isCross, Map<Long, DatasourceSchemaDTO> dsMap, PluginManageApi pluginManage, UserFormVO userEntity) {
        return handleVariableDefaultValueWithPreparedParams(sql, sqlVariableDetails, isEdit, isFromDataSet, parameters, isCross, dsMap, pluginManage, userEntity).getSql();
    }

    public SqlVariableHandleResult handleVariableDefaultValueWithPreparedParams(String sql, String sqlVariableDetails, boolean isEdit, boolean isFromDataSet, List<SqlVariableDetails> parameters, boolean isCross, Map<Long, DatasourceSchemaDTO> dsMap, PluginManageApi pluginManage, UserFormVO userEntity) {
        DatasourceSchemaDTO ds = dsMap.entrySet().iterator().next().getValue();
        if (StringUtils.isEmpty(sql)) {
            DEException.throwException(Translator.get("i18n_sql_not_empty"));
        }
        this.userEntity = userEntity;
        this.preparedBindings.clear();
        this.preparedBindingIndex = 0;
        sql = sql.trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }
        if (StringUtils.isNotEmpty(sqlVariableDetails)) {
            defaultsSqlVariableDetails = JsonUtil.parseList(sqlVariableDetails, listTypeReference);
        }
        List<TableFieldWithValue> tableFieldWithValues = new ArrayList<>();
        Pattern pattern = Pattern.compile(deVariablePattern);
        Matcher matcher = pattern.matcher(sql);
        StringBuilder sqlBuilder = new StringBuilder();
        int lastIndex = 0;
        while (matcher.find()) {
            sqlBuilder.append(sql, lastIndex, matcher.start());
            String sqlItemWithParam = matcher.group();
            String sqlItem = sqlItemWithParam.substring(10, sqlItemWithParam.length() - 1);
            boolean replaceParam = false;
            List<TableFieldWithValue> sqlItemFieldWithValues = new ArrayList<>();
            Pattern p = Pattern.compile(sqlParamsRegex);
            Matcher m = p.matcher(sqlItem);
            StringBuilder sqlItemBuilder = new StringBuilder();
            int sqlItemLastIndex = 0;
            while (m.find()) {
                if (m.start() < sqlItemLastIndex) {
                    continue;
                }
                String sqlVariable = m.group();
                boolean replaceParamItem = false;
                String variableName = sqlVariable.substring(2, sqlVariable.length() - 1);
                QuotedLiteralContext quotedLiteralContext = findQuotedLiteralContext(sqlItem, m.start(), m.end());
                int appendEnd = quotedLiteralContext == null ? m.start() : quotedLiteralContext.start();
                if (appendEnd < sqlItemLastIndex) {
                    continue;
                }
                sqlItemBuilder.append(sqlItem, sqlItemLastIndex, appendEnd);
                PreparedSqlFragment preparedSqlFragment;
                if (quotedLiteralContext != null) {
                    preparedSqlFragment = buildPreparedSqlFragmentForQuotedLiteral(quotedLiteralContext, parameters, isEdit, isFromDataSet);
                    if (preparedSqlFragment != null) {
                        sqlItemBuilder.append(preparedSqlFragment.replacement());
                        sqlItemLastIndex = quotedLiteralContext.end() + 1;
                        sqlItemFieldWithValues.addAll(preparedSqlFragment.tableFieldWithValues());
                        replaceParamItem = true;
                    }
                } else {
                    preparedSqlFragment = resolvePreparedSqlFragment(variableName, parameters, isEdit, isFromDataSet);
                    if (preparedSqlFragment != null) {
                        sqlItemBuilder.append(preparedSqlFragment.replacement());
                        sqlItemLastIndex = m.end();
                        sqlItemFieldWithValues.addAll(preparedSqlFragment.tableFieldWithValues());
                        replaceParamItem = true;
                    }
                }
                if (!replaceParamItem) {
                    if (quotedLiteralContext != null) {
                        sqlItemBuilder.append(sqlItem, appendEnd, quotedLiteralContext.end() + 1);
                        sqlItemLastIndex = quotedLiteralContext.end() + 1;
                    } else {
                        sqlItemBuilder.append(sqlItem, sqlItemLastIndex, m.end());
                        sqlItemLastIndex = m.end();
                    }
                }
                if (!replaceParamItem) {
                    replaceParam = false;
                    break;
                } else {
                    replaceParam = true;
                }
            }
            if (replaceParam) {
                sqlItemBuilder.append(sqlItem.substring(sqlItemLastIndex));
                sqlItem = sqlItemBuilder.toString();
            }
            p = Pattern.compile(sysVariableRegex);
            m = p.matcher(sqlItem);
            StringBuilder sysItemBuilder = new StringBuilder();
            int sysItemLastIndex = 0;
            while (m.find()) {
                if (m.start() < sysItemLastIndex) {
                    continue;
                }
                boolean replaceParamItem = false;

                String sysVariableId = m.group().substring(7, m.group().length() - 1);
                if (!isParams(sysVariableId)) {
                    continue;
                }
                QuotedLiteralContext quotedLiteralContext = findQuotedLiteralContext(sqlItem, m.start(), m.end());
                int appendEnd = quotedLiteralContext == null ? m.start() : quotedLiteralContext.start();
                if (appendEnd < sysItemLastIndex) {
                    continue;
                }
                sysItemBuilder.append(sqlItem, sysItemLastIndex, appendEnd);
                PreparedSqlFragment preparedSqlFragment;
                if (quotedLiteralContext != null) {
                    preparedSqlFragment = buildPreparedSqlFragmentForQuotedSysLiteral(quotedLiteralContext);
                    if (preparedSqlFragment != null) {
                        sysItemBuilder.append(preparedSqlFragment.replacement());
                        sysItemLastIndex = quotedLiteralContext.end() + 1;
                        sqlItemFieldWithValues.addAll(preparedSqlFragment.tableFieldWithValues());
                        replaceParamItem = true;
                    }
                } else {
                    String substitutedSql = sqlItem.replace(m.group(), SysParamsSubstitutedParams + sysVariableId);
                    try {
                        Expression expression = CCJSqlParserUtil.parseCondExpression(substitutedSql);
                        if (expression instanceof InExpression) {
                            preparedSqlFragment = buildPreparedSysSqlFragment(sysVariableId, true);
                        } else {
                            preparedSqlFragment = buildPreparedSysSqlFragment(sysVariableId, false);
                        }
                        if (preparedSqlFragment != null) {
                            sysItemBuilder.append(preparedSqlFragment.replacement());
                            sysItemLastIndex = m.end();
                            sqlItemFieldWithValues.addAll(preparedSqlFragment.tableFieldWithValues());
                            replaceParamItem = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (!replaceParamItem) {
                    if (quotedLiteralContext != null) {
                        sysItemBuilder.append(sqlItem, appendEnd, quotedLiteralContext.end() + 1);
                        sysItemLastIndex = quotedLiteralContext.end() + 1;
                    } else {
                        sysItemBuilder.append(sqlItem, sysItemLastIndex, m.end());
                        sysItemLastIndex = m.end();
                    }
                }
                if (!replaceParamItem) {
                    replaceParam = false;
                    break;
                } else {
                    replaceParam = true;
                }
            }
            if (replaceParam) {
                sysItemBuilder.append(sqlItem.substring(sysItemLastIndex));
                sqlItem = sysItemBuilder.toString();
            }
            if (!replaceParam) {
                sqlBuilder.append(SubstitutedSql);
            } else {
                sqlBuilder.append(sqlItem);
                tableFieldWithValues.addAll(sqlItemFieldWithValues);
            }
            lastIndex = matcher.end();
        }
        sqlBuilder.append(sql.substring(lastIndex));
        sql = sqlBuilder.toString();

        try {
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

                sql = replaceQuotedIdentifiers(sql, prefix, suffix);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalizePreparedSql(sql);
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

    private boolean isQuotedVariable(String sqlItem, int start, int end) {
        return start > 0
                && end < sqlItem.length()
                && sqlItem.charAt(start - 1) == '\''
                && sqlItem.charAt(end) == '\'';
    }

    private QuotedLiteralContext findQuotedLiteralContext(String sqlItem, int start, int end) {
        int literalStart = -1;
        for (int i = 0; i < sqlItem.length(); i++) {
            if (sqlItem.charAt(i) != '\'') {
                continue;
            }
            if (literalStart < 0) {
                literalStart = i;
                continue;
            }
            if (i + 1 < sqlItem.length() && sqlItem.charAt(i + 1) == '\'') {
                i++;
                continue;
            }
            if (start > literalStart && end <= i) {
                return new QuotedLiteralContext(literalStart, i, sqlItem.substring(literalStart + 1, i));
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
        return buildPreparedSqlFragment(Collections.singletonList(tableFieldWithValue));
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
        Matcher matcher = Pattern.compile(sqlParamsRegex).matcher(literalContent);
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

    private PreparedSqlFragment buildPreparedSqlFragmentForQuotedSysLiteral(QuotedLiteralContext quotedLiteralContext) {
        List<LiteralSegment> literalSegments = parseSysLiteralSegments(quotedLiteralContext.content());
        if (literalSegments.isEmpty()) {
            return null;
        }
        if (literalSegments.size() == 1 && literalSegments.get(0).variable()) {
            return buildPreparedSysSqlFragment(literalSegments.get(0).content(), false);
        }
        StringBuilder preparedValueBuilder = new StringBuilder();
        boolean hasVariable = false;
        for (LiteralSegment literalSegment : literalSegments) {
            if (!literalSegment.variable()) {
                preparedValueBuilder.append(unescapeQuotedLiteralText(literalSegment.content()));
                continue;
            }
            if (!isParams(literalSegment.content())) {
                return null;
            }
            hasVariable = true;
            String preparedValue = resolveQuotedSysLiteralVariableValue(literalSegment.content());
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
        return buildPreparedSqlFragment(Collections.singletonList(tableFieldWithValue));
    }

    private String resolveQuotedSysLiteralVariableValue(String sysVariableId) {
        SysVariableBinding sysVariableBinding = resolveSysVariableBinding(sysVariableId, false);
        if (sysVariableBinding == null || CollectionUtils.isEmpty(sysVariableBinding.values())) {
            return null;
        }
        if (sysVariableBinding.values().size() != 1) {
            DEException.throwException("SQL模板字符串仅支持单值参数");
        }
        return sysVariableBinding.values().get(0);
    }

    private List<LiteralSegment> parseSysLiteralSegments(String literalContent) {
        List<LiteralSegment> literalSegments = new ArrayList<>();
        Matcher matcher = Pattern.compile(sysVariableRegex).matcher(literalContent);
        int lastIndex = 0;
        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                literalSegments.add(new LiteralSegment(false, literalContent.substring(lastIndex, matcher.start())));
            }
            literalSegments.add(new LiteralSegment(true, matcher.group().substring(7, matcher.group().length() - 1)));
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
        List<String> preparedValues = resolvePreparedValues(sqlVariableDetails);
        for (String preparedValue : preparedValues) {
            values.add(buildPreparedValue(sqlVariableDetails, preparedValue));
        }
        return buildPreparedSqlFragment(values);
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

    private PreparedSqlFragment buildPreparedSqlFragment(List<TableFieldWithValue> tableFieldWithValues) {
        List<String> replacements = new ArrayList<>();
        for (TableFieldWithValue tableFieldWithValue : tableFieldWithValues) {
            replacements.add(registerPreparedBinding(tableFieldWithValue));
        }
        return new PreparedSqlFragment(String.join(",", replacements), tableFieldWithValues);
    }

    private String registerPreparedBinding(TableFieldWithValue tableFieldWithValue) {
        String token = PREPARED_BINDING_TOKEN_PREFIX + preparedBindingIndex++;
        preparedBindings.put(token, tableFieldWithValue);
        return "'" + token + "'";
    }

    private SqlVariableHandleResult finalizePreparedSql(String sql) {
        Pattern tokenPattern = Pattern.compile("'(" + PREPARED_BINDING_TOKEN_PREFIX + "\\d+)'");
        Matcher matcher = tokenPattern.matcher(sql);
        StringBuilder sqlBuilder = new StringBuilder();
        List<TableFieldWithValue> orderedBindings = new ArrayList<>();
        while (matcher.find()) {
            orderedBindings.add(preparedBindings.get(matcher.group(1)));
            matcher.appendReplacement(sqlBuilder, "?");
        }
        matcher.appendTail(sqlBuilder);
        SqlVariableHandleResult result = new SqlVariableHandleResult(sqlBuilder.toString());
        result.setTableFieldWithValues(orderedBindings);
        return result;
    }

    private record PreparedSqlFragment(String replacement, List<TableFieldWithValue> tableFieldWithValues) {
    }

    private record QuotedLiteralContext(int start, int end, String content) {
    }

    private record LiteralSegment(boolean variable, String content) {
    }

    private String replaceQuotedIdentifiers(String sql, String prefix, String suffix) {
        Matcher matcher = Pattern.compile("(`.*?`)").matcher(sql);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String group = matcher.group();
            String info = group.substring(1, group.length() - 1);
            matcher.appendReplacement(builder, Matcher.quoteReplacement(prefix + info + suffix));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    private PreparedSqlFragment buildPreparedSysSqlFragment(String sysVariableId, boolean inOperator) {
        SysVariableBinding sysVariableBinding = resolveSysVariableBinding(sysVariableId, inOperator);
        if (sysVariableBinding == null || CollectionUtils.isEmpty(sysVariableBinding.values())) {
            return null;
        }
        List<TableFieldWithValue> values = new ArrayList<>();
        for (String value : sysVariableBinding.values()) {
            SqlVariableDetails sqlVariableDetails = new SqlVariableDetails();
            sqlVariableDetails.setVariableName(sysVariableId);
            sqlVariableDetails.setOperator(inOperator ? "in" : "eq");
            sqlVariableDetails.setDeType(sysVariableBinding.deType());
            values.add(buildPreparedValue(sqlVariableDetails, value));
        }
        return buildPreparedSqlFragment(values);
    }

    private SysVariableBinding resolveSysVariableBinding(String sysVariableId, boolean inOperator) {
        if (userEntity != null) {
            if (sysVariableId.equalsIgnoreCase("sysParams.userId")) {
                return buildSysVariableBinding(0, inOperator ? Collections.singletonList(userEntity.getAccount()) : Collections.singletonList(userEntity.getAccount()));
            }
            if (sysVariableId.equalsIgnoreCase("sysParams.userEmail")) {
                return buildSysVariableBinding(0, Collections.singletonList(userEntity.getEmail()));
            }
            if (sysVariableId.equalsIgnoreCase("sysParams.userName")) {
                return buildSysVariableBinding(0, Collections.singletonList(userEntity.getName()));
            }
            if (sysVariableId.equalsIgnoreCase("sysParams.userPhone")) {
                return buildSysVariableBinding(0, Collections.singletonList(userEntity.getPhone()));
            }
            for (SysVariableValueItem variable : userEntity.getVariables()) {
                if (!variable.isValid()) {
                    continue;
                }
                if (!sysVariableId.equalsIgnoreCase(variable.getVariableId().toString())) {
                    continue;
                }
                if (variable.getSysVariableDto().getType().equalsIgnoreCase("text")) {
                    List<String> values = new ArrayList<>();
                    for (SysVariableValueDto sysVariableValueDto : variable.getValueList()) {
                        if (variable.getVariableValueIds().contains(sysVariableValueDto.getId().toString())) {
                            values.add(sysVariableValueDto.getValue());
                            if (!inOperator) {
                                break;
                            }
                        }
                    }
                    return buildSysVariableBinding(0, values);
                } else {
                    int deType = variable.getSysVariableDto().getType().equalsIgnoreCase("num") ? 2 : 1;
                    return buildSysVariableBinding(deType, Collections.singletonList(variable.getVariableValue()));
                }
            }
            return null;
        } else {
            return null;
        }
    }

    private SysVariableBinding buildSysVariableBinding(int deType, List<String> values) {
        List<String> validValues = values == null ? Collections.emptyList() : values.stream().filter(StringUtils::isNotEmpty).toList();
        if (CollectionUtils.isEmpty(validValues)) {
            return null;
        }
        return new SysVariableBinding(deType, validValues);
    }

    private record SysVariableBinding(int deType, List<String> values) {
    }
}

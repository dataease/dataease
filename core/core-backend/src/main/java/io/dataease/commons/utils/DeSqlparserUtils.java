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
    private UserFormVO userEntity;
    private static final String SubstitutedSql = " 'DE-BI' = 'DE-BI' ";
    private final List<Map<String, String>> sysParams = new ArrayList<>();
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
                String sqlVariable = m.group();
                sqlItemBuilder.append(sqlItem, sqlItemLastIndex, m.start());
                boolean replaceParamItem = false;
                String variableName = sqlVariable.substring(2, sqlVariable.length() - 1);
                SqlVariableDetails defaultsSqlVariableDetail = findSqlVariableDetail(defaultsSqlVariableDetails, variableName);
                SqlVariableDetails filterParameter = findSqlVariableDetail(parameters, variableName);
                if (filterParameter != null) {
                    PreparedSqlFragment preparedSqlFragment = buildPreparedSqlFragment(filterParameter);
                    boolean quoted = isQuotedVariable(sqlItem, m.start(), m.end());
                    if (quoted) {
                        sqlItemBuilder.setLength(sqlItemBuilder.length() - 1);
                    }
                    sqlItemBuilder.append(preparedSqlFragment.replacement());
                    sqlItemLastIndex = quoted ? m.end() + 1 : m.end();
                    sqlItemFieldWithValues.addAll(preparedSqlFragment.tableFieldWithValues());
                    replaceParamItem = true;
                } else {
                    if (defaultsSqlVariableDetail != null && StringUtils.isNotEmpty(defaultsSqlVariableDetail.getDefaultValue())) {
                        if (!isEdit && isFromDataSet && defaultsSqlVariableDetail.getDefaultValueScope().equals(SqlVariableDetails.DefaultValueScope.ALLSCOPE)) {
                            PreparedSqlFragment preparedSqlFragment = buildPreparedSqlFragmentForDefaultValue(defaultsSqlVariableDetail);
                            boolean quoted = isQuotedVariable(sqlItem, m.start(), m.end());
                            if (quoted) {
                                sqlItemBuilder.setLength(sqlItemBuilder.length() - 1);
                            }
                            sqlItemBuilder.append(preparedSqlFragment.replacement());
                            sqlItemLastIndex = quoted ? m.end() + 1 : m.end();
                            sqlItemFieldWithValues.addAll(preparedSqlFragment.tableFieldWithValues());
                            replaceParamItem = true;
                        }
                        if (isEdit) {
                            PreparedSqlFragment preparedSqlFragment = buildPreparedSqlFragmentForDefaultValue(defaultsSqlVariableDetail);
                            boolean quoted = isQuotedVariable(sqlItem, m.start(), m.end());
                            if (quoted) {
                                sqlItemBuilder.setLength(sqlItemBuilder.length() - 1);
                            }
                            sqlItemBuilder.append(preparedSqlFragment.replacement());
                            sqlItemLastIndex = quoted ? m.end() + 1 : m.end();
                            sqlItemFieldWithValues.addAll(preparedSqlFragment.tableFieldWithValues());
                            replaceParamItem = true;
                        }
                    }
                }
                if (!replaceParamItem) {
                    sqlItemBuilder.append(sqlItem, sqlItemLastIndex, m.end());
                    sqlItemLastIndex = m.end();
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
            while (m.find()) {
                boolean replaceParamItem = false;

                String sysVariableId = m.group().substring(7, m.group().length() - 1);
                if (!isParams(sysVariableId)) {
                    continue;
                }
                sqlItem = sqlItem.replace(m.group(), SysParamsSubstitutedParams + sysVariableId);
                try {
                    Expression expression = CCJSqlParserUtil.parseCondExpression(sqlItem);
                    String value = null;
                    if (expression instanceof InExpression) {
                        value = handleSubstitutedSqlForIn(sysVariableId);
                    } else {
                        value = handleSubstitutedSql(sysVariableId);
                    }
                    if (StringUtils.isNotEmpty(value)) {
                        sqlItem = sqlItem.replace(SysParamsSubstitutedParams + sysVariableId, value);
                        replaceParamItem = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!replaceParamItem) {
                    replaceParam = false;
                    break;
                } else {
                    replaceParam = true;
                }
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

                Pattern patternCross = Pattern.compile("(`.*?`)");
                Matcher matcherCross = patternCross.matcher(sql);
                while (matcherCross.find()) {
                    String group = matcherCross.group();
                    String info = group.substring(1, group.length() - 1);
                    sql = sql.replaceAll(group, prefix + info + suffix);
                }
            }
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

    private boolean isQuotedVariable(String sqlItem, int start, int end) {
        return start > 0
                && end < sqlItem.length()
                && sqlItem.charAt(start - 1) == '\''
                && sqlItem.charAt(end) == '\'';
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
        if (StringUtils.equals(sqlVariableDetails.getOperator(), "between")) {
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

    private String handleSubstitutedSql(String sysVariableId) {
        if (userEntity != null) {
            if (sysVariableId.equalsIgnoreCase("sysParams.userId")) {
                return userEntity.getAccount();
            }
            if (sysVariableId.equalsIgnoreCase("sysParams.userEmail")) {
                return userEntity.getEmail();
            }
            if (sysVariableId.equalsIgnoreCase("sysParams.userName")) {
                return userEntity.getName();
            }
            if (sysVariableId.equalsIgnoreCase("sysParams.userPhone")) {
                return userEntity.getPhone();
            }
            for (SysVariableValueItem variable : userEntity.getVariables()) {
                if (!variable.isValid()) {
                    continue;
                }
                if (!sysVariableId.equalsIgnoreCase(variable.getVariableId().toString())) {
                    continue;
                }
                if (variable.getSysVariableDto().getType().equalsIgnoreCase("text")) {
                    for (SysVariableValueDto sysVariableValueDto : variable.getValueList()) {
                        if (variable.getVariableValueIds().contains(sysVariableValueDto.getId().toString())) {
                            return sysVariableValueDto.getValue();
                        }
                    }
                } else {
                    return variable.getVariableValue();
                }
            }
            return null;
        } else {
            return null;
        }
    }


    private String handleSubstitutedSqlForIn(String sysVariableId) {
        if (userEntity != null) {
            for (SysVariableValueItem variable : userEntity.getVariables()) {
                List<String> values = new ArrayList<>();
                if (!variable.isValid()) {
                    continue;
                }
                if (!sysVariableId.equalsIgnoreCase(variable.getVariableId().toString())) {
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
                    return "'" + String.join("','", values) + "'";
                }
            }
            return null;
        } else {
            return null;
        }
    }
}

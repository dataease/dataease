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
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

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
        Pattern pattern = Pattern.compile(deVariablePattern);
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            String sqlItemWithParam = matcher.group();
            String sqlItem = sqlItemWithParam.substring(10, sqlItemWithParam.length() - 1);
            boolean replaceParam = false;
            Pattern p = Pattern.compile(sqlParamsRegex);
            Matcher m = p.matcher(sqlItemWithParam);
            while (m.find()) {
                String sqlVariable = m.group();
                boolean replaceParamItem = false;
                SqlVariableDetails defaultsSqlVariableDetail = null;
                for (SqlVariableDetails sqlVariableDetail : defaultsSqlVariableDetails) {
                    if (sqlVariable.substring(2, sqlVariable.length() - 1).equalsIgnoreCase(sqlVariableDetail.getVariableName())) {
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
                    sqlItem = sqlItem.replace(sqlVariable, transFilter(filterParameter, dsMap));
                    replaceParamItem = true;
                } else {
                    if (defaultsSqlVariableDetail != null && StringUtils.isNotEmpty(defaultsSqlVariableDetail.getDefaultValue())) {
                        if (!isEdit && isFromDataSet && defaultsSqlVariableDetail.getDefaultValueScope().equals(SqlVariableDetails.DefaultValueScope.ALLSCOPE)) {
                            sqlItem = sqlItem.replace(sqlVariable, defaultsSqlVariableDetail.getDefaultValue());
                            replaceParamItem = true;
                        }
                        if (isEdit) {
                            sqlItem = sqlItem.replace(sqlVariable, defaultsSqlVariableDetail.getDefaultValue());
                            replaceParamItem = true;
                        }
                    }
                }
                if (!replaceParamItem) {
                    replaceParam = false;
                    break;
                } else {
                    replaceParam = true;
                }
            }
            p = Pattern.compile(sysVariableRegex);
            m = p.matcher(sqlItemWithParam);
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
                    if (value != null) {
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
                sql = sql.replace(sqlItemWithParam, SubstitutedSql);
            } else {
                sql = sql.replace(sqlItemWithParam, sqlItem);
            }
        }

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
        return sql;
    }

    private static boolean isParams(String paramId) {
        if (Arrays.asList("sysParams.userId", "sysParams.userEmail", "sysParams.userName").contains(paramId)) {
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


    private String transFilter(SqlVariableDetails sqlVariableDetails, Map<Long, DatasourceSchemaDTO> dsMap) {
        if (sqlVariableDetails.getOperator().equals("in")) {
            if (StringUtils.equalsIgnoreCase(dsMap.entrySet().iterator().next().getValue().getType(), DatasourceConfiguration.DatasourceType.sqlServer.getType()) && sqlVariableDetails.getDeType() == 0) {
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




package io.dataease.engine.trans;

import io.dataease.engine.constant.SQLConstants;
import io.dataease.engine.utils.Utils;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.constant.SqlPlaceholderConstants;
import io.dataease.extensions.datasource.dto.CalParam;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.model.SQLObj;
import io.dataease.extensions.view.dto.ChartExtFilterDTO;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Junjun
 */
public class ExtWhere2Str {

    public static void extWhere2sqlOjb(SQLMeta meta, List<ChartExtFilterDTO> fields, List<DatasetTableFieldDTO> originFields, boolean isCross, Map<Long, DatasourceSchemaDTO> dsMap, List<CalParam> fieldParam, List<CalParam> chartParam, PluginManageApi pluginManage) {
        SQLObj tableObj = meta.getTable();
        if (ObjectUtils.isEmpty(tableObj)) {
            return;
        }
        Map<String, String> paramMap = Utils.mergeParam(fieldParam, chartParam);
        List<SQLObj> list = new ArrayList<>();
        Map<String, String> fieldsDialect = new HashMap<>();
        if (ObjectUtils.isNotEmpty(fields)) {
            for (ChartExtFilterDTO request : fields) {
                List<String> value = request.getValue();

                List<String> whereNameList = new ArrayList<>();
                List<DatasetTableFieldDTO> fieldList = new ArrayList<>();
                if (request.getIsTree()) {
                    fieldList.addAll(request.getDatasetTableFieldList());
                } else {
                    fieldList.add(request.getDatasetTableField());
                }

                for (DatasetTableFieldDTO field : fieldList) {
                    if (ObjectUtils.isEmpty(value) || ObjectUtils.isEmpty(field)) {
                        continue;
                    }
                    String whereName = "";

                    String originName;
                    if (ObjectUtils.isNotEmpty(field.getExtField()) && field.getExtField() == 2) {
                        // 解析origin name中有关联的字段生成sql表达式
                        String calcFieldExp = Utils.calcFieldRegex(field.getOriginName(), tableObj, originFields, isCross, dsMap, paramMap, pluginManage);
                        // 给计算字段处加一个占位符，后续SQL方言转换后再替换
                        originName = String.format(SqlPlaceholderConstants.CALC_FIELD_PLACEHOLDER, field.getId());
                        fieldsDialect.put(originName, calcFieldExp);
                        if (isCross) {
                            originName = calcFieldExp;
                        }
                    } else if (ObjectUtils.isNotEmpty(field.getExtField()) && field.getExtField() == 1) {
                        originName = String.format(SQLConstants.FIELD_NAME, tableObj.getTableAlias(), field.getDataeaseName());
                    } else {
                        originName = String.format(SQLConstants.FIELD_NAME, tableObj.getTableAlias(), field.getDataeaseName());
                    }

                    if (field.getDeType() == 1) {
                        if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                            // 此处获取标准格式的日期
                            whereName = String.format(SQLConstants.DE_STR_TO_DATE, originName, StringUtils.isEmpty(field.getDateFormat()) ? SQLConstants.DEFAULT_DATE_FORMAT : field.getDateFormat());
                        }
                        if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                            String cast = String.format(SQLConstants.CAST, originName, SQLConstants.DEFAULT_INT_FORMAT);
                            // 此处获取标准格式的日期
                            whereName = String.format(SQLConstants.FROM_UNIXTIME, cast, SQLConstants.DEFAULT_DATE_FORMAT);
                            if (isCross) {
                                whereName = String.format(SQLConstants.UNIX_TIMESTAMP, whereName);
                            }
                        }
                        if (field.getDeExtractType() == 1) {
                            // 如果都是时间类型，把date和time类型进行字符串拼接
                            if (isCross) {
                                if (StringUtils.equalsIgnoreCase(field.getType(), "date")) {
                                    originName = String.format(SQLConstants.DE_STR_TO_DATE, String.format(SQLConstants.CONCAT, originName, "' 00:00:00'"), SQLConstants.DEFAULT_DATE_FORMAT);
                                } else if (StringUtils.equalsIgnoreCase(field.getType(), "time")) {
                                    originName = String.format(SQLConstants.DE_STR_TO_DATE, String.format(SQLConstants.CONCAT, "'1970-01-01 '", originName), SQLConstants.DEFAULT_DATE_FORMAT);
                                }
                            }
                            // 此处获取标准格式的日期
                            whereName = originName;
                        }
                    } else if (field.getDeType() == 2 || field.getDeType() == 3) {
                        if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                            whereName = String.format(SQLConstants.CAST, originName, SQLConstants.DEFAULT_FLOAT_FORMAT);
                        }
                        if (field.getDeExtractType() == 1) {
                            whereName = String.format(SQLConstants.UNIX_TIMESTAMP, originName);
                        }
                        if (field.getDeExtractType() == 2 || field.getDeExtractType() == 4) {
                            whereName = String.format(SQLConstants.CAST, originName, SQLConstants.DEFAULT_INT_FORMAT);
                        }
                        if (field.getDeExtractType() == 3) {
                            whereName = String.format(SQLConstants.CAST, originName, SQLConstants.DEFAULT_FLOAT_FORMAT);
                        }
                    } else {
                        whereName = originName;
                    }
                    whereNameList.add(whereName);
                }

                String whereName = "";
                if (request.getIsTree()) {
                    whereName = "CONCAT(" + StringUtils.join(whereNameList, ",',',") + ")";
                } else {
                    whereName = whereNameList.get(0);
                }
                String whereTerm = Utils.transFilterTerm(request.getOperator());
                String whereValue = "";

                if (StringUtils.containsIgnoreCase(request.getOperator(), "-")) {
                    String[] split = request.getOperator().split("-");
                    String term1 = split[0];
                    String logic = split[1];
                    String term2 = split[2];
                    whereValue = Utils.transFilterTerm(term1) + getValue(term1, value.get(0)) + " " + logic + " " + whereName + Utils.transFilterTerm(term2) + getValue(term2, value.get(1));
                } else if (StringUtils.containsIgnoreCase(request.getOperator(), "in")) {
                    // 过滤空数据
                    if (value.contains(SQLConstants.EMPTY_SIGN)) {
                        whereValue = "('" + StringUtils.join(value, "','") + "', '')" + " or " + whereName + " is null ";
                    } else {
                        if (StringUtils.equalsIgnoreCase(request.getDatasetTableField().getType(), "NVARCHAR")) {
                            whereValue = "(" + value.stream().map(str -> "N" + "'" + str + "'").collect(Collectors.joining(",")) + ")";
                        } else {
                            whereValue = "('" + StringUtils.join(value, "','") + "')";
                        }
                    }
                } else if (StringUtils.containsIgnoreCase(request.getOperator(), "like")) {
                    if (StringUtils.equalsIgnoreCase(request.getDatasetTableField().getType(), "NVARCHAR")) {
                        whereValue = "N'%" + value.get(0) + "%'";
                    } else {
                        whereValue = "'%" + value.get(0) + "%'";
                    }
                } else if (StringUtils.containsIgnoreCase(request.getOperator(), "between")) {
                    if (request.getDatasetTableField().getDeType() == 1) {
                        if (request.getDatasetTableField().getDeExtractType() == 2
                                || request.getDatasetTableField().getDeExtractType() == 3
                                || request.getDatasetTableField().getDeExtractType() == 4) {
                            if (isCross) {
                                whereValue = String.format(SQLConstants.WHERE_VALUE_BETWEEN, value.get(0), value.get(1));
                            } else {
                                whereValue = String.format(SQLConstants.WHERE_BETWEEN, Utils.transLong2Str(Long.parseLong(value.get(0))), Utils.transLong2Str(Long.parseLong(value.get(1))));
                            }
                        } else {
                            if (isCross) {
                                whereName = String.format(SQLConstants.UNIX_TIMESTAMP, whereName);
                                whereValue = String.format(SQLConstants.WHERE_BETWEEN, Long.parseLong(value.get(0)), Long.parseLong(value.get(1)));
                            } else {
                                whereValue = String.format(SQLConstants.WHERE_BETWEEN, Utils.transLong2Str(Long.parseLong(value.get(0))), Utils.transLong2Str(Long.parseLong(value.get(1))));
                            }
                        }
                    } else {
                        whereValue = String.format(SQLConstants.WHERE_BETWEEN, value.get(0), value.get(1));
                    }
                } else {
                    // 过滤空数据
                    if (StringUtils.equals(value.get(0), SQLConstants.EMPTY_SIGN)) {
                        whereValue = String.format(SQLConstants.WHERE_VALUE_VALUE, "") + " or " + whereName + " is null ";
                    } else {
                        if (StringUtils.equalsIgnoreCase(request.getDatasetTableField().getType(), "NVARCHAR")) {
                            whereValue = String.format(SQLConstants.WHERE_VALUE_VALUE_CH, value.get(0));
                        } else {
                            whereValue = String.format(SQLConstants.WHERE_VALUE_VALUE, value.get(0));
                        }
                    }
                }
                list.add(SQLObj.builder()
                        .whereField(whereName)
                        .whereTermAndValue(whereTerm + whereValue)
                        .build());
            }
            List<String> strList = new ArrayList<>();
            list.forEach(ele -> strList.add("(" + ele.getWhereField() + " " + ele.getWhereTermAndValue() + ")"));
            meta.setExtWheres(ObjectUtils.isNotEmpty(list) ? "(" + String.join(" AND ", strList) + ")" : null);
        }
        meta.setExtWheresDialect(fieldsDialect);
    }

    private static String getValue(String term, String value) {
        switch (term) {
            case "like":
                return "'%" + value + "%'";
            case "eq":
                return "'" + value + "'";
        }
        return null;
    }

}

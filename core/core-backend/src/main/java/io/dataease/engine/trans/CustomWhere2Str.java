package io.dataease.engine.trans;

import io.dataease.engine.constant.SQLConstants;
import io.dataease.engine.utils.Utils;
import io.dataease.extensions.datasource.constant.SqlPlaceholderConstants;
import io.dataease.extensions.datasource.dto.CalParam;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.model.SQLObj;
import io.dataease.extensions.view.filter.DynamicTimeSetting;
import io.dataease.extensions.view.filter.FilterTreeItem;
import io.dataease.extensions.view.filter.FilterTreeObj;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @Author Junjun
 */
public class CustomWhere2Str {

    public static void customWhere2sqlObj(SQLMeta meta, FilterTreeObj tree, List<DatasetTableFieldDTO> originFields, boolean isCross, Map<Long, DatasourceSchemaDTO> dsMap, List<CalParam> fieldParam, List<CalParam> chartParam) {
        SQLObj tableObj = meta.getTable();
        if (ObjectUtils.isEmpty(tableObj)) {
            return;
        }
        List<String> res = new ArrayList<>();
        // permission trees
        // 解析每个tree，然后多个tree之间用and拼接
        // 每个tree，如果是sub tree节点，则使用递归合并成一组条件
        if (ObjectUtils.isEmpty(tree)) {
            return;
        }
        Map<String, String> fieldsDialect = new HashMap<>();
        String treeExp = transTreeToWhere(tableObj, tree, fieldsDialect, originFields, isCross, dsMap, fieldParam, chartParam);
        if (StringUtils.isNotEmpty(treeExp)) {
            res.add(treeExp);
        }
        meta.setCustomWheres(ObjectUtils.isNotEmpty(res) ? "(" + String.join(" AND ", res) + ")" : null);
        meta.setCustomWheresDialect(fieldsDialect);
    }

    private static String transTreeToWhere(SQLObj tableObj, FilterTreeObj tree, Map<String, String> fieldsDialect, List<DatasetTableFieldDTO> originFields, boolean isCross, Map<Long, DatasourceSchemaDTO> dsMap, List<CalParam> fieldParam, List<CalParam> chartParam) {
        if (ObjectUtils.isEmpty(tree)) {
            return null;
        }
        String logic = tree.getLogic();
        List<FilterTreeItem> items = tree.getItems();
        List<String> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(items)) {
            // type is item or tree
            for (FilterTreeItem item : items) {
                String exp = null;
                if (StringUtils.equalsIgnoreCase(item.getType(), "item")) {
                    // 单个item拼接SQL，最后根据logic汇总
                    exp = transTreeItem(tableObj, item, fieldsDialect, originFields, isCross, dsMap, fieldParam, chartParam);
                } else if (StringUtils.equalsIgnoreCase(item.getType(), "tree")) {
                    // 递归tree
                    exp = transTreeToWhere(tableObj, item.getSubTree(), fieldsDialect, originFields, isCross, dsMap, fieldParam, chartParam);
                }
                if (StringUtils.isNotEmpty(exp)) {
                    list.add(exp);
                }
            }
        }
        return CollectionUtils.isNotEmpty(list) ? "(" + String.join(" " + logic + " ", list) + ")" : null;
    }

    private static String transTreeItem(SQLObj tableObj, FilterTreeItem item, Map<String, String> fieldsDialect, List<DatasetTableFieldDTO> originFields, boolean isCross, Map<Long, DatasourceSchemaDTO> dsMap, List<CalParam> fieldParam, List<CalParam> chartParam) {
        String res = null;
        DatasetTableFieldDTO field = item.getField();

        if (ObjectUtils.isEmpty(field)) {
            return null;
        }
        Map<String, String> paramMap = Utils.mergeParam(fieldParam, chartParam);
        String whereName = "";
        String originName;
        if (ObjectUtils.isNotEmpty(field.getExtField()) && field.getExtField() == 2) {
            // 解析origin name中有关联的字段生成sql表达式
            String calcFieldExp = Utils.calcFieldRegex(field.getOriginName(), tableObj, originFields, isCross, dsMap, paramMap);
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
                whereName = String.format(SQLConstants.DE_STR_TO_DATE, originName, StringUtils.isNotEmpty(field.getDateFormat()) ? field.getDateFormat() : SQLConstants.DEFAULT_DATE_FORMAT);
            }
            if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                String cast = String.format(SQLConstants.CAST, originName, SQLConstants.DEFAULT_INT_FORMAT);
                // 此处获取标准格式的日期
                whereName = String.format(SQLConstants.FROM_UNIXTIME, cast, SQLConstants.DEFAULT_DATE_FORMAT);
            }
            if (field.getDeExtractType() == 1) {
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
                whereName = originName;
            }
            if (field.getDeExtractType() == 3) {
                whereName = String.format(SQLConstants.CAST, originName, SQLConstants.DEFAULT_FLOAT_FORMAT);
            }
        } else {
            whereName = originName;
        }

        if (StringUtils.equalsIgnoreCase(item.getFilterType(), "enum")) {
            if (ObjectUtils.isNotEmpty(item.getEnumValue())) {
                res = "(" + whereName + " IN ('" + String.join("','", item.getEnumValue()) + "'))";
            }
        } else {
            if (field.getDeType() == 1) {
                // 规定几种日期格式，一一匹配，匹配到就是该格式
                whereName = String.format(SQLConstants.UNIX_TIMESTAMP, whereName);
            }

            String value = item.getValue();
            String whereTerm = Utils.transFilterTerm(item.getTerm());
            String whereValue = "";

            if (StringUtils.equalsIgnoreCase(item.getTerm(), "null")) {
                whereValue = "";
            } else if (StringUtils.equalsIgnoreCase(item.getTerm(), "not_null")) {
                whereValue = "";
            } else if (StringUtils.equalsIgnoreCase(item.getTerm(), "empty")) {
                whereValue = "''";
            } else if (StringUtils.equalsIgnoreCase(item.getTerm(), "not_empty")) {
                whereValue = "''";
            } else if (StringUtils.containsIgnoreCase(item.getTerm(), "in") || StringUtils.containsIgnoreCase(item.getTerm(), "not in")) {
                whereValue = "('" + String.join("','", value.split(",")) + "')";
            } else if (StringUtils.containsIgnoreCase(item.getTerm(), "like")) {
                whereValue = "'%" + value + "%'";
            } else {
                // 如果是时间字段过滤，当条件是等于和不等于的时候转换成between和not between
                if (field.getDeType() == 1) {
                    // 如果是动态时间，计算具体值
                    value = fixValue(item);
                    if (StringUtils.equalsIgnoreCase(whereTerm, " = ")) {
                        whereTerm = " BETWEEN ";
                        // 把value类似过滤组件处理，获得start time和end time
                        Map<String, Long> stringLongMap = Utils.parseDateTimeValue(value);
                        whereValue = String.format(SQLConstants.WHERE_VALUE_BETWEEN, stringLongMap.get("startTime"), stringLongMap.get("endTime"));
                    } else if (StringUtils.equalsIgnoreCase(whereTerm, " <> ")) {
                        whereTerm = " NOT BETWEEN ";
                        Map<String, Long> stringLongMap = Utils.parseDateTimeValue(value);
                        whereValue = String.format(SQLConstants.WHERE_VALUE_BETWEEN, stringLongMap.get("startTime"), stringLongMap.get("endTime"));
                    } else {
                        value = Utils.allDateFormat2Long(value) + "";
                        whereValue = String.format(SQLConstants.WHERE_VALUE_VALUE, value);
                    }
                } else {
                    whereValue = String.format(SQLConstants.WHERE_VALUE_VALUE, value);
                }
            }
            SQLObj build = SQLObj.builder()
                    .whereField(whereName)
                    .whereTermAndValue(whereTerm + whereValue)
                    .build();
            res = build.getWhereField() + " " + build.getWhereTermAndValue();
        }
        return res;
    }

    private static String fixValue(FilterTreeItem item) {
        if (StringUtils.isNotEmpty(item.getFilterTypeTime()) && StringUtils.equalsIgnoreCase(item.getFilterTypeTime(), "dynamicDate")) {
            DynamicTimeSetting dynamicTimeSetting = item.getDynamicTimeSetting();
            Calendar instance = Calendar.getInstance();
            if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getTimeGranularity(), "year")) {
                if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrent(), "thisYear")) {
                } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrent(), "lastYear")) {
                    instance.add(Calendar.YEAR, -1);
                } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrent(), "custom")) {
                    if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getAround(), "f")) {
                        instance.add(Calendar.YEAR, -dynamicTimeSetting.getTimeNum());
                    } else {
                        instance.add(Calendar.YEAR, dynamicTimeSetting.getTimeNum());
                    }
                }
                return "" + instance.get(Calendar.YEAR);
            } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getTimeGranularity(), "month")) {
                if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrent(), "thisMonth")) {
                } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrent(), "lastMonth")) {
                    instance.add(Calendar.MONTH, -1);
                } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrent(), "custom")) {
                    if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrentType(), "year")) {
                        if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getAround(), "f")) {
                            instance.add(Calendar.YEAR, -dynamicTimeSetting.getTimeNum());
                        } else {
                            instance.add(Calendar.YEAR, dynamicTimeSetting.getTimeNum());
                        }
                    } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrentType(), "month")) {
                        if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getAround(), "f")) {
                            instance.add(Calendar.MONTH, -dynamicTimeSetting.getTimeNum());
                        } else {
                            instance.add(Calendar.MONTH, dynamicTimeSetting.getTimeNum());
                        }
                    }
                }
                return instance.get(Calendar.YEAR) + "-" + (instance.get(Calendar.MONTH) + 1 < 10 ? "0" : "") + (instance.get(Calendar.MONTH) + 1);
            } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getTimeGranularity(), "date")) {
                if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrent(), "today")) {
                } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrent(), "yesterday")) {
                    instance.add(Calendar.DAY_OF_MONTH, -1);
                } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrent(), "monthBeginning")) {
                    instance.set(Calendar.DAY_OF_MONTH, 1);
                } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrent(), "yearBeginning")) {
                    instance.set(Calendar.MONTH, 0);
                    instance.set(Calendar.DAY_OF_MONTH, 1);
                } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrent(), "custom")) {
                    if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrentType(), "year")) {
                        if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getAround(), "f")) {
                            instance.add(Calendar.YEAR, -dynamicTimeSetting.getTimeNum());
                        } else {
                            instance.add(Calendar.YEAR, dynamicTimeSetting.getTimeNum());
                        }
                    } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrentType(), "month")) {
                        if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getAround(), "f")) {
                            instance.add(Calendar.MONTH, -dynamicTimeSetting.getTimeNum());
                        } else {
                            instance.add(Calendar.MONTH, dynamicTimeSetting.getTimeNum());
                        }
                    } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrentType(), "date")) {
                        if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getAround(), "f")) {
                            instance.add(Calendar.DAY_OF_MONTH, -dynamicTimeSetting.getTimeNum());
                        } else {
                            instance.add(Calendar.DAY_OF_MONTH, dynamicTimeSetting.getTimeNum());
                        }
                    }
                }
                return instance.get(Calendar.YEAR) + "-" + (instance.get(Calendar.MONTH) + 1 < 10 ? "0" : "") + (instance.get(Calendar.MONTH) + 1) + "-" + (instance.get(Calendar.DAY_OF_MONTH) < 10 ? "0" : "") + instance.get(Calendar.DAY_OF_MONTH);
            } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getTimeGranularity(), "datetime")) {
                String time = " 00:00:00";
                if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrent(), "today")) {
                } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrent(), "yesterday")) {
                    instance.add(Calendar.DAY_OF_MONTH, -1);
                } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrent(), "monthBeginning")) {
                    instance.set(Calendar.DAY_OF_MONTH, 1);
                } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrent(), "yearBeginning")) {
                    instance.set(Calendar.MONTH, 0);
                    instance.set(Calendar.DAY_OF_MONTH, 1);
                } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrent(), "custom")) {
                    time = " " + dynamicTimeSetting.getArbitraryTime().substring(11, 19);
                    if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrentType(), "year")) {
                        if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getAround(), "f")) {
                            instance.add(Calendar.YEAR, -dynamicTimeSetting.getTimeNum());
                        } else {
                            instance.add(Calendar.YEAR, dynamicTimeSetting.getTimeNum());
                        }
                    } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrentType(), "month")) {
                        if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getAround(), "f")) {
                            instance.add(Calendar.MONTH, -dynamicTimeSetting.getTimeNum());
                        } else {
                            instance.add(Calendar.MONTH, dynamicTimeSetting.getTimeNum());
                        }
                    } else if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getRelativeToCurrentType(), "date")) {
                        if (StringUtils.equalsIgnoreCase(dynamicTimeSetting.getAround(), "f")) {
                            instance.add(Calendar.DAY_OF_MONTH, -dynamicTimeSetting.getTimeNum());
                        } else {
                            instance.add(Calendar.DAY_OF_MONTH, dynamicTimeSetting.getTimeNum());
                        }
                    }
                }
                return instance.get(Calendar.YEAR) + "-" + (instance.get(Calendar.MONTH) + 1 < 10 ? "0" : "") + (instance.get(Calendar.MONTH) + 1) + "-" + (instance.get(Calendar.DAY_OF_MONTH) < 10 ? "0" : "") + instance.get(Calendar.DAY_OF_MONTH) + time;
            }
        }
        return item.getValue();
    }
}

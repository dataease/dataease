package io.dataease.engine.trans;

import io.dataease.extensions.datasource.constant.SqlPlaceholderConstants;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.api.permissions.dataset.dto.DataSetRowPermissionsTreeDTO;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.model.SQLObj;
import io.dataease.extensions.view.dto.DatasetRowPermissionsTreeItem;
import io.dataease.extensions.view.dto.DatasetRowPermissionsTreeObj;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.engine.constant.SQLConstants;
import io.dataease.engine.utils.Utils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @Author Junjun
 */
public class WhereTree2Str {
    public static void transFilterTrees(SQLMeta meta, List<DataSetRowPermissionsTreeDTO> requestList, List<DatasetTableFieldDTO> originFields, boolean isCross, Map<Long, DatasourceSchemaDTO> dsMap) {
        SQLObj tableObj = meta.getTable();
        if (ObjectUtils.isEmpty(tableObj)) {
            return;
        }
        if (CollectionUtils.isEmpty(requestList)) {
            return;
        }
        List<String> res = new ArrayList<>();
        Map<String, String> fieldsDialect = new HashMap<>();
        // permission trees
        // 解析每个tree，然后多个tree之间用and拼接
        // 每个tree，如果是sub tree节点，则使用递归合并成一组条件
        for (DataSetRowPermissionsTreeDTO request : requestList) {
            DatasetRowPermissionsTreeObj tree = request.getTree();
            if (ObjectUtils.isEmpty(tree)) {
                continue;
            }
            String treeExp = transTreeToWhere(tableObj, tree, originFields, fieldsDialect, isCross, dsMap);
            if (StringUtils.isNotEmpty(treeExp)) {
                res.add(treeExp);
            }
        }
        meta.setWhereTrees(CollectionUtils.isNotEmpty(res) ? "(" + String.join(" OR ", res) + ")" : null);
        meta.setWhereTreesDialect(fieldsDialect);
    }

    private static String transTreeToWhere(SQLObj tableObj, DatasetRowPermissionsTreeObj tree, List<DatasetTableFieldDTO> originFields, Map<String, String> fieldsDialect, boolean isCross, Map<Long, DatasourceSchemaDTO> dsMap) {
        if (ObjectUtils.isEmpty(tree)) {
            return null;
        }
        String logic = tree.getLogic();
        List<DatasetRowPermissionsTreeItem> items = tree.getItems();
        List<String> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(items)) {
            // type is item or tree
            for (DatasetRowPermissionsTreeItem item : items) {
                String exp = null;
                if (StringUtils.equalsIgnoreCase(item.getType(), "item")) {
                    // 单个item拼接SQL，最后根据logic汇总
                    exp = transTreeItem(tableObj, item, originFields, fieldsDialect, isCross, dsMap);
                } else if (StringUtils.equalsIgnoreCase(item.getType(), "tree")) {
                    // 递归tree
                    exp = transTreeToWhere(tableObj, item.getSubTree(), originFields, fieldsDialect, isCross, dsMap);
                }
                if (StringUtils.isNotEmpty(exp)) {
                    list.add(exp);
                }
            }
        }
        return CollectionUtils.isNotEmpty(list) ? "(" + String.join(" " + logic + " ", list) + ")" : null;
    }

    public static String transTreeItem(SQLObj tableObj, DatasetRowPermissionsTreeItem item, List<DatasetTableFieldDTO> originFields, Map<String, String> fieldsDialect, boolean isCross, Map<Long, DatasourceSchemaDTO> dsMap) {
        String res = null;
        DatasetTableFieldDTO field = item.getField();
        if (ObjectUtils.isEmpty(field)) {
            return null;
        }
        String whereName = "";
        String originName;
        if (ObjectUtils.isNotEmpty(field.getExtField()) && Objects.equals(field.getExtField(), ExtFieldConstant.EXT_CALC)) {
            // 解析origin name中有关联的字段生成sql表达式
            String calcFieldExp = Utils.calcFieldRegex(field.getOriginName(), tableObj, originFields, isCross, dsMap);
            // 给计算字段处加一个占位符，后续SQL方言转换后再替换
            originName = String.format(SqlPlaceholderConstants.CALC_FIELD_PLACEHOLDER, field.getId());
            fieldsDialect.put(originName, calcFieldExp);
            if (isCross) {
                originName = calcFieldExp;
            }
        } else if (ObjectUtils.isNotEmpty(field.getExtField()) && Objects.equals(field.getExtField(), ExtFieldConstant.EXT_COPY)) {
            originName = String.format(SQLConstants.FIELD_NAME, tableObj.getTableAlias(), field.getDataeaseName());
        } else {
            originName = String.format(SQLConstants.FIELD_NAME, tableObj.getTableAlias(), field.getDataeaseName());
        }
        if (field.getDeType() == 1) {
            if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                whereName = String.format(SQLConstants.DE_STR_TO_DATE, originName, StringUtils.isNotEmpty(field.getDateFormat()) ? field.getDateFormat() : SQLConstants.DEFAULT_DATE_FORMAT);
            }
            if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                String cast = String.format(SQLConstants.CAST, originName, SQLConstants.DEFAULT_INT_FORMAT);
                whereName = String.format(SQLConstants.FROM_UNIXTIME, cast, SQLConstants.DEFAULT_DATE_FORMAT);
            }
            if (field.getDeExtractType() == 1) {
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
            if (CollectionUtils.isNotEmpty(item.getEnumValue())) {
                res = "(" + whereName + " IN ('" + String.join("','", item.getEnumValue()) + "'))";
            }
        } else {
            String value = item.getValue();
            String whereTerm = Utils.transFilterTerm(item.getTerm());
            String whereValue = "";

            if (field.getDeType() == 1) {
                whereName = String.format(SQLConstants.UNIX_TIMESTAMP, whereName);
            }

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
}

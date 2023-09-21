package io.dataease.engine.trans;

import io.dataease.api.dataset.union.model.SQLMeta;
import io.dataease.api.dataset.union.model.SQLObj;
import io.dataease.api.permissions.dataset.dto.DataSetRowPermissionsTreeDTO;
import io.dataease.api.permissions.dataset.dto.DatasetRowPermissionsTreeItem;
import io.dataease.api.permissions.dataset.dto.DatasetRowPermissionsTreeObj;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.engine.constant.SQLConstants;
import io.dataease.engine.func.scalar.ScalarFunctions;
import io.dataease.engine.utils.Utils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author Junjun
 */
public class WhereTree2Str {
    public static void transFilterTrees(SQLMeta meta, List<DataSetRowPermissionsTreeDTO> requestList, List<DatasetTableFieldDTO> originFields) {
        SQLObj tableObj = meta.getTable();
        if (ObjectUtils.isEmpty(tableObj)) {
            return;
        }
        if (CollectionUtils.isEmpty(requestList)) {
            return;
        }
        List<String> res = new ArrayList<>();
        // permission trees
        // 解析每个tree，然后多个tree之间用and拼接
        // 每个tree，如果是sub tree节点，则使用递归合并成一组条件
        for (DataSetRowPermissionsTreeDTO request : requestList) {
            DatasetRowPermissionsTreeObj tree = request.getTree();
            if (ObjectUtils.isEmpty(tree)) {
                continue;
            }
            String treeExp = transTreeToWhere(tableObj, tree, originFields);
            if (StringUtils.isNotEmpty(treeExp)) {
                res.add(treeExp);
            }
        }
        meta.setWhereTrees(CollectionUtils.isNotEmpty(res) ? "(" + String.join(" AND ", res) + ")" : null);
    }

    private static String transTreeToWhere(SQLObj tableObj, DatasetRowPermissionsTreeObj tree, List<DatasetTableFieldDTO> originFields) {
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
                    exp = transTreeItem(tableObj, item, originFields);
                } else if (StringUtils.equalsIgnoreCase(item.getType(), "tree")) {
                    // 递归tree
                    exp = transTreeToWhere(tableObj, item.getSubTree(), originFields);
                }
                if (StringUtils.isNotEmpty(exp)) {
                    list.add(exp);
                }
            }
        }
        return CollectionUtils.isNotEmpty(list) ? "(" + String.join(" " + logic + " ", list) + ")" : null;
    }

    public static String transTreeItem(SQLObj tableObj, DatasetRowPermissionsTreeItem item, List<DatasetTableFieldDTO> originFields) {
        String res = null;
        DatasetTableFieldDTO field = item.getField();
        if (ObjectUtils.isEmpty(field)) {
            return null;
        }
        String whereName = "";
        String originName;
        if (ObjectUtils.isNotEmpty(field.getExtField()) && Objects.equals(field.getExtField(), ExtFieldConstant.EXT_CALC)) {
            // 解析origin name中有关联的字段生成sql表达式
            originName = Utils.calcFieldRegex(field.getOriginName(), tableObj, originFields);
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
                String f = ScalarFunctions.get_date_format(originName);
                whereName = String.format(SQLConstants.DATE_FORMAT, originName, f);
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
                // 规定几种日期格式，一一匹配，匹配到就是该格式
                if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                    String f = ScalarFunctions.get_date_format(item.getValue());
                    whereName = String.format(SQLConstants.DE_CAST_DATE_FORMAT, whereName, StringUtils.isNotEmpty(field.getDateFormat()) ? field.getDateFormat() : SQLConstants.DEFAULT_DATE_FORMAT, f);
                    whereName = String.format(SQLConstants.UNIX_TIMESTAMP, whereName);
                } else {
                    String f = ScalarFunctions.get_date_format(item.getValue());
                    whereName = String.format(SQLConstants.DE_DATE_FORMAT, whereName, f);
                    whereName = String.format(SQLConstants.UNIX_TIMESTAMP, whereName);
                }
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
                if (field.getDeType() == 1) {
                    value = Utils.allDateFormat2Long(value) + "";
                }
                whereValue = String.format(SQLConstants.WHERE_VALUE_VALUE, value);
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

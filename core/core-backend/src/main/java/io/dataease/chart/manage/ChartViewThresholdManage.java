package io.dataease.chart.manage;

import io.dataease.dataset.manage.DatasetTableFieldManage;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.view.filter.FilterTreeItem;
import io.dataease.extensions.view.filter.FilterTreeObj;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("chartViewThresholdManage")
public class ChartViewThresholdManage {

    @Resource
    private DatasetTableFieldManage datasetTableFieldManage;

    public String convertThresholdRules(Long tableId, String thresholdRules) {
        List<DatasetTableFieldDTO> fieldList = datasetTableFieldManage.selectByDatasetGroupId(tableId);
        FilterTreeObj filterTreeObj = JsonUtil.parseObject(thresholdRules, FilterTreeObj.class);
        Map<String, DatasetTableFieldDTO> fieldMap = fieldList.stream().collect(Collectors.toMap(item -> item.getId().toString(), item -> item));
        return convertTree(filterTreeObj, fieldMap);
    }

    private String convertTree(FilterTreeObj filterTreeObj, Map<String, DatasetTableFieldDTO> fieldMap) {
        String logic = filterTreeObj.getLogic();
        String logicText = translateLogic(logic);
        List<FilterTreeItem> items = filterTreeObj.getItems();

        StringBuilder result = new StringBuilder();
        for (FilterTreeItem item : items) {
            String type = item.getType();
            if (StringUtils.equals("tree", type) && ObjectUtils.isNotEmpty(item.getSubTree())) {
                String childResult = convertTree(item.getSubTree(), fieldMap);
                result.append(childResult);
            } else {
                String itemResult = convertItem(item, fieldMap);
                result.append(itemResult);
            }
            result.append(logicText);
        }
        int lastIndex = -1;
        if ((!result.isEmpty()) && (lastIndex = result.lastIndexOf(logicText)) > 0) {
            return result.substring(0, lastIndex);
        }

        return null;
    }

    private String convertItem(FilterTreeItem item, Map<String, DatasetTableFieldDTO> fieldMap) {
        String filterType = item.getFilterType();
        Long fieldId = item.getFieldId();
        DatasetTableFieldDTO map = fieldMap.get(fieldId.toString());
        String fieldName = map.getName();
        if (StringUtils.equals(filterType, "enum")) {
            List<String> enumValue = item.getEnumValue();
            String enumValueText = String.join(",", enumValue);
            return fieldName + " 包括 " + "( " + enumValueText + " )";
        } else {
            return fieldName + " " + translateTerm(item.getTerm()) + " " + item.getValue();
        }
    }

    private String translateTerm(String term) {
        return switch (term) {
            case "eq" -> "等于";
            case "not_eq" -> "不等于";
            case "lt" -> "小于";
            case "le" -> "小于等于";
            case "gt" -> "大于";
            case "ge" -> "大于等于";
            case "in" -> "包括";
            case "not in" -> "不包括";
            case "like" -> "包含";
            case "not like" -> "不包含";
            case "null" -> "为空";
            case "not null" -> "不为空";
            case "empty" -> "空字符串";
            case "not empty" -> "非字符串";
            case "between" -> "范围是";
            default -> " 等于 ";
        };
    }

    private String translateLogic(String logic) {
        if (StringUtils.equals(logic, "and")) return " 且 ";
        return " 或 ";
    }
}

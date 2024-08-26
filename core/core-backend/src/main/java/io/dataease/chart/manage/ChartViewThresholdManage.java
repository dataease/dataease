package io.dataease.chart.manage;

import io.dataease.api.chart.request.ThresholdCheckRequest;
import io.dataease.api.chart.vo.ThresholdCheckVO;
import io.dataease.dataset.manage.DatasetTableFieldManage;
import io.dataease.engine.constant.DeTypeConstants;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.extensions.view.filter.FilterTreeItem;
import io.dataease.extensions.view.filter.FilterTreeObj;
import io.dataease.utils.DateUtils;
import io.dataease.utils.JsonUtil;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component("chartViewThresholdManage")
public class ChartViewThresholdManage {

    @Resource
    private DatasetTableFieldManage datasetTableFieldManage;

    @Resource
    private ChartViewManege chartViewManege;

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
            return fieldName + " 属于 " + "( " + enumValueText + " )";
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
            case "in" -> "属于";
            case "not in" -> "不属于";
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

    private String convertStyle(String htmlString) {
        String regex = "<span\\s+id=\"(changeText-0|changeText-1)\"\\s+style=\"([^\"]*)\">";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(htmlString);
        if (matcher.find()) {
            String styleAttribute = matcher.group();
            String newStyle = styleAttribute.replace("background: #3370FF33", "background: #FFFFFF")
                    .replace("color: #2b5fd9", "color: #000000");
            return matcher.replaceAll(Matcher.quoteReplacement(newStyle));
        }
        return htmlString;
    }

    public ThresholdCheckVO checkThreshold(ThresholdCheckRequest request) throws Exception {
        String thresholdTemplate = request.getThresholdTemplate();
        String thresholdRules = request.getThresholdRules();
        Long chartId = request.getChartId();
        try {
            ChartViewDTO chart = chartViewManege.getChart(chartId);
            Map<String, Object> data = chart.getData();
            List<Map<String, Object>> tableRow = (List<Map<String, Object>>) data.get("tableRow");
            List<DatasetTableFieldDTO> fields = (List<DatasetTableFieldDTO>) data.get("fields");
            Map<Long, DatasetTableFieldDTO> fieldMap = fields.stream().collect(Collectors.toMap(DatasetTableFieldDTO::getId, item -> item));
            FilterTreeObj filterTreeObj = JsonUtil.parseObject(thresholdRules, FilterTreeObj.class);
            List<Map<String, Object>> rows = filterRows(tableRow, filterTreeObj, fieldMap);
            if (CollectionUtils.isEmpty(rows)) {
                return new ThresholdCheckVO(false, null, null, null);
            }
            String regex = "<span[^>]*id=\"changeText-(\\d+)(?!0$)(?!1$)\"[^>]*>.*?</span>";
            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(thresholdTemplate);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                long id = Long.parseLong(matcher.group(1));
                // 根据id从map中获取替换文本
                DatasetTableFieldDTO fieldDTO = fieldMap.get(id);
                if (ObjectUtils.isEmpty(fieldDTO)) continue;
                String fieldDTOName = fieldDTO.getName();
                String dataeaseName = fieldDTO.getDataeaseName();
                List<String> valueList = rows.stream().map(row -> row.get(dataeaseName).toString()).collect(Collectors.toList());
                String replacement = fieldDTOName + ": " + JsonUtil.toJSONString(valueList);
                // 替换文本
                matcher.appendReplacement(sb, replacement);
            }
            matcher.appendTail(sb);

            // 输出替换后的HTML内容
            String msgContent = sb.toString();
            msgContent = msgContent.replace("[检测时间]", DateUtils.time2String(System.currentTimeMillis()));
            Long tableId = chart.getTableId();
            String s = convertThresholdRules(tableId, thresholdRules);
            String result = convertStyle(msgContent.replace("[触发告警]", s));
            return new ThresholdCheckVO(true, result, null, null);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), new Throwable(e));
            return new ThresholdCheckVO(false, null, e.getMessage(), null);
        }
    }

    public List<Map<String, Object>> filterRows(List<Map<String, Object>> rows, FilterTreeObj conditionTree, Map<Long, DatasetTableFieldDTO> fieldMap) {
        List<Map<String, Object>> filteredRows = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            if (matchesConditionTree(row, conditionTree, fieldMap)) {
                filteredRows.add(row);
            }
        }
        return filteredRows;
    }

    private boolean matchesConditionTree(Map<String, Object> row, FilterTreeObj conditionTree, Map<Long, DatasetTableFieldDTO> fieldMap) {
        if (conditionTree == null || conditionTree.getItems().isEmpty()) {
            return true; // 如果没有条件树或条件列表为空，返回所有行
        }
        List<FilterTreeItem> items = conditionTree.getItems();
        if (conditionTree.getLogic().equals("or")) {
            return matchesAnyItem(row, items, fieldMap);
        }
        return matchesAllItems(row, items, fieldMap);
    }

    private boolean matchesAllItems(Map<String, Object> row, List<FilterTreeItem> items, Map<Long, DatasetTableFieldDTO> fieldMap) {
        for (FilterTreeItem item : items) {
            if (!matchesConditionItem(row, item, fieldMap)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesAnyItem(Map<String, Object> row, List<FilterTreeItem> items, Map<Long, DatasetTableFieldDTO> fieldMap) {
        for (FilterTreeItem item : items) {
            if (matchesConditionItem(row, item, fieldMap)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesConditionItem(Map<String, Object> row, FilterTreeItem item, Map<Long, DatasetTableFieldDTO> fieldMap) {
        if ("item".equals(item.getType())) {
            DatasetTableFieldDTO fieldDTO = fieldMap.get(item.getFieldId());
            return rowMatch(row, item, fieldDTO);
        } else if ("tree".equals(item.getType()) && item.getSubTree() != null) {
            return matchesConditionTree(row, item.getSubTree(), fieldMap);
        }
        return false; // 如果类型不匹配或没有子树，不匹配
    }

    private boolean rowMatch(Map<String, Object> row, FilterTreeItem item, DatasetTableFieldDTO fieldDTO) {
        String dataeaseName = fieldDTO.getDataeaseName();
        String filterType = item.getFilterType();
        Integer deType = fieldDTO.getDeType();
        Object valueObj = row.get(dataeaseName);
        if (StringUtils.equals(filterType, "enum")) {
            List<String> enumValue = item.getEnumValue();
            return ObjectUtils.isNotEmpty(valueObj) && enumValue.contains(valueObj);
        } else {
            String term = item.getTerm();
            if (Objects.equals(deType, DeTypeConstants.DE_STRING)) {
                if (StringUtils.equals(term, "eq")) {
                    return StringUtils.equals(item.getValue(), valueObj.toString());
                } else if (StringUtils.equals(term, "not_eq")) {
                    return !StringUtils.equals(item.getValue(), valueObj.toString());
                } else if (StringUtils.equals(term, "in")) {
                    return Arrays.stream(item.getValue().split(",")).toList().contains(valueObj.toString());
                } else if (StringUtils.equals(term, "not_in")) {
                    return !Arrays.stream(item.getValue().split(",")).toList().contains(valueObj.toString());
                } else if (StringUtils.equals(term, "like")) {
                    return StringUtils.contains(item.getValue(), valueObj.toString());
                } else if (StringUtils.equals(term, "not_like")) {
                    return !StringUtils.contains(item.getValue(), valueObj.toString());
                } else if (StringUtils.equals(term, "null")) {
                    return valueObj == null;
                } else if (StringUtils.equals(term, "not_null")) {
                    return valueObj != null;
                } else if (StringUtils.equals(term, "empty")) {
                    return StringUtils.isBlank(valueObj.toString());
                } else if (StringUtils.equals(term, "not_empty")) {
                    return !StringUtils.isBlank(valueObj.toString());
                } else {
                    return StringUtils.equals(item.getValue(), valueObj.toString());
                }
            } else if (Objects.equals(deType, DeTypeConstants.DE_INT) || Objects.equals(deType, DeTypeConstants.DE_FLOAT)) {
                if (StringUtils.equals(term, "eq")) {
                    return StringUtils.equals(item.getValue().toString(), valueObj.toString());
                } else if (StringUtils.equals(term, "not_eq")) {
                    return !StringUtils.equals(item.getValue().toString(), valueObj.toString());
                } else if (StringUtils.equals(term, "gt")) {
                    return Float.parseFloat(item.getValue().toString()) < Float.parseFloat(valueObj.toString());
                } else if (StringUtils.equals(term, "ge")) {
                    return Float.parseFloat(item.getValue().toString()) <= Float.parseFloat(valueObj.toString());
                } else if (StringUtils.equals(term, "lt")) {
                    return Float.parseFloat(item.getValue().toString()) > Float.parseFloat(valueObj.toString());
                } else if (StringUtils.equals(term, "le")) {
                    return Float.parseFloat(item.getValue().toString()) >= Float.parseFloat(valueObj.toString());
                } else {
                    return StringUtils.equals(item.getValue().toString(), valueObj.toString());
                }
            } else if (Objects.equals(deType, DeTypeConstants.DE_TIME)) {
                // 补充时间逻辑
                return true;
            } else {
                return true;
            }
        }
    }

}

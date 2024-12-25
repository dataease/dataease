package io.dataease.chart.manage;

import io.dataease.api.chart.request.ThresholdCheckRequest;
import io.dataease.api.chart.vo.ThresholdCheckVO;
import io.dataease.engine.constant.DeTypeConstants;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import io.dataease.extensions.view.filter.FilterTreeItem;
import io.dataease.extensions.view.filter.FilterTreeObj;
import io.dataease.i18n.Translator;
import io.dataease.utils.DateUtils;
import io.dataease.utils.JsonUtil;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component("chartViewThresholdManage")
public class ChartViewThresholdManage {


    @Resource
    private ChartViewManege chartViewManege;

    public String convertThresholdRules(Long chartId, String thresholdRules) {
        ChartViewDTO details = chartViewManege.getDetails(chartId);
        return convertThresholdRules(details, thresholdRules);
    }

    private String convertThresholdRules(ChartViewDTO chart, String thresholdRules) {
        List<DatasetTableFieldDTO> fieldList = chartFields(chart);
        FilterTreeObj filterTreeObj = JsonUtil.parseObject(thresholdRules, FilterTreeObj.class);
        Map<String, DatasetTableFieldDTO> fieldMap = fieldList.stream().collect(Collectors.toMap(item -> item.getId().toString(), item -> item));
        return convertTree(filterTreeObj, fieldMap);
    }

    private List<DatasetTableFieldDTO> chartFields(ChartViewDTO details) {
        List<DatasetTableFieldDTO> result = new ArrayList<>();
        List<ChartViewFieldDTO> xAxis = details.getXAxis();
        if (CollectionUtils.isNotEmpty(xAxis)) {
            result.addAll(xAxis);
        }
        List<ChartViewFieldDTO> xAxisExt = details.getXAxisExt();
        if (CollectionUtils.isNotEmpty(xAxisExt)) {
            result.addAll(xAxisExt);
        }
        List<ChartViewFieldDTO> yAxis = details.getYAxis();
        if (CollectionUtils.isNotEmpty(yAxis)) {
            result.addAll(yAxis);
        }
        List<ChartViewFieldDTO> yAxisExt = details.getYAxisExt();
        if (CollectionUtils.isNotEmpty(yAxisExt)) {
            result.addAll(yAxisExt);
        }
        List<ChartViewFieldDTO> extStack = details.getExtStack();
        if (CollectionUtils.isNotEmpty(extStack)) {
            result.addAll(extStack);
        }
        List<ChartViewFieldDTO> extBubble = details.getExtBubble();
        if (CollectionUtils.isNotEmpty(extBubble)) {
            result.addAll(extBubble);
        }
        List<ChartViewFieldDTO> extLabel = details.getExtLabel();
        if (CollectionUtils.isNotEmpty(extLabel)) {
            result.addAll(extLabel);
        }
        List<ChartViewFieldDTO> extTooltip = details.getExtTooltip();
        if (CollectionUtils.isNotEmpty(extTooltip)) {
            result.addAll(extTooltip);
        }
        List<ChartViewFieldDTO> extColor = details.getExtColor();
        if (CollectionUtils.isNotEmpty(extColor)) {
            result.addAll(extColor);
        }
        List<ChartViewFieldDTO> flowMapStartName = details.getFlowMapStartName();
        if (CollectionUtils.isNotEmpty(flowMapStartName)) {
            result.addAll(flowMapStartName);
        }
        List<ChartViewFieldDTO> flowMapEndName = details.getFlowMapEndName();
        if (CollectionUtils.isNotEmpty(flowMapEndName)) {
            result.addAll(flowMapEndName);
        }
        return result;
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
            return fieldName + " " + Translator.get("i18n_threshold_logic_in") + " " + "( " + enumValueText + " )";
        } else {
            Integer deType = map.getDeType();
            String valueType = item.getValueType();
            return fieldName + " " + translateTerm(item.getTerm()) + " " + formatFieldValue(item.getValue(), valueType, deType);
        }
    }

    private String formatFieldValue(String value, String valueType, Integer deType) {
        if (StringUtils.isBlank(valueType)) {
            valueType = "fixed";
        }
        if (StringUtils.equals("fixed", valueType)) {
            return value;
        }
        if (StringUtils.equals("max", value)) {
            return Translator.get("i18n_threshold_max");
        } else if (StringUtils.equals("min", value)) {
            return Translator.get("i18n_threshold_min");
        } else if (StringUtils.equals("average", value)) {
            return Translator.get("i18n_threshold_average");
        } else if (deType == 1) {
            return formatDynamicTimeLabel(value);
        } else {
            return value;
        }
    }

    private String formatDynamicTimeLabel(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        try {
            Map map = JsonUtil.parseObject(value, Map.class);
            String format = map.get("format").toString();
            int timeFlag = Integer.parseInt(map.get("timeFlag").toString());

            if (timeFlag == 9) {
                int count = Integer.parseInt(map.get("count").toString());
                int unit = Integer.parseInt(map.get("unit").toString());
                int suffix = Integer.parseInt(map.get("suffix").toString());
                String time = map.get("time").toString();

                List<String> unitLabels = null;
                if (StringUtils.equalsIgnoreCase("YYYY", format)) {
                    unitLabels = List.of(Translator.get("i18n_time_year"));
                } else if (StringUtils.equalsIgnoreCase("YYYY-MM", format)) {
                    unitLabels = List.of(Translator.get("i18n_time_year"), Translator.get("i18n_time_month"));
                } else if (StringUtils.equalsIgnoreCase("YYYY-MM-DD", format)) {
                    unitLabels = List.of(Translator.get("i18n_time_year"), Translator.get("i18n_time_month"), Translator.get("i18n_time_date"));
                } else if (StringUtils.equalsIgnoreCase("HH:mm:ss", format)) {
                    DEException.throwException("纯时间格式不支持动态格式");
                } else {
                    unitLabels = List.of(Translator.get("i18n_time_year"), Translator.get("i18n_time_month"), Translator.get("i18n_time_date"));
                }
                String unitText = unitLabels.get(unit - 1);
                String suffixText = Translator.get("i18n_time_ago");
                if (suffix == 2) {
                    suffixText = Translator.get("i18n_time_later");
                }
                String timeText = "";
                if (StringUtils.containsIgnoreCase(format, "HH")) {
                    timeText = " " + time;
                }
                return count + " " + unitText + suffixText + timeText;
            } else {
                List<String> shortLabels = null;
                if (StringUtils.equalsIgnoreCase("YYYY", format)) {
                    shortLabels = List.of(Translator.get("i18n_time_year_current"), Translator.get("i18n_time_year_last"), Translator.get("i18n_time_year_next"));
                } else if (StringUtils.equalsIgnoreCase("YYYY-MM", format)) {
                    shortLabels = List.of(Translator.get("i18n_time_month_current"), Translator.get("i18n_time_month_last"), Translator.get("i18n_time_month_next"),
                            Translator.get("i18n_time_month_start"), Translator.get("i18n_time_month_end"));
                } else if (StringUtils.equalsIgnoreCase("YYYY-MM-DD", format)) {
                    shortLabels = List.of(Translator.get("i18n_time_date_current"), Translator.get("i18n_time_date_last"), Translator.get("i18n_time_date_next"),
                            Translator.get("i18n_time_date_start"), Translator.get("i18n_time_date_end"));
                } else if (StringUtils.equalsIgnoreCase("HH:mm:ss", format)) {
                    shortLabels = List.of("当前", "1小时前", "1小时后");
                } else {
                    shortLabels = List.of(Translator.get("i18n_time_date_current"), Translator.get("i18n_time_date_last"), Translator.get("i18n_time_date_next"),
                            Translator.get("i18n_time_date_start"), Translator.get("i18n_time_date_end"));
                }
                return shortLabels.get(timeFlag - 1);
            }

        } catch (Exception e) {
            LogUtil.error("动态时间配置错误，请重新配置！");
            return value;
        }
    }

    private String translateTerm(String term) {
        if (StringUtils.equals(term, "not in")) {
            return Translator.get("i18n_threshold_logic_not_in");
        } else if (StringUtils.equals(term, "not like")) {
            return Translator.get("i18n_threshold_logic_not_like");
        } else {
            return Translator.get("i18n_threshold_logic_" + term);
        }
    }

    private String translateLogic(String logic) {
        if (StringUtils.equals(logic, "and")) return String.format(" %s ", Translator.get("i18n_threshold_logic_and"));
        return String.format(" %s ", Translator.get("i18n_threshold_logic_or"));
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
            Map<String, Object> data = null;
            if (ObjectUtils.isEmpty(chart) || MapUtils.isEmpty(data = chart.getData())) {
                return new ThresholdCheckVO(false, null, "查询图表异常！", null);
            }
            thresholdTemplate = thresholdTemplate.replace("[检测时间]", DateUtils.time2String(System.currentTimeMillis()));
            String s = convertThresholdRules(chart, thresholdRules);
            thresholdTemplate = convertStyle(thresholdTemplate.replace("[触发告警]", s));
            List<Map<String, Object>> tableRow = (List<Map<String, Object>>) data.get("tableRow");
            List<DatasetTableFieldDTO> fields = (List<DatasetTableFieldDTO>) data.get("fields");
            if (CollectionUtils.isEmpty(fields)) {
                return new ThresholdCheckVO(false, null, String.format("当前图表类型[%s]暂不支持阈值告警！", chart.getType()), null);
            }
            Map<Long, DatasetTableFieldDTO> fieldMap = fields.stream().collect(Collectors.toMap(DatasetTableFieldDTO::getId, item -> item));
            FilterTreeObj filterTreeObj = JsonUtil.parseObject(thresholdRules, FilterTreeObj.class);
            List<Map<String, Object>> rows = filterRows(tableRow, filterTreeObj, fieldMap);
            if (CollectionUtils.isEmpty(rows)) {
                return new ThresholdCheckVO(false, null, null, null);
            }
            String regex = "<span[^>]*id=\"changeText-(-?\\d+)(?!0$)(?!1$)\"[^>]*>.*?</span>";
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
                List<String> valueList = rows.stream().map(row -> ObjectUtils.isEmpty(row.get(dataeaseName)) ? null : row.get(dataeaseName).toString()).collect(Collectors.toList());
                String replacement = fieldDTOName + ": " + JsonUtil.toJSONString(valueList);
                // 替换文本
                matcher.appendReplacement(sb, replacement);
            }
            matcher.appendTail(sb);

            // 输出替换后的HTML内容
            String result = sb.toString();
            return new ThresholdCheckVO(true, result, null, null);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), new Throwable(e));
            return new ThresholdCheckVO(false, null, e.getMessage(), null);
        }
    }

    private void chartDynamicMap(List<Map<String, Object>> rows, FilterTreeObj conditionTree, Map<Long, DatasetTableFieldDTO> fieldMap) {
        List<FilterTreeItem> items = conditionTree.getItems();
        items.forEach(item -> {
            if (!StringUtils.equals("item", item.getType())) {
                chartDynamicMap(rows, item.getSubTree(), fieldMap);
            } else {
                Long fieldId = item.getFieldId();
                DatasetTableFieldDTO fieldDTO = fieldMap.get(fieldId);
                if ((Objects.equals(fieldDTO.getDeType(), DeTypeConstants.DE_INT) || Objects.equals(fieldDTO.getDeType(), DeTypeConstants.DE_FLOAT)) && StringUtils.equals("dynamic", item.getValueType())) {
                    item.setField(fieldDTO);
                    item.setValue(formatValue(rows, item));
                } else if (Objects.equals(fieldDTO.getDeType(), DeTypeConstants.DE_TIME) && StringUtils.equals("dynamic", item.getValueType())) {
                    item.setField(fieldDTO);
                    item.setValue(dynamicFormatValue(item));
                }
            }
        });
    }

    private String dynamicFormatValue(FilterTreeItem item) {
        String value = item.getValue();

        if (StringUtils.isBlank(value)) {
            return value;
        }
        try {
            Map map = JsonUtil.parseObject(value, Map.class);
            String format = map.get("format").toString();
            int timeFlag = Integer.parseInt(map.get("timeFlag").toString());
            if (timeFlag == 9) {
                int count = Integer.parseInt(map.get("count").toString());
                int unit = Integer.parseInt(map.get("unit").toString());
                int suffix = Integer.parseInt(map.get("suffix").toString());
                String time = map.get("time").toString();
                String timeValue = getCustomTimeValue(format, unit, suffix, count, false);
                if (StringUtils.containsIgnoreCase(format, "yyyy-MM-dd HH") && StringUtils.isNotBlank(time)) {
                    return timeValue + " " + time;
                }
                return timeValue;
            } else {
                LocalDateTime now = LocalDateTime.now();
                String fullFormat = "yyyy-MM-dd HH:mm:ss";
                int length = format.length();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(fullFormat.substring(0, length));
                int count = timeFlag == 1 ? 0 : 1;
                int suffix = timeFlag - 1;
                if (StringUtils.equalsIgnoreCase("YYYY", format)) {
                    return getCustomTimeValue(format, 1, suffix, count, true);
                } else if (StringUtils.equalsIgnoreCase("YYYY-MM", format)) {
                    if (timeFlag == 4) {
                        return now.withMonth(1).withDayOfMonth(1).format(formatter);
                    } else if (timeFlag == 5) {
                        return now.withMonth(12).withDayOfMonth(31).format(formatter);
                    } else {
                        return getCustomTimeValue(format, 2, suffix, count, true);
                    }
                } else {
                    if (timeFlag == 4) {
                        return now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).format(formatter);
                    } else if (timeFlag == 5) {
                        return now.plusMonths(1).withDayOfMonth(1).minusDays(1).withHour(0).withMinute(0).withSecond(0).format(formatter);
                    } else {
                        return getCustomTimeValue(format, 3, suffix, count, true);
                    }
                }
            }

        } catch (Exception e) {
            LogUtil.error("动态时间配置错误，请重新配置！" + e.getMessage());
            return value;
        }
    }

    private String getCustomTimeValue(String format, int unit, int suffix, int count, boolean hasTime) {
        LocalDateTime now = LocalDateTime.now();
        String fullFormat = "yyyy-MM-dd HH:mm:ss";
        int len = format.length();
        if (hasTime) {
            now = now.withHour(0).withMinute(0).withSecond(0);
        } else {
            len = Math.min(len, 10);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(fullFormat.substring(0, len));
        if (count == 0) {
            return now.format(formatter);
        }
        if (unit == 1) {
            if (suffix == 1) {
                return now.minusYears(count).format(formatter);
            }
            return now.plusYears(count).format(formatter);
        } else if (unit == 2) {
            if (suffix == 1) {
                return now.minusMonths(count).format(formatter);
            }
            return now.plusMonths(count).format(formatter);
        } else {
            if (suffix == 1) {
                return now.minusDays(count).format(formatter);
            }
            return now.plusDays(count).format(formatter);
        }
    }


    private String formatValue(List<Map<String, Object>> rows, FilterTreeItem item) {
        DatasetTableFieldDTO field = item.getField();
        String dataeaseName = field.getDataeaseName();
        String value = item.getValue();
        Float tempFVal = StringUtils.equalsAny(value, "min", "max") ? null : 0f;
        int validLen = 0;

        for (Map<String, Object> row : rows) {
            Object o = row.get(dataeaseName);
            if (ObjectUtils.isEmpty(o)) continue;
            float fvalue = Float.parseFloat(o.toString());
            if (StringUtils.equals("min", value)) {
                if (ObjectUtils.isEmpty(tempFVal)) {
                    tempFVal = fvalue;
                } else {
                    tempFVal = Math.min(tempFVal, fvalue);
                }
            } else if (StringUtils.equals("max", value)) {
                if (ObjectUtils.isEmpty(tempFVal)) {
                    tempFVal = fvalue;
                } else {
                    tempFVal = Math.max(tempFVal, fvalue);
                }
            } else if (StringUtils.equals("average", value)) {
                tempFVal += fvalue;
                validLen++;
            }
        }
        if (StringUtils.equals("average", value)) {
            return validLen == 0 ? "0f" : String.valueOf((tempFVal / validLen));
        }
        return String.valueOf(tempFVal);
    }

    public List<Map<String, Object>> filterRows(List<Map<String, Object>> rows, FilterTreeObj conditionTree, Map<Long, DatasetTableFieldDTO> fieldMap) {
        chartDynamicMap(rows, conditionTree, fieldMap);
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
                if (valueObj == null) {
                    return StringUtils.equals(term, "null");
                }
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
                    return false;
                } else if (StringUtils.equals(term, "not_null")) {
                    return true;
                } else if (StringUtils.equals(term, "empty")) {
                    return StringUtils.isBlank(valueObj.toString());
                } else if (StringUtils.equals(term, "not_empty")) {
                    return !StringUtils.isBlank(valueObj.toString());
                } else {
                    return StringUtils.equals(item.getValue(), valueObj.toString());
                }
            } else if (Objects.equals(deType, DeTypeConstants.DE_INT) || Objects.equals(deType, DeTypeConstants.DE_FLOAT)) {
                if (valueObj == null) return false;
                if (ObjectUtils.isEmpty(item.getValue())) {
                    return false;
                }
                float targetVal = Float.parseFloat(item.getValue());
                float originVal = Float.parseFloat(valueObj.toString());
                if (StringUtils.equals(term, "eq")) {
                    return StringUtils.equals(String.valueOf(originVal), String.valueOf(targetVal));
                } else if (StringUtils.equals(term, "not_eq")) {
                    return !StringUtils.equals(String.valueOf(originVal), String.valueOf(targetVal));
                } else if (StringUtils.equals(term, "gt")) {
                    return targetVal < originVal;
                } else if (StringUtils.equals(term, "ge")) {
                    return targetVal <= originVal;
                } else if (StringUtils.equals(term, "lt")) {
                    return targetVal > originVal;
                } else if (StringUtils.equals(term, "le")) {
                    return targetVal >= originVal;
                } else {
                    return StringUtils.equals(item.getValue(), valueObj.toString());
                }
            } else if (Objects.equals(deType, DeTypeConstants.DE_TIME)) {
                // 补充时间逻辑
                return timeMatch(item, valueObj);
            } else {
                return true;
            }
        }
    }

    private boolean timeMatch(FilterTreeItem item, Object valueObj) {
        if (ObjectUtils.isEmpty(valueObj)) return false;
        String valueText = valueObj.toString();
        String target = item.getValue();
        target = target.replaceAll("[^0-9]", "");
        valueText = valueText.replaceAll("[^0-9]", "");
        long targetLong = Long.parseLong(target);
        long valueLong = Long.parseLong(valueText);
        String term = item.getTerm();
        if (StringUtils.equals(term, "eq")) {
            return valueLong == targetLong;
        } else if (StringUtils.equals(term, "not_eq")) {
            return valueLong != targetLong;
        } else if (StringUtils.equals(term, "gt")) {
            return valueLong > targetLong;
        } else if (StringUtils.equals(term, "ge")) {
            return valueLong >= targetLong;
        } else if (StringUtils.equals(term, "lt")) {
            return valueLong < targetLong;
        } else if (StringUtils.equals(term, "le")) {
            return valueLong <= targetLong;
        } else {
            return valueLong == targetLong;
        }
    }
}

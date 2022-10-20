package io.dataease.service.chart;

import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.dto.chart.FilterParamTO;
import io.dataease.plugins.common.request.chart.ChartExtFilterRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public abstract class FilterBuildTemplate {

    protected abstract FilterParamTO buildParam(Map<String, Object> component);

    public static Map<String, List<ChartExtFilterRequest>> buildFilters(List<Map<String, Object>> components) {
        Map<String, Object> searchButton = components.stream().filter(item -> {
            if (ObjectUtils.isEmpty(item.get("type"))) return false;
            if (ObjectUtils.isEmpty(item.get("serviceName"))) return false;
            return StringUtils.equals("custom-button", item.get("type").toString()) && StringUtils.equals("buttonSureWidget", item.get("serviceName").toString());
        }).findFirst().orElse(null);

        List<Map<String, Object>> filters = componentsFilter(components, "custom", null, null);

        if (ObjectUtils.isNotEmpty(searchButton)) {
            Map<String, Object> options = (Map<String, Object>) searchButton.get("options");
            Map<String, Object> attrs = (Map<String, Object>) options.get("attrs");
            Boolean customRange = (Boolean) attrs.get("customRange");
            Boolean autoTrigger = (Boolean) attrs.get("autoTrigger");
            List<String> filterIds = (List<String>) attrs.get("filterIds");
            if (!autoTrigger) { // 不是自动触发 // 需要过滤掉按钮关联的条件组件
                if (customRange) { // 自定义控制范围 //过滤掉被按钮关联的
                    filters = filters.stream().filter(filter -> !filterIds.contains(filter.get("id"))).collect(Collectors.toList());
                } else { // 按钮控制所有 // 过滤掉所有条件
                    filters = new ArrayList<>();
                }
            }
        }

        Map<String, List<ChartExtFilterRequest>> emptyResult = buildEmpty(components);

        emptyResult = fillFilters(emptyResult, filters);

        return emptyResult;
    }

    private static Map<String, List<ChartExtFilterRequest>> fillFilters(Map<String, List<ChartExtFilterRequest>> emptyResult, List<Map<String, Object>> filters) {
        filters.forEach(element -> {
            String serviceName = element.get("serviceName").toString();
            FilterBuildTemplate template = getInstance(serviceName);
            FilterParamTO filterParamTO = template.buildParam(element);
            ChartExtFilterRequest condition = formatCondition(filterParamTO);
            Boolean vValid = valueValid(condition);
            String filterComponentId = condition.getComponentId();
            emptyResult.entrySet().forEach(entry -> {
                String viewId = entry.getKey();
                boolean vidMatch = viewIdMatch(condition.getViewIds(), viewId);
                List<ChartExtFilterRequest> viewFilters = emptyResult.get(viewId);
                int j = viewFilters.size();
                while (j-- > 0) {
                    ChartExtFilterRequest filter = viewFilters.get(j);
                    if (StringUtils.equals(filter.getComponentId(), filterComponentId)) {
                        viewFilters.remove(j);
                    }
                }
                if (vidMatch && vValid) {
                    viewFilters.add(condition);
                }
            });
        });

        return emptyResult;
    }

    private static Boolean valueValid(ChartExtFilterRequest condition) {
        return ObjectUtils.isNotEmpty(condition) && CollectionUtils.isNotEmpty(condition.getValue()) && StringUtils.isNotBlank(condition.getValue().get(0));
    }

    private static Boolean viewIdMatch(List<String> viewIds, String viewId) {
        return CollectionUtils.isEmpty(viewIds) || viewIds.contains(viewId);
    }

    private static ChartExtFilterRequest formatCondition(FilterParamTO filterParamTO) {

        Boolean isTree = filterParamTO.getIsTree();
        List<String> value = filterParamTO.getValue();
        Map<String, Object> component = filterParamTO.getComponent();
        Map<String, Object> attrs = (Map<String, Object>) ((Map<String, Object>) component.get("options")).get("attrs");
        String fieldId = attrs.get("fieldId").toString();
        List<String> viewIds = (List<String>) attrs.get("viewIds");
        List<String> parameters = (List<String>) attrs.get("parameters");
        Boolean multiple = ObjectUtils.isNotEmpty(attrs.get("multiple")) && (Boolean) attrs.get("multiple");
        if (isTree && !multiple && CollectionUtils.isNotEmpty(value)) {
            // 单选树
            String val = value.get(0);
            if (StringUtils.isNotBlank(val)) {
                int len = val.split(",").length;
                if (len > 0) {
                    List<String> fieldIdList = Arrays.asList(fieldId.split(","));
                    fieldId = fieldIdList.stream().limit(len).collect(Collectors.joining(","));
                }
            }
        }
        ChartExtFilterRequest condition = new ChartExtFilterRequest();
        condition.setComponentId(component.get("id").toString());
        condition.setFieldId(fieldId);
        condition.setValue(value);
        condition.setOperator(filterParamTO.getOperator());
        condition.setViewIds(viewIds);
        condition.setParameters(parameters);
        condition.setIsTree(isTree);
        return condition;
    }

    public static Map<String, List<ChartExtFilterRequest>> buildEmpty(List<Map<String, Object>> components) {
        Map<String, List<ChartExtFilterRequest>> result = new HashMap<>();
        components.forEach(element -> {
            if (StringUtils.equals(element.get("type").toString(), "view")) {
                String viewId = ((Map<String, Object>) element.get("propValue")).get("viewId").toString();
                result.put(viewId, new ArrayList<>());
            }
            if (StringUtils.equals(element.get("type").toString(), "de-tabs")) {
                List<Map<String, Object>> tabs = (List<Map<String, Object>>) ((Map<String, Object>) element.get("options")).get("tabList");
                if (CollectionUtils.isNotEmpty(tabs)) {
                    tabs.forEach(tab -> {
                        Object contentObj = null;
                        if ((contentObj = tab.get("content")) != null) {
                            Map<String, Object> content = (Map<String, Object>) contentObj;
                            Object propObj = null;
                            if ((propObj = content.get("propValue")) != null) {
                                Map<String, String> prop = (Map<String, String>) propObj;
                                String viewId = prop.get("viewId");
                                if (StringUtils.isNotBlank(viewId)) {
                                    result.put(viewId, new ArrayList<>());
                                }
                            }
                        }

                    });
                }


            }
        });
        return result;
    }

    public static List<Map<String, Object>> componentsFilter(List<Map<String, Object>> components, String type,
                                                             String componentType, String serviceName) {
        return components.stream().filter(component -> {
            String ctype = Optional.ofNullable(component.get("type")).orElse("").toString();
            String cComponentType = Optional.ofNullable(component.get("component")).orElse("").toString();
            String cServiceName = Optional.ofNullable(component.get("serviceName")).orElse("").toString();

            boolean typeMatch = true;
            boolean componentTypeMatch = true;
            boolean serviceNameMatch = true;

            if (StringUtils.isNotBlank(type)) {
                typeMatch = StringUtils.equals(type, ctype);
            }

            if (StringUtils.isNotBlank(componentType)) {
                componentTypeMatch = StringUtils.equals(componentType, cComponentType);
            }

            if (StringUtils.isNotBlank(serviceName)) {
                serviceNameMatch = StringUtils.equals(serviceName, cServiceName);
            }

            return typeMatch && componentTypeMatch && serviceNameMatch;

        }).collect(Collectors.toList());

    }

    public static FilterBuildTemplate getInstance(String serviceName) {
        Map<String, String> beanMapping = new HashMap<>();
        beanMapping.put("numberRangeWidget", "numberRangeWidget");
        beanMapping.put("textSelectTreeWidget", "textSelectTreeWidget");
        beanMapping.put("textInputWidget", "textInputWidget");
        beanMapping.put("timeDateWidget", "timeDateWidget");
        beanMapping.put("timeMonthWidget", "timeMonthWidget");
        beanMapping.put("timeYearWidget", "timeYearWidget");
        beanMapping.put("timeDateRangeWidget", "timeDateRangeWidget");
        String beanName = beanMapping.get(serviceName);
        if (StringUtils.isBlank(beanName) && StringUtils.containsIgnoreCase(serviceName, "select")) {
            beanName = "selectWidget";
        }

        return (FilterBuildTemplate) CommonBeanFactory.getBean(beanName);
    }

    protected Map<String, Object> buildAttrs(Map<String, Object> component) {
        Map<String, Object> attrs = (Map<String, Object>) ((Map<String, Object>) component.get("options")).get("attrs");
        return attrs;
    }
}

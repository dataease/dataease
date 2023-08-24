package io.dataease.service.chart.build;

import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.LogUtil;
import io.dataease.dto.chart.FilterParamTO;
import io.dataease.service.chart.FilterBuildTemplate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;


public abstract class TimeBuild extends FilterBuildTemplate {

    private static final String MULTIPLETYPE = "daterange";

    @Override
    protected FilterParamTO buildParam(Map<String, Object> component) {
        FilterParamTO result = new FilterParamTO();
        result.setComponent(component);
        result.setValue(null);
        result.setOperator("between");
        result.setIsTree(false);

        Map<String, Object> options = (Map<String, Object>) component.get("options");
        Map<String, Object> attrs = (Map<String, Object>) options.get("attrs");
        Object valueObj = options.get("value");
        String defaultValue = "";
        boolean multiple = attrs.get("type").equals(MULTIPLETYPE);
        Object defaultObject = attrs.get("default");
        Boolean isDynamic = false;
        if (ObjectUtils.isNotEmpty(defaultObject)) {
            Map<String, Object> defaultMap = (Map<String, Object>) defaultObject;
            isDynamic = (Boolean) defaultMap.getOrDefault("isDynamic", false);
        }

        List<String> realValues = null;

        if (!ObjectUtils.isEmpty(valueObj)) {
            if (valueObj instanceof List) {
                defaultValue = "";
            } else {
                defaultValue = valueObj.toString();
            }
        }
        String componentType = componentType(component);
        String labelFormat = labelFormat(component);
        if (isDynamic) {
            List<Long> dynamicTimes = dynamicDateFromNow(component);
            if (CollectionUtils.isNotEmpty(dynamicTimes)) {
                int size = dynamicTimes.size();
                if (size > 1) {
                    String start = timeSection(String.valueOf(dynamicTimes.get(0)), multiple ? "datetime" : componentType, labelFormat).get(0);
                    String end = timeSection(String.valueOf(dynamicTimes.get(1)), multiple ? "datetime" : componentType, labelFormat).get(1);
                    realValues = add2List(start, end);
                } else {
                    realValues = timeSection(String.valueOf(dynamicTimes.get(0)), componentType, labelFormat);
                }
            }

        } else {
            if (StringUtils.isBlank(defaultValue)) {
                realValues = new ArrayList<>();
            } else {
                if (multiple) {
                    List<String> realVals = Arrays.asList(defaultValue.split(","));
                    String start = realVals.get(0);
                    String end = realVals.get(1);
                    start = timeSection(start, "datetime", labelFormat).get(0);
                    end = timeSection(end, "datetime", labelFormat).get(1);
                    realValues = add2List(start, end);
                } else {
                    realValues = Arrays.asList(defaultValue.split(",")).stream().limit(1).collect(Collectors.toList());
                    realValues = timeSection(realValues.get(0), componentType, labelFormat);
                }
            }
        }
        result.setValue(realValues);

        return result;
    }

    public List<String> timeSection(String sourceTimeStr, String type, String labelFormat) {
        if (StringUtils.isBlank(sourceTimeStr)) return null;
        Long sourceTime = Long.parseLong(sourceTimeStr);

        List<String> result = new ArrayList<>();

        List<String> formatArr = new ArrayList<>();
        if (StringUtils.isNotBlank(labelFormat)) {
            formatArr = Arrays.stream(labelFormat.split(" ")).collect(Collectors.toList());
        }
        Integer[] fieldNames = {Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND};
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(sourceTime);
        int methodsLen = fieldNames.length;
        int childArrLength = 0;
        if (StringUtils.equals("datetime", type) && formatArr.size() > 1) {
            List<String> childArr = new ArrayList<>();
            if (StringUtils.isNotBlank(formatArr.get(1))) {
                childArr = Arrays.stream(formatArr.get(1).split(":")).collect(Collectors.toList());
            }
            childArrLength = childArr.size();
            while (--methodsLen >= childArrLength) {
                executeSetFieldValue(instance, fieldNames[methodsLen], 0);
            }
        } else {
            for (int i = 0; i < methodsLen; i++) {
                executeSetFieldValue(instance, fieldNames[i], 0);
            }
        }

        Calendar endInstance = Calendar.getInstance();
        endInstance.setTimeInMillis(instance.getTimeInMillis());
        if (StringUtils.equals("year", type)) {
            instance.set(Calendar.MONTH, 0);
            instance.set(Calendar.DATE, 1);
            endInstance.setTimeInMillis(instance.getTimeInMillis());
            endInstance.set(Calendar.YEAR, endInstance.get(Calendar.YEAR) + 1);
            endInstance.add(Calendar.SECOND, -1);
            endInstance.roll(Calendar.MILLISECOND, -1);

        }

        if (StringUtils.equals("month", type)) {
            instance.set(Calendar.DATE, 1);
            endInstance.setTimeInMillis(instance.getTimeInMillis());
            endInstance.set(Calendar.MONTH, endInstance.get(Calendar.MONTH) + 1);
            endInstance.add(Calendar.SECOND, -1);
            endInstance.roll(Calendar.MILLISECOND, -1);
        }

        if (StringUtils.equals("date", type)) {
            endInstance.set(Calendar.DATE, endInstance.get(Calendar.DATE) + 1);
            endInstance.add(Calendar.SECOND, -1);
            endInstance.roll(Calendar.MILLISECOND, -1);
        }

        if (StringUtils.equals("datetime", type)) {
            if (childArrLength == 0) {
                endInstance.set(Calendar.DATE, endInstance.get(Calendar.DATE) + 1);
            } else {
                Integer fieldNameFlag = fieldNames[childArrLength - 1];
                endInstance.set(fieldNameFlag, endInstance.get(fieldNameFlag) + 1);
            }
            endInstance.add(Calendar.SECOND, -1);
            endInstance.roll(Calendar.MILLISECOND, -1);
        }

        result.add(String.valueOf(instance.getTimeInMillis()));
        result.add(String.valueOf(endInstance.getTimeInMillis()));
        return result;
    }

    private void executeSetFieldValue(Calendar instance, Integer fieldFlag, Object value) {
        try {
            Method setMethod = instance.getClass().getMethod("set", int.class, int.class);
            setMethod.invoke(instance, fieldFlag, value);
        } catch (Exception e) {
            DEException.throwException(e);
            LogUtil.error(e.getMessage(), e);
        }
    }

    public Boolean isTimeWidget(String serviceName) {
        if (StringUtils.isBlank(serviceName)) return false;
        String[] timeWidgets = {"timeDateWidget", "timeDateRangeWidget"};
        return Arrays.stream(timeWidgets).anyMatch(widget -> StringUtils.equals(widget, serviceName));
    }

    public Boolean showTime(Map<String, Object> component) {
        Map attrs = (Map) ((Map) component.get("options")).get("attrs");
        if (ObjectUtils.isNotEmpty(attrs.get("showTime"))) {
            return (boolean) attrs.get("showTime");
        }
        return false;
    }

    public String componentType(Map<String, Object> component) {
        Map attrs = (Map) ((Map) component.get("options")).get("attrs");
        String result = ObjectUtils.isEmpty(attrs.get("type")) ? "date" : attrs.get("type").toString();
        String serviceName = component.get("serviceName").toString();
        if (isTimeWidget(serviceName) && showTime(component)) {
            result = StringUtils.equals("timeDateWidget", serviceName) ? "datetime" : "datetimerange";
        }
        return result;
    }

    public String labelFormat(Map<String, Object> component) {
        String result = "yyyy-MM-dd";
        Map attrs = (Map) ((Map) component.get("options")).get("attrs");
        String serviceName = component.get("serviceName").toString();
        if (isTimeWidget(serviceName) && showTime(component) && ObjectUtils.isNotEmpty(attrs.get("accuracy"))) {
            return result + " " + attrs.get("accuracy");
        }
        return result;
    }

    public List add2List(Object... elements) {
        return Arrays.stream(elements).collect(Collectors.toList());
    }

    public abstract List<Long> dynamicDateFromNow(Map<String, Object> component);


}

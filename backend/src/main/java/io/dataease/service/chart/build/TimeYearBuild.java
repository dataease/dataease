package io.dataease.service.chart.build;

import io.dataease.dto.chart.FilterParamTO;
import io.dataease.service.chart.FilterBuildTemplate;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


public class TimeYearBuild extends FilterBuildTemplate {
    @Override
    protected FilterParamTO buildParam(Map<String, Object> component) {
        List<String> realVals = null;
        Object valueObj = null;
        String defaultValue = "";
        Map<String, Object> options = (Map<String, Object>) component.get("options");
        Map<String, Object> attrs = (Map<String, Object>) options.get("attrs");
        Object aDefault = attrs.get("default");
        Boolean isDynamic = (Boolean) attrs.getOrDefault("isDynamic", false);
        if (ObjectUtils.isNotEmpty(aDefault) && isDynamic) {
            Long aLong = dynamicDateFormNow(component);
            realVals = new ArrayList<>();
            realVals.add(aLong.toString());
        }else {
            if(!ObjectUtils.isEmpty(valueObj)) {
                if(valueObj instanceof List) {
                    defaultValue = "";
                }else {
                    defaultValue = valueObj.toString();
                }
            }
            if (StringUtils.isBlank(defaultValue)) {
                realVals = new ArrayList<>();
            }else {
                realVals = Arrays.asList(defaultValue.split(",")).stream().limit(1).collect(Collectors.toList());
            }
        }



        return null;
    }


    private Long dynamicDateFormNow(Map<String, Object> component) {
        Map<String, Object> attrs = (Map<String, Object>) ((Map<String, Object>) component.get("options")).get("attrs");
        Object aDefault = attrs.get("default");
        Boolean isDynamic = (Boolean) attrs.getOrDefault("isDynamic", false);
        if (ObjectUtils.isEmpty(aDefault) || !isDynamic) return null;

        Calendar now = Calendar.getInstance();
        int nowYear = now.get(Calendar.YEAR);
        Map<String, Object> aDefaultMap = (Map<String, Object>) aDefault;
        if (Integer.parseInt(aDefaultMap.get("dkey").toString()) == 0){
            now.set(nowYear, 0, 1, 0, 0, 0);
            return now.getTimeInMillis();
        }
        if (Integer.parseInt(aDefaultMap.get("dkey").toString()) == 1){
            now.set(nowYear - 1, 0, 1, 0, 0, 0);
            return now.getTimeInMillis();
        }
        if (Integer.parseInt(aDefaultMap.get("dkey").toString()) == 2){
            int dynamicPrefix = Integer.parseInt(aDefaultMap.get("dynamicPrefix").toString());
            String dynamicSuffix = aDefaultMap.get("dynamicSuffix").toString();
            now.set(StringUtils.equals("before", dynamicSuffix) ? (nowYear - dynamicPrefix) : (nowYear + dynamicPrefix), 0, 1, 0, 0, 0);
            return now.getTimeInMillis();
        }
        return 0L;
    }
}

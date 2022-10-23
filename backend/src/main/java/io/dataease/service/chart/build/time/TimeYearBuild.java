package io.dataease.service.chart.build.time;

import io.dataease.service.chart.build.TimeBuild;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Service("timeYearWidget")
public class TimeYearBuild extends TimeBuild {
    @Override
    public List<Long> dynamicDateFromNow(Map<String, Object> component) {

        Boolean isDynamic = false;
        Map<String, Object> defaultMap = null;
        Map<String, Object> attrs = buildAttrs(component);
        Object defaultObject = attrs.get("default");
        if (ObjectUtils.isNotEmpty(defaultObject)) {
            defaultMap = (Map<String, Object>) defaultObject;
            isDynamic = (Boolean) defaultMap.getOrDefault("isDynamic", false);
        }
        if (ObjectUtils.isEmpty(defaultObject) || !isDynamic) return null;

        Calendar now = Calendar.getInstance();
        int nowYear = now.get(Calendar.YEAR);
        int dkey = (int) Double.parseDouble(defaultMap.get("dkey").toString());
        if (dkey == 0) {
            now.set(nowYear, 0, 1, 0, 0, 0);
            return add2List(now.getTimeInMillis());
        }
        if (dkey == 1) {
            now.set(nowYear - 1, 0, 1, 0, 0, 0);
            return add2List(now.getTimeInMillis());
        }
        if (dkey == 2) {
            int dynamicPrefix = (int) Double.parseDouble(defaultMap.get("dynamicPrefix").toString());
            String dynamicSuffix = defaultMap.get("dynamicSuffix").toString();
            now.set(StringUtils.equals("before", dynamicSuffix) ? (nowYear - dynamicPrefix) : (nowYear + dynamicPrefix), 0, 1, 0, 0, 0);
            return add2List(now.getTimeInMillis());
        }
        return null;
    }

}

package io.dataease.service.chart.build.time;

import io.dataease.service.chart.build.TimeBuild;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Service("timeDateWidget")
public class TimeDateBuild extends TimeBuild {
    public List<Long> dynamicDateFromNow(Map<String, Object> component) {
        Map<String, Object> attrs = buildAttrs(component);
        Object defaultObject = attrs.get("default");

        Boolean isDynamic = false;
        Map<String, Object> defaultMap = null;
        if (ObjectUtils.isNotEmpty(defaultObject)) {
            defaultMap = (Map<String, Object>) defaultObject;
            isDynamic = (Boolean) defaultMap.getOrDefault("isDynamic", false);
        }
        if (ObjectUtils.isEmpty(defaultObject) || !isDynamic) return null;

        Calendar now = Calendar.getInstance();
        int nowYear = now.get(Calendar.YEAR);
        int nowMonth = now.get(Calendar.MONTH);
        int nowDate = now.get(Calendar.DATE);
        int dkey = (int) Double.parseDouble(defaultMap.get("dkey").toString());
        if (dkey == 0) {
            now.set(nowYear, nowMonth, nowDate, 0, 0, 0);
            return add2List(now.getTimeInMillis());
        }
        if (dkey == 1) {
            now.set(nowYear, nowMonth, nowDate - 1, 0, 0, 0);
            return add2List(now.getTimeInMillis());
        }

        if (dkey == 2) {
            now.set(nowYear, nowMonth, 1, 0, 0, 0);
            return add2List(now.getTimeInMillis());
        }

        if (dkey == 4) {
            now.set(nowYear, 0, 1, 0, 0, 0);
            return add2List(now.getTimeInMillis());
        }

        if (dkey == 3) {
            int dynamicPrefix = (int) Double.parseDouble(defaultMap.get("dynamicPrefix").toString());
            String dynamicInfill = defaultMap.get("dynamicInfill").toString();
            String dynamicSuffix = defaultMap.get("dynamicSuffix").toString();
            now.set(nowYear, nowMonth, nowDate, 0, 0, 0);
            if (StringUtils.equals("day", dynamicInfill)) {
                int step = dynamicPrefix * (StringUtils.equals("before", dynamicSuffix) ? -1 : 1);
                now.roll(Calendar.DATE, step);
                return add2List(now.getTimeInMillis());
            }
            if (StringUtils.equals("week", dynamicInfill)) {
                int step = dynamicPrefix * (StringUtils.equals("before", dynamicSuffix) ? -1 : 1) * 7;
                now.roll(Calendar.DATE, step);
                return add2List(now.getTimeInMillis());
            }
            if (StringUtils.equals("month", dynamicInfill)) {
                int step = dynamicPrefix * (StringUtils.equals("before", dynamicSuffix) ? -1 : 1);
                now.roll(Calendar.MONTH, step);
                return add2List(now.getTimeInMillis());
            }
            if (StringUtils.equals("year", dynamicInfill)) {
                int step = dynamicPrefix * (StringUtils.equals("before", dynamicSuffix) ? -1 : 1);
                now.roll(Calendar.YEAR, step);
                return add2List(now.getTimeInMillis());
            }
        }

        return null;
    }
}

package io.dataease.service.chart.build.time;

import io.dataease.service.chart.build.TimeBuild;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Service("timeMonthWidget")
public class TimeMonthBuild extends TimeBuild {
    @Override
    public List<Long> dynamicDateFromNow(Map<String, Object> component) {
        Map<String, Object> attrs = buildAttrs(component);

        Object defaultObject = attrs.get("default");

        if (ObjectUtils.isEmpty(defaultObject)) return null;
        Map<String, Object> defaultMap = (Map<String, Object>) defaultObject;

        Boolean isDynamic = (Boolean) defaultMap.getOrDefault("isDynamic", false);
        if (!isDynamic) return null;
        Calendar now = Calendar.getInstance();
        int nowYear = now.get(Calendar.YEAR);
        int nowMonth = now.get(Calendar.MONTH);

        int dkey = (int) Double.parseDouble(defaultMap.get("dkey").toString());
        if (dkey == 0) {
            now.set(nowYear, nowMonth, 1, 0, 0, 0);
            return add2List(now.getTimeInMillis());
        }
        if (dkey == 1) {
            now.set(nowYear, nowMonth - 1, 1, 0, 0, 0);
            return add2List(now.getTimeInMillis());
        }
        if (dkey == 2) {
            now.set(nowYear, 0, 1, 0, 0, 0);
            return add2List(now.getTimeInMillis());
        }
        if (dkey == 4) {
            now.set(nowYear - 1, nowMonth, 1, 0, 0, 0);
            return add2List(now.getTimeInMillis());
        }
        if (dkey == 3) {
            int dynamicPrefix = (int) Double.parseDouble(defaultMap.get("dynamicPrefix").toString());
            String dynamicSuffix = defaultMap.get("dynamicSuffix").toString();
            int targetMonth = StringUtils.equals("before", dynamicSuffix) ? (nowMonth - dynamicPrefix) : (nowMonth + dynamicPrefix);
            now.set(nowYear, targetMonth, 1, 0, 0, 0);
            return add2List(now.getTimeInMillis());
        }
        return null;
    }
}

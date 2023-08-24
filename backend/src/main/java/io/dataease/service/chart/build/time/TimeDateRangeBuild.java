package io.dataease.service.chart.build.time;

import io.dataease.service.chart.build.TimeBuild;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Service("timeDateRangeWidget")
public class TimeDateRangeBuild extends TimeBuild {
    @Override
    public List<Long> dynamicDateFromNow(Map<String, Object> component) {

        Map<String, Object> attrs = buildAttrs(component);
        Object defaultObject = null;
        if ((defaultObject = attrs.get("default")) == null) return null;
        Map<String, Object> defaultMap = (Map<String, Object>) defaultObject;

        Boolean isDynamic = (Boolean) defaultMap.getOrDefault("isDynamic", false);
        if (!isDynamic) return null;

        int dkey = (int) Double.parseDouble(defaultMap.get("dkey").toString());
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        if (dkey % 5 == 0) {
            if (dkey == 5) {
                now.add(Calendar.DATE, -7);
            }
            now.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            long start = now.getTimeInMillis();
            now.add(Calendar.DATE, 7);
            now.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            long end = now.getTimeInMillis();
            return add2List(start, end);
        }

        if (dkey == 1) {
            Calendar endBase = (Calendar) now.clone();
            now.set(Calendar.DAY_OF_MONTH, 1);
            long start = now.getTimeInMillis();
            endBase.add(Calendar.MONTH, 1);
            endBase.set(Calendar.DAY_OF_MONTH, 0);
            long end = endBase.getTimeInMillis();
            return add2List(start, end);
        }
        if (dkey == 6) {
            Calendar endBase = (Calendar) now.clone();
            now.add(Calendar.MONTH, -1);
            now.set(Calendar.DAY_OF_MONTH, 1);
            long start = now.getTimeInMillis();
            endBase.set(Calendar.DAY_OF_MONTH, 0);
            long end = endBase.getTimeInMillis();
            return add2List(start, end);
        }

        if (dkey % 5 == 2) {
            int step = 0;
            if (dkey == 7) {
                step = -1;
            }
            Calendar endBase = (Calendar) now.clone();
            Long start = quarterBegin(now, step);
            Long end = quarterEnd(endBase, step);
            return add2List(start, end);
        }

        if (dkey == 3) {
            now.set(Calendar.MONTH, 0);
            now.set(Calendar.DAY_OF_MONTH, 1);
            long start = now.getTimeInMillis();
            now.add(Calendar.YEAR, 1);
            now.add(Calendar.DAY_OF_MONTH, -1);
            long end = now.getTimeInMillis();
            return add2List(start, end);
        }
        if (dkey == 8) {
            now.add(Calendar.YEAR, -1);
            now.set(Calendar.MONTH, 0);
            now.set(Calendar.DAY_OF_MONTH, 1);
            long start = now.getTimeInMillis();
            now.add(Calendar.YEAR, 1);
            now.add(Calendar.DAY_OF_MONTH, -1);
            long end = now.getTimeInMillis();
            return add2List(start, end);
        }

        if (dkey == 4) {
            Object startDynamicObject = defaultMap.get("sDynamicPrefix");
            Object endDynamicObject = defaultMap.get("eDynamicPrefix");
            if (ObjectUtils.isNotEmpty(startDynamicObject) && ObjectUtils.isNotEmpty(endDynamicObject)) {
                String startDynamicInfill = defaultMap.get("sDynamicInfill").toString();
                String endDynamicInfill = defaultMap.get("eDynamicInfill").toString();
                String startDynamicSuffix = defaultMap.get("sDynamicSuffix").toString();
                String endDynamicSuffix = defaultMap.get("eDynamicSuffix").toString();
                int startDynamic = (int) Double.parseDouble(startDynamicObject.toString());
                int endDynamic = (int) Double.parseDouble(endDynamicObject.toString());
                Calendar endBase = (Calendar) now.clone();
                Long start = customTime(now, startDynamic, startDynamicInfill, startDynamicSuffix);
                Long end = customTime(endBase, endDynamic, endDynamicInfill, endDynamicSuffix);
                return add2List(start, end);
            }
        }

        return null;
    }

    private Long customTime(Calendar now, int dynamicPrefix, String dynamicInfill, String dynamicSuffix) {
        if (StringUtils.equals(dynamicInfill, "day")) {
            int step = dynamicPrefix * (StringUtils.equals("before", dynamicSuffix) ? -1 : 1);
            now.add(Calendar.DAY_OF_MONTH, step);
        }
        if (StringUtils.equals(dynamicInfill, "week")) {
            int step = dynamicPrefix * (StringUtils.equals("before", dynamicSuffix) ? -1 : 1) * 7;
            now.add(Calendar.DAY_OF_MONTH, step);
        }
        if (StringUtils.equals(dynamicInfill, "month")) {
            int step = dynamicPrefix * (StringUtils.equals("before", dynamicSuffix) ? -1 : 1);
            now.add(Calendar.MONTH, step);
        }
        if (StringUtils.equals("year", dynamicInfill)) {
            int step = dynamicPrefix * (StringUtils.equals("before", dynamicSuffix) ? -1 : 1);
            now.add(Calendar.YEAR, step);
        }
        return now.getTimeInMillis();
    }

    private Long quarterBegin(Calendar instance, int step) {
        int month = instance.get(Calendar.MONTH);
        int quarterBegin = (int) Math.floor(month / 3) * 3;
        instance.set(Calendar.MONTH, quarterBegin + (3 * step));
        instance.set(Calendar.DAY_OF_MONTH, 1);
        return instance.getTimeInMillis();
    }

    private Long quarterEnd(Calendar instance, int step) {
        int month = instance.get(Calendar.MONTH);
        int quarterBegin = (int) Math.floor(month / 3) * 3;
        instance.set(Calendar.MONTH, quarterBegin + (3 * (step + 1)));
        instance.set(Calendar.DAY_OF_MONTH, 1);
        instance.add(Calendar.DATE, -1);
        return instance.getTimeInMillis();
    }
}

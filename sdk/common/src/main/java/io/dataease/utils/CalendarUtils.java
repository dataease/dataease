package io.dataease.utils;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Calendar;

public class CalendarUtils {

    public static Long getTimeAfterMonth(Long time, int months) {
        if (ObjectUtils.isEmpty(time)) time = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTimeInMillis();
    }
}

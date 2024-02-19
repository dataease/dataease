package io.dataease.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    public static String time2String(Long time, String pattern) {
        if (StringUtils.isBlank(pattern)) pattern = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime timeByMilli = Instant.ofEpochMilli(time).atZone(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
        return format.format(timeByMilli);
    }
    public static String time2String(Long time) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return time2String(time, pattern);
    }
}

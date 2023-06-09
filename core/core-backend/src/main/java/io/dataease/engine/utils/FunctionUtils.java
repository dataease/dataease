package io.dataease.engine.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FunctionUtils {
    public static String format = "yyyy-MM-dd HH:mm:ss";

    public static String date_format(String date, String format) throws Exception {
        if (StringUtils.isEmpty(date)) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Date parse = simpleDateFormat.parse(date);
        return simpleDateFormat.format(parse);
    }

    public static String str_to_date(String date, String format) throws Exception {
        if (StringUtils.isEmpty(date)) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Date parse = simpleDateFormat.parse(date);
        return simpleDateFormat.format(parse);
    }

    public static Long unix_timestamp(String date) throws Exception {
        if (StringUtils.isEmpty(date)) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Date parse = simpleDateFormat.parse(date);
        return parse.getTime();
    }

    public static String from_unixtime(Long timestamp, String format) throws Exception {
        if (ObjectUtils.isEmpty(timestamp)) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Date date = new Date(timestamp);
        return simpleDateFormat.format(date);
    }
}

package io.dataease.engine.func.scalar;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ScalarFunctions {
    public static String format = "yyyy-MM-dd HH:mm:ss";

    public static String date_format(String date, String format) {
        try {
            if (StringUtils.isEmpty(date)) {
                return null;
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            Date parse = simpleDateFormat.parse(date);
            return simpleDateFormat.format(parse);
        } catch (Exception e) {
            return null;
        }
    }

    public static String str_to_date(String date, String format) {
        try {
            if (StringUtils.isEmpty(date)) {
                return null;
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            Date parse = simpleDateFormat.parse(date);
            return simpleDateFormat.format(parse);
        } catch (Exception e) {
            return null;
        }
    }

    public static Long unix_timestamp(String date) {
        try {
            if (StringUtils.isEmpty(date)) {
                return null;
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            Date parse = simpleDateFormat.parse(date);
            return parse.getTime();
        } catch (Exception e) {
            return null;
        }
    }

    public static String from_unixtime(Long timestamp, String format) {
        try {
            if (ObjectUtils.isEmpty(timestamp)) {
                return null;
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            Date date = new Date(timestamp);
            return simpleDateFormat.format(date);
        } catch (Exception e) {
            return null;
        }
    }
}

package io.dataease.engine.func.scalar;

import io.dataease.engine.utils.Utils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ScalarFunctions {
    public static String format = "yyyy-MM-dd HH:mm:ss";
    public static String minuteFormat = "yyyy-MM-dd HH:mm";
    public static String hourFormat = "yyyy-MM-dd HH";
    public static String dateOnly = "yyyy-MM-dd";
    public static String monthOnly = "yyyy-MM";
    public static String yearOnly = "yyyy";
    public static String timeOnly = "HH:mm:ss";

    public static String date_format(String date, String format) {
        try {
            if (StringUtils.isEmpty(date)) {
                return null;
            }
            if (StringUtils.equalsIgnoreCase(format, timeOnly)) {
                String[] s = date.split(" ");
                if (s.length > 1) {
                    date = s[1];
                } else {
                    date = s[0];
                }
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
            if (StringUtils.equalsIgnoreCase(format, timeOnly)) {
                String[] s = date.split(" ");
                if (s.length > 1) {
                    date = s[1];
                } else {
                    date = s[0];
                }
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            Date parse = simpleDateFormat.parse(date);
            return simpleDateFormat.format(parse);
        } catch (Exception e) {
            return null;
        }
    }

    public static String cast_date_format(String date, String sourceFormat, String targetFormat) {
        try {
            if (StringUtils.isEmpty(date)) {
                return null;
            }
            if (StringUtils.equalsIgnoreCase(sourceFormat, timeOnly)) {
                date = date.split(" ")[1];
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(sourceFormat);
            Date parse = simpleDateFormat.parse(date);

            SimpleDateFormat s = new SimpleDateFormat(targetFormat);
            return s.format(parse);
        } catch (Exception e) {
            return null;
        }
    }

    public static Long unix_timestamp(String date) {
        try {
            if (StringUtils.isEmpty(date)) {
                return null;
            }
            return Utils.allDateFormat2Long(date);
        } catch (Exception e) {
            return null;
        }
    }

    public static String get_date_format(String date) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            simpleDateFormat.parse(date);
            return format;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(minuteFormat);
            simpleDateFormat.parse(date);
            return minuteFormat;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(hourFormat);
            simpleDateFormat.parse(date);
            return hourFormat;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateOnly);
            simpleDateFormat.parse(date);
            return dateOnly;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(monthOnly);
            simpleDateFormat.parse(date);
            return monthOnly;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(yearOnly);
            simpleDateFormat.parse(date);
            return yearOnly;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeOnly);
            simpleDateFormat.parse(date);
            return timeOnly;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return format;
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

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
            format = get_date_format(date);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            Date parse = simpleDateFormat.parse(date);
            return simpleDateFormat.format(parse);
        } catch (Exception e) {
            return null;
        }
    }

    public static String date_format_real(String date, String format) {
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
            format = get_date_format(date);
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
            sourceFormat = get_date_format(date);
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
        // check date split '-' or '/'
        String format1 = format;
        String minuteFormat1 = minuteFormat;
        String hourFormat1 = hourFormat;
        String timeOnly1 = timeOnly;
        String dateOnly1 = dateOnly;
        String monthOnly1 = monthOnly;
        String yearOnly1 = yearOnly;
        if (date != null && date.contains("/")) {
            format1 = format1.replaceAll("-", "/");
            minuteFormat1 = minuteFormat1.replaceAll("-", "/");
            hourFormat1 = hourFormat1.replaceAll("-", "/");
            timeOnly1 = timeOnly1.replaceAll("-", "/");
            dateOnly1 = dateOnly1.replaceAll("-", "/");
            monthOnly1 = monthOnly1.replaceAll("-", "/");
            yearOnly1 = yearOnly1.replaceAll("-", "/");
        }
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format1);
            simpleDateFormat.parse(date);
            return format1;
        } catch (Exception e) {
        }
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(minuteFormat1);
            simpleDateFormat.parse(date);
            return minuteFormat1;
        } catch (Exception e) {
        }
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(hourFormat1);
            simpleDateFormat.parse(date);
            return hourFormat1;
        } catch (Exception e) {
        }
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeOnly1);
            simpleDateFormat.parse(date);
            return timeOnly1;
        } catch (Exception e) {
        }
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateOnly1);
            simpleDateFormat.parse(date);
            return dateOnly1;
        } catch (Exception e) {
        }
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(monthOnly1);
            simpleDateFormat.parse(date);
            return monthOnly1;
        } catch (Exception e) {
        }
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(yearOnly1);
            simpleDateFormat.parse(date);
            return yearOnly1;
        } catch (Exception e) {
        }
        return format1;
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

    public static String concat(String str1, String str2) {
        return str1 + str2;
    }
}

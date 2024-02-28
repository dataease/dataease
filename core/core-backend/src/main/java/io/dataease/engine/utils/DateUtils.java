package io.dataease.engine.utils;

import java.text.SimpleDateFormat;

/**
 * @Author Junjun
 */
public class DateUtils {
    private static final String format = "yyyy-MM-dd HH:mm:ss";
    private static final String minuteFormat = "yyyy-MM-dd HH:mm";
    private static final String hourFormat = "yyyy-MM-dd HH";
    private static final String dateOnly = "yyyy-MM-dd";
    private static final String monthOnly = "yyyy-MM";
    private static final String yearOnly = "yyyy";
    private static final String timeOnly = "HH:mm:ss";

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
}

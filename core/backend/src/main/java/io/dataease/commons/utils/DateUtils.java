package io.dataease.commons.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class DateUtils {
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";


    public static Date getDate(String dateString) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        return dateFormat.parse(dateString);
    }

    public static Date getTime(String timeString) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_PATTERN);
        return dateFormat.parse(timeString);
    }

    public static String getDateString(Date date) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        return dateFormat.format(date);
    }

    public static String getDateString(long timeStamp) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        return dateFormat.format(timeStamp);
    }

    public static String getTimeString(Date date) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_PATTERN);
        return dateFormat.format(date);
    }

    public static String getTimeString(long timeStamp) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_PATTERN);
        return dateFormat.format(timeStamp);
    }

    public static String getTimeStr(long timeStamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_PATTERN);
        return dateFormat.format(timeStamp);
    }


    public static Date dateSum(Date date, int countDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, countDays);

        return calendar.getTime();
    }

    /**
     * 获取入参日期所在周的周一周末日期。 日期对应的时间为当日的零点
     *
     * @return Map<String, String>(2); key取值范围：firstTime/lastTime
     */
    public static Map<String, Date> getWeedFirstTimeAndLastTime(Date date) {
        Map<String, Date> returnMap = new HashMap<>();
        Calendar calendar = Calendar.getInstance();

        //Calendar默认一周的开始是周日。业务需求从周一开始算，所以要"+1"
        int weekDayAdd = 1;

        try {
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMinimum(Calendar.DAY_OF_WEEK));
            calendar.add(Calendar.DAY_OF_MONTH, weekDayAdd);

            //第一天的时分秒是 00:00:00 这里直接取日期，默认就是零点零分
            Date thisWeekFirstTime = getDate(getDateString(calendar.getTime()));

            calendar.clear();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMaximum(Calendar.DAY_OF_WEEK));
            calendar.add(Calendar.DAY_OF_MONTH, weekDayAdd);

            //最后一天的时分秒应当是23:59:59。 处理方式是增加一天计算日期再-1
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            Date nextWeekFirstDay = getDate(getDateString(calendar.getTime()));
            Date thisWeekLastTime = getTime(getTimeString(nextWeekFirstDay.getTime() - 1));

            returnMap.put("firstTime", thisWeekFirstTime);
            returnMap.put("lastTime", thisWeekLastTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnMap;

    }


    /**
     * 获取当天的起始时间Date
     *
     * @param time 指定日期  例： 2020-12-13 06:12:42
     * @return 当天起始时间 例： 2020-12-13 00:00:00
     * @throws Exception
     */
    public static Date getDayStartTime(Date time) throws Exception {
        return getDate(getDateString(time));
    }

    public static List<String> getForecastPeriod(String baseTime, int period, String dateStyle, String pattern) throws ParseException {
        String split = "-";
        if (StringUtils.equalsIgnoreCase(pattern, "date_split")) {
            split = "/";
        }

        List<String> result = new ArrayList<>(period);
        switch (dateStyle) {
            case "y":
                int baseYear = Integer.parseInt(baseTime);
                for (int i = 1; i <= period; i++) {
                    result.add(baseYear + i + "");
                }
                break;
            case "y_Q":
                String[] yQ = baseTime.split(split);
                int year = Integer.parseInt(yQ[0]);
                int quarter = Integer.parseInt(yQ[1].split("Q")[1]);
                for (int i = 0; i < period; i++) {
                    quarter = quarter % 4 + 1;
                    if (quarter == 1) {
                        year += 1;
                    }
                    result.add(year + split + "Q" + quarter);
                }
                break;
            case "y_M":
                String[] yM = baseTime.split(split);
                int y = Integer.parseInt(yM[0]);
                int month = Integer.parseInt(yM[1]);
                for (int i = 0; i < period; i++) {
                    month = month % 12 + 1;
                    if (month == 1) {
                        y += 1;
                    }
                    String padMonth = month < 10 ? "0" + month : "" + month;
                    result.add(y + split + padMonth);
                }
                break;
            case "y_W":
                String[] yW = baseTime.split(split);
                int yy = Integer.parseInt(yW[0]);
                int w = Integer.parseInt(yW[1].split("W")[1]);
                for (int i = 0; i < period; i++) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setMinimalDaysInFirstWeek(7);
                    calendar.setFirstDayOfWeek(Calendar.MONDAY);
                    calendar.set(Calendar.YEAR, yy);
                    calendar.set(Calendar.MONTH, Calendar.DECEMBER);
                    calendar.set(Calendar.DAY_OF_MONTH, 31);
                    int lastWeek = calendar.get(Calendar.WEEK_OF_YEAR);
                    w += 1;
                    if (w > lastWeek) {
                        yy += 1;
                        w = 1;
                    }
                    result.add(yy + split + "W" + w);
                }
                break;
            case "y_M_d":
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy" + split + "MM" + split + "dd");
                Calendar calendar = Calendar.getInstance();
                Date baseDate = sdf.parse(baseTime);
                calendar.setTime(baseDate);
                for (int i = 0; i < period; i++) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    Date curDate = calendar.getTime();
                    String date = sdf.format(curDate);
                    result.add(date);
                }
                break;
            default:
                break;
        }
        return result;
    }
}

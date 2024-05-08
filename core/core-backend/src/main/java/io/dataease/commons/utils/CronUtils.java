package io.dataease.commons.utils;

import io.dataease.utils.LogUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CronUtils {


    public static CronTrigger getCronTrigger(String cron) {
        if (!CronExpression.isValidExpression(cron)) {
            throw new RuntimeException("cron :" + cron + "表达式解析错误");
        }
        return TriggerBuilder.newTrigger().withIdentity("Calculate Date").withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
    }

    public static Date getNextTriggerTime(String cron) {
        Date date = null;
        try {
            CronTrigger trigger = getCronTrigger(cron);
            Date startDate = trigger.getStartTime();
            date = trigger.getFireTimeAfter(startDate);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        return date;
    }

    public static String tempCron() {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, 5);
        return instance.get(Calendar.SECOND) + " " +
                instance.get(Calendar.MINUTE) + " " +
                instance.get(Calendar.HOUR_OF_DAY) + " * * ?";
    }

    public static String cron(Integer rateType, String rateVal) {
        if (rateType == 0) {
            return rateVal;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = null;
        try {
            date = sdf.parse(rateVal);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar instance = Calendar.getInstance();
        assert date != null;
        instance.setTime(date);

        if (rateType == 1) {
            return instance.get(Calendar.SECOND) + " " +
                    instance.get(Calendar.MINUTE) + " " +
                    instance.get(Calendar.HOUR_OF_DAY) + " * * ?";
        }
        if (rateType == 2) {
            return instance.get(Calendar.SECOND) + " " +
                    instance.get(Calendar.MINUTE) + " " +
                    instance.get(Calendar.HOUR_OF_DAY) + " ? * " +
                    getDayOfWeek(instance);
        }
        if (rateType == 3) {
            return instance.get(Calendar.SECOND) + " " +
                    instance.get(Calendar.MINUTE) + " " +
                    instance.get(Calendar.HOUR_OF_DAY) + " " +
                    instance.get(Calendar.DATE) + " * ?";
        }

        return null;
    }


    private static String getDayOfWeek(Calendar instance) {
        int index = instance.get(Calendar.DAY_OF_WEEK);
        index = (index % 7) + 1;
        return String.valueOf(index);
    }

    // 判断任务是否过期
    public static Boolean taskExpire(Long endTime) {
        if (ObjectUtils.isEmpty(endTime))
            return false;
        long now = System.currentTimeMillis();
        return now > endTime;
    }


}

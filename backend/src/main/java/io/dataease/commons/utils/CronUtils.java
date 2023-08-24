package io.dataease.commons.utils;

import io.dataease.plugins.common.entity.GlobalTaskEntity;
import org.apache.commons.lang3.ObjectUtils;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author song.tianyang
 * @Date 2020/12/17 4:06 下午
 * @Description CRON解析类
 */
public class CronUtils {

    /**
     * 解析表达式，获取CronTrigger
     *
     * @param cron
     * @return
     */
    public static CronTrigger getCronTrigger(String cron) {
        if (!CronExpression.isValidExpression(cron)) {
            throw new RuntimeException("cron :" + cron + "表达式解析错误");
        }
        return TriggerBuilder.newTrigger().withIdentity("Calculate Date").withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
    }

    /**
     * 获取以指定时间为开始时间的下一次执行时间
     *
     * @param cron
     * @param start
     * @return
     */
    public static Date getNextTriggerTime(String cron, Date start) {
        if (start == null) {
            return getNextTriggerTime(cron);
        } else {
            CronTrigger trigger = getCronTrigger(cron);
            return trigger.getFireTimeAfter(start);
        }
    }

    /**
     * 获取以当前日期为准的下一次执行时间
     *
     * @param cron
     * @return
     */
    public static Date getNextTriggerTime(String cron) {
        Date date = null;
        try {
            CronTrigger trigger = getCronTrigger(cron);
            Date startDate = trigger.getStartTime();
            date = trigger.getFireTimeAfter(startDate);
        } catch (Exception e) {

        }
        return date;
    }

    public static String cron(GlobalTaskEntity taskEntity) {
        if (taskEntity.getRateType() == -1) {
            return taskEntity.getRateVal();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = null;
        try {
            date = sdf.parse(taskEntity.getRateVal());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);

        if (taskEntity.getRateType() == 0) {
            return instance.get(Calendar.SECOND) + " " +
                    instance.get(Calendar.MINUTE) + " " +
                    instance.get(Calendar.HOUR_OF_DAY) + " * * ?";
        }
        if (taskEntity.getRateType() == 1) {
            return instance.get(Calendar.SECOND) + " " +
                    instance.get(Calendar.MINUTE) + " " +
                    instance.get(Calendar.HOUR_OF_DAY) + " ? * " +
                    getDayOfWeek(instance);
        }
        if (taskEntity.getRateType() == 2) {
            return instance.get(Calendar.SECOND) + " " +
                    instance.get(Calendar.MINUTE) + " " +
                    instance.get(Calendar.HOUR_OF_DAY) + " " +
                    instance.get(Calendar.DATE) + " * ?";
        }

        return null;
    }

    public static String cron() {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, 5);
        return instance.get(Calendar.SECOND) + " " +
                instance.get(Calendar.MINUTE) + " " +
                instance.get(Calendar.HOUR_OF_DAY) + " * * ?";
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
        Long now = System.currentTimeMillis();
        return now > endTime;
    }


}

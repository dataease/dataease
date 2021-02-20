package io.dataease.commons.utils;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;

import java.util.Date;

/**
 * @author song.tianyang
 * @Date 2020/12/17 4:06 下午
 * @Description CRON解析类
 */
public class CronUtils {

    /**
     * 解析表达式，获取CronTrigger
     * @param cron
     * @return
     */
    public static CronTrigger getCronTrigger(String cron) {
        if (!CronExpression.isValidExpression(cron)) {
            throw new RuntimeException("cron :" + cron + "表达式解析错误");
        }
        return TriggerBuilder.newTrigger().withIdentity("Caclulate Date").withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
    }

    /**
     * 获取以指定时间为开始时间的下一次执行时间
     * @param cron
     * @param start
     * @return
     */
    public static Date getNextTriggerTime(String cron, Date start) {
        if (start == null) {
            return getNextTriggerTime(cron);
        }else{
            CronTrigger trigger = getCronTrigger(cron);
            return trigger.getFireTimeAfter(start);
        }
    }

    /**
     * 获取以当前日期为准的下一次执行时间
     * @param cron
     * @return
     */
    public static Date getNextTriggerTime(String cron) {
        Date date = null;
        try{
            CronTrigger trigger = getCronTrigger(cron);
            Date startDate = trigger.getStartTime();
            date = trigger.getFireTimeAfter(startDate);
        }catch (Exception e){

        }
        return  date;
    }


}

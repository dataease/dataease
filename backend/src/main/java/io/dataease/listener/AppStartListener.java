package io.dataease.listener;

import io.dataease.job.sechedule.ScheduleManager;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class AppStartListener implements ApplicationListener<ApplicationReadyEvent> {
    @Resource
    private ScheduleManager scheduleManager;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        System.out.println("================= 应用启动 =================");
        /* cron schedule */
//        scheduleManager.addCronJob(new JobKey("abc", "def"), new TriggerKey("abc", "def"), TestJob.class, "*/10 * * * * ?");
        /* single schedule*/
//        long timestamp = System.currentTimeMillis() + 90 * 1000;
//        Date date = new Date(timestamp);
//        scheduleManager.addSingleJob(new JobKey("abc", "def"), new TriggerKey("abc", "def"), TestJob.class, date);
        // TODO 项目启动，从数据库读取任务加入到Quartz
    }
}

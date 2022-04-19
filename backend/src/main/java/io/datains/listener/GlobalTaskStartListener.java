package io.datains.listener;

import io.datains.auth.service.AuthUserService;
import io.datains.job.sechedule.ScheduleManager;
import io.datains.job.sechedule.strategy.TaskHandler;
import io.datains.job.sechedule.strategy.TaskStrategyFactory;
import io.datains.plugins.common.entity.GlobalTaskEntity;
import io.datains.plugins.config.SpringContextUtil;
import io.datains.plugins.xpack.email.service.EmailXpackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GlobalTaskStartListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private AuthUserService authUserService;

    @Autowired
    private ScheduleManager scheduleManager;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (authUserService.pluginLoaded()) {
            EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);

            List<GlobalTaskEntity> tasks = emailXpackService.allTask();
            tasks.stream().forEach(task -> {
                TaskHandler taskHandler = TaskStrategyFactory.getInvokeStrategy(task.getTaskType());
                try {
                    taskHandler.resetRunningInstance(task.getTaskId());
                    taskHandler.addTask(scheduleManager, task);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}

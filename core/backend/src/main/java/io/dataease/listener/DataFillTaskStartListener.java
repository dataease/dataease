package io.dataease.listener;

import io.dataease.auth.service.AuthUserService;
import io.dataease.service.datafill.DataFillTaskService;
import io.dataease.job.sechedule.ScheduleManager;
import io.dataease.job.sechedule.strategy.TaskHandler;
import io.dataease.job.sechedule.strategy.TaskStrategyFactory;
import io.dataease.plugins.common.entity.GlobalTaskEntity;
import io.dataease.plugins.common.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataFillTaskStartListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private AuthUserService authUserService;

    @Autowired
    private ScheduleManager scheduleManager;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (authUserService.pluginLoaded()) {
            DataFillTaskService service = SpringContextUtil.getBean(DataFillTaskService.class);

            List<GlobalTaskEntity> tasks = service.listActiveTasksGlobalTaskEntity();
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

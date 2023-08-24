package io.dataease.listener;

import io.dataease.commons.utils.LogUtil;
import io.dataease.dto.dataset.DataSetTaskDTO;
import io.dataease.plugins.common.base.domain.DatasetTableTask;
import io.dataease.commons.constants.ScheduleType;
import io.dataease.service.ScheduleService;
import io.dataease.service.dataset.DataSetTableTaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Order(value = 1)
public class AppStartListener implements ApplicationListener<ApplicationReadyEvent> {
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private DataSetTableTaskService dataSetTableTaskService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        LogUtil.info("================= Application start =================");
        // 项目启动，从数据库读取任务加入到Quartz
        List<DataSetTaskDTO> list = dataSetTableTaskService.list(new DatasetTableTask());
        for (DatasetTableTask task : list) {
            try {
                if (!StringUtils.equalsIgnoreCase(task.getRate(), ScheduleType.SIMPLE.toString())) {
                    if (StringUtils.equalsIgnoreCase(task.getEnd(), "1")) {
                        if (task.getEndTime() != null && task.getEndTime() > 0) {
                            if (task.getEndTime() > System.currentTimeMillis()) {
                                scheduleService.addSchedule(task);
                            }
                        } else {
                            scheduleService.addSchedule(task);
                        }
                    } else {
                        scheduleService.addSchedule(task);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

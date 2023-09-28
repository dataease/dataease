package io.dataease.job.sechedule;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.dataease.datasource.server.DatasourceServer;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;



@Component
public class Schedular {

    @Resource
    private DatasourceServer datasourceServer;

    @QuartzScheduled(cron = "0 0/1 * * * ?")
    public void updateDatasourceStatus() {
        System.out.println("updateDatasourceStatus");
        datasourceServer.updateDatasourceStatus();
    }


}

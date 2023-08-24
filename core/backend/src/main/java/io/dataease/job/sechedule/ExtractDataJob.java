package io.dataease.job.sechedule;

import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.service.dataset.ExtractDataService;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
public class ExtractDataJob extends DeScheduleJob{
    private ExtractDataService extractDataService;

    public ExtractDataJob() {
        extractDataService = (ExtractDataService) CommonBeanFactory.getBean(ExtractDataService.class);
    }

    @Override
    void businessExecute(JobExecutionContext context) {
        extractDataService.extractData(datasetTableId, taskId, updateType, context);
    }

}

package io.dataease.service.dataset;

import io.dataease.base.domain.DatasetTableTask;
import io.dataease.base.domain.DatasetTableTaskExample;
import io.dataease.base.mapper.DatasetTableTaskMapper;
import io.dataease.job.sechedule.ScheduleManager;
import io.dataease.job.sechedule.TestJob;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @Author gin
 * @Date 2021/3/4 1:26 下午
 */
@Service
public class DataSetTableTaskService {
    @Resource
    private DatasetTableTaskMapper datasetTableTaskMapper;

    @Resource
    private ScheduleManager scheduleManager;

    public DatasetTableTask save(DatasetTableTask datasetTableTask) throws Exception {
        if (StringUtils.isEmpty(datasetTableTask.getId())) {
            datasetTableTask.setId(UUID.randomUUID().toString());
            datasetTableTask.setCreateTime(System.currentTimeMillis());
            datasetTableTaskMapper.insert(datasetTableTask);
        } else {
            datasetTableTaskMapper.updateByPrimaryKey(datasetTableTask);
        }

        if (StringUtils.equalsIgnoreCase(datasetTableTask.getRate(), "0")) {
            scheduleManager.addOrUpdateSingleJob(new JobKey(datasetTableTask.getId(), datasetTableTask.getTableId()),
                    new TriggerKey(datasetTableTask.getId(), datasetTableTask.getTableId()),
                    TestJob.class,
                    new Date(datasetTableTask.getStartTime()));
        } else if (StringUtils.equalsIgnoreCase(datasetTableTask.getRate(), "1")) {
            Date endTime;
            if (datasetTableTask.getEndTime() == null || datasetTableTask.getEndTime() == 0) {
                endTime = null;
            } else {
                endTime = new Date(datasetTableTask.getEndTime());
            }

            scheduleManager.addOrUpdateCronJob(new JobKey(datasetTableTask.getId(), datasetTableTask.getTableId()),
                    new TriggerKey(datasetTableTask.getId(), datasetTableTask.getTableId()),
                    TestJob.class,
                    datasetTableTask.getCron(), new Date(datasetTableTask.getStartTime()), endTime);
        }
        return datasetTableTask;
    }

    public void delete(String id) {
        DatasetTableTask datasetTableTask = datasetTableTaskMapper.selectByPrimaryKey(id);
        datasetTableTaskMapper.deleteByPrimaryKey(id);
        scheduleManager.removeJob(new JobKey(datasetTableTask.getId(), datasetTableTask.getTableId()), new TriggerKey(datasetTableTask.getId(), datasetTableTask.getTableId()));
    }

    public List<DatasetTableTask> list(DatasetTableTask datasetTableTask) {
        DatasetTableTaskExample datasetTableTaskExample = new DatasetTableTaskExample();
        DatasetTableTaskExample.Criteria criteria = datasetTableTaskExample.createCriteria();
        if (StringUtils.isNotEmpty(datasetTableTask.getTableId())) {
            criteria.andTableIdEqualTo(datasetTableTask.getTableId());
        }
        datasetTableTaskExample.setOrderByClause("create_time desc,name asc");
        return datasetTableTaskMapper.selectByExample(datasetTableTaskExample);
    }
}

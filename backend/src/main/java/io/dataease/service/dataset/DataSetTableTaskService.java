package io.dataease.service.dataset;

import io.dataease.base.domain.DatasetTableTask;
import io.dataease.base.domain.DatasetTableTaskExample;
import io.dataease.base.mapper.DatasetTableTaskMapper;
import io.dataease.service.ScheduleService;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    private DataSetTableTaskLogService dataSetTableTaskLogService;
    @Resource
    private ScheduleService scheduleService;

    public DatasetTableTask save(DatasetTableTask datasetTableTask) throws Exception {
        // check
        if (StringUtils.isNotEmpty(datasetTableTask.getCron())) {
            if (!CronExpression.isValidExpression(datasetTableTask.getCron())) {
                throw new RuntimeException("cron expression error.");
            }
        }
        if (StringUtils.isEmpty(datasetTableTask.getId())) {
            datasetTableTask.setId(UUID.randomUUID().toString());
            datasetTableTask.setCreateTime(System.currentTimeMillis());
            datasetTableTaskMapper.insert(datasetTableTask);
        } else {
            datasetTableTaskMapper.updateByPrimaryKey(datasetTableTask);
        }
        scheduleService.addSchedule(datasetTableTask);
        return datasetTableTask;
    }

    public void delete(String id) {
        DatasetTableTask datasetTableTask = datasetTableTaskMapper.selectByPrimaryKey(id);
        datasetTableTaskMapper.deleteByPrimaryKey(id);
        scheduleService.deleteSchedule(datasetTableTask);
        dataSetTableTaskLogService.deleteByTaskId(id);
    }

    public DatasetTableTask get(String id) {
        return datasetTableTaskMapper.selectByPrimaryKey(id);
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

package io.dataease.service.dataset;

import io.dataease.base.domain.*;
import io.dataease.base.mapper.DatasetTableTaskMapper;
import io.dataease.commons.constants.JobStatus;
import io.dataease.commons.constants.ScheduleType;
import io.dataease.controller.request.dataset.DataSetTaskRequest;
import io.dataease.i18n.Translator;
import io.dataease.service.ScheduleService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

/**
 * @Author gin
 * @Date 2021/3/4 1:26 下午
 */
@Service
@Transactional
public class DataSetTableTaskService {
    @Resource
    private DatasetTableTaskMapper datasetTableTaskMapper;
    @Resource
    private DataSetTableTaskLogService dataSetTableTaskLogService;
    @Resource
    private ScheduleService scheduleService;
    @Resource
    @Lazy
    private DataSetTableService dataSetTableService;
    @Resource
    private ExtractDataService extractDataService;

    public DatasetTableTask save(DataSetTaskRequest dataSetTaskRequest) throws Exception {
        checkName(dataSetTaskRequest);
        DatasetTableTask datasetTableTask = dataSetTaskRequest.getDatasetTableTask();
        dataSetTableService.saveIncrementalConfig(dataSetTaskRequest.getDatasetTableIncrementalConfig());

        // check
        if (StringUtils.isNotEmpty(datasetTableTask.getCron())) {
            if (!CronExpression.isValidExpression(datasetTableTask.getCron())) {
                throw new RuntimeException(Translator.get("i18n_cron_expression_error"));
            }
        }
        // check start time and end time
        if (ObjectUtils.isNotEmpty(datasetTableTask.getStartTime())
                && ObjectUtils.isNotEmpty(datasetTableTask.getEndTime())
                && datasetTableTask.getStartTime() != 0
                && datasetTableTask.getEndTime() != 0
                && datasetTableTask.getStartTime() > datasetTableTask.getEndTime()) {
            throw new RuntimeException(Translator.get("i18n_cron_time_error"));
        }
        if (StringUtils.isEmpty(datasetTableTask.getId())) {
            datasetTableTask.setId(UUID.randomUUID().toString());
            datasetTableTask.setCreateTime(System.currentTimeMillis());
            // SIMPLE 类型，提前占位
            if (datasetTableTask.getRate().equalsIgnoreCase(ScheduleType.SIMPLE.toString())) {
                if (extractDataService.updateSyncStatus(dataSetTableService.get(datasetTableTask.getTableId()))) {
                    throw new Exception(Translator.get("i18n_sync_job_exists"));
                } else {
                    //write log
                    DatasetTableTaskLog datasetTableTaskLog = new DatasetTableTaskLog();
                    datasetTableTaskLog.setTableId(datasetTableTask.getTableId());
                    datasetTableTaskLog.setTaskId(datasetTableTask.getId());
                    datasetTableTaskLog.setStatus(JobStatus.Underway.name());
                    datasetTableTaskLog.setStartTime(System.currentTimeMillis());
                    dataSetTableTaskLogService.save(datasetTableTaskLog);
                }
            }
            datasetTableTaskMapper.insert(datasetTableTask);
        } else {
            datasetTableTaskMapper.updateByPrimaryKeySelective(datasetTableTask);
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

    public void delete(DatasetTableTask task) {
        datasetTableTaskMapper.deleteByPrimaryKey(task.getId());
        scheduleService.deleteSchedule(task);
        dataSetTableTaskLogService.deleteByTaskId(task.getId());
    }

    public void deleteByTableId(String id) {
        DatasetTableTaskExample datasetTableTaskExample = new DatasetTableTaskExample();
        DatasetTableTaskExample.Criteria criteria = datasetTableTaskExample.createCriteria();
        criteria.andTableIdEqualTo(id);
        List<DatasetTableTask> datasetTableTasks = datasetTableTaskMapper.selectByExample(datasetTableTaskExample);
        datasetTableTasks.forEach(this::delete);
    }

    public DatasetTableTask get(String id) {
        return datasetTableTaskMapper.selectByPrimaryKey(id);
    }

    public void update(DatasetTableTask datasetTableTask) {
        datasetTableTaskMapper.updateByPrimaryKey(datasetTableTask);
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

    private void checkName(DataSetTaskRequest dataSetTaskRequest) {
        DatasetTableTaskExample datasetTableTaskExample = new DatasetTableTaskExample();
        DatasetTableTaskExample.Criteria criteria = datasetTableTaskExample.createCriteria();
        if (StringUtils.isNotEmpty(dataSetTaskRequest.getDatasetTableTask().getId())) {
            criteria.andIdNotEqualTo(dataSetTaskRequest.getDatasetTableTask().getId());
        }
        if (StringUtils.isNotEmpty(dataSetTaskRequest.getDatasetTableTask().getTableId())) {
            criteria.andTableIdEqualTo(dataSetTaskRequest.getDatasetTableTask().getTableId());
        }
        if (StringUtils.isNotEmpty(dataSetTaskRequest.getDatasetTableTask().getName())) {
            criteria.andNameEqualTo(dataSetTaskRequest.getDatasetTableTask().getName());
        }
        List<DatasetTableTask> list = datasetTableTaskMapper.selectByExample(datasetTableTaskExample);
        if (list.size() > 0) {
            throw new RuntimeException(Translator.get("i18n_task_name_repeat"));
        }
    }
}

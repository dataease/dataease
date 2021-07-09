package io.dataease.service.dataset;

import com.google.gson.Gson;
import io.dataease.base.domain.DatasetTable;
import io.dataease.base.domain.DatasetTableTask;
import io.dataease.base.domain.DatasetTableTaskExample;
import io.dataease.base.domain.DatasetTableTaskLog;
import io.dataease.base.mapper.DatasetTableTaskMapper;
import io.dataease.base.mapper.ext.ExtDataSetTaskMapper;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.commons.constants.JobStatus;
import io.dataease.commons.constants.ScheduleType;
import io.dataease.commons.constants.TaskStatus;
import io.dataease.commons.constants.TriggerType;
import io.dataease.controller.request.dataset.DataSetTaskRequest;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.response.SysUserGridResponse;
import io.dataease.controller.sys.response.SysUserRole;
import io.dataease.dto.dataset.DataSetTaskDTO;
import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import io.dataease.service.ScheduleService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Author gin
 * @Date 2021/3/4 1:26 下午
 */
@Service
@Transactional(rollbackFor = Exception.class)
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
    @Resource
    private ExtDataSetTaskMapper extDataSetTaskMapper;

    public DatasetTableTask save(DataSetTaskRequest dataSetTaskRequest) throws Exception {
        checkName(dataSetTaskRequest);
        DatasetTableTask datasetTableTask = dataSetTaskRequest.getDatasetTableTask();
        if(!datasetTableTask.getType().equalsIgnoreCase("add_scope")){
            dataSetTableService.saveIncrementalConfig(dataSetTaskRequest.getDatasetTableIncrementalConfig());
        }
        // check
        if (!StringUtils.equalsIgnoreCase(datasetTableTask.getRate(), ScheduleType.SIMPLE.toString())){
            if (StringUtils.isNotEmpty(datasetTableTask.getCron())) {
                if (!CronExpression.isValidExpression(datasetTableTask.getCron())) {
                    throw new RuntimeException(Translator.get("i18n_cron_expression_error"));
                }
            }
            // check start time and end time
            if (StringUtils.equalsIgnoreCase(datasetTableTask.getEnd(), "1")
                    && ObjectUtils.isNotEmpty(datasetTableTask.getStartTime())
                    && ObjectUtils.isNotEmpty(datasetTableTask.getEndTime())
                    && datasetTableTask.getStartTime() != 0
                    && datasetTableTask.getEndTime() != 0
                    && datasetTableTask.getStartTime() > datasetTableTask.getEndTime()) {
                throw new RuntimeException(Translator.get("i18n_cron_time_error"));
            }
        }

        if (StringUtils.isEmpty(datasetTableTask.getId())) {
            datasetTableTask.setId(UUID.randomUUID().toString());
            datasetTableTask.setCreateTime(System.currentTimeMillis());
            datasetTableTask.setStatus(TaskStatus.Underway.name());
            datasetTableTaskMapper.insert(datasetTableTask);
        } else {
            datasetTableTaskMapper.updateByPrimaryKeySelective(datasetTableTask);
        }
        if (datasetTableTask.getRate().equalsIgnoreCase(ScheduleType.SIMPLE.toString()) && datasetTableTask.getStatus().equalsIgnoreCase(TaskStatus.Underway.name())) { // SIMPLE 类型，提前占位
            execNow(datasetTableTask);
            datasetTableTask.setLastExecStatus(JobStatus.Underway.name());
            datasetTableTask.setLastExecTime(System.currentTimeMillis());
            update(datasetTableTask);
        }
        if(!datasetTableTask.getRate().equalsIgnoreCase(ScheduleType.SIMPLE.name())){
            scheduleService.addSchedule(datasetTableTask);
        }else {
            if(datasetTableTask.getStatus().equalsIgnoreCase(JobStatus.Underway.name())){
                System.out.println(new Gson().toJson(datasetTableTask));
                scheduleService.addSchedule(datasetTableTask);
            }
        }

        return datasetTableTask;
    }

    private void execNow(DatasetTableTask datasetTableTask) {
        if (datasetTableTask.getType().equalsIgnoreCase("add_scope")) {
            DatasetTable datasetTable = dataSetTableService.get(datasetTableTask.getTableId());
            if (datasetTable.getLastUpdateTime() == null || datasetTable.getLastUpdateTime() == 0) {
                DataEaseException.throwException(Translator.get("i18n_not_exec_add_sync"));
            }
        }
        if (extractDataService.existSyncTask(dataSetTableService.get(datasetTableTask.getTableId()), null)) {
            DataEaseException.throwException(Translator.get("i18n_sync_job_exists"));
        }
        //write log
        DatasetTableTaskLog datasetTableTaskLog = new DatasetTableTaskLog();
        datasetTableTaskLog.setTableId(datasetTableTask.getTableId());
        datasetTableTaskLog.setTaskId(datasetTableTask.getId());
        datasetTableTaskLog.setStatus(JobStatus.Underway.name());
        datasetTableTaskLog.setStartTime(System.currentTimeMillis());
        datasetTableTaskLog.setTriggerType(TriggerType.Custom.name());
        dataSetTableTaskLogService.save(datasetTableTaskLog);
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
        datasetTableTaskMapper.updateByPrimaryKeySelective(datasetTableTask);
    }

    public void update(List<String> taskIds, DatasetTableTask datasetTableTask) {
        if (CollectionUtils.isEmpty(taskIds)){
            return;
        }
        DatasetTableTaskExample example = new DatasetTableTaskExample();
        example.createCriteria().andIdIn(taskIds);
        datasetTableTaskMapper.updateByExampleSelective(datasetTableTask, example);
    }

    public List<DatasetTableTask> list(DatasetTableTask datasetTableTask) {
        DatasetTableTaskExample datasetTableTaskExample = new DatasetTableTaskExample();
        DatasetTableTaskExample.Criteria criteria = datasetTableTaskExample.createCriteria();
        if (datasetTableTask != null && StringUtils.isNotEmpty(datasetTableTask.getTableId())) {
            criteria.andTableIdEqualTo(datasetTableTask.getTableId());
        }
        datasetTableTaskExample.setOrderByClause("create_time desc,name asc");
        return datasetTableTaskMapper.selectByExample(datasetTableTaskExample);
    }

    public List<DataSetTaskDTO> taskList(BaseGridRequest request) {
        GridExample gridExample = request.convertExample();
        List<DataSetTaskDTO> dataSetTaskDTOS = extDataSetTaskMapper.taskList(gridExample);
        return dataSetTaskDTOS;
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

    public void updateDatasetTableTaskStatus(DatasetTableTask datasetTableTask){
        DatasetTableTaskExample datasetTableTaskExample = new DatasetTableTaskExample();
        DatasetTableTaskExample.Criteria criteria = datasetTableTaskExample.createCriteria();
        criteria.andIdEqualTo(datasetTableTask.getId());
        DatasetTableTask record = new DatasetTableTask();
        record.setStatus(datasetTableTask.getStatus());
        datasetTableTaskMapper.updateByExampleSelective(record, datasetTableTaskExample);
    }

    public void execTask(DatasetTableTask datasetTableTask) throws Exception{
        execNow(datasetTableTask);
//        datasetTableTask.setStatus(TaskStatus.Underway.name());
        datasetTableTask.setLastExecStatus(JobStatus.Underway.name());
        datasetTableTask.setLastExecTime(System.currentTimeMillis());
        update(datasetTableTask);

        if(datasetTableTask.getRate().equalsIgnoreCase(ScheduleType.CRON.toString())){
            scheduleService.fireNow(datasetTableTask);
        }
        if(datasetTableTask.getRate().equalsIgnoreCase(ScheduleType.SIMPLE.toString())){
            scheduleService.addSchedule(datasetTableTask);
        }

    }
}

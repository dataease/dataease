package io.dataease.service.dataset;

import io.dataease.base.domain.*;
import io.dataease.base.mapper.DatasetTableMapper;
import io.dataease.base.mapper.DatasetTableTaskMapper;
import io.dataease.base.mapper.ext.ExtDataSetTaskMapper;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.commons.constants.JobStatus;
import io.dataease.commons.constants.ScheduleType;
import io.dataease.commons.constants.TaskStatus;
import io.dataease.commons.constants.TriggerType;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.controller.request.dataset.DataSetTaskRequest;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.base.ConditionEntity;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
    private ExtDataSetTaskMapper extDataSetTaskMapper;
    @Resource
    private DatasetTableMapper datasetTableMapper;

    public DatasetTableTask save(DataSetTaskRequest dataSetTaskRequest) throws Exception {
        checkName(dataSetTaskRequest);
        DatasetTableTask datasetTableTask = dataSetTaskRequest.getDatasetTableTask();
        if(datasetTableTask.getType().equalsIgnoreCase("add_scope")){
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
            if (StringUtils.equalsIgnoreCase(datasetTableTask.getRate(), ScheduleType.SIMPLE.toString())){
                datasetTableTask.setStatus(TaskStatus.Exec.name());
            }else {
                datasetTableTask.setStatus(TaskStatus.Underway.name());
            }
            datasetTableTaskMapper.insert(datasetTableTask);
        } else {
            datasetTableTask.setStatus(null);
            datasetTableTask.setLastExecTime(null);
            datasetTableTask.setLastExecStatus(null);
            datasetTableTaskMapper.updateByPrimaryKeySelective(datasetTableTask);
        }

        scheduleService.addSchedule(datasetTableTask);

        // simple
        if (datasetTableTask.getRate().equalsIgnoreCase(ScheduleType.SIMPLE.toString())) { // SIMPLE 类型，提前占位
            execNow(datasetTableTask);
        }else {
            checkTaskIsStopped(datasetTableTask);
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
        if (existSyncTask(dataSetTableService.get(datasetTableTask.getTableId()), datasetTableTask)) {
            DataEaseException.throwException(Translator.get("i18n_sync_job_exists"));
        }
    }

    private synchronized boolean existSyncTask(DatasetTable  datasetTable,  DatasetTableTask datasetTableTask) {
        datasetTable.setSyncStatus(JobStatus.Underway.name());
        DatasetTableExample example = new DatasetTableExample();
        example.createCriteria().andIdEqualTo(datasetTable.getId()).andSyncStatusNotEqualTo(JobStatus.Underway.name());
        example.or(example.createCriteria().andIdEqualTo(datasetTable.getId()).andSyncStatusIsNull());
        Boolean existSyncTask = datasetTableMapper.updateByExampleSelective(datasetTable, example) == 0;
        if(!existSyncTask){
            Long startTime = System.currentTimeMillis();
            datasetTableTask.setLastExecTime(startTime);
            datasetTableTask.setLastExecStatus(JobStatus.Underway.name());
            datasetTableTask.setStatus(TaskStatus.Exec.name());
            update(datasetTableTask);
            DatasetTableTaskLog datasetTableTaskLog = new DatasetTableTaskLog();
            datasetTableTaskLog.setTableId(datasetTableTask.getTableId());
            datasetTableTaskLog.setTaskId(datasetTableTask.getId());
            datasetTableTaskLog.setStatus(JobStatus.Underway.name());
            datasetTableTaskLog.setStartTime(startTime);
            datasetTableTaskLog.setTriggerType(TriggerType.Custom.name());
            dataSetTableTaskLogService.save(datasetTableTaskLog);
        }
        return existSyncTask;
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

    public void updateTaskStatus(List<String> taskIds, JobStatus lastExecStatus) {
        if (CollectionUtils.isEmpty(taskIds)){
            return;
        }
        DatasetTableTaskExample example = new DatasetTableTaskExample();
        example.createCriteria().andIdIn(taskIds);
        List<DatasetTableTask>  datasetTableTasks = datasetTableTaskMapper.selectByExample(example);
        for (DatasetTableTask tableTask : datasetTableTasks) {
            updateTaskStatus(tableTask, lastExecStatus);
        }
    }

    public void checkTaskIsStopped(DatasetTableTask datasetTableTask){
        if(StringUtils.isNotEmpty(datasetTableTask.getEnd()) && datasetTableTask.getEnd().equalsIgnoreCase("1")){
            BaseGridRequest request = new BaseGridRequest();
            ConditionEntity conditionEntity = new ConditionEntity();
            conditionEntity.setField("dataset_table_task.id");
            conditionEntity.setOperator("eq");
            conditionEntity.setValue(datasetTableTask.getId());
            request.setConditions(Arrays.asList(conditionEntity));
            List<DataSetTaskDTO> dataSetTaskDTOS = taskWithTriggers(request);
            if(CollectionUtils.isEmpty(dataSetTaskDTOS)){
                return;
            }
            if(dataSetTaskDTOS.get(0).getNextExecTime() == null || dataSetTaskDTOS.get(0).getNextExecTime() <= 0){
                datasetTableTask.setStatus(TaskStatus.Stopped.name());
                update(datasetTableTask);
            }
        }
    }
    public void updateTaskStatus(DatasetTableTask datasetTableTask, JobStatus lastExecStatus){
        datasetTableTask.setLastExecStatus(lastExecStatus.name());
        if(datasetTableTask.getRate().equalsIgnoreCase(ScheduleType.SIMPLE.name())){
            datasetTableTask.setStatus(TaskStatus.Stopped.name());
        }else {
            datasetTableTask = datasetTableTaskMapper.selectByPrimaryKey(datasetTableTask.getId());
            datasetTableTask.setLastExecStatus(lastExecStatus.name());
            if(StringUtils.isNotEmpty(datasetTableTask.getEnd()) && datasetTableTask.getEnd().equalsIgnoreCase("1")){
                BaseGridRequest request = new BaseGridRequest();
                ConditionEntity conditionEntity = new ConditionEntity();
                conditionEntity.setField("dataset_table_task.id");
                conditionEntity.setOperator("eq");
                conditionEntity.setValue(datasetTableTask.getId());
                request.setConditions(Arrays.asList(conditionEntity));
                List<DataSetTaskDTO> dataSetTaskDTOS = taskWithTriggers(request);
                if(CollectionUtils.isEmpty(dataSetTaskDTOS)){
                    return;
                }
                if(dataSetTaskDTOS.get(0).getNextExecTime() == null || dataSetTaskDTOS.get(0).getNextExecTime() <= 0){
                    datasetTableTask.setStatus(TaskStatus.Stopped.name());
                }else {
                    datasetTableTask.setStatus(TaskStatus.Underway.name());
                }
            }else {
                datasetTableTask.setStatus(TaskStatus.Underway.name());
            }
        }
        update(datasetTableTask);
    }

    public void update(DatasetTableTask datasetTableTask) {
        datasetTableTaskMapper.updateByPrimaryKeySelective(datasetTableTask);
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

    public List<DataSetTaskDTO> taskList4User(BaseGridRequest request) {
        List<ConditionEntity> conditionEntities = request.getConditions() == null ? new ArrayList<>() : new ArrayList(request.getConditions());;
        ConditionEntity entity = new ConditionEntity();
        entity.setField("1");
        entity.setOperator("eq");
        entity.setValue("1");
        conditionEntities.add(entity);
        request.setConditions(conditionEntities);
        GridExample gridExample = request.convertExample();
        gridExample.setExtendCondition(AuthUtils.getUser().getUserId().toString());
        if(AuthUtils.getUser().getIsAdmin()){
            List<DataSetTaskDTO> dataSetTaskDTOS = extDataSetTaskMapper.taskList(gridExample);
            return dataSetTaskDTOS;
        }else {
            List<DataSetTaskDTO> dataSetTaskDTOS = extDataSetTaskMapper.userTaskList(gridExample);
            return dataSetTaskDTOS;
        }
    }

    public List<DataSetTaskDTO> taskWithTriggers(BaseGridRequest request) {
        GridExample gridExample = request.convertExample();
        List<DataSetTaskDTO> dataSetTaskDTOS = extDataSetTaskMapper.taskWithTriggers(gridExample);
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

    public void updateDatasetTableTaskStatus(DatasetTableTask datasetTableTask)throws Exception{

        DatasetTableTask dbDatasetTableTask = datasetTableTaskMapper.selectByPrimaryKey(datasetTableTask.getId());
        if(dbDatasetTableTask.getStatus().equalsIgnoreCase(TaskStatus.Exec.name()) || dbDatasetTableTask.getStatus().equals(TaskStatus.Stopped.name())){
            throw new Exception(Translator.get("i18n_change_task_status_error") + Translator.get("i18n_" + dbDatasetTableTask.getStatus()));
        }

        DatasetTableTaskExample datasetTableTaskExample = new DatasetTableTaskExample();
        DatasetTableTaskExample.Criteria criteria = datasetTableTaskExample.createCriteria();
        criteria.andIdEqualTo(datasetTableTask.getId());
        DatasetTableTask record = new DatasetTableTask();
        record.setStatus(datasetTableTask.getStatus());
        datasetTableTaskMapper.updateByExampleSelective(record, datasetTableTaskExample);
    }

    public void execTask(DatasetTableTask datasetTableTask) throws Exception{
        execNow(datasetTableTask);
        if(!datasetTableTask.getRate().equalsIgnoreCase(ScheduleType.SIMPLE.toString())){
            scheduleService.fireNow(datasetTableTask);
        }
//        if(datasetTableTask.getRate().equalsIgnoreCase(ScheduleType.SIMPLE.toString())){
//            scheduleService.addSchedule(datasetTableTask);
//        }
    }
}

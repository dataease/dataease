package io.dataease.service.dataset;

import io.dataease.ext.ExtDataSetTaskMapper;
import io.dataease.ext.query.GridExample;
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
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.DatasetTableMapper;
import io.dataease.plugins.common.base.mapper.DatasetTableTaskMapper;
import io.dataease.service.ScheduleService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import java.util.*;

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
        if (datasetTableTask.getType().equalsIgnoreCase("add_scope")) {
            dataSetTableService.saveIncrementalConfig(dataSetTaskRequest.getDatasetTableIncrementalConfig());
        }
        // check
        if (!StringUtils.equalsIgnoreCase(datasetTableTask.getRate(), ScheduleType.SIMPLE.toString())) {
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
            if (StringUtils.equalsIgnoreCase(datasetTableTask.getRate(), ScheduleType.SIMPLE.toString())) {
                datasetTableTask.setStatus(TaskStatus.Exec.name());
            } else {
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
        } else {
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
        if (existSyncTask(datasetTableTask.getTableId(), datasetTableTask.getId())) {
            DataEaseException.throwException(Translator.get("i18n_sync_job_exists"));
        }
    }

    private synchronized boolean existSyncTask(String datasetTableId, String datasetTableTaskId) {
        DatasetTable record = new DatasetTable();
        record.setSyncStatus(JobStatus.Underway.name());
        DatasetTableExample example = new DatasetTableExample();
        example.createCriteria().andIdEqualTo(datasetTableId).andSyncStatusNotEqualTo(JobStatus.Underway.name());
        example.or(example.createCriteria().andIdEqualTo(datasetTableId).andSyncStatusIsNull());
        Boolean existSyncTask = datasetTableMapper.updateByExampleSelective(record, example) == 0;
        if (!existSyncTask) {
            Long startTime = System.currentTimeMillis();

            DatasetTableTask datasetTableTaskRecord = new DatasetTableTask();
            datasetTableTaskRecord.setLastExecTime(startTime);
            datasetTableTaskRecord.setLastExecStatus(JobStatus.Underway.name());
            datasetTableTaskRecord.setStatus(TaskStatus.Exec.name());
            DatasetTableTaskExample datasetTableTaskExample = new DatasetTableTaskExample();
            datasetTableTaskExample.createCriteria().andIdEqualTo(datasetTableTaskId);
            updateByExampleSelective(datasetTableTaskRecord, datasetTableTaskExample);

            DatasetTableTaskLog datasetTableTaskLog = new DatasetTableTaskLog();
            datasetTableTaskLog.setTableId(datasetTableId);
            datasetTableTaskLog.setTaskId(datasetTableTaskId);
            datasetTableTaskLog.setStatus(JobStatus.Underway.name());
            datasetTableTaskLog.setStartTime(startTime);
            datasetTableTaskLog.setTriggerType(TriggerType.Custom.name());
            dataSetTableTaskLogService.save(datasetTableTaskLog, true);
        }
        return existSyncTask;
    }

    public void delete(String id) {
        DatasetTableTask datasetTableTask = datasetTableTaskMapper.selectByPrimaryKey(id);
        datasetTableTaskMapper.deleteByPrimaryKey(id);
        scheduleService.deleteSchedule(datasetTableTask);
        dataSetTableTaskLogService.deleteByTaskId(id);
    }

    @Transactional
    public void batchDelete(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)){
            for (int i = 0; i < ids.size(); i++) {
                String id = ids.get(i);
                DatasetTableTask datasetTableTask = datasetTableTaskMapper.selectByPrimaryKey(id);
                datasetTableTaskMapper.deleteByPrimaryKey(id);
                scheduleService.deleteSchedule(datasetTableTask);
                dataSetTableTaskLogService.deleteByTaskId(id);
            }
        }
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


    public List<DatasetTableTask> list(DatasetTableTaskExample example) {
        return datasetTableTaskMapper.selectByExample(example);
    }


    public void updateTaskStatus(List<DatasetTableTask> datasetTableTasks, JobStatus lastExecStatus) {
        for (DatasetTableTask tableTask : datasetTableTasks) {
            updateTaskStatus(tableTask, lastExecStatus);
        }
    }

    public void checkTaskIsStopped(final DatasetTableTask datasetTableTask) {
        if (StringUtils.isNotEmpty(datasetTableTask.getEnd()) && datasetTableTask.getEnd().equalsIgnoreCase("1")) {
            BaseGridRequest request = new BaseGridRequest();
            ConditionEntity conditionEntity = new ConditionEntity();
            conditionEntity.setField("dataset_table_task.id");
            conditionEntity.setOperator("eq");
            conditionEntity.setValue(datasetTableTask.getId());
            request.setConditions(Collections.singletonList(conditionEntity));
            List<DataSetTaskDTO> dataSetTaskDTOS = taskWithTriggers(request);
            if (CollectionUtils.isEmpty(dataSetTaskDTOS)) {
                return;
            }
            if (dataSetTaskDTOS.get(0).getNextExecTime() == null || dataSetTaskDTOS.get(0).getNextExecTime() <= 0) {
                DatasetTableTask record = new DatasetTableTask();
                record.setStatus(TaskStatus.Stopped.name());
                DatasetTableTaskExample datasetTableTaskExample = new DatasetTableTaskExample();
                datasetTableTaskExample.createCriteria().andIdEqualTo(datasetTableTask.getId());
                updateByExampleSelective(record, datasetTableTaskExample);
                return;
            }
            if (dataSetTaskDTOS.get(0).getNextExecTime() > datasetTableTask.getEndTime()) {
                DatasetTableTask record = new DatasetTableTask();
                record.setStatus(TaskStatus.Stopped.name());
                DatasetTableTaskExample datasetTableTaskExample = new DatasetTableTaskExample();
                datasetTableTaskExample.createCriteria().andIdEqualTo(datasetTableTask.getId());
                updateByExampleSelective(record, datasetTableTaskExample);
            }
        }
    }

    public void updateTaskStatus(DatasetTableTask datasetTableTask, JobStatus lastExecStatus) {
        DatasetTableTask recore = new DatasetTableTask();
        recore.setLastExecStatus(lastExecStatus.name());
        if (datasetTableTask.getRate().equalsIgnoreCase(ScheduleType.SIMPLE.name())) {
            recore.setStatus(TaskStatus.Stopped.name());
        } else {
            recore.setLastExecStatus(lastExecStatus.name());
            if (StringUtils.isNotEmpty(datasetTableTask.getEnd()) && datasetTableTask.getEnd().equalsIgnoreCase("1")) {
                BaseGridRequest request = new BaseGridRequest();
                ConditionEntity conditionEntity = new ConditionEntity();
                conditionEntity.setField("dataset_table_task.id");
                conditionEntity.setOperator("eq");
                conditionEntity.setValue(datasetTableTask.getId());
                request.setConditions(Collections.singletonList(conditionEntity));
                List<DataSetTaskDTO> dataSetTaskDTOS = taskWithTriggers(request);
                if (CollectionUtils.isEmpty(dataSetTaskDTOS)) {
                    return;
                }
                if (dataSetTaskDTOS.get(0).getNextExecTime() == null || dataSetTaskDTOS.get(0).getNextExecTime() <= 0) {
                    recore.setStatus(TaskStatus.Stopped.name());
                } else {
                    recore.setStatus(TaskStatus.Underway.name());
                }
            } else {
                recore.setStatus(TaskStatus.Underway.name());
            }
        }
        DatasetTableTaskExample datasetTableTaskExample = new DatasetTableTaskExample();
        datasetTableTaskExample.createCriteria().andIdEqualTo(datasetTableTask.getId());
        updateByExampleSelective(recore, datasetTableTaskExample);
    }

    public DatasetTableTask selectByPrimaryKey(String id) {
        return datasetTableTaskMapper.selectByPrimaryKey(id);
    }

    public void updateByExampleSelective(DatasetTableTask datasetTableTask, DatasetTableTaskExample datasetTableTaskExample) {
        datasetTableTaskMapper.updateByExampleSelective(datasetTableTask, datasetTableTaskExample);
    }

    public List<DataSetTaskDTO> list(DatasetTableTask datasetTableTask) {
        BaseGridRequest request = new BaseGridRequest();
        List<ConditionEntity> conditionEntities = new ArrayList<>();
        if(datasetTableTask != null && StringUtils.isNotEmpty(datasetTableTask.getTableId())){
            ConditionEntity entity = new ConditionEntity();
            entity.setField("table_id");
            entity.setOperator("eq");
            entity.setValue(datasetTableTask.getTableId());
            conditionEntities.add(entity);
        }
        request.setConditions(conditionEntities);
        GridExample gridExample = request.convertExample();
        return extDataSetTaskMapper.taskList(gridExample);
    }

    public List<DataSetTaskDTO> taskList4User(BaseGridRequest request) {
        List<ConditionEntity> conditionEntities = request.getConditions() == null ? new ArrayList<>() : new ArrayList(request.getConditions());
        ConditionEntity entity = new ConditionEntity();
        entity.setField("1");
        entity.setOperator("eq");
        entity.setValue("1");
        conditionEntities.add(entity);
        request.setConditions(conditionEntities);
        GridExample gridExample = request.convertExample();
        gridExample.setExtendCondition(AuthUtils.getUser().getUserId().toString());
        if (AuthUtils.getUser().getIsAdmin()) {
            return extDataSetTaskMapper.taskList(gridExample);
        } else {
            return extDataSetTaskMapper.userTaskList(gridExample);
        }
    }

    public List<DataSetTaskDTO> taskWithTriggers(BaseGridRequest request) {
        GridExample gridExample = request.convertExample();
        return extDataSetTaskMapper.taskWithTriggers(gridExample);
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

    public void updateDatasetTableTaskStatus(DatasetTableTask datasetTableTask) throws Exception {

        DatasetTableTask dbDatasetTableTask = datasetTableTaskMapper.selectByPrimaryKey(datasetTableTask.getId());
        if (dbDatasetTableTask.getStatus().equalsIgnoreCase(TaskStatus.Exec.name()) || dbDatasetTableTask.getStatus().equals(TaskStatus.Stopped.name())) {
            throw new Exception(Translator.get("i18n_change_task_status_error") + Translator.get("i18n_" + dbDatasetTableTask.getStatus()));
        }

        DatasetTableTaskExample datasetTableTaskExample = new DatasetTableTaskExample();
        DatasetTableTaskExample.Criteria criteria = datasetTableTaskExample.createCriteria();
        criteria.andIdEqualTo(datasetTableTask.getId());
        DatasetTableTask record = new DatasetTableTask();
        record.setStatus(datasetTableTask.getStatus());
        datasetTableTaskMapper.updateByExampleSelective(record, datasetTableTaskExample);
    }

    public void execTask(DatasetTableTask datasetTableTask) throws Exception {
        execNow(datasetTableTask);
        if (!datasetTableTask.getRate().equalsIgnoreCase(ScheduleType.SIMPLE.toString())) {
            scheduleService.fireNow(datasetTableTask);
        }
    }

    public DataSetTaskDTO detail(String id) {
        BaseGridRequest request = new BaseGridRequest();
        List<ConditionEntity> conditionEntities = request.getConditions() == null ? new ArrayList<>() : new ArrayList(request.getConditions());
        ConditionEntity entity = new ConditionEntity();
        entity.setField("dataset_table_task.id");
        entity.setOperator("eq");
        entity.setValue(id);
        conditionEntities.add(entity);
        request.setConditions(conditionEntities);
        GridExample gridExample = request.convertExample();
        List<DataSetTaskDTO> dataSetTaskDTOS = extDataSetTaskMapper.taskList(gridExample);
        if (CollectionUtils.isNotEmpty(dataSetTaskDTOS)) {
            return dataSetTaskDTOS.get(0);
        }
        return null;
    }
}

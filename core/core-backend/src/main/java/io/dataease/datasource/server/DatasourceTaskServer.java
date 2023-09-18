package io.dataease.datasource.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.dataease.commons.constants.TaskStatus;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceTask;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceTaskLog;
import io.dataease.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.dataease.datasource.dao.auto.mapper.CoreDatasourceTaskLogMapper;
import io.dataease.datasource.dao.auto.mapper.CoreDatasourceTaskMapper;
import io.dataease.datasource.dto.CoreDatasourceTaskDTO;
import io.dataease.datasource.ext.ExtDatasourceTaskMapper;
import io.dataease.request.BaseGridRequest;
import io.dataease.request.ConditionEntity;
import io.dataease.request.GridExample;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

@Component
public class DatasourceTaskServer {

    @Resource
    private CoreDatasourceTaskMapper datasourceTaskMapper;
    @Resource
    private CoreDatasourceMapper coreDatasourceMapper;
    @Resource
    private ExtDatasourceTaskMapper extDatasourceTaskMapper;
    @Resource
    private CoreDatasourceTaskLogMapper coreDatasourceTaskLogMapper;


    public CoreDatasourceTask selectById(Long taskId) {
        return datasourceTaskMapper.selectById(taskId);
    }

    public List<CoreDatasourceTask> listAll() {
        return datasourceTaskMapper.selectList(null);
    }

    public CoreDatasourceTask selectByDSId(Long dsId) {
        QueryWrapper<CoreDatasourceTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ds_id", dsId);
        List<CoreDatasourceTask> coreDatasourceTasks = datasourceTaskMapper.selectList(queryWrapper);
        return CollectionUtils.isEmpty(coreDatasourceTasks) ? new CoreDatasourceTask() : coreDatasourceTasks.get(0);
    }

    public CoreDatasourceTaskLog lastSyncLog(Long dsId){
        QueryWrapper<CoreDatasourceTaskLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ds_id", dsId);
        queryWrapper.eq("status", "Completed");
        queryWrapper.orderByDesc("start_time");
        List<CoreDatasourceTaskLog> logs = coreDatasourceTaskLogMapper.selectList(queryWrapper);
        if(CollectionUtils.isEmpty(logs)){
            return null;
        }else {
            return logs.get(0);
        }
    }

    public void deleteByDSId(Long dsId) {
        QueryWrapper<CoreDatasourceTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ds_id", dsId);
        datasourceTaskMapper.delete(queryWrapper);
    }

    public void insert(CoreDatasourceTask coreDatasourceTask) {
        datasourceTaskMapper.insert(coreDatasourceTask);
    }

    public void update(CoreDatasourceTask coreDatasourceTask) {
        UpdateWrapper<CoreDatasourceTask> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", coreDatasourceTask.getId());
        datasourceTaskMapper.update(coreDatasourceTask, updateWrapper);
    }

    public void checkTaskIsStopped(CoreDatasourceTask coreDatasourceTask) {
        if (coreDatasourceTask.getEndLimit() != null && StringUtils.equalsIgnoreCase(coreDatasourceTask.getEndLimit(), "1")) {  // 结束限制 0 无限制 1 设定结束时间'
            BaseGridRequest request = new BaseGridRequest();
            ConditionEntity conditionEntity = new ConditionEntity();
            conditionEntity.setField("dataset_table_task.id");
            conditionEntity.setOperator("eq");
            conditionEntity.setValue(coreDatasourceTask.getId());
            request.setConditions(Collections.singletonList(conditionEntity));
            List<CoreDatasourceTaskDTO> dataSetTaskDTOS = taskWithTriggers(request);
            if (CollectionUtils.isEmpty(dataSetTaskDTOS)) {
                return;
            }

            UpdateWrapper<CoreDatasourceTask> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", coreDatasourceTask.getId());
            CoreDatasourceTask datasourceTask = new CoreDatasourceTask();
            if (dataSetTaskDTOS.get(0).getNextExecTime() == null || dataSetTaskDTOS.get(0).getNextExecTime() <= 0) {
                datasourceTask.setStatus(TaskStatus.Stopped.name());
            }
            if (dataSetTaskDTOS.get(0).getNextExecTime() > coreDatasourceTask.getEndTime()) {
                datasourceTask.setStatus(TaskStatus.Stopped.name());

            }
            datasourceTaskMapper.update(datasourceTask, updateWrapper);
        }
    }

    public List<CoreDatasourceTaskDTO> taskWithTriggers(BaseGridRequest request) {
        GridExample gridExample = request.convertExample();
        return extDatasourceTaskMapper.taskWithTriggers(gridExample);
    }

    public synchronized boolean existUnderExecutionTask(Long datasourceId, Long taskId, Long startTime) {
        UpdateWrapper<CoreDatasource> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", datasourceId);
        updateWrapper.ne("task_status", TaskStatus.UnderExecution.name());
        CoreDatasource coreDatasource = new CoreDatasource();
        coreDatasource.setTaskStatus(TaskStatus.UnderExecution.name());
        Boolean existSyncTask = coreDatasourceMapper.update(coreDatasource, updateWrapper) == 0;
        if (!existSyncTask) {
            UpdateWrapper<CoreDatasourceTask> updateTaskWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", taskId);
            CoreDatasourceTask record = new CoreDatasourceTask();
            record.setLastExecTime(startTime);
            record.setLastExecStatus(TaskStatus.UnderExecution.name());
            record.setStatus(TaskStatus.UnderExecution.name());
            datasourceTaskMapper.update(record, updateTaskWrapper);
        }
        return existSyncTask;
    }

    public CoreDatasourceTaskLog initTaskLog(Long datasourceId, Long taskId, Long startTime, String datasourceName) {
        CoreDatasourceTaskLog coreDatasourceTaskLog = new CoreDatasourceTaskLog();
        coreDatasourceTaskLog.setDsId(datasourceId);
        coreDatasourceTaskLog.setTaskId(taskId);
        coreDatasourceTaskLog.setStatus(TaskStatus.UnderExecution.name());
        coreDatasourceTaskLog.setTriggerType(TriggerType.Cron.name());
        coreDatasourceTaskLog.setStartTime(startTime);
        coreDatasourceTaskLog.setInfo("Begain to sync datasource: " + datasourceName);
        coreDatasourceTaskLogMapper.insert(coreDatasourceTaskLog);
        return coreDatasourceTaskLog;
    }

    public void saveLog(CoreDatasourceTaskLog coreDatasourceTaskLog) {
        coreDatasourceTaskLogMapper.updateById(coreDatasourceTaskLog);
    }

    public void updateTaskStatus(CoreDatasourceTask coreDatasourceTask, TaskStatus taskStatus) {
        CoreDatasourceTask record = new CoreDatasourceTask();
        record.setLastExecStatus(taskStatus.name());
        if (coreDatasourceTask.getSyncRate().equalsIgnoreCase(ScheduleType.RIGHTNOW.name())) {
            record.setStatus(TaskStatus.Stopped.name());
        } else {
            if (coreDatasourceTask.getEndLimit() != null && StringUtils.equalsIgnoreCase(coreDatasourceTask.getEndLimit(), "1")) {
                BaseGridRequest request = new BaseGridRequest();
                ConditionEntity conditionEntity = new ConditionEntity();
                conditionEntity.setField("core_datasource_task.id");
                conditionEntity.setOperator("eq");
                conditionEntity.setValue(coreDatasourceTask.getId());
                request.setConditions(Collections.singletonList(conditionEntity));
                List<CoreDatasourceTaskDTO> dataSetTaskDTOS = taskWithTriggers(request);
                if (CollectionUtils.isEmpty(dataSetTaskDTOS)) {
                    return;
                }
                if (dataSetTaskDTOS.get(0).getNextExecTime() == null || dataSetTaskDTOS.get(0).getNextExecTime() <= 0) {
                    record.setStatus(TaskStatus.Stopped.name());
                } else {
                    record.setStatus(TaskStatus.WaitingForExecution.name());
                }
            } else {
                record.setStatus(TaskStatus.WaitingForExecution.name());
            }
        }

        UpdateWrapper<CoreDatasourceTask> updateTaskWrapper = new UpdateWrapper<>();
        updateTaskWrapper.eq("id", coreDatasourceTask.getId());
        datasourceTaskMapper.update(record, updateTaskWrapper);
    }

    public enum TriggerType {
        Cron, Custom
    }

    public enum ScheduleType {
        CRON, RIGHTNOW, SIMPLE_CRON
    }
}

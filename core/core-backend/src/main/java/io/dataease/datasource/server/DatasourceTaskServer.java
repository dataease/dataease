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
import io.dataease.datasource.dao.ext.mapper.ExtDatasourceTaskMapper;
import io.dataease.datasource.manage.DatasourceSyncManage;
import io.dataease.utils.IDUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
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
    @Resource
    private DatasourceSyncManage datasourceSyncManage;


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

    public CoreDatasourceTaskLog lastSyncLogForTable(Long dsId, String tableName){
        List<CoreDatasourceTaskLog> coreDatasourceTaskLogs = new ArrayList<>();
        QueryWrapper<CoreDatasourceTaskLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ds_id", dsId);
        queryWrapper.eq("table_name", tableName);
        queryWrapper.orderByDesc("start_time");
        List<CoreDatasourceTaskLog> logs = coreDatasourceTaskLogMapper.selectList(queryWrapper);
        if(!CollectionUtils.isEmpty(logs)){
            return logs.get(0);
        }else {
            return null;
        }
    }

    public void deleteByDSId(Long dsId) {
        QueryWrapper<CoreDatasourceTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ds_id", dsId);
        List<CoreDatasourceTask> coreDatasourceTasks = datasourceTaskMapper.selectList(queryWrapper);
        if(!CollectionUtils.isEmpty(coreDatasourceTasks)){
            datasourceSyncManage.deleteSchedule(coreDatasourceTasks.get(0));
        }
        datasourceTaskMapper.delete(queryWrapper);
    }

    public void insert(CoreDatasourceTask coreDatasourceTask) {
        coreDatasourceTask.setId(IDUtils.snowID());
        datasourceTaskMapper.insert(coreDatasourceTask);
    }
    public void delete(Long id) {
        datasourceTaskMapper.deleteById(id);
    }

    public void update(CoreDatasourceTask coreDatasourceTask) {
        if(coreDatasourceTask.getId() == null){
            datasourceTaskMapper.insert(coreDatasourceTask);
        }else {
            UpdateWrapper<CoreDatasourceTask> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", coreDatasourceTask.getId());
            datasourceTaskMapper.updateById(coreDatasourceTask);
        }

    }

    public void updateByDsIds(List<Long> dsIds){
        UpdateWrapper<CoreDatasourceTask> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("ds_id", dsIds);
        CoreDatasourceTask record = new CoreDatasourceTask();
        record.setTaskStatus(TaskStatus.WaitingForExecution.name());
        datasourceTaskMapper.update(record, updateWrapper);
    }
    public void checkTaskIsStopped(CoreDatasourceTask coreDatasourceTask) {
        if (coreDatasourceTask.getEndLimit() != null && StringUtils.equalsIgnoreCase(coreDatasourceTask.getEndLimit(), "1")) {  // 结束限制 0 无限制 1 设定结束时间'
            List<CoreDatasourceTaskDTO> dataSetTaskDTOS = taskWithTriggers(coreDatasourceTask.getId());
            if (CollectionUtils.isEmpty(dataSetTaskDTOS)) {
                return;
            }
            UpdateWrapper<CoreDatasourceTask> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", coreDatasourceTask.getId());
            CoreDatasourceTask datasourceTask = new CoreDatasourceTask();
            if (dataSetTaskDTOS.get(0).getNextExecTime() == null || dataSetTaskDTOS.get(0).getNextExecTime() <= 0) {
                datasourceTask.setTaskStatus(TaskStatus.Stopped.name());
                datasourceTaskMapper.update(datasourceTask, updateWrapper);
            }
            if (dataSetTaskDTOS.get(0).getNextExecTime() != null && dataSetTaskDTOS.get(0).getNextExecTime() > coreDatasourceTask.getEndTime()) {
                datasourceTask.setTaskStatus(TaskStatus.Stopped.name());
                datasourceTaskMapper.update(datasourceTask, updateWrapper);
            }
        }
    }

    public List<CoreDatasourceTaskDTO> taskWithTriggers(Long taskId) {
        QueryWrapper<CoreDatasourceTaskDTO> wrapper = new QueryWrapper<>();
        wrapper.eq("core_datasource_task.id", taskId);
        return extDatasourceTaskMapper.taskWithTriggers(wrapper);
    }

    public synchronized boolean existUnderExecutionTask(Long datasourceId, Long taskId) {
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
            record.setTaskStatus(TaskStatus.UnderExecution.name());
            record.setLastExecTime(System.currentTimeMillis());
            datasourceTaskMapper.update(record, updateTaskWrapper);
        }
        return existSyncTask;
    }

    public CoreDatasourceTaskLog initTaskLog(Long datasourceId, Long taskId, String tableName, String triggerType) {
        Long startTime = System.currentTimeMillis();
        CoreDatasourceTaskLog coreDatasourceTaskLog = new CoreDatasourceTaskLog();
        coreDatasourceTaskLog.setId(IDUtils.snowID());
        coreDatasourceTaskLog.setDsId(datasourceId);
        coreDatasourceTaskLog.setTaskId(taskId);
        coreDatasourceTaskLog.setTaskStatus(TaskStatus.UnderExecution.name());
        coreDatasourceTaskLog.setTriggerType(triggerType);
        coreDatasourceTaskLog.setStartTime(startTime);
        coreDatasourceTaskLog.setCreateTime(startTime);
        coreDatasourceTaskLog.setTableName(tableName);
        coreDatasourceTaskLog.setInfo("");
        coreDatasourceTaskLogMapper.insert(coreDatasourceTaskLog);
        return coreDatasourceTaskLog;
    }

    public void saveLog(CoreDatasourceTaskLog coreDatasourceTaskLog) {
        coreDatasourceTaskLogMapper.updateById(coreDatasourceTaskLog);
    }

    public void updateTaskStatus(CoreDatasourceTask coreDatasourceTask) {
        CoreDatasourceTask record = new CoreDatasourceTask();
        if (coreDatasourceTask.getSyncRate().equalsIgnoreCase(ScheduleType.RIGHTNOW.name())) {
            record.setTaskStatus(TaskStatus.Stopped.name());
        } else {
            if (coreDatasourceTask.getEndLimit() != null && StringUtils.equalsIgnoreCase(coreDatasourceTask.getEndLimit(), "1")) {
                List<CoreDatasourceTaskDTO> dataSetTaskDTOS = taskWithTriggers(coreDatasourceTask.getId());
                if (CollectionUtils.isEmpty(dataSetTaskDTOS)) {
                    return;
                }
                if (dataSetTaskDTOS.get(0).getNextExecTime() == null || dataSetTaskDTOS.get(0).getNextExecTime() <= 0) {
                    record.setTaskStatus(TaskStatus.Stopped.name());
                } else {
                    record.setTaskStatus(TaskStatus.WaitingForExecution.name());
                }
            } else {
                record.setTaskStatus(TaskStatus.WaitingForExecution.name());
            }
        }

        UpdateWrapper<CoreDatasourceTask> updateTaskWrapper = new UpdateWrapper<>();
        updateTaskWrapper.eq("id", coreDatasourceTask.getId());
        datasourceTaskMapper.update(record, updateTaskWrapper);
    }


    public enum ScheduleType {
        CRON, RIGHTNOW, SIMPLE_CRON, MANUAL
    }
}

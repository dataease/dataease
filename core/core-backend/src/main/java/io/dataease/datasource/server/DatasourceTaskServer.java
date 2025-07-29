package io.dataease.datasource.server;

import io.dataease.commons.constants.TaskStatus;
import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceTask;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceTaskLog;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceRepository;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceTaskLogRepository;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceTaskRepository;
import io.dataease.datasource.dao.auto.repository.QrtzTriggersRepository;
import io.dataease.datasource.dto.CoreDatasourceTaskDTO;
import io.dataease.datasource.manage.DatasourceSyncManage;
import io.dataease.qrtz.dao.auto.repo.entity.QrtzTriggers;
import io.dataease.utils.IDUtils;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class DatasourceTaskServer {

    @Resource
    private CoreDatasourceTaskRepository coreDatasourceTaskRepository;
    @Autowired
    private CoreDatasourceRepository coreDatasourceRepository;
    @Resource
    private QrtzTriggersRepository qrtzTriggersRepository;
    @Resource
    private CoreDatasourceTaskLogRepository coreDatasourceTaskLogRepository;
    @Resource
    private DatasourceSyncManage datasourceSyncManage;


    public CoreDatasourceTask selectById(Long taskId) {
        return coreDatasourceTaskRepository.findById(taskId).orElse(null);
    }

    public List<CoreDatasourceTask> listAll() {
        return coreDatasourceTaskRepository.findAll();
    }

    public CoreDatasourceTask selectByDSId(Long dsId) {
        List<CoreDatasourceTask> coreDatasourceTasks = coreDatasourceTaskRepository.findByDsId(dsId);
        return CollectionUtils.isEmpty(coreDatasourceTasks) ? new CoreDatasourceTask() : coreDatasourceTasks.get(0);
    }

    public CoreDatasourceTaskLog lastSyncLogForTable(Long dsId, String tableName) {
        Specification<CoreDatasourceTaskLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("dsId"), dsId));
            if (tableName != null) {
                predicates.add(cb.equal(root.get("tableName"), tableName));
            }
            query.orderBy(cb.desc(root.get("startTime")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<CoreDatasourceTaskLog> logs = coreDatasourceTaskLogRepository.findAll(spec, PageRequest.of(0, 1)).getContent();
        if (!CollectionUtils.isEmpty(logs)) {
            return logs.get(0);
        } else {
            return null;
        }
    }

    public void deleteByDSId(Long dsId) {
        List<CoreDatasourceTask> coreDatasourceTasks = coreDatasourceTaskRepository.findByDsId(dsId);
        if (!CollectionUtils.isEmpty(coreDatasourceTasks)) {
            datasourceSyncManage.deleteSchedule(coreDatasourceTasks.get(0));
        }
        coreDatasourceTaskRepository.deleteByDsId(dsId);
    }

    public void insert(CoreDatasourceTask coreDatasourceTask) {
        coreDatasourceTask.setId(IDUtils.snowID());
        coreDatasourceTaskRepository.saveAndFlush(coreDatasourceTask);
    }

    public void delete(Long id) {
        coreDatasourceTaskRepository.deleteById(id);
    }

    public void update(CoreDatasourceTask coreDatasourceTask) {
        if (coreDatasourceTask.getId() == null) {
            coreDatasourceTask.setId(IDUtils.snowID());
            coreDatasourceTaskRepository.saveAndFlush(coreDatasourceTask);
        } else {
            coreDatasourceTaskRepository.saveAndFlush(coreDatasourceTask);
        }

    }

    public void updateByDsIds(List<Long> dsIds) {
        coreDatasourceTaskRepository.updateTaskStatusByDsIds(dsIds, TaskStatus.WaitingForExecution.name());
    }

    public void checkTaskIsStopped(CoreDatasourceTask coreDatasourceTask) {
        if (coreDatasourceTask.getEndTime() != null && coreDatasourceTask.getEndTime() > 0) {
            List<CoreDatasourceTaskDTO> dataSetTaskDTOS = taskWithTriggers(coreDatasourceTask.getId());
            if (CollectionUtils.isEmpty(dataSetTaskDTOS)) {
                return;
            }
            if (dataSetTaskDTOS.get(0).getNextExecTime() == null || dataSetTaskDTOS.get(0).getNextExecTime() <= 0) {
                coreDatasourceTaskRepository.updateTaskStatus(coreDatasourceTask.getId(), TaskStatus.Stopped.name());
            }
            if (dataSetTaskDTOS.get(0).getNextExecTime() != null && dataSetTaskDTOS.get(0).getNextExecTime() > coreDatasourceTask.getEndTime()) {
                coreDatasourceTaskRepository.updateTaskStatus(coreDatasourceTask.getId(), TaskStatus.Stopped.name());
            }
        }
    }

    public List<CoreDatasourceTaskDTO> taskWithTriggers(Long taskId) {
        Specification<QrtzTriggers> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("triggerName"), String.valueOf(taskId)));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return qrtzTriggersRepository.findAll(spec).stream().map(qrtzTriggers -> {
            CoreDatasourceTaskDTO coreDatasourceTaskDTO = new CoreDatasourceTaskDTO();
            coreDatasourceTaskDTO.setId(taskId);
            coreDatasourceTaskDTO.setNextExecTime(qrtzTriggers.getNextFireTime());
            return coreDatasourceTaskDTO;
        }).toList();
    }

    public synchronized boolean existUnderExecutionTask(Long datasourceId, Long taskId) {
        CoreDatasource coreDatasource = new CoreDatasource();
        coreDatasource.setId(datasourceId);
        coreDatasource.setTaskStatus(TaskStatus.UnderExecution.name());
        Example<CoreDatasource> example = Example.of(coreDatasource);

        boolean existSyncTask = coreDatasourceRepository.exists(example);
        if (!existSyncTask) {
            coreDatasourceTaskRepository.updateTaskStatusAndLastExecTime(taskId, TaskStatus.UnderExecution.name(), System.currentTimeMillis());
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
        coreDatasourceTaskLogRepository.saveAndFlush(coreDatasourceTaskLog);
        return coreDatasourceTaskLog;
    }

    public void saveLog(CoreDatasourceTaskLog coreDatasourceTaskLog) {
        coreDatasourceTaskLogRepository.saveAndFlush(coreDatasourceTaskLog);
    }

    public void updateTaskStatus(CoreDatasourceTask coreDatasourceTask) {
        CoreDatasourceTask record = new CoreDatasourceTask();
        if (coreDatasourceTask.getSyncRate().equalsIgnoreCase(ScheduleType.RIGHTNOW.name())) {
            record.setTaskStatus(TaskStatus.Stopped.name());
        } else {
            if (coreDatasourceTask.getEndTime() != null && coreDatasourceTask.getEndTime() > 0) {
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
        coreDatasourceTaskRepository.updateTaskStatus(coreDatasourceTask.getId(), record.getTaskStatus());
    }

    public void cleanLog() {
        long expTime = Long.parseLong("30") * 24L * 3600L * 1000L;
        long threshold = System.currentTimeMillis() - expTime;
        coreDatasourceTaskLogRepository.deleteByStartTimeLessThan(threshold);
    }


    public enum ScheduleType {
        CRON, RIGHTNOW, SIMPLE_CRON, MANUAL
    }
}

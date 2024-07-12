package io.dataease.service.datafill;

import io.dataease.auth.service.AuthUserService;
import io.dataease.commons.model.AuthURD;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.controller.request.datafill.DataFillTaskSearchRequest;
import io.dataease.controller.request.datafill.DataFillUserTaskSearchRequest;
import io.dataease.dto.datafill.DataFillTaskDTO;
import io.dataease.dto.datafill.DataFillUserTaskDTO;
import io.dataease.ext.ExtDataFillFormMapper;
import io.dataease.i18n.Translator;
import io.dataease.job.sechedule.ScheduleManager;
import io.dataease.job.sechedule.strategy.TaskHandler;
import io.dataease.job.sechedule.strategy.TaskStrategyFactory;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.DataFillTaskMapper;
import io.dataease.plugins.common.base.mapper.DataFillUserTaskMapper;
import io.dataease.plugins.common.base.mapper.SysUserMapper;
import io.dataease.plugins.common.entity.GlobalTaskEntity;
import io.dataease.plugins.common.exception.DataEaseException;
import org.apache.commons.lang3.StringUtils;
import org.pentaho.di.core.util.UUIDUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DataFillTaskService {

    @Resource
    private ExtDataFillFormMapper extDataFillFormMapper;

    @Resource
    private DataFillTaskMapper dataFillTaskMapper;
    @Resource
    private DataFillUserTaskMapper dataFillUserTaskMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private ScheduleManager scheduleManager;


    public List<DataFillTaskDTO> tasks(String formId, DataFillTaskSearchRequest request) {
        request.setFormId(formId);
        return extDataFillFormMapper.selectDataFillTasks(request);
    }

    private List<DataFillTask> listActiveTasks() {
        return extDataFillFormMapper.selectActiveDataFillTasks();
    }

    public List<GlobalTaskEntity> listActiveTasksGlobalTaskEntity() {
        List<GlobalTaskEntity> list = new ArrayList<>();
        List<DataFillTask> tasks = dataFillTaskMapper.selectByExample(null);
        for (DataFillTask task : tasks) {
            list.add(getGlobalTaskEntity(task));
        }
        return list;
    }

    private GlobalTaskEntity getGlobalTaskEntity(DataFillTask task) {
        GlobalTaskEntity entity = new GlobalTaskEntity()
                .setTaskId(task.getId())
                .setTaskName(task.getName())
                .setRateType(task.getRateType())
                .setRateVal(task.getRateVal())
                .setStatus(task.getStatus())
                .setTaskType("dataFillTaskHandler")
                .setCreator(task.getCreator());
        if (task.getRateType() == -1) {
            entity.setStartTime(task.getPublishStartTime() == null ? null : task.getPublishStartTime().getTime());
            entity.setEndTime(task.getPublishEndTime() == null ? null : task.getPublishEndTime().getTime());
        } else {
            entity.setStartTime(task.getStartTime() == null ? null : task.getStartTime().getTime());
            entity.setEndTime(task.getEndTime() == null ? null : task.getEndTime().getTime());
        }
        return entity;
    }

    public void saveTask(String formId, DataFillTaskSearchRequest request) throws Exception {
        if (!CommonBeanFactory.getBean(AuthUserService.class).pluginLoaded()) {
            DataEaseException.throwException("invalid");
        }

        request.setCreator(AuthUtils.getUser().getUserId());
        boolean insert = false;
        if (request.getId() == null) {
            insert = true;
            request.setFormId(formId);
            request.setCreateTime(new Date());
        }

        if (StringUtils.isNotBlank(request.getName())) {
            DataFillTaskExample example = new DataFillTaskExample();
            DataFillTaskExample.Criteria criteria = example.createCriteria()
                    .andFormIdEqualTo(formId)
                    .andNameEqualTo(request.getName());

            if (insert) {
                if (dataFillTaskMapper.countByExample(example) > 0) {
                    DataEaseException.throwException(Translator.get("I18N_DATA_FILL_TASK_EXIST"));
                }
                dataFillTaskMapper.insertSelective(request);
            } else {
                criteria.andIdNotEqualTo(request.getId());
                if (dataFillTaskMapper.countByExample(example) > 0) {
                    DataEaseException.throwException(Translator.get("I18N_DATA_FILL_TASK_EXIST"));
                }
                dataFillTaskMapper.updateByPrimaryKeySelective(request);
            }
        } else {
            if (insert) {
                dataFillTaskMapper.insertSelective(request);
            } else {
                dataFillTaskMapper.updateByPrimaryKeySelective(request);
            }
        }

        DataFillTaskWithBLOBs task = dataFillTaskMapper.selectByPrimaryKey(request.getId());


        GlobalTaskEntity entity = getGlobalTaskEntity(task);
        TaskHandler taskHandler = TaskStrategyFactory.getInvokeStrategy(entity.getTaskType());
        taskHandler.addTask(scheduleManager, entity);

    }

    public DataFillTaskWithBLOBs getTaskById(Long id) {
        return dataFillTaskMapper.selectByPrimaryKey(id);
    }

    public void finishTask(Long id) {
        DataFillTaskWithBLOBs task = new DataFillTaskWithBLOBs();
        task.setId(id);
        task.setStatus(false);
        dataFillTaskMapper.updateByPrimaryKeySelective(task);
    }

    public void deleteTaskByFormId(String formId) {
        DataFillTaskExample example = new DataFillTaskExample();
        example.createCriteria().andFormIdEqualTo(formId);
        for (DataFillTask dataFillTask : dataFillTaskMapper.selectByExample(example)) {
            deleteTask(dataFillTask.getId());
        }
    }

    public void deleteTask(Long id) {

        try {
            GlobalTaskEntity entity = getGlobalTaskEntity(dataFillTaskMapper.selectByPrimaryKey(id));
            TaskHandler taskHandler = TaskStrategyFactory.getInvokeStrategy(entity.getTaskType());
            taskHandler.removeTask(scheduleManager, entity);
        } catch (Exception e) {
            e.printStackTrace();
        }

        dataFillTaskMapper.deleteByPrimaryKey(id);

        DataFillUserTaskExample example = new DataFillUserTaskExample();
        example.createCriteria().andTaskIdEqualTo(id);
        dataFillUserTaskMapper.deleteByExample(example);

    }

    public void enableTask(Long taskId) throws Exception {
        DataFillTask task = dataFillTaskMapper.selectByPrimaryKey(taskId);
        DataFillTaskSearchRequest request = new DataFillTaskSearchRequest();
        request.setId(taskId);
        request.setStatus(true);
        saveTask(task.getFormId(), request);
    }

    public void disableTask(Long taskId) throws Exception {
        DataFillTask task = dataFillTaskMapper.selectByPrimaryKey(taskId);
        DataFillTaskSearchRequest request = new DataFillTaskSearchRequest();
        request.setId(taskId);
        request.setStatus(false);
        saveTask(task.getFormId(), request);
    }

    public void createUserTasks(DataFillTaskWithBLOBs dataFillTask, String valueId) throws Exception {
        String reciUsers = dataFillTask.getReciUsers();
        List<String> accountSet = new ArrayList<>();
        if (StringUtils.isNotBlank(reciUsers)) {
            accountSet.addAll(Arrays.stream(reciUsers.split(",")).collect(Collectors.toSet()));
        }
        List<SysUser> sysUsers = new ArrayList<>();
        if (!accountSet.isEmpty()) {
            SysUserExample example = new SysUserExample();
            example.createCriteria().andUsernameIn(accountSet);
            sysUsers = sysUserMapper.selectByExample(example);
        }


        String roleList = dataFillTask.getRoleList();
        String orgList = dataFillTask.getOrgList();
        AuthURD authURD = new AuthURD();
        if (StringUtils.isNotBlank(roleList)) {
            authURD.setRoleIds(Arrays.stream(roleList.split(",")).map(Long::parseLong).collect(Collectors.toList()));
        }
        if (StringUtils.isNotBlank(orgList)) {
            authURD.setDeptIds(Arrays.stream(orgList.split(",")).map(Long::parseLong).collect(Collectors.toList()));
        }
        Set<Long> ids = AuthUtils.userIdsByURD(authURD);

        for (SysUser sysUser : sysUsers) {
            ids.add(sysUser.getUserId());
        }

        Date startTime = null;
        Date endTime = null;

        if (dataFillTask.getRateType() == -1) {
            //startTime = dataFillTask.getPublishStartTime();
            startTime = new Date();
            endTime = dataFillTask.getPublishEndTime();
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date val = sdf.parse(dataFillTask.getRateVal());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(val);

            Calendar current = Calendar.getInstance();
            calendar.set(Calendar.YEAR, current.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, current.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));

            startTime = calendar.getTime();

            if (dataFillTask.getPublishRangeTimeType() == -1) {
                calendar.add(Calendar.HOUR_OF_DAY, dataFillTask.getPublishRangeTime());
            } else if (dataFillTask.getPublishRangeTimeType() == 0) {
                calendar.add(Calendar.DAY_OF_MONTH, dataFillTask.getPublishRangeTime());
            } else if (dataFillTask.getPublishRangeTimeType() == 1) {
                calendar.add(Calendar.WEEK_OF_YEAR, dataFillTask.getPublishRangeTime());
            } else if (dataFillTask.getPublishRangeTimeType() == 2) {
                calendar.add(Calendar.MONTH, dataFillTask.getPublishRangeTime());
            }
            endTime = calendar.getTime();
        }

        for (Long id : ids) {
            DataFillUserTask task = new DataFillUserTask();
            task.setId(UUIDUtil.getUUIDAsString());
            task.setTaskId(dataFillTask.getId());
            task.setFormId(dataFillTask.getFormId());
            task.setUser(id);
            task.setStartTime(startTime);
            task.setEndTime(endTime);
            task.setValueId(valueId);

            dataFillUserTaskMapper.insertSelective(task);
        }

    }


    public List<DataFillUserTaskDTO> userTasks(Long userId, String type, DataFillUserTaskSearchRequest request) {
        List<DataFillUserTaskDTO> list = new ArrayList<>();

        String taskName = StringUtils.isNotBlank(request.getTaskName()) ? request.getTaskName() : null;

        if (StringUtils.equalsIgnoreCase(type, "finished")) {
            list = extDataFillFormMapper.listFinishedUserTask(userId, new Date(), taskName);
        } else if (StringUtils.equalsIgnoreCase(type, "expired")) {
            list = extDataFillFormMapper.listExpiredUserTask(userId, new Date(), taskName);
        } else {
            list = extDataFillFormMapper.listTodoUserTask(userId, new Date(), taskName);
        }

        return list;

    }
}

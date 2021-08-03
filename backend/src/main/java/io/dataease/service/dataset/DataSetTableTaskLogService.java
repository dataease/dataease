package io.dataease.service.dataset;

import io.dataease.base.domain.DatasetTableTaskLog;
import io.dataease.base.domain.DatasetTableTaskLogExample;
import io.dataease.base.mapper.DatasetTableTaskLogMapper;
import io.dataease.base.mapper.ext.ExtDataSetTaskMapper;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.base.ConditionEntity;
import io.dataease.dto.dataset.DataSetTaskDTO;
import io.dataease.dto.dataset.DataSetTaskLogDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Author gin
 * @Date 2021/3/4 1:26 下午
 */
@Service
public class DataSetTableTaskLogService {
    @Resource
    private DatasetTableTaskLogMapper datasetTableTaskLogMapper;
    @Resource
    private ExtDataSetTaskMapper extDataSetTaskMapper;

    public DatasetTableTaskLog save(DatasetTableTaskLog datasetTableTaskLog) {
        if (StringUtils.isEmpty(datasetTableTaskLog.getId())) {
            datasetTableTaskLog.setId(UUID.randomUUID().toString());
            datasetTableTaskLog.setCreateTime(System.currentTimeMillis());
            datasetTableTaskLogMapper.insert(datasetTableTaskLog);
        } else {
            datasetTableTaskLogMapper.updateByPrimaryKeySelective(datasetTableTaskLog);
        }
        return datasetTableTaskLog;
    }

    public void delete(String id) {
        datasetTableTaskLogMapper.deleteByPrimaryKey(id);
    }

    public List<DataSetTaskLogDTO> listTaskLog(BaseGridRequest request, String type) {
        List<ConditionEntity> conditionEntities = request.getConditions();
        if(!type.equalsIgnoreCase("excel")){
            ConditionEntity entity = new ConditionEntity();
            entity.setField("task_id");
            entity.setOperator("not in");
            List<String>status = new ArrayList<>();status.add("初始导入");status.add("替换");status.add("追加");
            entity.setValue(status);
            if(CollectionUtils.isEmpty(conditionEntities)){
                conditionEntities = new ArrayList<>();
            }
            conditionEntities.add(entity);
        }

        ConditionEntity entity2 = new ConditionEntity();
        entity2.setField("1");
        entity2.setOperator("eq");
        entity2.setValue("1");
        conditionEntities.add(entity2);
        request.setConditions(conditionEntities);

        GridExample gridExample = request.convertExample();
        gridExample.setExtendCondition(AuthUtils.getUser().getUserId().toString());

        if(AuthUtils.getUser().getIsAdmin()){
            List<DataSetTaskLogDTO> dataSetTaskLogDTOS = extDataSetTaskMapper.listTaskLog(gridExample);
            dataSetTaskLogDTOS.forEach(dataSetTaskLogDTO -> {
                if(StringUtils.isEmpty(dataSetTaskLogDTO.getName())){
                    dataSetTaskLogDTO.setName(dataSetTaskLogDTO.getTaskId());
                }
            });
            return dataSetTaskLogDTOS;
        }else {
            List<DataSetTaskLogDTO> dataSetTaskLogDTOS = extDataSetTaskMapper.listUserTaskLog(gridExample);
            dataSetTaskLogDTOS.forEach(dataSetTaskLogDTO -> {
                if(StringUtils.isEmpty(dataSetTaskLogDTO.getName())){
                    dataSetTaskLogDTO.setName(dataSetTaskLogDTO.getTaskId());
                }
            });
            return dataSetTaskLogDTOS;
        }

    }

    public void deleteByTaskId(String taskId){
        DatasetTableTaskLogExample datasetTableTaskLogExample = new DatasetTableTaskLogExample();
        DatasetTableTaskLogExample.Criteria criteria = datasetTableTaskLogExample.createCriteria();
        criteria.andTaskIdEqualTo(taskId);
        datasetTableTaskLogMapper.deleteByExample(datasetTableTaskLogExample);
    }

    public List<DatasetTableTaskLog> getByTableId(String datasetId){
        DatasetTableTaskLogExample datasetTableTaskLogExample = new DatasetTableTaskLogExample();
        DatasetTableTaskLogExample.Criteria criteria = datasetTableTaskLogExample.createCriteria();
        criteria.andTableIdEqualTo(datasetId);
        return datasetTableTaskLogMapper.selectByExampleWithBLOBs(datasetTableTaskLogExample);
    }

    public List<DatasetTableTaskLog> select(DatasetTableTaskLog datasetTableTaskLog){
        DatasetTableTaskLogExample example = getDatasetTableTaskLogExample(datasetTableTaskLog);
        example.setOrderByClause("create_time desc");
        return datasetTableTaskLogMapper.selectByExampleWithBLOBs(example);
    }

    public DataSetTaskDTO lastExecStatus(DataSetTaskDTO dataSetTaskDTO){
        DatasetTableTaskLogExample example = new DatasetTableTaskLogExample();
        DatasetTableTaskLogExample.Criteria criteria = example.createCriteria();
        if(StringUtils.isNotEmpty(dataSetTaskDTO.getTableId())){
            criteria.andTableIdEqualTo(dataSetTaskDTO.getTableId());
        }
        if(StringUtils.isNotEmpty(dataSetTaskDTO.getId())){
            criteria.andTaskIdEqualTo(dataSetTaskDTO.getId());
        }
        example.setOrderByClause("create_time desc");
        List<DatasetTableTaskLog>  datasetTableTaskLogs = datasetTableTaskLogMapper.selectByExampleWithBLOBs(example);
        if(CollectionUtils.isNotEmpty(datasetTableTaskLogs)){
            dataSetTaskDTO.setLastExecStatus(datasetTableTaskLogs.get(0).getStatus());
            dataSetTaskDTO.setLastExecTime(datasetTableTaskLogs.get(0).getCreateTime());
            dataSetTaskDTO.setMsg(datasetTableTaskLogs.get(0).getInfo());
        }
        return dataSetTaskDTO;
    }

    private DatasetTableTaskLogExample getDatasetTableTaskLogExample(DatasetTableTaskLog datasetTableTaskLog) {
        DatasetTableTaskLogExample example = new DatasetTableTaskLogExample();
        DatasetTableTaskLogExample.Criteria criteria = example.createCriteria();
        if(StringUtils.isNotEmpty(datasetTableTaskLog.getStatus())){
            criteria.andStatusEqualTo(datasetTableTaskLog.getStatus());
        }
        if(StringUtils.isNotEmpty(datasetTableTaskLog.getTableId())){
            criteria.andTableIdEqualTo(datasetTableTaskLog.getTableId());
        }
        if(StringUtils.isNotEmpty(datasetTableTaskLog.getTaskId())){
            criteria.andTaskIdEqualTo(datasetTableTaskLog.getTaskId());
        }
        return example;
    }


}

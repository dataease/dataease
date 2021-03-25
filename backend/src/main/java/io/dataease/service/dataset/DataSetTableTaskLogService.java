package io.dataease.service.dataset;

import io.dataease.base.domain.DatasetTableTaskLog;
import io.dataease.base.domain.DatasetTableTaskLogExample;
import io.dataease.base.mapper.DatasetTableTaskLogMapper;
import io.dataease.base.mapper.ext.ExtDataSetTaskMapper;
import io.dataease.dto.dataset.DataSetTaskLogDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    public List<DataSetTaskLogDTO> list(DatasetTableTaskLog request) {
        DatasetTableTaskLogExample datasetTableTaskLogExample = new DatasetTableTaskLogExample();
        DatasetTableTaskLogExample.Criteria criteria = datasetTableTaskLogExample.createCriteria();
        if (StringUtils.isNotEmpty(request.getTableId())) {
            criteria.andTableIdEqualTo(request.getTableId());
        }
        datasetTableTaskLogExample.setOrderByClause("create_time desc");
        return extDataSetTaskMapper.list(request);
    }

    public void deleteByTaskId(String taskId){
        DatasetTableTaskLogExample datasetTableTaskLogExample = new DatasetTableTaskLogExample();
        DatasetTableTaskLogExample.Criteria criteria = datasetTableTaskLogExample.createCriteria();
        criteria.andTaskIdEqualTo(taskId);
        datasetTableTaskLogMapper.deleteByExample(datasetTableTaskLogExample);
    }

}

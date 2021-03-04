package io.dataease.service.dataset;

import io.dataease.base.domain.DatasetTableTaskLog;
import io.dataease.base.mapper.DatasetTableTaskLogMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @Author gin
 * @Date 2021/3/4 1:26 下午
 */
@Service
public class DataSetTableTaskLogService {
    @Resource
    private DatasetTableTaskLogMapper datasetTableTaskLogMapper;

    public DatasetTableTaskLog save(DatasetTableTaskLog datasetTableTaskLog) {
        if (StringUtils.isEmpty(datasetTableTaskLog.getId())) {
            datasetTableTaskLog.setId(UUID.randomUUID().toString());
            datasetTableTaskLog.setCreateTime(System.currentTimeMillis());
            datasetTableTaskLogMapper.insert(datasetTableTaskLog);
        } else {
            datasetTableTaskLogMapper.updateByPrimaryKey(datasetTableTaskLog);
        }
        return datasetTableTaskLog;
    }

    public void delete(String id) {
        datasetTableTaskLogMapper.deleteByPrimaryKey(id);
    }


}

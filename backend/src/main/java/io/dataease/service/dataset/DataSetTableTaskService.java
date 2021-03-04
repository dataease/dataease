package io.dataease.service.dataset;

import io.dataease.base.domain.DatasetTableTask;
import io.dataease.base.domain.DatasetTableTaskExample;
import io.dataease.base.mapper.DatasetTableTaskMapper;
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
public class DataSetTableTaskService {
    @Resource
    private DatasetTableTaskMapper datasetTableTaskMapper;

    public DatasetTableTask save(DatasetTableTask datasetTableTask) {
        if (StringUtils.isEmpty(datasetTableTask.getId())) {
            datasetTableTask.setId(UUID.randomUUID().toString());
            datasetTableTask.setCreateTime(System.currentTimeMillis());
            datasetTableTaskMapper.insert(datasetTableTask);
        } else {
            datasetTableTaskMapper.updateByPrimaryKey(datasetTableTask);
        }
        return datasetTableTask;
    }

    public void delete(String id) {
        datasetTableTaskMapper.deleteByPrimaryKey(id);
    }

    public List<DatasetTableTask> list(DatasetTableTask datasetTableTask) {
        DatasetTableTaskExample datasetTableTaskExample = new DatasetTableTaskExample();
        DatasetTableTaskExample.Criteria criteria = datasetTableTaskExample.createCriteria();
        if (StringUtils.isNotEmpty(datasetTableTask.getTableId())) {
            criteria.andTableIdEqualTo(datasetTableTask.getTableId());
        }
        datasetTableTaskExample.setOrderByClause("create_time desc,name asc");
        return datasetTableTaskMapper.selectByExample(datasetTableTaskExample);
    }
}

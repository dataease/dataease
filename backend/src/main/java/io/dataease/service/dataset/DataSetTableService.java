package io.dataease.service.dataset;

import com.alibaba.nacos.common.util.UuidUtils;
import io.dataease.base.domain.DatasetTable;
import io.dataease.base.domain.DatasetTableExample;
import io.dataease.base.mapper.DatasetTableMapper;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/2/23 2:54 下午
 */
@Service
public class DataSetTableService {
    @Resource
    private DatasetTableMapper datasetTableMapper;

    public void batchInsert(List<DatasetTable> datasetTable) {
        for (DatasetTable table : datasetTable) {
            save(table);
        }
    }

    public DatasetTable save(DatasetTable datasetTable) {
        if (StringUtils.isEmpty(datasetTable.getId())) {
            datasetTable.setId(UuidUtils.generateUuid());
            datasetTable.setCreateTime(System.currentTimeMillis());
            datasetTableMapper.insert(datasetTable);
        } else {
            datasetTableMapper.updateByPrimaryKey(datasetTable);
        }
        return datasetTable;
    }

    public void delete(String id) {
        datasetTableMapper.deleteByPrimaryKey(id);
    }

    public List<DatasetTable> list(DataSetTableRequest dataSetTableRequest) {
        DatasetTableExample datasetTableExample = new DatasetTableExample();
        datasetTableExample.createCriteria().andSceneIdEqualTo(dataSetTableRequest.getSceneId());
        if (StringUtils.isNotEmpty(dataSetTableRequest.getSort())) {
            datasetTableExample.setOrderByClause(dataSetTableRequest.getSort());
        }
        return datasetTableMapper.selectByExample(datasetTableExample);
    }

    public DatasetTable get(String id) {
        return datasetTableMapper.selectByPrimaryKey(id);
    }
}

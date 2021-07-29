package io.dataease.service.dataset;

import io.dataease.base.domain.*;
import io.dataease.base.mapper.DatasetTableFunctionMapper;
import io.dataease.commons.utils.DorisTableUtils;
import io.dataease.datasource.service.DatasourceService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

/**
 * @Author gin
 * @Date 2021/7/29 11:58 上午
 */
@Service
public class DatasetFunctionService {
    @Resource
    private DatasetTableFunctionMapper datasetTableFunctionMapper;
    @Resource
    private DataSetTableService dataSetTableService;
    @Resource
    private DatasourceService datasourceService;

    public DatasetTableFunction get(Long id) {
        return datasetTableFunctionMapper.selectByPrimaryKey(id);
    }

    public List<DatasetTableFunction> list(DatasetTableFunction datasetTableFunction) {
        DatasetTableFunctionExample datasetTableFunctionExample = new DatasetTableFunctionExample();
        DatasetTableFunctionExample.Criteria criteria = datasetTableFunctionExample.createCriteria();
        if (StringUtils.isNotEmpty(datasetTableFunction.getDbType())) {
            criteria.andDbTypeEqualTo(datasetTableFunction.getDbType());
        }
        return datasetTableFunctionMapper.selectByExampleWithBLOBs(datasetTableFunctionExample);
    }

    public List<DatasetTableFunction> listByTableId(String id) {
        DatasetTable datasetTable = dataSetTableService.get(id);
        String dbType;
        if (datasetTable.getMode() == 0) {
            Datasource datasource = datasourceService.get(datasetTable.getDataSourceId());
            dbType = datasource.getType();
        } else {
            dbType = "doris";
        }
        DatasetTableFunction datasetTableFunction = new DatasetTableFunction();
        datasetTableFunction.setDbType(dbType);
        return list(datasetTableFunction);
    }
}

package io.dataease.service.dataset;

import io.dataease.plugins.common.base.domain.DatasetTable;
import io.dataease.plugins.common.base.domain.DatasetTableFunction;
import io.dataease.plugins.common.base.domain.DatasetTableFunctionExample;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.base.mapper.DatasetTableFunctionMapper;
import io.dataease.service.datasource.DatasourceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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
        datasetTableFunctionExample.setOrderByClause("name asc");
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

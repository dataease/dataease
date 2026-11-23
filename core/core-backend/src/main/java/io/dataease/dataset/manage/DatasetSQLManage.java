package io.dataease.dataset.manage;

import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.DsTypeDTO;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.view.dto.ChartExtRequest;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 查询编译门面：保持既有调用契约，实际编译逻辑下沉到只读服务 DatasetSqlCompiler。
 */
@Component
public class DatasetSQLManage {

    @Resource
    private DatasetSqlCompiler datasetSqlCompiler;

    public Map<String, Object> getUnionSQLForEdit(DatasetGroupInfoDTO dataTableInfoDTO, ChartExtRequest chartExtRequest) throws Exception {
        return datasetSqlCompiler.getUnionSQLForEdit(dataTableInfoDTO, chartExtRequest);
    }

    public Map<String, Object> getUnionSQLForEdit(DatasetGroupInfoDTO dataTableInfoDTO, ChartExtRequest chartExtRequest, CoreDatasource coreDatasource, boolean isFromDataSet) throws Exception {
        return datasetSqlCompiler.getUnionSQLForEdit(dataTableInfoDTO, chartExtRequest, coreDatasource, isFromDataSet);
    }

    public DsTypeDTO getDatasourceType(Map<Long, DatasourceSchemaDTO> dsMap, Long datasourceId) {
        return datasetSqlCompiler.getDatasourceType(dsMap, datasourceId);
    }

    public String subPrefixSuffixChar(String str) {
        return datasetSqlCompiler.subPrefixSuffixChar(str);
    }

    public String putObj2Map(Map<Long, DatasourceSchemaDTO> dsMap, DatasetTableDTO ds, boolean isCross) {
        return datasetSqlCompiler.putObj2Map(dsMap, ds, isCross);
    }

    public String putObj2Map(Map<Long, DatasourceSchemaDTO> dsMap, DatasetTableDTO ds, boolean isCross, CoreDatasource coreDatasource) {
        return datasetSqlCompiler.putObj2Map(dsMap, ds, isCross, coreDatasource);
    }
}

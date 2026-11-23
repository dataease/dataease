package io.dataease.datasource.manage;

import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.factory.ProviderFactory;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.datasource.provider.ExcelUtils;
import io.dataease.utils.BeanUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 数据源表列表查询服务（只读，不写缓存）。
 * DatasourceServer 与 DatasetCacheManage 共用，避免缓存层反向依赖 Server。
 */
@Component
public class DatasetTableQueryManage {

    private static final Pattern ORACLE_RECYCLE_BIN_TABLE_PATTERN = Pattern.compile("^BIN\\$.*\\$[0-9]+$", Pattern.CASE_INSENSITIVE);

    @Resource
    private DataSourceManage dataSourceManage;
    @Resource
    private DatasourceInvokeManage datasourceInvokeManage;

    public List<DatasetTableDTO> getTables(DatasetTableDTO datasetTableDTO) throws DEException {
        CoreDatasource coreDatasource = dataSourceManage.getCoreDatasource(datasetTableDTO.getDatasourceId());
        if (coreDatasource == null) {
            DEException.throwException("无效数据源！");
        }
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(datasourceDTO, coreDatasource);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(datasourceDTO);
        List<DatasetTableDTO> result;
        if (coreDatasource.getType().contains(DatasourceConfiguration.DatasourceType.API.name())) {
            result = (List<DatasetTableDTO>) datasourceInvokeManage.invokeMethod(coreDatasource.getType(), "getApiTables", DatasourceRequest.class, datasourceRequest);
            return result;
        }
        if (coreDatasource.getType().contains("Excel")) {
            return ExcelUtils.getTables(datasourceRequest);
        }
        Provider provider = ProviderFactory.getProvider(datasourceDTO.getType());
        List<DatasetTableDTO> tables = provider.getTables(datasourceRequest);
        if (StringUtils.equalsIgnoreCase(coreDatasource.getType(), DatasourceConfiguration.DatasourceType.oracle.name())) {
            result = tables.stream().filter(table -> !isOracleRecycleBinTable(table)).collect(Collectors.toList());
        } else {
            result = tables;
        }
        return result;
    }

    private boolean isOracleRecycleBinTable(DatasetTableDTO table) {
        if (table == null) {
            return false;
        }
        return isOracleRecycleBinName(table.getTableName()) || isOracleRecycleBinName(table.getName());
    }

    private boolean isOracleRecycleBinName(String tableName) {
        if (StringUtils.isBlank(tableName)) {
            return false;
        }
        String normalized = StringUtils.removeEnd(StringUtils.removeStart(tableName.trim(), "\""), "\"");
        return ORACLE_RECYCLE_BIN_TABLE_PATTERN.matcher(normalized).matches();
    }
}

package io.dataease.dataset.manage;

import io.dataease.constant.CacheConstant;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableFieldMapper;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableMapper;
import io.dataease.datasource.server.DatasourceServer;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.i18n.Translator;
import io.dataease.utils.CacheUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 数据集表和字段缓存管理
 * 表缓存：key = datasourceId，value = List（该数据源下所有表）
 * 字段缓存：key = datasourceId_tableName，value = List（该表下所有字段）
 */
@Component
public class DatasetCacheManage {
    @Resource
    private CoreDatasetTableMapper coreDatasetTableMapper;
    @Resource
    private CoreDatasetTableFieldMapper coreDatasetTableFieldMapper;
    @Resource
    private DatasourceServer datasourceServer;

    /**
     * 缓存某个数据源下的所有表
     */
    public void cacheTablesByDatasource(Long datasourceId, List<DatasetTableDTO> tables) {
        if (datasourceId == null || CollectionUtils.isEmpty(tables)) return;
        String cacheKey = String.valueOf(datasourceId);
        List<String> tableNames = tables.stream().map(DatasetTableDTO::getTableName).toList();
        CacheUtils.put(CacheConstant.DatasetCacheConstant.DATASET_TABLE_CACHE, cacheKey, tableNames);
    }

    /**
     * 缓存某个表下的所有字段
     */
    public void cacheFieldsByTable(Long datasourceId, String tableName, List<DatasetTableFieldDTO> fields) {
        if (datasourceId == null || StringUtils.isBlank(tableName) || CollectionUtils.isEmpty(fields)) return;
        String cacheKey = datasourceId + "_" + tableName;
        List<String> fieldNames = fields.stream().map(DatasetTableFieldDTO::getOriginName).toList();
        CacheUtils.put(CacheConstant.DatasetCacheConstant.DATASET_FIELD_CACHE, cacheKey, fieldNames);
    }

    /**
     * 获取某个数据源下的所有表，缓存未命中则查询数据源并缓存
     */
    public List<String> getCachedTablesByDatasource(Long datasourceId) {
        if (datasourceId == null) return Collections.emptyList();
        String cacheKey = String.valueOf(datasourceId);
        Object cached = CacheUtils.get(CacheConstant.DatasetCacheConstant.DATASET_TABLE_CACHE, cacheKey);
        if (cached != null) {
            return (List<String>) cached;
        } else {
            DatasetTableDTO dto = new DatasetTableDTO();
            dto.setDatasourceId(datasourceId);
            List<DatasetTableDTO> tables = datasourceServer.getTables(dto);
            cacheTablesByDatasource(datasourceId, tables);
            return tables.stream().map(DatasetTableDTO::getTableName).toList();
        }
    }

    /**
     * 获取某个表下的所有字段，缓存未命中则查询并缓存
     */
    public List<String> getCachedFieldsByTable(Long datasourceId, String tableName) {
        if (datasourceId == null || StringUtils.isBlank(tableName)) return Collections.emptyList();
        String cacheKey = datasourceId + "_" + tableName;
        Object cached = CacheUtils.get(CacheConstant.DatasetCacheConstant.DATASET_FIELD_CACHE, cacheKey);
        if (cached != null) {
            return (List<String>) cached;
        }
        return null;
    }

    /**
     * 校验表是否存在：检查该数据源的缓存表列表中是否包含此表
     * 如果缓存中没有该数据源的表列表，则放行（由调用方确保缓存已填充）
     */
    public void validateTable(Long datasourceId, String tableName) {
        if (StringUtils.isBlank(tableName)) return;
        List<String> cachedTables = getCachedTablesByDatasource(datasourceId);
        if (cachedTables.isEmpty()) {
            return;
        }
        boolean exists = cachedTables.stream()
                .anyMatch(t -> StringUtils.equalsIgnoreCase(t, tableName));
        if (!exists) {
            DEException.throwException(Translator.get("i18n_dataset_table_not_exist"));
        }
    }
}

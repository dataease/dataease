package io.dataease.datasource.strategy;

import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.extensions.datasource.dto.DatasourceDTO;

/**
 * 数据源写入策略：按类型执行 Commit 之后的外部操作（建表、导入、调度、连接池更新）。
 * 实现必须保证步骤幂等：重试不重复创建/写入已成功资源。
 */
public interface DatasourceWriteStrategy {

    boolean supports(String type);

    void executeCreate(DatasourceDTO dto, CoreDatasource coreDatasource, Long executionId) throws Exception;

    void executeUpdate(DatasourceDTO dto, DatasourceDTO sourceData, CoreDatasource requestDatasource, Long executionId) throws Exception;
}

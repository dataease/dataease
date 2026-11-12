package io.dataease.datasource.dao.auto.repository;

import io.dataease.datasource.dao.auto.entity.CoreDatasourceTableSemantic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoreDatasourceTableSemanticRepository extends JpaRepository<CoreDatasourceTableSemantic, Long> {
    List<CoreDatasourceTableSemantic> findByDatasource_IdOrderById(Long datasourceId);

    Optional<CoreDatasourceTableSemantic> findByDatasource_IdAndTableName(Long datasourceId, String tableName);
}

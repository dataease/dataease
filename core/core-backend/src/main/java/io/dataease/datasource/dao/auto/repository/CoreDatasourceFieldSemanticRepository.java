package io.dataease.datasource.dao.auto.repository;

import io.dataease.datasource.dao.auto.entity.CoreDatasourceFieldSemantic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoreDatasourceFieldSemanticRepository extends JpaRepository<CoreDatasourceFieldSemantic, Long> {
    List<CoreDatasourceFieldSemantic> findByTableSemantic_IdOrderByPosition(Long tableId);

    List<CoreDatasourceFieldSemantic> findByTableSemantic_Id(Long tableId);

    Optional<CoreDatasourceFieldSemantic> findByTableSemantic_IdAndFieldName(Long tableId, String fieldName);

    List<CoreDatasourceFieldSemantic> findByTableSemantic_Datasource_Id(Long datasourceId);
}

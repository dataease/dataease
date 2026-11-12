package io.dataease.datasource.dao.auto.repository;

import io.dataease.datasource.dao.auto.entity.CoreDatasourceFieldSemantic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CoreDatasourceFieldSemanticRepository extends JpaRepository<CoreDatasourceFieldSemantic, Long> {
    @Query("""
            select f from CoreDatasourceFieldSemantic f join fetch f.tableSemantic t
            where t.datasource.id = :datasourceId and t.active = true order by t.id, f.position
            """)
    List<CoreDatasourceFieldSemantic> findByActiveTable(@Param("datasourceId") Long datasourceId);

    List<CoreDatasourceFieldSemantic> findByTableSemantic_Id(Long tableId);

    Optional<CoreDatasourceFieldSemantic> findByTableSemantic_IdAndFieldName(Long tableId, String fieldName);

    List<CoreDatasourceFieldSemantic> findByTableSemantic_Datasource_Id(Long datasourceId);
}

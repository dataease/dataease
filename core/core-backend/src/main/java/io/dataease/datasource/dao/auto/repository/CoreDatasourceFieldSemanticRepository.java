package io.dataease.datasource.dao.auto.repository;

import io.dataease.datasource.dao.auto.entity.CoreDatasourceFieldSemantic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CoreDatasourceFieldSemanticRepository extends JpaRepository<CoreDatasourceFieldSemantic, Long> {
    interface TableFieldCounts {
        Long getTableId();
        long getFieldCount();
        long getConfirmedFieldCount();
    }

    @Query("""
            select f.tableSemantic.id as tableId, count(f) as fieldCount,
                   sum(case when f.confirmed = true then 1 else 0 end) as confirmedFieldCount
            from CoreDatasourceFieldSemantic f
            where f.tableSemantic.datasource.id = :datasourceId
            group by f.tableSemantic.id
            """)
    List<TableFieldCounts> countFieldsByDatasource(@Param("datasourceId") Long datasourceId);

    List<CoreDatasourceFieldSemantic> findByTableSemantic_IdOrderByPosition(Long tableId);

    List<CoreDatasourceFieldSemantic> findByTableSemantic_Id(Long tableId);

    Optional<CoreDatasourceFieldSemantic> findByTableSemantic_IdAndFieldName(Long tableId, String fieldName);

    @Query("""
            select f from CoreDatasourceFieldSemantic f join fetch f.tableSemantic t
            where t.datasource.id = :datasourceId and t.active = true and t.available = true
            """)
    List<CoreDatasourceFieldSemantic> findAvailableFields(@Param("datasourceId") Long datasourceId);

    List<CoreDatasourceFieldSemantic> findByTableSemantic_Datasource_Id(Long datasourceId);
}

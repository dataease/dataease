package io.dataease.datasource.dao.auto.repository;

import io.dataease.datasource.dao.auto.entity.CoreDatasourceRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CoreDatasourceRelationRepository extends JpaRepository<CoreDatasourceRelation, Long> {
    @Query("""
            select r from CoreDatasourceRelation r left join fetch r.conditions
            where r.datasource.id = :datasourceId order by r.id
            """)
    List<CoreDatasourceRelation> findWithConditions(@Param("datasourceId") Long datasourceId);

    @Query("""
            select r from CoreDatasourceRelation r left join fetch r.conditions
            where r.datasource.id = :datasourceId and (r.leftTable = :tableName or r.rightTable = :tableName)
            """)
    List<CoreDatasourceRelation> findByReferencedTable(@Param("datasourceId") Long datasourceId,
                                                       @Param("tableName") String tableName);

    @Query("""
            select r.leftTable, r.rightTable from CoreDatasourceRelation r
            where r.datasource.id = :datasourceId
            """)
    List<Object[]> findTablePairs(@Param("datasourceId") Long datasourceId);

    Optional<CoreDatasourceRelation> findByIdAndDatasource_Id(Long id, Long datasourceId);
}

package io.dataease.dataset.dao.auto.mapper;

import io.dataease.dao.auto.entity.CoreDatasetGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoreDatasetGroupRepository extends JpaRepository<CoreDatasetGroup, Long>, JpaSpecificationExecutor<CoreDatasetGroup> {

    List<CoreDatasetGroup> findByNodeType(String nodeType);

    @Query(value = "select cdg.* from core_dataset_group cdg where EXISTS " +
            "(SELECT 1 FROM core_chart_view cv WHERE cv.table_id = cdg.id and cv.id IN :viewIds)",
            nativeQuery = true)
    List<CoreDatasetGroup> findByViewIds(@Param("viewIds") List<Long> viewIds);
}

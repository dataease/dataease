package io.dataease.dataset.dao.auto.mapper;


import io.dataease.dao.auto.entity.CoreDatasetTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CoreDatasetTableRepository extends JpaRepository<CoreDatasetTable, Long>, JpaSpecificationExecutor<CoreDatasetTable> {

    List<CoreDatasetTable> findByDatasetGroupId(Long datasetId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CoreDatasetTable c WHERE c.datasetGroupId = :datasetGroupId AND c.id NOT IN :ids")
    void deleteByDatasetGroupIdAndNotInIds(Long datasetGroupId, List<Long> ids);

    @Modifying
    @Transactional
    @Query("DELETE FROM CoreDatasetTable c WHERE c.datasetGroupId = :datasetGroupId")
    void deleteByDatasetGroupId(Long datasetGroupId);

}

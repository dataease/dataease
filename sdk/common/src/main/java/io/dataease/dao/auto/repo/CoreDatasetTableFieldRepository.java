package io.dataease.dao.auto.repo;

import io.dataease.dao.auto.entity.CoreDatasetTableField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface CoreDatasetTableFieldRepository extends JpaRepository<CoreDatasetTableField, Long>, JpaSpecificationExecutor<CoreDatasetTableField> {

    List<CoreDatasetTableField> findByChartId(Long datasetId);

    List<CoreDatasetTableField> findByDatasetGroupId(Long datasetGroupId);

    List<CoreDatasetTableField> findByDatasetTableId(Long datasetTableId);

    void deleteByChartId(Long chartId);

    List<CoreDatasetTableField> findByDatasetGroupIdIn(List<Long> tableIds);

    void deleteByDatasetGroupId(Long datasetGroupId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CoreDatasetTableField c WHERE c.datasetTableId = :datasetTableId AND c.id NOT IN :fieldIds")
    void deleteByDatasetTableIdAndNotInFieldIds(Long datasetTableId, List<Long> fieldIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM CoreDatasetTableField c WHERE c.datasetGroupId = :datasetGroupId AND c.id NOT IN :fieldIds")
    void deleteByDatasetGroupIdAndNotInFieldIds(Long datasetGroupId, List<Long> fieldIds);

    @Query("SELECT f FROM CoreDatasetTableField f LEFT JOIN CoreChartView v ON f.datasetTableId = v.tableId WHERE v.id = :viewId")
    List<CoreDatasetTableField> findByViewId(Long viewId);
}

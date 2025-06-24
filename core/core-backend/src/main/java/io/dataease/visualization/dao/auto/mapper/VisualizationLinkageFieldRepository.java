package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationLinkageField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface VisualizationLinkageFieldRepository extends JpaRepository<VisualizationLinkageField, Long>, JpaSpecificationExecutor<VisualizationLinkageField> {


    @Modifying
    @Transactional
    @Query("DELETE FROM VisualizationLinkageField v where v.linkageId in :linkageIds")
    void deleteByLinkageIds(List<Long> linkageIds);

    List<VisualizationLinkageField> findByLinkageIdIn(List<Long> linkageIds);
}

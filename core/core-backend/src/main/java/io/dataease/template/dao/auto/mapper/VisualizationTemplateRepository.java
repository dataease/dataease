package io.dataease.template.dao.auto.mapper;

import io.dataease.template.dao.auto.entity.VisualizationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface VisualizationTemplateRepository extends JpaRepository<VisualizationTemplate, String>, JpaSpecificationExecutor<VisualizationTemplate> {

    @Modifying
    @Transactional
    @Query("UPDATE VisualizationTemplate vt SET vt.useCount = 0")
    void updateUseCountToZero();

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM VisualizationTemplate WHERE name = :name")
    int deleteByTemplateName(String name);

    @Modifying
    @Transactional
    @Query("UPDATE VisualizationTemplate vt SET vt.version = 2")
    void updateVersion();

    List<VisualizationTemplate> findByName(String name);
}

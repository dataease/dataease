package io.dataease.template.dao.auto.mapper;

import io.dataease.template.dao.auto.entity.VisualizationTemplate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface VisualizationTemplateRepository extends JpaRepository<VisualizationTemplate, String>, JpaSpecificationExecutor<VisualizationTemplate> {

    @Transactional
    default void updateUseCountToZero() {
        List<VisualizationTemplate> templates = findAll();
        for (VisualizationTemplate template : templates) {
            template.setUseCount(0);
        }
        saveAllAndFlush(templates);
    }

    @Transactional
    default void deleteByTemplateName(String name) {
        Specification<VisualizationTemplate> spec = (root, query, cb) ->
                cb.equal(root.get("name"), name);
        List<VisualizationTemplate> templates = findAll(spec);
        if (!templates.isEmpty()) {
            deleteAll(templates);
        }
    }

    @Transactional
    default void updateVersion() {
        List<VisualizationTemplate> templates = findAll();
        for (VisualizationTemplate template : templates) {
            template.setVersion(2);
        }
        saveAllAndFlush(templates);
    }

    List<VisualizationTemplate> findByName(String name);
}

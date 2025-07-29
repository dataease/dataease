package io.dataease.template.dao.auto.mapper;

import io.dataease.template.dao.auto.entity.VisualizationTemplateCategoryMap;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface VisualizationTemplateCategoryMapRepository extends JpaRepository<VisualizationTemplateCategoryMap, String>, JpaSpecificationExecutor<VisualizationTemplateCategoryMap> {


    @Transactional
    default void deleteByTemplateIdAndCategoryId(String templateId, String categoryId) {
        Specification<VisualizationTemplateCategoryMap> spec = (root, query, cb) ->
                cb.and(cb.equal(root.get("templateId"), templateId), cb.equal(root.get("categoryId"), categoryId));
        List<VisualizationTemplateCategoryMap> maps = findAll(spec);
        if (!maps.isEmpty()) {
            deleteAll(maps);
        }
    }

    @Transactional
    default void deleteByCategoryId(String categoryId) {
        Specification<VisualizationTemplateCategoryMap> spec = (root, query, cb) ->
                cb.equal(root.get("categoryId"), categoryId);
        List<VisualizationTemplateCategoryMap> maps = findAll(spec);
        if (!maps.isEmpty()) {
            deleteAll(maps);
        }
    }

    @Transactional
    default void deleteByTemplateId(String templateId) {
        Specification<VisualizationTemplateCategoryMap> spec = (root, query, cb) ->
                cb.equal(root.get("templateId"), templateId);
        List<VisualizationTemplateCategoryMap> maps = findAll(spec);
        if (!maps.isEmpty()) {
            deleteAll(maps);
        }
    }

    @Transactional
    default void deleteByTemplateIds(List<String> templateIds) {
        Specification<VisualizationTemplateCategoryMap> spec = (root, query, cb) ->
                root.get("templateId").in(templateIds);
        List<VisualizationTemplateCategoryMap> maps = findAll(spec);
        if (!maps.isEmpty()) {
            deleteAll(maps);
        }
    }

    default List<String> findTemplateArrayCategories(List<String> templateIds) {
        Specification<VisualizationTemplateCategoryMap> spec = (root, query, cb) -> {
            query.distinct(true);
            return root.get("templateId").in(templateIds);
        };
        List<VisualizationTemplateCategoryMap> maps = findAll(spec);
        return maps.stream().map(VisualizationTemplateCategoryMap::getCategoryId).toList();
    }

}

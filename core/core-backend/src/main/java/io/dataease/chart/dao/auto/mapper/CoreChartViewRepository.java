package io.dataease.chart.dao.auto.mapper;


import io.dataease.dao.auto.entity.CoreChartView;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;


public interface CoreChartViewRepository extends JpaRepository<CoreChartView, Long>, JpaSpecificationExecutor<CoreChartView> {

    default Long countDistinctTableIdByIdIn(List<String> ids) {
        Specification<CoreChartView> spec = (root, query, cb) ->
                cb.and(
                        root.get("id").in(ids),
                        cb.isNotNull(root.get("tableId"))
                );
        return count(spec);
    }

    @Transactional
    default void deleteBySceneIdAndNotInIds(Long sceneId, List<Long> chartIds) {
        Specification<CoreChartView> spec = (root, query, cb) ->
                cb.and(
                        cb.equal(root.get("sceneId"), sceneId),
                        root.get("id").in(chartIds).not()
                );
        List<CoreChartView> views = findAll(spec);
        if (!views.isEmpty()) {
            deleteAll(views);
        }
    }

    default List<CoreChartView> findAllTablePivotViews() {
        Specification<CoreChartView> spec = (root, query, cb) ->
                cb.equal(root.get("type"), "table-pivot");
        return findAll(spec);
    }

    @Transactional
    default void deleteBySceneId(Long sceneId) {
        Specification<CoreChartView> spec = (root, query, cb) ->
                cb.equal(root.get("sceneId"), sceneId);
        List<CoreChartView> views = findAll(spec);
        if (!views.isEmpty()) {
            deleteAll(views);
        }
    }

    default List<CoreChartView> findBySceneId(Long sceneId) {
        Specification<CoreChartView> spec = (root, query, cb) ->
                cb.equal(root.get("sceneId"), sceneId);
        return findAll(spec);
    }

    @Transactional
    default void deleteBySceneIds(Set<Long> sceneIds) {
        Specification<CoreChartView> spec = (root, query, cb) ->
                root.get("sceneId").in(sceneIds);
        List<CoreChartView> views = findAll(spec);
        if (!views.isEmpty()) {
            deleteAll(views);
        }
    }

    default List<CoreChartView> findViewInfoByCopyId(Long copyId) {
        Specification<CoreChartView> spec = (root, query, cb) ->
                cb.equal(root.get("copyId"), copyId);
        return findAll(spec);
    }

    List<CoreChartView> findByIdInAndTypeNot(List<Long> ids, String type);

    @Query("SELECT c.id as id FROM CoreChartView c WHERE c.id IN :ids AND c.linkageActive = :linkageActive")
    List<Long> findIdsByIdInAndLinkageActive(@Param("ids") List<Long> ids, @Param("linkageActive") Boolean linkageActive);
}

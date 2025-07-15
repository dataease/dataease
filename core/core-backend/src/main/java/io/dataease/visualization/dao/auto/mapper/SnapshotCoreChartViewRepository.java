package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotCoreChartView;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;


public interface SnapshotCoreChartViewRepository extends JpaRepository<SnapshotCoreChartView, Long>, JpaSpecificationExecutor<SnapshotCoreChartView> {

    @Transactional
    default void deleteBySceneId(Long sceneId) {
        Specification<SnapshotCoreChartView> spec = (root, query, cb) ->
                cb.equal(root.get("sceneId"), sceneId);
        List<SnapshotCoreChartView> entities = findAll(spec);
        if (!entities.isEmpty()) {
            deleteAll(entities);
        }
    }

    default List<SnapshotCoreChartView> findBySceneId(Long sceneId) {
        Specification<SnapshotCoreChartView> spec = (root, query, cb) ->
                cb.equal(root.get("sceneId"), sceneId);
        return findAll(spec);
    }

    @Transactional
    default void deleteBySceneIds(Set<Long> sceneIds) {
        Specification<SnapshotCoreChartView> spec = (root, query, cb) ->
                root.get("sceneId").in(sceneIds);
        List<SnapshotCoreChartView> entities = findAll(spec);
        if (!entities.isEmpty()) {
            deleteAll(entities);
        }
    }

    List<SnapshotCoreChartView> findByIdInAndTypeNot(List<Long> ids, String type);
}

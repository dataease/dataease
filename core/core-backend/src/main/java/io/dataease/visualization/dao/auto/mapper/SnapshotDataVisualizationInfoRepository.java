package io.dataease.visualization.dao.auto.mapper;


import io.dataease.visualization.dao.auto.entity.SnapshotDataVisualizationInfo;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;


public interface SnapshotDataVisualizationInfoRepository extends JpaRepository<SnapshotDataVisualizationInfo, Long>, JpaSpecificationExecutor<SnapshotDataVisualizationInfo> {

    default Optional<SnapshotDataVisualizationInfo> findSnapshotDvInfoEntity(Long dvId, String dvType) {
        Specification<SnapshotDataVisualizationInfo> spec = (root, query, cb) ->
                cb.and(
                        cb.or(
                                cb.equal(root.get("deleteFlag"), false),
                                cb.isNull(root.get("deleteFlag"))
                        ),
                        cb.equal(root.get("id"), dvId),
                        dvType == null ? cb.conjunction() : cb.equal(root.get("type"), dvType)
                );
        return findOne(spec);
    }

}

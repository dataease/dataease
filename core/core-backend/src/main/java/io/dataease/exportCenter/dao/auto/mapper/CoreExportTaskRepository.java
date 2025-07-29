package io.dataease.exportCenter.dao.auto.mapper;

import io.dataease.exportCenter.dao.auto.entity.CoreExportTask;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface CoreExportTaskRepository extends JpaRepository<CoreExportTask, String>, JpaSpecificationExecutor<CoreExportTask> {

    default long countByUserIdAndExportStatus(Long userId, String exportStatus) {
        Specification<CoreExportTask> spec = (root, query, cb) ->
                cb.and(cb.equal(root.get("userId"), userId), cb.equal(root.get("exportStatus"), exportStatus));
        return count(spec);
    }

    default long countByUserId(Long userId) {
        Specification<CoreExportTask> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("userId"), userId);
        return count(spec);
    }

    @Transactional
    default void deleteByExportTimeLessThan(long threshold) {
        Specification<CoreExportTask> spec = (root, query, cb) ->
                cb.lessThan(root.get("exportTime"), threshold);
        List<CoreExportTask> tasks = findAll(spec);
        if (!tasks.isEmpty()) {
            deleteAll(tasks);
        }
    }
}

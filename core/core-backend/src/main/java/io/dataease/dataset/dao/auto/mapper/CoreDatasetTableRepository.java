package io.dataease.dataset.dao.auto.mapper;


import io.dataease.dao.auto.entity.CoreDatasetTable;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

public interface CoreDatasetTableRepository extends JpaRepository<CoreDatasetTable, Long>, JpaSpecificationExecutor<CoreDatasetTable> {

    List<CoreDatasetTable> findByDatasetGroupId(Long datasetId);

    @Transactional
    default void deleteByDatasetGroupIdAndNotInIds(Long datasetGroupId, List<Long> ids) {
        Specification<CoreDatasetTable> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("datasetGroupId"), datasetGroupId));
            if (ids != null && !ids.isEmpty()) {
                predicates.add(cb.not(root.get("id").in(ids)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        deleteAllInBatch(findAll(spec));
    }

    @Transactional
    default void deleteByDatasetGroupId(Long datasetGroupId) {
        Specification<CoreDatasetTable> spec = (root, query, cb) ->
                cb.equal(root.get("datasetGroupId"), datasetGroupId);
        deleteAllInBatch(findAll(spec));
    }

}

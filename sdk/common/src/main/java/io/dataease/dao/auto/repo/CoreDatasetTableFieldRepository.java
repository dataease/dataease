package io.dataease.dao.auto.repo;

import io.dataease.dao.auto.entity.CoreDatasetTableField;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


public interface CoreDatasetTableFieldRepository extends JpaRepository<CoreDatasetTableField, Long>, JpaSpecificationExecutor<CoreDatasetTableField> {

    List<CoreDatasetTableField> findByChartId(Long datasetId);

    List<CoreDatasetTableField> findByDatasetGroupId(Long datasetGroupId);

    List<CoreDatasetTableField> findByDatasetTableId(Long datasetTableId);

    void deleteByChartId(Long chartId);

    List<CoreDatasetTableField> findByDatasetGroupIdIn(List<Long> tableIds);

    void deleteByDatasetGroupId(Long datasetGroupId);

    @Transactional
    default void deleteByDatasetTableIdAndNotInFieldIds(Long datasetTableId, List<Long> fieldIds) {
        Specification<CoreDatasetTableField> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("datasetTableId"), datasetTableId));
            if (fieldIds != null && !fieldIds.isEmpty()) {
                predicates.add(cb.not(root.get("id").in(fieldIds)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        deleteAllInBatch(findAll(spec));
    }

    @Transactional
    default void deleteByDatasetGroupIdAndNotInFieldIds(Long datasetGroupId, List<Long> fieldIds) {
        Specification<CoreDatasetTableField> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("datasetGroupId"), datasetGroupId));
            if (fieldIds != null && !fieldIds.isEmpty()) {
                predicates.add(cb.not(root.get("id").in(fieldIds)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        deleteAllInBatch(findAll(spec));
    }

}

package io.dataease.operation.dao.auto.mapper;

import io.dataease.operation.dao.auto.entity.CoreOptRecent;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


public interface CoreOptRecentRepository extends JpaRepository<CoreOptRecent, Long>, JpaSpecificationExecutor<CoreOptRecent> {

    @Transactional
    default int updateByParams(
            @Param("resourceId") Long resourceId,
            @Param("resourceName") String resourceName,
            @Param("resourceType") int resourceType,
            @Param("uid") Long uid,
            @Param("optType") int optType,
            @Param("time") Long time
    ) {
        Specification<CoreOptRecent> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("resourceType"), resourceType));
            predicates.add(cb.equal(root.get("uid"), uid));
            if (resourceId != null) predicates.add(cb.equal(root.get("resourceId"), resourceId));
            if (resourceId == null) predicates.add(cb.isNull(root.get("resourceId")));
            if (resourceName != null) predicates.add(cb.equal(root.get("resourceName"), resourceName));
            if (resourceName == null) predicates.add(cb.isNull(root.get("resourceName")));
            predicates.add(cb.equal(root.get("optType"), optType));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<CoreOptRecent> results = findAll(spec);
        if (!results.isEmpty()) {
            results.forEach(coreOptRecent -> {
                coreOptRecent.setTime(time);
                coreOptRecent.setOptType(optType);
            });
            saveAllAndFlush(results);
            return 0;
        }
        return 1;
    }

    List<CoreOptRecent> findByUid(Long uid);

}

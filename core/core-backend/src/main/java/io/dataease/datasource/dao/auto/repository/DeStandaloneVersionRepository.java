package io.dataease.datasource.dao.auto.repository;

import io.dataease.datasource.dao.auto.entity.DeStandaloneVersion;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface DeStandaloneVersionRepository extends JpaRepository<DeStandaloneVersion, Long>, JpaSpecificationExecutor<DeStandaloneVersion> {
    default List<DeStandaloneVersion> findRecords() {
        Specification<DeStandaloneVersion> spec = (root, query, cb) ->
                cb.isNotNull(root.get("id"));
        return findAll(spec, Sort.by(Sort.Direction.DESC, "id"));
    }
}

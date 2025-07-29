package io.dataease.dao.auto.repo;


import io.dataease.dao.auto.entity.PerBusiResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;


public interface PerBusiResourceRepository extends JpaRepository<PerBusiResource, Long>, JpaSpecificationExecutor<PerBusiResource> {
    void deleteByOrgId(Long orgId);
    void deleteAllByIdIn(Collection<Long> ids);
    Long countByOrgId(Long orgId);
}

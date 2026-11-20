package io.dataease.ai.dao.repository;

import io.dataease.ai.dao.entity.CoreAiOrgPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CoreAiOrgPolicyRepository extends JpaRepository<CoreAiOrgPolicy, Long>, JpaSpecificationExecutor<CoreAiOrgPolicy> {
}

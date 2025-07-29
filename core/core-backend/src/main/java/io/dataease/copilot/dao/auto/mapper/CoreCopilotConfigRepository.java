package io.dataease.copilot.dao.auto.mapper;

import io.dataease.copilot.dao.auto.entity.CoreCopilotConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;



public interface CoreCopilotConfigRepository extends JpaRepository<CoreCopilotConfig, Long>, JpaSpecificationExecutor<CoreCopilotConfig> {
}

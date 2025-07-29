package io.dataease.copilot.dao.auto.mapper;

import io.dataease.copilot.dao.auto.entity.CoreCopilotToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface CoreCopilotTokenRepository extends JpaRepository<CoreCopilotToken, Long>, JpaSpecificationExecutor<CoreCopilotToken> {
}

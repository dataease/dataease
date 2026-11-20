package io.dataease.ai.dao.repository;

import io.dataease.ai.dao.entity.CoreAiModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CoreAiModelRepository extends JpaRepository<CoreAiModel, Long>, JpaSpecificationExecutor<CoreAiModel> {
}

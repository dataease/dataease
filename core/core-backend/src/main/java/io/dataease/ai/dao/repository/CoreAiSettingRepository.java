package io.dataease.ai.dao.repository;

import io.dataease.ai.dao.entity.CoreAiSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CoreAiSettingRepository extends JpaRepository<CoreAiSetting, Long>, JpaSpecificationExecutor<CoreAiSetting> {
}

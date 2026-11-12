package io.dataease.ai.dao.repository;

import io.dataease.ai.dao.entity.CoreAiSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CoreAiSettingRepository extends JpaRepository<CoreAiSetting, Long>, JpaSpecificationExecutor<CoreAiSetting> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_READ)
    @org.springframework.data.jpa.repository.Query("select s from CoreAiSetting s where s.id=1")
    java.util.Optional<CoreAiSetting> admissionPolicy();
}

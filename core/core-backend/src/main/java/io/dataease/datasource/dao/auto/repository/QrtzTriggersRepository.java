package io.dataease.datasource.dao.auto.repository;

import io.dataease.qrtz.dao.auto.repo.entity.QrtzTriggers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface QrtzTriggersRepository extends JpaRepository<QrtzTriggers, String>, JpaSpecificationExecutor<QrtzTriggers> {




}

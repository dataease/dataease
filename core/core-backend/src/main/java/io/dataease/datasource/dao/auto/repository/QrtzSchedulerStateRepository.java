package io.dataease.datasource.dao.auto.repository;

import io.dataease.qrtz.dao.auto.repo.entity.QrtzSchedulerState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface QrtzSchedulerStateRepository extends JpaRepository<QrtzSchedulerState, String>, JpaSpecificationExecutor<QrtzSchedulerState> {




}

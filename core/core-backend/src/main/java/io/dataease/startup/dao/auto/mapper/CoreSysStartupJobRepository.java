package io.dataease.startup.dao.auto.mapper;

import io.dataease.startup.dao.auto.entity.CoreSysStartupJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface CoreSysStartupJobRepository extends JpaRepository<CoreSysStartupJob, String>, JpaSpecificationExecutor<CoreSysStartupJob> {


}

package io.dataease.datasource.dao.auto.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;

public interface TimestampRepository extends JpaRepository<Timestamp, String>, JpaSpecificationExecutor<Timestamp> {

    @Query("SELECT CURRENT_TIMESTAMP FROM QrtzSchedulerState")
    Timestamp getCurrentTimestamp();
}

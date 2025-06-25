package io.dataease.datasource.dao.auto.repository;

import io.dataease.qrtz.dao.auto.repo.entity.QrtzSchedulerState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TimestampRepository extends JpaRepository<QrtzSchedulerState, String> {

    @Query(value = "SELECT CURRENT_TIMESTAMP", nativeQuery = true)
    java.sql.Timestamp getCurrentTimestamp();
}

package io.dataease.dataset.dao.auto.mapper;

import io.dataease.dataset.dao.auto.entity.CoreDatasetTableSqlLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface CoreDatasetTableSqlLogRepository extends JpaRepository<CoreDatasetTableSqlLog, Long>, JpaSpecificationExecutor<CoreDatasetTableSqlLog> {

    List<CoreDatasetTableSqlLog> findByTableId(String tableId);

    void deleteByTableId(String tableId);
}

package io.dataease.datasource.dao.auto.repository;

import io.dataease.datasource.dao.auto.entity.CoreDatasourceSaveExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoreDatasourceSaveExecutionRepository extends JpaRepository<CoreDatasourceSaveExecution, Long> {

    List<CoreDatasourceSaveExecution> findByDsIdOrderByCreateTimeDesc(Long dsId);

    List<CoreDatasourceSaveExecution> findByStatus(String status);
}

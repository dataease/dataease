package io.dataease.datasource.dao.auto.repository;

import io.dataease.datasource.dao.auto.entity.CoreDatasourceTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CoreDatasourceTaskRepository extends JpaRepository<CoreDatasourceTask, Long>, JpaSpecificationExecutor<CoreDatasourceTask> {

    List<CoreDatasourceTask> findByDsId(Long dsId);

    void deleteByDsId(Long dsId);

    @Modifying
    @Transactional
    @Query("UPDATE CoreDatasourceTask c SET c.taskStatus = :taskStatus WHERE c.id = :id")
    void updateTaskStatus(Long id, String taskStatus);

    @Modifying
    @Transactional
    @Query("UPDATE CoreDatasourceTask c SET c.taskStatus = :taskStatus, c.lastExecTime = :lastExecTime WHERE c.id = :id")
    void updateTaskStatusAndLastExecTime(Long id, String taskStatus, Long lastExecTime);

    @Modifying
    @Transactional
    @Query("UPDATE CoreDatasourceTask c SET c.taskStatus = :taskStatus WHERE c.dsId IN :dsIds")
    void updateTaskStatusByDsIds(List<Long> dsIds, String taskStatus);

}

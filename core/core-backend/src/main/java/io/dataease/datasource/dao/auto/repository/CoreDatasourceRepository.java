package io.dataease.datasource.dao.auto.repository;

import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CoreDatasourceRepository extends JpaRepository<CoreDatasource, Long>, JpaSpecificationExecutor<CoreDatasource> {

    @Query("SELECT p FROM CoreDatasource p WHERE p.id IN :ids")
    List<CoreDatasource> findInIds(@Param("ids") List<Long> ids);

    @Query("SELECT p FROM CoreDatasource p WHERE p.type IN :types")
    List<CoreDatasource> findInTypes(@Param("types") List<String> types);


    @Query("SELECT c FROM CoreDatasource c WHERE c.pid = :pid")
    List<CoreDatasource> findByPid(Long pid);

    @Query("SELECT c FROM CoreDatasource c WHERE c.createBy = :createBy")
    List<CoreDatasource> findCoreDatasourcesByCreateBy(Long createBy, Pageable pageable);

    @Query("SELECT c FROM CoreDatasource c WHERE c.type NOT IN :types")
    List<CoreDatasource> findTypeNotIn(List<String> types);

    List<CoreDatasource> findByTaskStatus(String taskStatus);

    @Modifying
    @Transactional
    @Query("UPDATE CoreDatasource c SET c.taskStatus = :taskStatus WHERE c.id IN :ids")
    int updateTaskStatusByIds(List<Long> ids, String taskStatus);

    @Modifying
    @Transactional
    @Query("UPDATE CoreDatasource c SET c.taskStatus = :taskStatus WHERE c.id = :id AND c.taskStatus = :taskStatus")
    int updateTaskStatusByIds(Long id, String taskStatus);

    @Modifying
    @Transactional
    @Query("UPDATE CoreDatasource c SET c.status = :status WHERE c.id = :id")
    int updateStatusById(String status, Long id);

    @Modifying
    @Transactional
    @Query("UPDATE CoreDatasource c SET c.qrtzInstance = :qrtzInstance WHERE c.id = :id")
    int updateQrtzInstanceById(String qrtzInstance, Long id);

    @Modifying
    @Transactional
    @Query("UPDATE CoreDatasource c SET c.updateTime = :updateTime, c.pid = :pid, c.name = :name, c.updateBy = :updateBy WHERE c.id = :id")
    int move(Long id, Long updateTime, Long pid, String name, Long updateBy);
}

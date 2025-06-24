package io.dataease.operation.dao.auto.mapper;

import io.dataease.operation.dao.auto.entity.CoreOptRecent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface CoreOptRecentRepository extends JpaRepository<CoreOptRecent, Long>, JpaSpecificationExecutor<CoreOptRecent> {
    @Modifying
    @Query("UPDATE CoreOptRecent r SET r.optType = :optType, r.time = :time " +
            "WHERE (:resourceId IS NULL OR r.resourceId = :resourceId) " +
            "AND (:resourceName IS NULL OR r.resourceName = :resourceName) " +
            "AND r.resourceType = :resourceType " +
            "AND r.uid = :uid")
    int updateByParams(
            @Param("resourceId") Long resourceId,
            @Param("resourceName") String resourceName,
            @Param("resourceType") int resourceType,
            @Param("uid") Long uid,
            @Param("optType") int optType,
            @Param("time") Long time
    );

    List<CoreOptRecent> findByUid(Long uid);

}

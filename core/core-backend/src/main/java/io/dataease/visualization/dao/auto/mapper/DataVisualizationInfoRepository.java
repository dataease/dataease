package io.dataease.visualization.dao.auto.mapper;

import io.dataease.dao.auto.entity.DataVisualizationInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface DataVisualizationInfoRepository extends JpaRepository<DataVisualizationInfo, String>, JpaSpecificationExecutor<DataVisualizationInfo> {

    @Modifying
    @Transactional
    @Query("UPDATE DataVisualizationInfo dv SET dv.mobileLayout = 0")
    void updateMobileLayout();


    @Modifying
    @Transactional
    @Query("UPDATE DataVisualizationInfo dv SET dv.version = 2")
    void updateVersion();

    @Modifying
    @Transactional
    @Query("UPDATE DataVisualizationInfo dv SET dv.checkVersion = :checkVersion")
    void updateCheckVersion(String checkVersion);


    @Query("select dv.id from DataVisualizationInfo dv where dv.pid = :pid and dv.deleteFlag = true")
    List<Long> queryChildrenId(@Param("pid") Long pid);

    @Query("select dv.status from DataVisualizationInfo dv where dv.id = :dvId")
    Integer findDvInfoStats(@Param("dvId") Long dvId);

    @Query("SELECT d FROM DataVisualizationInfo d " +
            "WHERE d.deleteFlag = false AND d.id = :dvId " +
            "AND (:dvType IS NULL OR d.type = :dvType)")
    Optional<DataVisualizationInfo> findDvInfoEntity( Long dvId, String dvType);

    List<DataVisualizationInfo> findByDeleteFlagAndNodeTypeAndStatusNot(boolean deleteFlag, String nodeType,Integer status);
}

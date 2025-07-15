package io.dataease.visualization.dao.auto.mapper;

import io.dataease.dao.auto.entity.DataVisualizationInfo;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface DataVisualizationInfoRepository extends JpaRepository<DataVisualizationInfo, Long>, JpaSpecificationExecutor<DataVisualizationInfo> {

    @Transactional
    default void updateMobileLayout() {
        List<DataVisualizationInfo> dataVisualizationInfos = findAll();
        for (DataVisualizationInfo dv : dataVisualizationInfos) {
            dv.setMobileLayout((byte) 0L);
        }
        saveAllAndFlush(dataVisualizationInfos);
    }

    @Transactional
    default void updateVersion() {
        List<DataVisualizationInfo> dataVisualizationInfos = findAll();
        for (DataVisualizationInfo dv : dataVisualizationInfos) {
            dv.setVersion(2);
        }
        saveAllAndFlush(dataVisualizationInfos);
    }

    @Transactional
    default void updateCheckVersion(String checkVersion) {
        List<DataVisualizationInfo> dataVisualizationInfos = findAll();
        for (DataVisualizationInfo dv : dataVisualizationInfos) {
            dv.setCheckVersion(checkVersion);
        }
        saveAllAndFlush(dataVisualizationInfos);
    }


    default List<Long> queryChildrenId(@Param("pid") Long pid) {
        Specification<DataVisualizationInfo> spec = (root, query, cb) ->
                cb.and(cb.equal(root.get("pid"), pid), cb.equal(root.get("deleteFlag"), true));
        return findAll(spec).stream()
                .map(DataVisualizationInfo::getId)
                .toList();
    }

    default Integer findDvInfoStats(@Param("dvId") Long dvId) {
        return findById(dvId)
                .map(DataVisualizationInfo::getStatus)
                .orElse(null);
    }

    default Optional<DataVisualizationInfo> findDvInfoEntity(Long dvId, String dvType) {
        Specification<DataVisualizationInfo> spec = (root, query, cb) ->
                cb.and(
                        cb.equal(root.get("deleteFlag"), false),
                        cb.equal(root.get("id"), String.valueOf(dvId)),
                        dvType == null ? cb.conjunction() : cb.equal(root.get("type"), dvType)
                );
        return findOne(spec);
    }

    List<DataVisualizationInfo> findByDeleteFlagAndNodeTypeAndStatusNot(boolean deleteFlag, String nodeType, Integer status);
}

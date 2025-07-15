package io.dataease.map.dao.auto.mapper;

import io.dataease.map.dao.auto.entity.CoreCustomGeoSubArea;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface CoreCustomGeoSubAreaRepository extends JpaRepository<CoreCustomGeoSubArea, Long>, JpaSpecificationExecutor<CoreCustomGeoSubArea> {

    @Transactional
    default void deleteByGeoAreaId(String areaId) {
        Specification<CoreCustomGeoSubArea> spec = (root, query, cb) ->
                cb.equal(root.get("geoAreaId"), areaId);
        List<CoreCustomGeoSubArea> list = findAll(spec);
        if (!list.isEmpty()) {
            deleteAll(list);
        }
    }
}

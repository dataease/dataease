package io.dataease.map.dao.auto.mapper;

import io.dataease.map.dao.auto.entity.Area;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


public interface AreaRepository extends JpaRepository<Area, String>, JpaSpecificationExecutor<Area> {

    @Transactional
    default void updateArea(String oldId, String newId, String newName) {
        findById(oldId).ifPresent(area -> {
            Area newArea = new Area();
            // 复制除 id 外的其他属性
            newArea.setId(newId);
            newArea.setName(newName);
            BeanUtils.copyProperties(area, newArea, "id", "name");
            save(newArea);
            deleteById(oldId);
        });
    }

    @Transactional
    default void deleteByPidOrId(String areaId) {
        Specification<Area> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("pid"), areaId));
            predicates.add(cb.equal(root.get("id"), areaId));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
        List<Area> areas = findAll(spec);
        if (!areas.isEmpty()) {
            deleteAll(areas);
        }
    }

    @Transactional
    default void updateNameById(String id, String newName){
        Area area = findById(id).orElse(null);
        if (area != null) {
            area.setName(newName);
            save(area);
        }
    }

    @Transactional
    default void updatePid(String newPid, String oldPid){
        Specification<Area> spec = (root, query, cb) -> cb.equal(root.get("pid"), oldPid);
        List<Area> areas = findAll(spec);
        for (Area area : areas) {
            area.setPid(newPid);
            save(area);
        }
    }
}

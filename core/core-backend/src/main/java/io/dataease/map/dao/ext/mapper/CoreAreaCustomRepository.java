package io.dataease.map.dao.ext.mapper;

import io.dataease.map.dao.ext.entity.CoreAreaCustom;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface CoreAreaCustomRepository extends JpaRepository<CoreAreaCustom, String>, JpaSpecificationExecutor<CoreAreaCustom> {

    @Transactional
    default void deleteBatchIds(List<String> ids) {
        Specification<CoreAreaCustom> spec = (root, query, cb) ->
                root.get("id").in(ids);
        List<CoreAreaCustom> areaCustoms = findAll(spec);
        if (!areaCustoms.isEmpty()) {
            deleteAll(areaCustoms);
        }
    }
}

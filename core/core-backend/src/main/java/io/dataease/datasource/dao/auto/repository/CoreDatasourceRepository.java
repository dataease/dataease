package io.dataease.datasource.dao.auto.repository;

import io.dataease.dao.auto.entity.CoreDatasource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CoreDatasourceRepository extends JpaRepository<CoreDatasource, Long>, JpaSpecificationExecutor<CoreDatasource> {

    List<CoreDatasource> findByTaskStatus(String taskStatus);

    @Transactional
    default void updateTaskStatusByIds(List<Long> ids, String taskStatus) {
        Specification<CoreDatasource> spec = (root, query, cb) ->
                root.get("id").in(ids);
        findAll(spec).forEach(coreDatasource -> {
            coreDatasource.setTaskStatus(taskStatus);
            saveAndFlush(coreDatasource);
        });
    }

    @Transactional
    default void updateStatusById(String status, Long id) {
        Specification<CoreDatasource> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("id"), id);
        CoreDatasource datasource = findOne(spec).orElse(null);
        if (datasource != null) {
            datasource.setStatus(status);
            saveAndFlush(datasource);
        }
    }

    @Transactional
    default void updateQrtzInstanceById(String qrtzInstance, Long id) {
        Specification<CoreDatasource> spec = (root, query, cb) ->
                cb.equal(root.get("id"), id);
        CoreDatasource datasource = findOne(spec).orElse(null);
        if (datasource != null) {
            datasource.setQrtzInstance(qrtzInstance);
            saveAndFlush(datasource);
        }
    }

    @Transactional
    default void move(Long id, Long updateTime, Long pid, String name, Long updateBy) {
        Specification<CoreDatasource> spec = (root, query, cb) ->
                cb.equal(root.get("id"), id);
        CoreDatasource datasource = findOne(spec).orElse(null);
        if (datasource != null) {
            datasource.setUpdateTime(updateTime);
            datasource.setPid(pid);
            datasource.setName(name);
            datasource.setUpdateBy(updateBy);
            saveAndFlush(datasource);
        }
    }

    List<CoreDatasource> findByTypeNot(String type);
}

package io.dataease.datasource.dao.auto.repository;

import io.dataease.datasource.dao.auto.entity.CoreDatasourceTask;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CoreDatasourceTaskRepository extends JpaRepository<CoreDatasourceTask, Long>, JpaSpecificationExecutor<CoreDatasourceTask> {

    List<CoreDatasourceTask> findByDsId(Long dsId);

    void deleteByDsId(Long dsId);

    @Transactional
    default void updateTaskStatus(Long id, String taskStatus) {
        CoreDatasourceTask task = findById(id).orElse(null);
        if (task != null) {
            task.setTaskStatus(taskStatus);
            saveAndFlush(task);
        }
    }

    @Transactional
    default void updateTaskStatusAndLastExecTime(Long id, String taskStatus, Long lastExecTime) {
        Specification<CoreDatasourceTask> spec = (root, query, cb) ->
                cb.equal(root.get("id"), id);
        findAll(spec).stream().findFirst().ifPresent(task -> {
            task.setTaskStatus(taskStatus);
            task.setLastExecTime(lastExecTime);
            saveAndFlush(task);
        });
    }

    @Transactional
    default void updateTaskStatusByDsIds(List<Long> dsIds, String taskStatus) {
        Specification<CoreDatasourceTask> spec = (root, query, cb) ->
                root.get("dsId").in(dsIds);
        findAll(spec).forEach(task -> {
            task.setTaskStatus(taskStatus);
            saveAndFlush(task);
        });
    }

}

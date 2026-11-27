package io.dataease.exportCenter.dao.auto.mapper;

import io.dataease.dao.auto.entity.CoreExportTask;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface CoreExportTaskRepository extends JpaRepository<CoreExportTask, String>, JpaSpecificationExecutor<CoreExportTask> {

    default long countByUserIdAndExportStatus(Long userId, String exportStatus) {
        Specification<CoreExportTask> spec = (root, query, cb) ->
                cb.and(cb.equal(root.get("userId"), userId), cb.equal(root.get("exportStatus"), exportStatus));
        return count(spec);
    }

    default long countByUserId(Long userId) {
        Specification<CoreExportTask> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("userId"), userId);
        return count(spec);
    }

    default List<CoreExportTask> findByExportTimeLessThan(long threshold) {
        Specification<CoreExportTask> spec = (root, query, cb) ->
                cb.lessThan(root.get("exportTime"), threshold);
        return findAll(spec);
    }

    default List<CoreExportTask> findByExportStatusIn(List<String> statuses) {
        Specification<CoreExportTask> spec = (root, query, cb) -> root.get("exportStatus").in(statuses);
        return findAll(spec);
    }

    default List<CoreExportTask> findByCancelFlagTrue() {
        Specification<CoreExportTask> spec = (root, query, cb) -> cb.isTrue(root.get("cancelFlag"));
        return findAll(spec);
    }

    /** 失联节点上的未完成任务：按归属和旧状态原子转为失败可重试。 */
    @Modifying
    @Query("update CoreExportTask t set t.exportStatus = 'FAILED', t.exportProgress = '0', t.msg = :msg "
            + "where t.id = :id and t.exportMachineName = :machineName "
            + "and t.exportStatus in ('PENDING', 'IN_PROGRESS') "
            + "and (t.cancelFlag is null or t.cancelFlag = false)")
    int markExportTaskFailedIfOwned(@Param("id") String id,
                                    @Param("machineName") String machineName,
                                    @Param("msg") String msg);

    /** 失联节点上已请求取消的未完成任务：按归属和旧状态原子删除，由调用方清理导出目录。 */
    @Modifying
    @Query("delete from CoreExportTask t where t.id = :id and t.exportMachineName = :machineName "
            + "and t.exportStatus in ('PENDING', 'IN_PROGRESS') and t.cancelFlag = true")
    int deleteCancelRequestedIfOwned(@Param("id") String id,
                                     @Param("machineName") String machineName);

    @Transactional
    default void deleteByExportTimeLessThan(long threshold) {
        List<CoreExportTask> tasks = findByExportTimeLessThan(threshold);
        if (!tasks.isEmpty()) {
            deleteAll(tasks);
        }
    }
}

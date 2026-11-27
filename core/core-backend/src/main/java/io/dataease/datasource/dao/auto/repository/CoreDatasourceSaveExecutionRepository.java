package io.dataease.datasource.dao.auto.repository;

import io.dataease.datasource.dao.auto.entity.CoreDatasourceSaveExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoreDatasourceSaveExecutionRepository extends JpaRepository<CoreDatasourceSaveExecution, Long> {

    List<CoreDatasourceSaveExecution> findByDsIdOrderByCreateTimeDesc(Long dsId);

    List<CoreDatasourceSaveExecution> findByStatusAndLeaseTimeLessThan(String status, Long leaseTime);

    /** 仅当记录仍为 PENDING 且属于当前节点时更新步骤状态，并续约。 */
    @Modifying
    @Query("update CoreDatasourceSaveExecution e set e.step = :step, e.status = :status, e.result = :result, "
            + "e.updateTime = :updateTime, e.leaseTime = :leaseTime "
            + "where e.id = :id and e.status = 'PENDING' and e.nodeId = :nodeId")
    int markPendingExecution(@Param("id") Long id,
                             @Param("nodeId") String nodeId,
                             @Param("step") String step,
                             @Param("status") String status,
                             @Param("result") String result,
                             @Param("updateTime") Long updateTime,
                             @Param("leaseTime") Long leaseTime);

    /** 仅当记录仍为 PENDING 且属于当前节点时写失败，保留首个失败原因。 */
    @Modifying
    @Query("update CoreDatasourceSaveExecution e set e.step = :step, e.status = 'FAILED', e.result = :result, "
            + "e.updateTime = :updateTime "
            + "where e.id = :id and e.status = 'PENDING' and e.nodeId = :nodeId")
    int markPendingExecutionFailed(@Param("id") Long id,
                                   @Param("nodeId") String nodeId,
                                   @Param("step") String step,
                                   @Param("result") String result,
                                   @Param("updateTime") Long updateTime);

    /** 租约过期且仍为 PENDING 时，由启动恢复原子标记失败。 */
    @Modifying
    @Query("update CoreDatasourceSaveExecution e set e.step = 'RECOVERY', e.status = 'FAILED', e.result = :result, "
            + "e.updateTime = :updateTime "
            + "where e.id = :id and e.status = 'PENDING' and e.leaseTime < :now")
    int markExpiredPendingFailed(@Param("id") Long id,
                                 @Param("result") String result,
                                 @Param("updateTime") Long updateTime,
                                 @Param("now") Long now);
}

package io.dataease.copilot.dao.auto.mapper;

import io.dataease.copilot.dao.auto.entity.CoreCopilotMsg;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface CoreCopilotMsgRepository extends JpaRepository<CoreCopilotMsg, Long>, JpaSpecificationExecutor<CoreCopilotMsg> {

    @Query("SELECT c FROM CoreCopilotMsg c WHERE c.userId = :userId AND c.datasetGroupId = :datasetGroupId ORDER BY c.createTime ASC")
    List<CoreCopilotMsg> findByUserIdAndDatasetGroupIdOrderByCreateTimeAsc(Long userId, Long datasetGroupId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CoreCopilotMsg c WHERE c.userId = :userId AND c.datasetGroupId != :datasetGroupId")
    void deleteByUserIdAndNotDatasetGroupId(Long userId, Long datasetGroupId);

    @Transactional
    default void deleteByUserId(Long userId){
        Specification<CoreCopilotMsg> specification = (root, query, cb) -> cb.equal(root.get("userId"), userId);
        deleteAll(findAll(specification));
    }

    @Query("SELECT c FROM CoreCopilotMsg c WHERE c.userId = :userId ORDER BY c.createTime DESC")
    List<CoreCopilotMsg> findByUserIdOrderByCreateTimeDesc(Long userId);

    @Query("SELECT c FROM CoreCopilotMsg c WHERE c.userId = :userId AND c.datasetGroupId = :datasetGroupId AND c.msgStatus = 1 AND c.msgType = 'api' ORDER BY c.createTime DESC")
    List<CoreCopilotMsg> findLastSuccessMsg(Long userId, Long datasetGroupId);
}

package io.dataease.copilot.dao.auto.mapper;

import io.dataease.copilot.dao.auto.entity.CoreCopilotMsg;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface CoreCopilotMsgRepository extends JpaRepository<CoreCopilotMsg, Long>, JpaSpecificationExecutor<CoreCopilotMsg> {

    default List<CoreCopilotMsg> findByUserIdAndDatasetGroupIdOrderByCreateTimeAsc(Long userId, Long datasetGroupId) {
        Specification<CoreCopilotMsg> specification = (root, query, cb) ->
                cb.and(
                        cb.equal(root.get("userId"), userId),
                        cb.equal(root.get("datasetGroupId"), datasetGroupId)
                );
        return findAll(specification, Sort.by(Sort.Direction.ASC, "createTime"));
    }

    @Transactional
    default void deleteByUserIdAndNotDatasetGroupId(Long userId, Long datasetGroupId) {
        Specification<CoreCopilotMsg> specification = (root, query, cb) ->
                cb.and(
                        cb.equal(root.get("userId"), userId),
                        cb.notEqual(root.get("datasetGroupId"), datasetGroupId)
                );
        List<CoreCopilotMsg> messages = findAll(specification);
        if (!messages.isEmpty()) {
            deleteAll(messages);
        }
    }

    @Transactional
    default void deleteByUserId(Long userId) {
        Specification<CoreCopilotMsg> specification = (root, query, cb) -> cb.equal(root.get("userId"), userId);
        deleteAll(findAll(specification));
    }

    default List<CoreCopilotMsg> findByUserIdOrderByCreateTimeDesc(Long userId) {
        Specification<CoreCopilotMsg> specification = (root, query, cb) ->
                cb.equal(root.get("userId"), userId);
        return findAll(specification, Sort.by(Sort.Direction.DESC, "createTime"));
    }

    default List<CoreCopilotMsg> findLastSuccessMsg(Long userId, Long datasetGroupId) {
        Specification<CoreCopilotMsg> specification = (root, query, cb) ->
                cb.and(
                        cb.equal(root.get("userId"), userId),
                        cb.equal(root.get("datasetGroupId"), datasetGroupId),
                        cb.equal(root.get("msgStatus"), 1),
                        cb.equal(root.get("msgType"), "api")
                );
        return findAll(specification, Sort.by(Sort.Direction.DESC, "createTime"));
    }
}

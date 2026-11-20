package io.dataease.visualization.manage;

import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.api.visualization.request.VisualizationDraftRevision;
import io.dataease.dao.auto.entity.DataVisualizationInfo;
import io.dataease.exception.DEException;
import io.dataease.exception.ErrorCategory;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;
import io.dataease.visualization.dao.auto.mapper.SnapshotDataVisualizationInfoRepository;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

@Component
public class VisualizationDraftManage {
    @Resource
    private DataVisualizationInfoRepository dataVisualizationInfoRepository;
    @Resource
    private SnapshotDataVisualizationInfoRepository snapshotDataVisualizationInfoRepository;
    @Resource
    private CoreVisualizationManage coreVisualizationManage;

    public DataVisualizationInfo lock(Long id) {
        if (id == null) {
            throw DEException.of(ErrorCategory.VALIDATION, "i18n_error_invalid_parameter");
        }
        return dataVisualizationInfoRepository.lockForSave(id)
                .filter(info -> !Boolean.TRUE.equals(info.getDeleteFlag()))
                .orElseThrow(() -> DEException.of(ErrorCategory.NOT_FOUND, "i18n_error_not_found"));
    }

    public DataVisualizationInfo lockAndCheck(DataVisualizationBaseRequest request) {
        DataVisualizationInfo resource = lock(request.getId());
        checkRevision(resource, request.getCheckHistory(), request.getExpectedContentId());
        return resource;
    }

    public void lockForAssociation(Long id, VisualizationDraftRevision revision) {
        DataVisualizationInfo resource = lock(id);
        String nextContentId = revision == null ? UUID.randomUUID().toString() : revision.contentId();
        if (revision != null) {
            checkRevision(resource, revision.checkHistory(), revision.expectedContentId());
            if (StringUtils.isBlank(nextContentId) || nextContentId.length() > 50
                    || nextContentId.equals(resource.getContentId())) {
                throw DEException.of(ErrorCategory.VALIDATION, "i18n_error_invalid_parameter");
            }
        }

        // 与关联数据在同一事务中更新，草稿和资源行使用同一个修订号。
        resource.setContentId(nextContentId);
        snapshotDataVisualizationInfoRepository.findById(id)
                .filter(draft -> !Boolean.TRUE.equals(draft.getDeleteFlag()))
                .ifPresent(draft -> draft.setContentId(nextContentId));
    }

    private void checkRevision(DataVisualizationInfo resource, Boolean checkHistory, String expectedContentId) {
        if (Boolean.TRUE.equals(checkHistory) && !Objects.equals(resource.getContentId(), expectedContentId)) {
            throw DEException.of(ErrorCategory.CONFLICT, "i18n_error_conflict")
                    .setErrorData(Collections.singletonMap("currentContentId", resource.getContentId()));
        }
    }

    @Transactional
    public void initializeSnapshot(Long id) {
        lock(id);
        if (snapshotDataVisualizationInfoRepository.findSnapshotDvInfoEntity(id, null).isEmpty()) {
            coreVisualizationManage.dvSnapshotRecover(id);
        }
    }
}

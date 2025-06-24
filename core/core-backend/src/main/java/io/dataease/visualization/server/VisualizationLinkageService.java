package io.dataease.visualization.server;

import io.dataease.api.commons.BaseRspModel;
import io.dataease.api.visualization.VisualizationLinkageApi;
import io.dataease.api.visualization.dto.LinkageInfoDTO;
import io.dataease.api.visualization.dto.VisualizationLinkageDTO;
import io.dataease.api.visualization.request.VisualizationLinkageRequest;
import io.dataease.api.visualization.vo.VisualizationLinkageFieldVO;
import io.dataease.auth.DeLinkPermit;
import io.dataease.constant.CommonConstants;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.visualization.dao.auto.entity.*;
import io.dataease.visualization.dao.auto.mapper.*;
import io.dataease.visualization.manage.VisualizationLinkageManage;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : WangJiaHao
 * @date : 2023/7/13
 */
@RestController
@RequestMapping("linkage")
public class VisualizationLinkageService implements VisualizationLinkageApi {

    @Resource
    private SnapshotVisualizationLinkageFieldRepository snapshotVisualizationLinkageFieldRepository;

    @Resource
    private SnapshotVisualizationLinkageRepository snapshotVisualizationLinkageRepository;

    @Resource
    private SnapshotCoreChartViewRepository snapshotCoreChartViewRepository;
    @Resource
    private VisualizationLinkageManage visualizationLinkageManage;

    @Override
    public Map<String, VisualizationLinkageDTO> getViewLinkageGather(VisualizationLinkageRequest request) {
        if (CollectionUtils.isNotEmpty(request.getTargetViewIds())) {
            List<VisualizationLinkageDTO> linkageDTOList = null;
            if (CommonConstants.RESOURCE_TABLE.SNAPSHOT.equals(request.getResourceTable())) {
                linkageDTOList = visualizationLinkageManage.getViewLinkageGatherSnapshot(request.getDvId(), request.getSourceViewId(), request.getTargetViewIds());
            } else {
                linkageDTOList = visualizationLinkageManage.getViewLinkageGather(request.getDvId(), request.getSourceViewId(), request.getTargetViewIds());
            }
            return linkageDTOList.stream().collect(Collectors.toMap(targetViewId -> String.valueOf(targetViewId), PanelViewLinkageDTO -> PanelViewLinkageDTO));
        }
        return new HashMap<>();
    }

    @Override
    public List<VisualizationLinkageDTO> getViewLinkageGatherArray(VisualizationLinkageRequest request) {
        if (CommonConstants.RESOURCE_TABLE.SNAPSHOT.equals(request.getResourceTable())) {
            return visualizationLinkageManage.getViewLinkageGatherSnapshot(request.getDvId(), request.getSourceViewId(), request.getTargetViewIds());
        } else {
            return visualizationLinkageManage.getViewLinkageGather(request.getDvId(), request.getSourceViewId(), request.getTargetViewIds());
        }
    }

    @Override
    @Transactional
    public BaseRspModel saveLinkage(VisualizationLinkageRequest request) {
        // 向镜像中保存
        Long updateTime = System.currentTimeMillis();
        List<VisualizationLinkageDTO> linkageInfo = request.getLinkageInfo();
        Long sourceViewId = request.getSourceViewId();
        Long dvId = request.getDvId();

        Assert.notNull(sourceViewId, "source View ID can not be null");
        Assert.notNull(dvId, "dvId can not be null");

        // 清理原有关系
        snapshotVisualizationLinkageRepository.deleteViewLinkageFieldSnapshot(dvId, sourceViewId);
        snapshotVisualizationLinkageRepository.deleteViewLinkageSnapshot(dvId, sourceViewId);

        //重新建立关系
        for (VisualizationLinkageDTO linkageDTO : linkageInfo) {
            //去掉source view 的信息
            if (sourceViewId.equals(linkageDTO.getTargetViewId())) {
                continue;
            }
            List<VisualizationLinkageFieldVO> linkageFields = linkageDTO.getLinkageFields();
            Long linkageId = IDUtils.snowID();
            SnapshotVisualizationLinkage linkage = new SnapshotVisualizationLinkage();
            linkage.setId(linkageId);
            linkage.setDvId(dvId);
            linkage.setSourceViewId(sourceViewId);
            linkage.setTargetViewId(linkageDTO.getTargetViewId());
            linkage.setUpdatePeople("");
            linkage.setUpdateTime(updateTime);
            linkage.setLinkageActive(linkageDTO.getLinkageActive());
            snapshotVisualizationLinkageRepository.saveAndFlush(linkage);
            if (CollectionUtils.isNotEmpty(linkageFields) && linkageDTO.getLinkageActive()) {
                linkageFields.forEach(linkageField -> {
                    linkageField.setId(IDUtils.snowID());
                    linkageField.setLinkageId(linkageId);
                    linkageField.setUpdateTime(updateTime);
                    SnapshotVisualizationLinkageField fieldInsert = new SnapshotVisualizationLinkageField();
                    snapshotVisualizationLinkageFieldRepository.saveAndFlush(BeanUtils.copyBean(fieldInsert, linkageField));
                });
            }
        }
        return new BaseRspModel();
    }

    @DeLinkPermit
    @Override
    public Map<String, List<String>> getVisualizationAllLinkageInfo(Long dvId, String resourceTable) {
        List<LinkageInfoDTO> info = null;
        if (CommonConstants.RESOURCE_TABLE.SNAPSHOT.equals(resourceTable)) {
            info = visualizationLinkageManage.getPanelAllLinkageInfoSnapshot(dvId);
        } else {
            info = visualizationLinkageManage.getPanelAllLinkageInfo(dvId);
        }
        return Optional.ofNullable(info).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(LinkageInfoDTO::getSourceInfo, LinkageInfoDTO::getTargetInfoList));
    }

    @Override
    public Map updateLinkageActive(VisualizationLinkageRequest request) {
        SnapshotCoreChartView coreChartView = new SnapshotCoreChartView();
        coreChartView.setId(request.getSourceViewId());
        coreChartView.setLinkageActive(request.getActiveStatus());
        snapshotCoreChartViewRepository.saveAndFlush(coreChartView);
        return getVisualizationAllLinkageInfo(request.getDvId(), CommonConstants.RESOURCE_TABLE.SNAPSHOT);
    }

    @Override
    public void removeLinkage(VisualizationLinkageRequest request) {
        // 清理原有关系
        snapshotVisualizationLinkageRepository.deleteViewLinkageFieldSnapshot(request.getDvId(), request.getSourceViewId());
        snapshotVisualizationLinkageRepository.deleteViewLinkageSnapshot(request.getDvId(), request.getSourceViewId());
    }
}

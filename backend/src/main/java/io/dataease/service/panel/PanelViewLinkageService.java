package io.dataease.service.panel;

import io.dataease.ext.ExtPanelViewLinkageMapper;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.controller.request.panel.PanelLinkageRequest;
import io.dataease.dto.LinkageInfoDTO;
import io.dataease.dto.PanelViewLinkageDTO;
import io.dataease.plugins.common.base.domain.PanelGroupWithBLOBs;
import io.dataease.plugins.common.base.domain.PanelViewLinkage;
import io.dataease.plugins.common.base.domain.PanelViewLinkageField;
import io.dataease.plugins.common.base.mapper.PanelGroupMapper;
import io.dataease.plugins.common.base.mapper.PanelViewLinkageFieldMapper;
import io.dataease.plugins.common.base.mapper.PanelViewLinkageMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: wangjiahao
 * Date: 8/4/21
 * Description:
 */
@Service
public class PanelViewLinkageService {

    @Resource
    private PanelViewLinkageMapper panelViewLinkageMapper;

    @Resource
    private PanelViewLinkageFieldMapper panelViewLinkageFieldMapper;

    @Resource
    private ExtPanelViewLinkageMapper extPanelViewLinkageMapper;

    @Resource
    private PanelGroupMapper panelGroupMapper;


    public Map<String, PanelViewLinkageDTO> getViewLinkageGather(PanelLinkageRequest request) {
        if (CollectionUtils.isNotEmpty(request.getTargetViewIds())) {
            List<PanelViewLinkageDTO> linkageDTOList = extPanelViewLinkageMapper.getViewLinkageGather(request.getPanelId(), request.getSourceViewId(), request.getTargetViewIds());
            return linkageDTOList.stream().collect(Collectors.toMap(PanelViewLinkageDTO::getTargetViewId, PanelViewLinkageDTO -> PanelViewLinkageDTO));
        }
        return new HashMap<>();
    }

    @Transactional
    public void saveLinkage(PanelLinkageRequest request) {
        Long updateTime = System.currentTimeMillis();
        Map<String, PanelViewLinkageDTO> linkageInfo = request.getLinkageInfo();
        String sourceViewId = request.getSourceViewId();
        String panelId = request.getPanelId();

        Assert.notNull(sourceViewId, "source View ID can not be null");
        Assert.notNull(panelId, "panelId can not be null");

        //去掉source view 的信息
        linkageInfo.remove(sourceViewId);

        // 清理原有关系
        extPanelViewLinkageMapper.deleteViewLinkageField(panelId, sourceViewId);
        extPanelViewLinkageMapper.deleteViewLinkage(panelId, sourceViewId);

        //重新建立关系
        for (Map.Entry<String, PanelViewLinkageDTO> entry : linkageInfo.entrySet()) {
            String targetViewId = entry.getKey();
            PanelViewLinkageDTO linkageDTO = entry.getValue();
            List<PanelViewLinkageField> linkageFields = linkageDTO.getLinkageFields();
            String linkageId = UUID.randomUUID().toString();
            PanelViewLinkage linkage = new PanelViewLinkage();
            linkage.setId(linkageId);
            linkage.setPanelId(panelId);
            linkage.setSourceViewId(sourceViewId);
            linkage.setTargetViewId(targetViewId);
            linkage.setUpdatePeople(AuthUtils.getUser().getUsername());
            linkage.setUpdateTime(updateTime);
            linkage.setLinkageActive(linkageDTO.getLinkageActive());
            panelViewLinkageMapper.insert(linkage);
            if (CollectionUtils.isNotEmpty(linkageFields) && linkageDTO.getLinkageActive()) {
                linkageFields.forEach(linkageField -> {
                    linkageField.setId(UUID.randomUUID().toString());
                    linkageField.setLinkageId(linkageId);
                    linkageField.setUpdateTime(updateTime);
                    panelViewLinkageFieldMapper.insert(linkageField);
                });

            }
        }
    }

    public Map<String, List<String>> getPanelAllLinkageInfo(String panelId) {
        PanelGroupWithBLOBs panelInfo = panelGroupMapper.selectByPrimaryKey(panelId);
        if (panelInfo != null && StringUtils.isNotEmpty(panelInfo.getSource())) {
            panelId = panelInfo.getSource();
        }
        List<LinkageInfoDTO> info = extPanelViewLinkageMapper.getPanelAllLinkageInfo(panelId);
        return Optional.ofNullable(info).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(LinkageInfoDTO::getSourceInfo, LinkageInfoDTO::getTargetInfoList));
    }

}

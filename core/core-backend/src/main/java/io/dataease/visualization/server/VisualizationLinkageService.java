package io.dataease.visualization.server;

import io.dataease.api.commons.BaseRspModel;
import io.dataease.api.visualization.VisualizationLinkageApi;
import io.dataease.api.visualization.dto.LinkageInfoDTO;
import io.dataease.api.visualization.dto.VisualizationLinkageDTO;
import io.dataease.api.visualization.request.VisualizationLinkageRequest;
import io.dataease.api.visualization.vo.VisualizationLinkageFieldVO;
import io.dataease.utils.BeanUtils;
import io.dataease.visualization.dao.auto.entity.VisualizationLinkage;
import io.dataease.visualization.dao.auto.entity.VisualizationLinkageField;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoMapper;
import io.dataease.visualization.dao.auto.mapper.VisualizationLinkageFieldMapper;
import io.dataease.visualization.dao.auto.mapper.VisualizationLinkageMapper;
import io.dataease.visualization.dao.ext.mapper.ExtVisualizationLinkageMapper;
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
    private ExtVisualizationLinkageMapper extVisualizationLinkageMapper;

    @Resource
    private VisualizationLinkageFieldMapper visualizationLinkageFieldMapper;

    @Resource
    private VisualizationLinkageMapper visualizationLinkageMapper;

    @Resource
    private DataVisualizationInfoMapper dataVisualizationInfoMapper;

    @Override
    public Map<String, VisualizationLinkageDTO> getViewLinkageGather(VisualizationLinkageRequest request) {
        if (CollectionUtils.isNotEmpty(request.getTargetViewIds())) {
            List<VisualizationLinkageDTO> linkageDTOList = extVisualizationLinkageMapper.getViewLinkageGather(request.getDvId(), request.getSourceViewId(), request.getTargetViewIds());
            return linkageDTOList.stream().collect(Collectors.toMap(VisualizationLinkageDTO::getTargetViewId, PanelViewLinkageDTO -> PanelViewLinkageDTO));
        }
        return new HashMap<>();
    }

    @Override
    public List<VisualizationLinkageDTO> getViewLinkageGatherArray(VisualizationLinkageRequest request) {
        List<VisualizationLinkageDTO> linkageDTOList = extVisualizationLinkageMapper.getViewLinkageGather(request.getDvId(), request.getSourceViewId(), request.getTargetViewIds());
        return linkageDTOList;
    }

    @Override
    @Transactional
    public BaseRspModel saveLinkage(VisualizationLinkageRequest request) {
        Long updateTime = System.currentTimeMillis();
        List<VisualizationLinkageDTO> linkageInfo =  request.getLinkageInfo();;
        String sourceViewId = request.getSourceViewId();
        String dvId = request.getDvId();

        Assert.notNull(sourceViewId, "source View ID can not be null");
        Assert.notNull(dvId, "dvId can not be null");

        // 清理原有关系
        extVisualizationLinkageMapper.deleteViewLinkageField(dvId, sourceViewId);
        extVisualizationLinkageMapper.deleteViewLinkage(dvId, sourceViewId);

        //重新建立关系
        for (VisualizationLinkageDTO linkageDTO:linkageInfo) {
            //去掉source view 的信息
            if(sourceViewId.equals(linkageDTO.getTargetViewId())){
                continue;
            }
            List<VisualizationLinkageFieldVO> linkageFields = linkageDTO.getLinkageFields();
            String linkageId = UUID.randomUUID().toString();
            VisualizationLinkage linkage = new VisualizationLinkage();
            linkage.setId(linkageId);
            linkage.setDvId(dvId);
            linkage.setSourceViewId(sourceViewId);
            linkage.setTargetViewId(linkageDTO.getTargetViewId());
            linkage.setUpdatePeople("");
            linkage.setUpdateTime(updateTime);
            linkage.setLinkageActive(linkageDTO.getLinkageActive());
            visualizationLinkageMapper.insert(linkage);
            if (CollectionUtils.isNotEmpty(linkageFields) && linkageDTO.getLinkageActive()) {
                linkageFields.forEach(linkageField -> {
                    linkageField.setId(UUID.randomUUID().toString());
                    linkageField.setLinkageId(linkageId);
                    linkageField.setUpdateTime(updateTime);
                    VisualizationLinkageField fieldInsert = new VisualizationLinkageField();
                    visualizationLinkageFieldMapper.insert(BeanUtils.copyBean(fieldInsert,linkageField));
                });
            }
        }
        return new BaseRspModel();
    }

    @Override
    public Map<String, List<String>> getVisualizationAllLinkageInfo(String dvId) {
        List<LinkageInfoDTO> info = extVisualizationLinkageMapper.getPanelAllLinkageInfo(dvId);
        return Optional.ofNullable(info).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(LinkageInfoDTO::getSourceInfo, LinkageInfoDTO::getTargetInfoList));
    }
}

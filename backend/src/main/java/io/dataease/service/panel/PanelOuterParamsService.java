package io.dataease.service.panel;

import io.dataease.base.mapper.PanelOuterParamsInfoMapper;
import io.dataease.base.mapper.PanelOuterParamsMapper;
import io.dataease.base.mapper.PanelOuterParamsTargetViewInfoMapper;
import io.dataease.base.mapper.ext.ExtPanelOuterParamsMapper;
import io.dataease.dto.panel.linkJump.PanelLinkJumpDTO;
import io.dataease.dto.panel.outerParams.PanelOuterParamsBaseResponse;
import io.dataease.dto.panel.outerParams.PanelOuterParamsDTO;
import io.dataease.dto.panel.outerParams.PanelOuterParamsInfoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Author: wangjiahao
 * Date: 2022/3/17
 * Description:
 */
@Service
public class PanelOuterParamsService {

    @Resource
    private ExtPanelOuterParamsMapper extPanelOuterParamsMapper;

    @Resource
    private PanelOuterParamsMapper panelOuterParamsMapper;

    @Resource
    private PanelOuterParamsInfoMapper panelOuterParamsInfoMapper;

    @Resource
    private PanelOuterParamsTargetViewInfoMapper panelOuterParamsTargetViewInfoMapper;


    public PanelOuterParamsDTO queryWithPanelId(String panelId){
        PanelOuterParamsDTO panelOuterParamsDTO =  extPanelOuterParamsMapper.queryWithPanelId(panelId);
        return panelOuterParamsDTO;
    }

    @Transactional
    public void updateOuterParamsSet(PanelOuterParamsDTO outerParamsDTO) {
        String panelId = outerParamsDTO.getPanelId();
        Assert.notNull(panelId, "panelId cannot be null");
        //清理原有数据
        extPanelOuterParamsMapper.deleteOuterParamsTargetWithPanelId(panelId);
        extPanelOuterParamsMapper.deleteOuterParamsInfoWithPanelId(panelId);
        extPanelOuterParamsMapper.deleteOuterParamsWithPanelId(panelId);
        // 插入新的数据
        String paramsId = UUID.randomUUID().toString();
        outerParamsDTO.setParamsId(paramsId);
        panelOuterParamsMapper.insertSelective(outerParamsDTO);
        Optional.ofNullable(outerParamsDTO.getOuterParamsInfoArray()).orElse(new ArrayList<>()).forEach(outerParamsInfo -> {
            String paramsInfoId = UUID.randomUUID().toString();
            outerParamsInfo.setParamsInfoId(paramsInfoId);
            outerParamsInfo.setParamsId(paramsId);
            panelOuterParamsInfoMapper.insertSelective(outerParamsInfo);
            Optional.ofNullable(outerParamsInfo.getTargetViewInfoList()).orElse(new ArrayList<>()).forEach(targetViewInfo -> {
                String targetViewInfoId = UUID.randomUUID().toString();
                targetViewInfo.setTargetId(targetViewInfoId);
                targetViewInfo.setParamsInfoId(paramsInfoId);
                panelOuterParamsTargetViewInfoMapper.insertSelective(targetViewInfo);
            });
        });
    }

    public PanelOuterParamsBaseResponse getOuterParamsInfo(String panelId){
        List<PanelOuterParamsInfoDTO>  result = extPanelOuterParamsMapper.getPanelOuterParamsInfo(panelId);
        return new PanelOuterParamsBaseResponse(Optional.ofNullable(result).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(PanelOuterParamsInfoDTO::getSourceInfo, PanelOuterParamsInfoDTO::getTargetInfoList)));
    }

}

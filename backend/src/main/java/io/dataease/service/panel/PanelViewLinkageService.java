package io.dataease.service.panel;

import io.dataease.base.mapper.PanelViewLinkageMapper;
import io.dataease.base.mapper.ext.ExtPanelViewLinkageMapper;
import io.dataease.controller.request.panel.PanelLinkageRequest;
import io.dataease.dto.PanelViewLinkageDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

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
    private ExtPanelViewLinkageMapper extPanelViewLinkageMapper;


    public Map<String, PanelViewLinkageDTO> getViewLinkageGather(PanelLinkageRequest request) {
        if(CollectionUtils.isNotEmpty(request.getTargetViewIds())){
            Map<String, PanelViewLinkageDTO> result = Optional.ofNullable(extPanelViewLinkageMapper.getViewLinkageGather(request.getPanelId(),request.getSourceViewId(),request.getTargetViewIds()))
                    .orElse(new ArrayList<>()).stream()
                    .collect(Collectors.toMap(PanelViewLinkageDTO::getTargetViewId,PanelViewLinkageDTO->PanelViewLinkageDTO));
            return result;
        }
        return new HashMap<>();
    }


}

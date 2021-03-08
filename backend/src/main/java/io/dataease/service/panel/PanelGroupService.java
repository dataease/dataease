package io.dataease.service.panel;

import io.dataease.base.domain.DatasetGroup;
import io.dataease.base.mapper.PanelGroupMapper;
import io.dataease.base.mapper.ext.ExtPanelGroupMapper;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.request.panel.PanelGroupRequest;
import io.dataease.dto.dataset.DataSetGroupDTO;
import io.dataease.dto.panel.PanelGroupDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
@Service
public class PanelGroupService {

    @Resource
    private PanelGroupMapper panelGroupMapper;
    @Resource
    private ExtPanelGroupMapper extPanelGroupMapper;

    public List<PanelGroupDTO> tree(PanelGroupRequest panelGroupRequest) {
        List<PanelGroupDTO> panelGroupDTOList = extPanelGroupMapper.panelGroupList(panelGroupRequest);
        getTreeChildren(panelGroupDTOList);
        return panelGroupDTOList;
    }

    public void getTreeChildren(List<PanelGroupDTO> parentPanelGroupDTO){
        Optional.ofNullable(parentPanelGroupDTO).ifPresent(parent -> parent.forEach(panelGroupDTO -> {
            List<PanelGroupDTO> panelGroupDTOChildren = extPanelGroupMapper.panelGroupList(new PanelGroupRequest(panelGroupDTO.getId()));
            panelGroupDTO.setChildren(panelGroupDTOChildren);
            getTreeChildren(panelGroupDTOChildren);
        }));
    }

    public List<PanelGroupDTO> getDefaultTree(PanelGroupRequest panelGroupRequest){
        return extPanelGroupMapper.panelGroupList(panelGroupRequest);
    }


    public PanelGroupDTO save(PanelGroupRequest request) {
        if (StringUtils.isEmpty(request.getId())) {
            request.setId(UUID.randomUUID().toString());
            request.setCreateTime(System.currentTimeMillis());
            panelGroupMapper.insert(request);
        } else {
            panelGroupMapper.updateByPrimaryKey(request);
        }
        PanelGroupDTO panelGroupDTO = new PanelGroupDTO();
        BeanUtils.copyBean(panelGroupDTO, request);
        panelGroupDTO.setLabel(request.getName());
        return panelGroupDTO;
    }


    public void deleteCircle(String id){
        Assert.notNull(id, "id cannot be null");
        extPanelGroupMapper.deleteCircle(id);
    }


}

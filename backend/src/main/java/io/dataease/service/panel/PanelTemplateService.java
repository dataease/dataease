package io.dataease.service.panel;

import io.dataease.base.domain.*;
import io.dataease.base.mapper.PanelTemplateMapper;
import io.dataease.base.mapper.ext.ExtPanelTemplateMapper;
import io.dataease.commons.constants.PanelConstants;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.request.panel.PanelTemplateRequest;
import io.dataease.dto.panel.PanelTemplateDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class PanelTemplateService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Resource
    private PanelTemplateMapper panelTemplateMapper;
    @Resource
    private ExtPanelTemplateMapper extPanelTemplateMapper;

    public List<PanelTemplateDTO> templateList(PanelTemplateRequest panelTemplateRequest) {
        List<PanelTemplateDTO> panelTemplateList = extPanelTemplateMapper.panelTemplateList(panelTemplateRequest);
        if(panelTemplateRequest.getWithChildren()){
            getTreeChildren(panelTemplateList);
        }
        return panelTemplateList;
    }

    public void getTreeChildren(List<PanelTemplateDTO> parentPanelTemplateDTO){
        Optional.ofNullable(parentPanelTemplateDTO).ifPresent(parent -> parent.forEach(panelTemplateDTO -> {
            List<PanelTemplateDTO> panelTemplateDTOChildren = extPanelTemplateMapper.panelTemplateList(new PanelTemplateRequest(panelTemplateDTO.getId()));
            panelTemplateDTO.setChildren(panelTemplateDTOChildren);
            getTreeChildren(panelTemplateDTOChildren);
        }));
    }

    public List<PanelTemplateDTO> getSystemTemplateType(PanelTemplateRequest panelTemplateRequest){
        return extPanelTemplateMapper.panelTemplateList(panelTemplateRequest);
    }


    public PanelTemplateDTO save(PanelTemplateRequest request) {
        if (StringUtils.isEmpty(request.getId())) {
            //如果level 是0（第一级）设置父级为对应的templateType
            if(request.getLevel()==0){
                request.setPid(request.getTemplateType());
            }
            request.setId(UUID.randomUUID().toString());
            request.setCreateTime(System.currentTimeMillis());
            panelTemplateMapper.insert(request);
        } else {
            panelTemplateMapper.updateByPrimaryKeySelective(request);
        }
        PanelTemplateDTO panelTemplateDTO = new PanelTemplateDTO();
        BeanUtils.copyBean(panelTemplateDTO, request);
        panelTemplateDTO.setLabel(request.getName());
        return panelTemplateDTO;
    }


    public void delete(String id){
        Assert.notNull(id, "id cannot be null");
        panelTemplateMapper.deleteByPrimaryKey(id);
    }


    public PanelTemplateWithBLOBs findOne(String panelId){
       return panelTemplateMapper.selectByPrimaryKey(panelId);
    }

    public List<PanelTemplateDTO> find(PanelTemplateRequest panelTemplateRequest){
        List<PanelTemplateDTO> panelTemplateList = extPanelTemplateMapper.panelTemplateList(panelTemplateRequest);
        return panelTemplateList;
    }

}

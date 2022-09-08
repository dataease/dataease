package io.dataease.service.panel;

import io.dataease.commons.constants.CommonConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.request.panel.PanelAppTemplateRequest;
import io.dataease.controller.request.panel.PanelTemplateRequest;
import io.dataease.dto.panel.PanelAppTemplateDTO;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.PanelAppTemplateMapper;
import org.pentaho.di.core.util.UUIDUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2022/9/8
 * Description:
 */
@Service
public class PanelAppTemplateService {

    @Resource
    private PanelAppTemplateMapper panelAppTemplateMapper;

    public List<PanelAppTemplateWithBLOBs> list(PanelAppTemplateRequest request){
        PanelAppTemplateExample example = new PanelAppTemplateExample();
        example.createCriteria().andPidEqualTo(request.getPid());
        return panelAppTemplateMapper.selectByExampleWithBLOBs(example);
    }

    public void save(PanelAppTemplateRequest request){
        request.setId(UUIDUtil.getUUIDAsString());
        request.setCreateUser(AuthUtils.getUser().getUsername());
        request.setCreateTime(System.currentTimeMillis());
        PanelAppTemplateWithBLOBs requestTemplate = new  PanelAppTemplateWithBLOBs();
        BeanUtils.copyBean(requestTemplate,request);
        panelAppTemplateMapper.insertSelective(requestTemplate);
    }


    public void update(PanelAppTemplateRequest request){
        request.setUpdateUser(AuthUtils.getUser().getUsername());
        request.setUpdateTime(System.currentTimeMillis());
        PanelAppTemplateWithBLOBs requestTemplate = new  PanelAppTemplateWithBLOBs();
        BeanUtils.copyBean(requestTemplate,request);
        panelAppTemplateMapper.updateByPrimaryKeySelective(requestTemplate);
    }

    public void delete(String templateAppId){
        panelAppTemplateMapper.deleteByPrimaryKey(templateAppId);
    }

    public String nameCheck(PanelAppTemplateRequest request) {
        return nameCheck(request.getOptType(), request.getName(), request.getPid(), request.getId());

    }

    //名称检查
    public String nameCheck(String optType, String name, String pid, String id) {
        PanelAppTemplateExample example = new PanelAppTemplateExample();
        if (CommonConstants.OPT_TYPE.INSERT.equals(optType)) {
            example.createCriteria().andPidEqualTo(pid).andNameEqualTo(name);

        } else if (CommonConstants.OPT_TYPE.UPDATE.equals(optType)) {
            example.createCriteria().andPidEqualTo(pid).andNameEqualTo(name).andIdNotEqualTo(id);
        }
        List<PanelAppTemplate> panelTemplates = panelAppTemplateMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(panelTemplates)) {
            return CommonConstants.CHECK_RESULT.NONE;
        } else {
            return CommonConstants.CHECK_RESULT.EXIST_ALL;
        }
    }


}

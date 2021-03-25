package io.dataease.service.panel;

import io.dataease.base.domain.PanelLink;
import io.dataease.base.mapper.PanelLinkMapper;
import io.dataease.controller.request.panel.link.LinkRequest;
import io.dataease.controller.request.panel.link.PasswordRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

@Service
public class PanelLinkService {

    @Resource
    private PanelLinkMapper mapper;

    public void generator(LinkRequest request){
        String resourceId = request.getResourceId();
        PanelLink panelLink = mapper.selectByPrimaryKey(resourceId);
        PanelLink po = new PanelLink();
        po.setResourceId(resourceId);
        po.setEnablePwd(request.getEnablePwd());
        po.setPwd(request.getPassword());
        if (ObjectUtils.isEmpty(panelLink)){
            mapper.insert(po);
        }else{
            mapper.updateByPrimaryKey(po);
        }
    }

    public void password(PasswordRequest request){
        PanelLink po = new PanelLink();
        po.setResourceId(request.getResourceId());
        po.setPwd(request.getPassword());
        mapper.updateByPrimaryKeySelective(po);
    }

    public PanelLink findOne(String resourceId){
        PanelLink panelLink = mapper.selectByPrimaryKey(resourceId);
        return panelLink;
    }




}

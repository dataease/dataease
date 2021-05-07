package io.dataease.service.panel;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.base.domain.PanelSubject;
import io.dataease.base.domain.PanelSubjectExample;
import io.dataease.base.domain.PanelTemplateWithBLOBs;
import io.dataease.base.mapper.PanelSubjectMapper;
import io.dataease.base.mapper.PanelTemplateMapper;
import io.dataease.base.mapper.ext.ExtPanelTemplateMapper;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.request.panel.PanelSubjectRequest;
import io.dataease.controller.request.panel.PanelTemplateRequest;
import io.dataease.dto.panel.PanelTemplateDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Author: wangjiahao
 * Date: 2021-05-06
 * Description:
 */
@Service
public class PanelSubjectService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Resource
    private PanelSubjectMapper panelSubjectMapper;

    public List<PanelSubject> query(PanelSubjectRequest request){
        PanelSubjectExample example = new PanelSubjectExample();
        return panelSubjectMapper.selectByExampleWithBLOBs(null);
    }

    public List querySubjectWithGroup(PanelSubjectRequest request){
        List result = new ArrayList();
        int pageSize = 4;
        List<PanelSubject> allInfo  = panelSubjectMapper.selectByExampleWithBLOBs(null);
        for(int i =0;i<allInfo.size();i=i+pageSize){
            List<PanelSubject> tmp = allInfo.subList(i,i+pageSize<allInfo.size()?i+pageSize:allInfo.size());
            result.add(tmp);
        }
        return result;
    }

    public void update(PanelSubject request){
        if(StringUtils.isEmpty(request.getId())){
            request.setId(UUID.randomUUID().toString());
            request.setCreateTime(System.currentTimeMillis());
            request.setType("self");
            request.setName("个人主题");
            panelSubjectMapper.insertSelective(request);
        }else{
            request.setUpdateTime(System.currentTimeMillis());
            panelSubjectMapper.updateByPrimaryKeySelective(request);
        }
    }

    public void delete(String id){
        Assert.notNull(id,"subjectId should not be null");
        panelSubjectMapper.deleteByPrimaryKey(id);
    }



}

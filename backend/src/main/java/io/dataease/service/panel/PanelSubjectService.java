package io.dataease.service.panel;

import io.dataease.controller.request.panel.PanelSubjectRequest;
import io.dataease.exception.DataEaseException;
import io.dataease.plugins.common.base.domain.PanelSubject;
import io.dataease.plugins.common.base.domain.PanelSubjectExample;
import io.dataease.plugins.common.base.mapper.PanelSubjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Author: wangjiahao
 * Date: 2021-05-06
 * Description:
 */
@Service
public class PanelSubjectService {

    @Resource
    private PanelSubjectMapper panelSubjectMapper;

    public List<PanelSubject> query(PanelSubjectRequest request) {
        PanelSubjectExample example = new PanelSubjectExample();
        example.setOrderByClause("create_time asc");
        return panelSubjectMapper.selectByExampleWithBLOBs(example);
    }

    public List querySubjectWithGroup(PanelSubjectRequest request) {
        List result = new ArrayList();
        int pageSize = 4;
        PanelSubjectExample example = new PanelSubjectExample();
        example.setOrderByClause("create_time asc");
        List<PanelSubject> allInfo = panelSubjectMapper.selectByExampleWithBLOBs(example);
        for (int i = 0; i < allInfo.size(); i = i + pageSize) {
            List<PanelSubject> tmp = allInfo.subList(i, Math.min(i + pageSize, allInfo.size()));
            result.add(tmp);
        }
        return result;
    }

    public synchronized void update(PanelSubject request) {
        if (StringUtils.isEmpty(request.getId())) {
            PanelSubjectExample example = new PanelSubjectExample();
            example.createCriteria().andTypeEqualTo("self");
            List<PanelSubject> subjectAll = panelSubjectMapper.selectByExample(example);
            int count = CollectionUtils.isEmpty(subjectAll) ? 0 : subjectAll.size();
            request.setId(UUID.randomUUID().toString());
            request.setCreateTime(System.currentTimeMillis());
            request.setType("self");
            request.setName("个人主题" + count);
            panelSubjectMapper.insertSelective(request);
        } else {
            PanelSubjectExample example = new PanelSubjectExample();
            example.createCriteria().andNameEqualTo(request.getName()).andIdNotEqualTo(request.getId());
            List<PanelSubject> subjectAll = panelSubjectMapper.selectByExample(example);
            if (CollectionUtils.isEmpty(subjectAll)) {
                request.setUpdateTime(System.currentTimeMillis());
                panelSubjectMapper.updateByPrimaryKeySelective(request);
            } else {
                DataEaseException.throwException("名称已经存在");
            }
        }
    }

    public void delete(String id) {
        Assert.notNull(id, "subjectId should not be null");
        panelSubjectMapper.deleteByPrimaryKey(id);
    }


}

package io.datains.service.panel;

import io.datains.base.domain.PanelPdfTemplate;
import io.datains.base.domain.PanelPdfTemplateExample;
import io.datains.base.mapper.PanelPdfTemplateMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author: wangjiahao
 * Date: 9/1/21
 * Description:
 */
@Service
public class PanelPdfTemplateService {

    @Resource
    private PanelPdfTemplateMapper panelPdfTemplateMapper;

    public List<PanelPdfTemplate> queryAll() {
        PanelPdfTemplateExample example = new PanelPdfTemplateExample();
        example.setOrderByClause("sort asc");
        return panelPdfTemplateMapper.selectByExampleWithBLOBs(example);
    }
}

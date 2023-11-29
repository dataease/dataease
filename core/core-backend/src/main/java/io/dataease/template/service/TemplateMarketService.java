package io.dataease.template.service;

import io.dataease.api.template.TemplateMarketApi;
import io.dataease.api.template.response.MarketBaseResponse;
import io.dataease.api.template.response.MarketPreviewBaseResponse;
import io.dataease.api.template.vo.MarketMetaDataVO;
import io.dataease.template.manage.TemplateCenterManage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author : WangJiaHao
 * @date : 2023/11/17 13:20
 */
@RestController
@RequestMapping("/templateMarket")
public class TemplateMarketService implements TemplateMarketApi {

    @Resource
    private TemplateCenterManage templateCenterManage;
    @Override
    public MarketBaseResponse searchTemplate() {
        return templateCenterManage.searchTemplate();
    }
    @Override
    public MarketBaseResponse searchTemplateRecommend() {
        return templateCenterManage.searchTemplateRecommend();
    }

    @Override
    public MarketPreviewBaseResponse searchTemplatePreview() {
        return templateCenterManage.searchTemplatePreview();
    }

    @Override
    public List<String> categories() {
        return templateCenterManage.getCategories();
    }

    @Override
    public List<MarketMetaDataVO> categoriesObject() {
        return templateCenterManage.getCategoriesObject();
    }
}

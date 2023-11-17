package io.dataease.template.service;

import io.dataease.api.template.TemplateMarketApi;
import io.dataease.api.template.response.MarketBaseResponse;
import io.dataease.template.manage.TemplateMarketManage;
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
    private TemplateMarketManage templateMarketManage;
    @Override
    public MarketBaseResponse searchTemplate() {
        return templateMarketManage.searchTemplate();
    }

    @Override
    public List<String> categories() {
        return templateMarketManage.getCategories();
    }
}

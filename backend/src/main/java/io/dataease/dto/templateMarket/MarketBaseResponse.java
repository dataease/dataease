package io.dataease.dto.templateMarket;

import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2022/7/15
 * Description:
 */
@Data
public class MarketBaseResponse {
    private String baseUrl;

    private List<TemplateMarketDTO> contents;

    public MarketBaseResponse() {
    }

    public MarketBaseResponse(String baseUrl, List<TemplateMarketDTO> contents) {
        this.baseUrl = baseUrl;
        this.contents = contents;
    }
}

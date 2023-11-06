package io.dataease.api.template.response;

import io.dataease.api.template.dto.TemplateMarketDTO;

import java.util.List;

/**
 * @author : WangJiaHao
 * @date : 2023/11/6 17:43
 */
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

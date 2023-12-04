package io.dataease.api.template.response;

import io.dataease.api.template.dto.TemplateMarketDTO;
import io.dataease.api.template.vo.MarketMetaDataVO;
import lombok.Data;

import java.util.List;

/**
 * @author : WangJiaHao
 * @date : 2023/11/6 17:43
 */
@Data
public class MarketBaseResponse {
    private String baseUrl;

    List<MarketMetaDataVO> categories;

    private List<TemplateMarketDTO> contents;

    public MarketBaseResponse() {
    }

    public MarketBaseResponse(String baseUrl, List<MarketMetaDataVO> categories, List<TemplateMarketDTO> contents) {
        this.baseUrl = baseUrl;
        this.categories = categories;
        this.contents = contents;
    }
}

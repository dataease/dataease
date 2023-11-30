package io.dataease.api.template.response;

import io.dataease.api.template.dto.TemplateMarketDTO;
import io.dataease.api.template.dto.TemplateMarketPreviewInfoDTO;
import io.dataease.api.template.vo.MarketMetaDataVO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : WangJiaHao
 * @date : 2023/11/6 17:43
 */
@Data
@NoArgsConstructor
public class MarketPreviewBaseResponse {
    private String baseUrl;

    private List<String> categories;

    private List<TemplateMarketPreviewInfoDTO> contents;

    public MarketPreviewBaseResponse(String baseUrl, List<String> categories, List<TemplateMarketPreviewInfoDTO> contents) {
        this.baseUrl = baseUrl;
        this.categories = categories;
        this.contents = contents;
    }
}

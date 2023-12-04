package io.dataease.api.template.dto;

import io.dataease.api.template.vo.MarketMetaDataVO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : WangJiaHao
 * @date : 2023/11/27
 */
@Data
@NoArgsConstructor
public class TemplateMarketPreviewInfoDTO {

    private MarketMetaDataVO category;

    private Boolean showFlag = true;

    List<TemplateMarketDTO> contents;

    public TemplateMarketPreviewInfoDTO(MarketMetaDataVO category, List<TemplateMarketDTO> contents) {
        this.category = category;
        this.contents = contents;
    }
}

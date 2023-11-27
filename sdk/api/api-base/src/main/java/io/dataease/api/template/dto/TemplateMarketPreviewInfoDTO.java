package io.dataease.api.template.dto;

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

    private String categoryType;

    List<TemplateMarketDTO> contents;

    public TemplateMarketPreviewInfoDTO(String categoryType, List<TemplateMarketDTO> contents) {
        this.categoryType = categoryType;
        this.contents = contents;
    }
}

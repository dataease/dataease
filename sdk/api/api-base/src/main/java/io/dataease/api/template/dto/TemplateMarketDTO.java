package io.dataease.api.template.dto;

import io.dataease.api.template.vo.MarketCategoryVO;
import io.dataease.api.template.vo.MarketMetasVO;
import lombok.Data;

import java.util.List;

@Data
public class TemplateMarketDTO {
    private String id;
    private String title;
    private String status;
    private String slug;
    private String editorType;
    private String summary;
    private String thumbnail;
    private Boolean showFlag = true;
    private List<MarketCategoryVO> categories;
    private MarketMetasVO metas;
}

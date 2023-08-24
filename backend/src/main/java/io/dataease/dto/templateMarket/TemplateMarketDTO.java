package io.dataease.dto.templateMarket;

import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2022/7/15
 * Description:
 */
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
    private List<MarketCategory> categories;
    private MarketMetas metas;
}

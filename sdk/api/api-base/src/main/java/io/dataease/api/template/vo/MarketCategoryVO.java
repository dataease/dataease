package io.dataease.api.template.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: wangjiahao
 * Date: 2022/7/15
 * Description:
 */
@Data
@NoArgsConstructor
public class MarketCategoryVO {
    private String id;
    private String name;
    private String slug;

    public MarketCategoryVO(String name) {
        this.name = name;
    }
}

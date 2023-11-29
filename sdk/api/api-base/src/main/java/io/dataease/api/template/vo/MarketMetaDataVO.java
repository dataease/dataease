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
public class MarketMetaDataVO {
    private String slug;
    private String value;
    private String label;

    public MarketMetaDataVO(String value, String label) {
        this.label = label;
        this.value = value;
        this.slug = value;
    }
}

package io.dataease.api.template.vo;

import io.dataease.constant.CommonConstants;
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

    // market 模板中心 manage 模板管理 public 公共
    private String source = CommonConstants.TEMPLATE_SOURCE.MARKET;

    public MarketMetaDataVO(String value, String label) {
        this.label = label;
        this.value = value;
        this.slug = value;
    }

    public MarketMetaDataVO(String value, String label,String source) {
        this.label = label;
        this.value = value;
        this.slug = value;
        this.source = source;
    }
}

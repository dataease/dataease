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
public class MarketMetasVO {
    private String theme_repo;

    public MarketMetasVO(String theme_repo) {
        this.theme_repo = theme_repo;
    }
}

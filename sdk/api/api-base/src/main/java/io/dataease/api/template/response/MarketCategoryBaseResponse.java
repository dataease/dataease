package io.dataease.api.template.response;

import io.dataease.api.template.vo.TemplateCategoryVO;
import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2022/7/15
 * Description:
 */
@Data
public class MarketCategoryBaseResponse {
    private List<TemplateCategoryVO> data;
}

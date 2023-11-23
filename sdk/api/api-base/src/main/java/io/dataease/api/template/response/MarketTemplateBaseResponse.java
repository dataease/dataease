package io.dataease.api.template.response;

import io.dataease.api.template.dto.TemplateMarketDTO;
import lombok.Data;

import java.util.List;

/**
 * @author : WangJiaHao
 * @date : 2023/11/17 13:41
 */
@Data
public class MarketTemplateBaseResponse {

    private MarketTemplateInnerResult data;

}

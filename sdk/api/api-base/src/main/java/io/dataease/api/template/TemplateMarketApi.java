package io.dataease.api.template;

import io.dataease.api.template.request.TemplateMarketSearchRequest;
import io.dataease.api.template.response.MarketBaseResponse;
import io.dataease.api.template.response.MarketPreviewBaseResponse;
import io.dataease.api.template.vo.MarketMetaDataVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author : WangJiaHao
 * @date : 2023/11/6 17:23
 */
public interface TemplateMarketApi {

    @GetMapping("/search")
    MarketBaseResponse searchTemplate();
    @GetMapping("/searchRecommend")
    MarketBaseResponse searchTemplateRecommend();

    @GetMapping("/searchPreview")
    MarketPreviewBaseResponse searchTemplatePreview();

    @GetMapping("/categories")
    List<String> categories();

    @GetMapping("/categoriesObject")
    List<MarketMetaDataVO> categoriesObject() ;

}

package io.dataease.api.template;

import io.dataease.api.template.request.TemplateMarketSearchRequest;
import io.dataease.api.template.response.MarketBaseResponse;
import io.dataease.api.template.response.MarketPreviewBaseResponse;
import io.dataease.api.template.vo.MarketMetaDataVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author : WangJiaHao
 * @date : 2023/11/6 17:23
 */
@Tag(name = "模版中心:基础")
public interface TemplateMarketApi {

    @GetMapping("/search")
    @Operation(summary = "查询")
    MarketBaseResponse searchTemplate();
    @GetMapping("/searchRecommend")
    @Operation(summary = "查询基础信息")
    MarketBaseResponse searchTemplateRecommend();

    @GetMapping("/searchPreview")
    @Operation(summary = "预览")
    MarketPreviewBaseResponse searchTemplatePreview();

    @GetMapping("/categories")
    @Operation(summary = "分类")
    List<String> categories();

    @GetMapping("/categoriesObject")
    @Operation(summary = "分类明细")
    List<MarketMetaDataVO> categoriesObject() ;

}

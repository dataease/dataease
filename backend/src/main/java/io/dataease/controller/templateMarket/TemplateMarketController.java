package io.dataease.controller.templateMarket;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.controller.request.templateMarket.TemplateMarketSearchRequest;
import io.dataease.dto.templateMarket.MarketBaseResponse;
import io.dataease.dto.templateMarket.TemplateMarketDTO;
import io.dataease.service.templateMarket.TemplateMarketService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2022/7/15
 * Description:
 */
@RestController
@Api(tags = "系统：模板市场")
@ApiSupport(order = 220)
@RequestMapping("/template/market")
public class TemplateMarketController {

    @Resource
    private TemplateMarketService marketService;


    @ApiOperation("查询模板")
    @PostMapping("/search")
    private MarketBaseResponse searchTemplate(@RequestBody TemplateMarketSearchRequest request){
        return marketService.searchTemplate(request);
    }

    @ApiOperation("查询分类")
    @GetMapping("/categories")
    private List<String> categories(){
        return marketService.getCategories();
    }


}

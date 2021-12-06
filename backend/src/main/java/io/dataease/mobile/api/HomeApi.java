package io.dataease.mobile.api;

import io.dataease.mobile.dto.HomeItemDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Api(tags = "移动端：首页")
@RequestMapping("/mobile/home")
public interface HomeApi {

    @ApiOperation("查询")
    @ApiImplicitParam(value = "类型", name = "type", paramType = "path", allowableValues = "{@code 0(收藏), 1(历史), 2(分享)}")
    @PostMapping("/query/{type}")
    List<HomeItemDTO> query(@PathVariable Integer type);

    @ApiOperation("详情")
    @ApiImplicitParam(value = "ID", name = "id", paramType = "path")
    @PostMapping("/detail/{id}")
    Object detail(@PathVariable String id);

}

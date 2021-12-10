package io.dataease.mobile.api;

import io.dataease.mobile.dto.MeItemDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;



@Api(tags = "移动端：我的")
@RequestMapping("/mobile/me")
public interface MeApi {

    @ApiOperation("查询个人信息")
    @PostMapping("/query")
    MeItemDTO query();
}

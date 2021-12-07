package io.dataease.mobile.api;


import io.dataease.mobile.dto.DirItemDTO;
import io.dataease.mobile.dto.DirRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Api(tags = "移动端：目录")
@RequestMapping("/mobile/dir")
public interface DirApi {


    @ApiOperation("查询")
    @PostMapping("/query")
    List<DirItemDTO> query(@RequestBody DirRequest request);
}

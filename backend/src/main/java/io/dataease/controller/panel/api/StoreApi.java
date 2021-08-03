package io.dataease.controller.panel.api;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.dto.panel.PanelStoreDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 收藏API
 */

@Api(tags = "仪表板：收藏管理")
@ApiSupport(order = 190)
@RequestMapping("/api/store")
public interface StoreApi {

    @ApiOperation("创建收藏")
    @PostMapping("/{id}")
    void store(@PathVariable("id") String id);


    @ApiOperation("查询收藏")
    @PostMapping("/list")
    List<PanelStoreDto> list(@RequestBody BaseGridRequest request);


    @ApiOperation("移除收藏")
    @PostMapping("/remove/{storeId}")
    void remove(@PathVariable("storeId") String storeId);

}

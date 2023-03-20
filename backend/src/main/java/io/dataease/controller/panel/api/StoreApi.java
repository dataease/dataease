package io.dataease.controller.panel.api;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.annotation.DePermission;
import io.dataease.auth.annotation.SqlInjectValidator;
import io.dataease.commons.constants.DePermissionType;
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

    @DePermission(type = DePermissionType.PANEL)
    @ApiOperation("创建收藏")
    @PostMapping("/{id}")
    void store(@PathVariable("id") String id);


    @ApiOperation("查询收藏")
    @PostMapping("/list")
    @SqlInjectValidator(value = {"s.create_time"})
    List<PanelStoreDto> list(@RequestBody BaseGridRequest request);


    @ApiOperation("移除收藏")
    @PostMapping("/remove/{panelId}")
    void remove(@PathVariable("panelId") String panelId);

    @ApiOperation("收藏状态")
    @PostMapping("/status/{id}")
    Boolean hasStar(@PathVariable("id") String id);


}

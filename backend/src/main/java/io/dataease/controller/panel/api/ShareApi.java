package io.dataease.controller.panel.api;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.annotation.DePermission;
import io.dataease.auth.annotation.SqlInjectValidator;
import io.dataease.plugins.common.base.domain.PanelShare;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.controller.request.panel.PanelShareFineDto;
import io.dataease.controller.request.panel.PanelShareRemoveRequest;
import io.dataease.controller.request.panel.PanelShareSearchRequest;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.dto.panel.PanelShareDto;
import io.dataease.dto.panel.PanelShareOutDTO;
import io.dataease.dto.panel.PanelSharePo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 分享API
 */
@Api(tags = "仪表板：分享管理")
@ApiSupport(order = 180)
@RequestMapping("/api/share")
public interface ShareApi {

    @ApiOperation("查询分享给我")
    @PostMapping("/treeList")
    @SqlInjectValidator(value = {"s.create_time"})
    List<PanelShareDto> treeList(BaseGridRequest request);

    @ApiOperation("查询我分享的")
    @PostMapping("/shareOut")
    List<PanelSharePo> shareOut();

    @ApiOperation("根据资源查询分享")
    @PostMapping("/queryWithResourceId")
    List<PanelShare> queryWithResourceId(PanelShareSearchRequest request);

    @ApiOperation("查询分享目标")
    @PostMapping("/queryTargets/{panelId}")
    @ApiImplicitParam(paramType = "path", value = "仪表板ID", name = "panelId", required = true, dataType = "String")
    List<PanelShareOutDTO> queryTargets(@PathVariable("panelId") String panelId);

    @DePermission(type = DePermissionType.PANEL, value = "resourceId")
    @ApiOperation("创建分享")
    @PostMapping("/fineSave")
    void fineSave(PanelShareFineDto panelShareFineDto);

    @ApiOperation("删除分享")
    @PostMapping("/removeShares")
    void removeShares(PanelShareRemoveRequest request);

    @DePermission(type = DePermissionType.PANEL)
    @ApiOperation("删除仪表板所有分享")
    @PostMapping("/removePanelShares/{panelId}")
    void removePanelShares(@PathVariable("panelId") String panelId);

}

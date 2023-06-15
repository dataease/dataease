package io.dataease.controller.panel;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.annotation.DePermission;
import io.dataease.auth.annotation.DePermissionProxy;
import io.dataease.auth.annotation.DePermissions;
import io.dataease.auth.filter.F2CLinkFilter;
import io.dataease.auth.service.impl.ExtAuthServiceImpl;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.PanelConstants;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.controller.handler.annotation.I18n;
import io.dataease.controller.request.panel.*;
import io.dataease.dto.PermissionProxy;
import io.dataease.dto.authModel.VAuthModelDTO;
import io.dataease.dto.panel.PanelExport2App;
import io.dataease.dto.panel.PanelGroupDTO;
import io.dataease.plugins.common.base.domain.PanelGroup;
import io.dataease.service.panel.PanelGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.pentaho.di.core.util.UUIDUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
@Api(tags = "仪表板：仪表板组")
@ApiSupport(order = 150)
@RestController
@RequestMapping("panel/group")
public class PanelGroupController {

    @Resource
    private PanelGroupService panelGroupService;
    @Resource
    private ExtAuthServiceImpl authService;

    @ApiOperation("查询树")
    @PostMapping("/tree")
    public List<PanelGroupDTO> tree(@RequestBody PanelGroupRequest request) {
        return panelGroupService.tree(request);
    }

    @ApiOperation("查询当前用户仪表板")
    @GetMapping("/list")
    public List<PanelGroup> list() {
        return panelGroupService.list();
    }

    @ApiOperation("默认树")
    @PostMapping("/defaultTree")
    public List<PanelGroupDTO> defaultTree(@RequestBody PanelGroupRequest request) {
        return panelGroupService.defaultTree(request);
    }

    @ApiOperation("保存")
    @PostMapping("/save")
    @DePermissions(value = {
            @DePermission(type = DePermissionType.PANEL, value = "id"),
            @DePermission(type = DePermissionType.PANEL, value = "pid", level = ResourceAuthLevel.PANEL_LEVEL_MANAGE)
    }, logical = Logical.AND)
    @I18n
    public PanelGroupDTO save(@RequestBody PanelGroupRequest request) throws Exception {
        String panelId = panelGroupService.save(request);
        PanelGroupDTO result = findOne(panelId);
        // 如果新建来源来自模板市场，在返回数据中加入父级ID便于跳转展开仪表板树
        if (PanelConstants.NEW_PANEL_FROM.NEW_MARKET_TEMPLATE.equals(request.getNewFrom())) {
            result.setParents(authService.parentResource(panelId, "panel"));
            result.setRequestId(UUIDUtil.getUUIDAsString());
        }
        return result;
    }

    @ApiOperation("更新")
    @PostMapping("/update")
    @DePermission(type = DePermissionType.PANEL, value = "id", level = ResourceAuthLevel.PANEL_LEVEL_MANAGE)
    @I18n
    public PanelGroupDTO update(@RequestBody PanelGroupRequest request) {
        return panelGroupService.update(request);
    }

    @ApiOperation("移动")
    @PostMapping("/move")
    @DePermissions(value = {
            @DePermission(type = DePermissionType.PANEL, value = "id", level = ResourceAuthLevel.PANEL_LEVEL_MANAGE),
            @DePermission(type = DePermissionType.PANEL, value = "pid", level = ResourceAuthLevel.PANEL_LEVEL_MANAGE)
    }, logical = Logical.AND)
    @I18n
    public PanelGroupDTO move(@RequestBody PanelGroupRequest request) {
        return panelGroupService.update(request);
    }


    @ApiOperation("删除")
    @DePermission(type = DePermissionType.PANEL, level = ResourceAuthLevel.PANEL_LEVEL_MANAGE)
    @PostMapping("/deleteCircle/{id}")
    public void deleteCircle(@PathVariable String id) {
        panelGroupService.deleteCircle(id);
    }

    @ApiOperation("详细信息")
    @DePermission(type = DePermissionType.PANEL, level = ResourceAuthLevel.PANEL_LEVEL_VIEW)
    @GetMapping("/findOne/{id}")
    public PanelGroupDTO findOne(@PathVariable String id) throws Exception {
        return panelGroupService.findOne(id);
    }

    @ApiIgnore
    @ApiOperation("详细信息(分享人代理)")
    @DePermissionProxy(paramIndex = 1)
    @DePermission(type = DePermissionType.PANEL, level = ResourceAuthLevel.PANEL_LEVEL_VIEW)
    @PostMapping("/proxy/findOne/{id}")
    public PanelGroupDTO proxyFindOne(@PathVariable String id, @RequestBody PermissionProxy proxy)
            throws Exception {
        return panelGroupService.findOne(id);
    }

    @ApiOperation("仪表板视图信息")
    @PostMapping("/queryPanelViewTree")
    @I18n
    public List<VAuthModelDTO> queryPanelViewTree() {
        return panelGroupService.queryPanelViewTree();
    }

    @ApiOperation("仪表板视图复用信息")
    @PostMapping("/queryPanelMultiplexingViewTree")
    @I18n
    public List<VAuthModelDTO> queryPanelMultiplexingViewTree() {
        return panelGroupService.queryPanelMultiplexingViewTree();
    }

    @ApiOperation("仪表板组件信息")
    @GetMapping("/queryPanelComponents/{id}")
    @I18n
    public Map queryPanelComponents(@PathVariable String id) {
        return panelGroupService.queryPanelComponents(id);
    }

    @ApiOperation("公共连接导出仪表板视图明细")
    @PostMapping("/exportDetails")
    @I18n
    public void exportDetails(@RequestBody PanelViewDetailsRequest request, HttpServletResponse response) throws IOException {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        String linkToken = httpServletRequest.getHeader(F2CLinkFilter.LINK_TOKEN_KEY);
        DecodedJWT jwt = JWT.decode(linkToken);
        Long userId = jwt.getClaim("userId").asLong();
        request.setUserId(userId);
        panelGroupService.exportPanelViewDetails(request, response);
    }

    @ApiOperation("站内导出仪表板视图明细")
    @PostMapping("/innerExportDetails")
    @DePermissionProxy(value = "proxy")
    @I18n
    public void innerExportDetails(@RequestBody PanelViewDetailsRequest request, HttpServletResponse response) throws IOException {
        panelGroupService.exportPanelViewDetails(request, response);
    }

    @ApiOperation("更新仪表板状态")
    @PostMapping("/updatePanelStatus/{panelId}")
    @I18n
    @DePermission(type = DePermissionType.PANEL, level = ResourceAuthLevel.PANEL_LEVEL_MANAGE)
    public void updatePanelStatus(@PathVariable String panelId, @RequestBody PanelGroupBaseInfoRequest request) {
        panelGroupService.updatePanelStatus(panelId, request);
    }

    @ApiOperation("自动缓存")
    @PostMapping("/autoCache")
    @DePermissions(value = {
            @DePermission(type = DePermissionType.PANEL, value = "id"),
            @DePermission(type = DePermissionType.PANEL, value = "pid", level = ResourceAuthLevel.PANEL_LEVEL_MANAGE)
    }, logical = Logical.AND)
    public void autoCache(@RequestBody PanelGroupRequest request) {
        panelGroupService.autoCache(request);

    }

    @ApiOperation("查找缓存")
    @GetMapping("/findUserCache/{panelId}")
    public PanelGroupDTO findUserCache(@PathVariable String panelId) {
        return panelGroupService.findUserPanelCache(panelId);
    }

    @ApiOperation("检查缓存")
    @GetMapping("/checkUserCache/{panelId}")
    public Boolean checkUserCache(@PathVariable String panelId) {
        return panelGroupService.checkUserCache(panelId);
    }

    @ApiOperation("删除缓存")
    @DeleteMapping("/removePanelCache/{panelId}")
    public void removePanelCache(@PathVariable String panelId) {
        panelGroupService.removePanelCache(panelId);
    }

    @ApiIgnore
    @PostMapping("/viewLog")
    public void viewLog(@RequestBody PanelViewLogRequest request) {
        panelGroupService.viewLog(request);
    }

    @ApiOperation("获取仪表板中视图Element信息")
    @GetMapping("/findPanelElementInfo/{viewId}")
    @I18n
    public Object findPanelElementInfo(@PathVariable String viewId) {
        return panelGroupService.findPanelElementInfo(viewId);
    }

    @GetMapping("/export2AppCheck/{panelId}")
    @I18n
    public PanelExport2App export2AppCheck(@PathVariable String panelId) {
        return panelGroupService.panelExport2AppCheck(panelId);
    }

    @PostMapping("/appApply")
    public PanelGroupDTO appApply(@RequestBody PanelAppTemplateApplyRequest request) throws Exception {
        String panelId = panelGroupService.appApply(request);
        PanelGroupDTO result = findOne(panelId);
        result.setParents(authService.parentResource(panelId, "panel"));
        result.setRequestId(UUIDUtil.getUUIDAsString());
        result.setResponseSource("appApply");
        return result;
    }

    @PostMapping("/appEdit")
    public void appEdit(@RequestBody PanelAppTemplateApplyRequest request) throws Exception {
        panelGroupService.appEdit(request);
    }

    @GetMapping("/findOneWithParent/{panelId}")
    public PanelGroupDTO findOneWithParent(@PathVariable String panelId) throws Exception {
        PanelGroupDTO result = findOne(panelId);
        result.setParents(authService.parentResource(panelId, "panel"));
        result.setRequestId(UUIDUtil.getUUIDAsString());
        result.setResponseSource("appApply");
        return result;
    }

    @PostMapping("/toTop/{panelId}")
    public void toTop(@PathVariable String panelId) throws Exception {
        panelGroupService.toTop(panelId);
    }
}

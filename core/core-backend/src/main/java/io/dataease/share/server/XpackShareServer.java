package io.dataease.share.server;

import io.dataease.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.dataease.api.xpack.share.XpackShareApi;
import io.dataease.api.xpack.share.request.*;
import io.dataease.api.xpack.share.vo.XpackShareGridVO;
import io.dataease.api.xpack.share.vo.XpackShareProxyVO;
import io.dataease.api.xpack.share.vo.XpackShareVO;
import io.dataease.utils.BeanUtils;
import io.dataease.auth.DeLinkPermit;
import io.dataease.share.dao.auto.entity.XpackShare;
import io.dataease.share.manage.XpackShareManage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import io.dataease.share.manage.ShareVisitorPermissionManage;
import io.dataease.share.dao.auto.mapper.XpackShareMapper;
import io.dataease.exception.DEException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequestMapping("/share")
@RestController
public class XpackShareServer implements XpackShareApi {

    @Resource(name = "xpackShareManage")
    private XpackShareManage xpackShareManage;
    @Resource
    private ShareVisitorPermissionManage visitorPermissionManage;
    @Resource
    private XpackShareMapper shareMapper;

    public record VisitorPermissionsRequest(Long resourceId, Integer visitorPermissions) {}

    @PostMapping("/visitorPermissions")
    public void saveVisitorPermissions(@RequestBody VisitorPermissionsRequest request) {
        if (!io.dataease.license.utils.LicenseUtil.licenseValid()) {
            DEException.throwException(io.dataease.i18n.Translator.get("i18n_share_operation_denied"));
        }
        if (request.resourceId() == null || request.visitorPermissions() == null
                || request.visitorPermissions() < 0 || request.visitorPermissions() > 7) {
            DEException.throwException(io.dataease.i18n.Translator.get("i18n_share_operation_denied"));
        }
        // queryByResource is scoped to the logged-in creator; cannot edit another user's link.
        XpackShare share = xpackShareManage.queryByResource(request.resourceId());
        if (share == null) DEException.throwException(io.dataease.i18n.Translator.get("i18n_share_operation_denied"));
        int allowed = visitorPermissionManage.creatorPermissions(request.resourceId());
        if ((request.visitorPermissions() & allowed) != request.visitorPermissions()) {
            DEException.throwException(io.dataease.i18n.Translator.get("i18n_share_operation_denied"));
        }
        share.setVisitorPermissions(request.visitorPermissions());
        shareMapper.updateById(share);
    }

    @GetMapping("/visitorPermissions/{resourceId}")
    @DeLinkPermit("#p0")
    public int visitorPermissions(@PathVariable("resourceId") Long resourceId) {
        return visitorPermissionManage.currentPermissions();
    }

    @Override
    public boolean status(Long resourceId) {
        return ObjectUtils.isNotEmpty(xpackShareManage.queryByResource(resourceId));
    }

    @Override
    public void switcher(Long resourceId) {
        xpackShareManage.switcher(resourceId);
    }

    @Override
    public void editExp(XpackShareExpRequest request) {
        xpackShareManage.editExp(request.getResourceId(), request.getExp());
    }

    @Override
    public void editPwd(XpackSharePwdRequest request) {
        xpackShareManage.editPwd(request.getResourceId(), request.getPwd(), request.getAutoPwd());
    }

    @Override
    public XpackShareVO detail(Long resourceId) {
        XpackShare xpackShare = xpackShareManage.queryByResource(resourceId);
        if (ObjectUtils.isEmpty(xpackShare)) return null;
        XpackShareVO vo = BeanUtils.copyBean(new XpackShareVO(), xpackShare);
        vo.setAllowedVisitorPermissions(visitorPermissionManage.creatorPermissions(resourceId));
        return vo;
    }

    @Override
    public List<XpackShareGridVO> query(VisualizationWorkbranchQueryRequest request) {
        return xpackShareManage.query(1, 20, request).getRecords();
    }

    @Override
    public XpackShareProxyVO proxyInfo(XpackShareProxyRequest request) {
        return xpackShareManage.proxyInfo(request);
    }

    @Override
    public boolean validatePwd(XpackSharePwdValidator validator) {
        return xpackShareManage.validatePwd(validator);
    }

    @Override
    public Map<String, String> queryRelationByUserId(Long uid) {
        return xpackShareManage.queryRelationByUserId(uid);
    }

    @Override
    public String editUuid(XpackShareUuidEditor editor) {
        return xpackShareManage.editUuid(editor);
    }
}

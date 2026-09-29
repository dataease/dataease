package io.dataease.share.manage;

import io.dataease.constant.AuthConstant;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.share.dao.auto.entity.XpackShare;
import io.dataease.share.dao.auto.mapper.XpackShareRepository;
import io.dataease.utils.ServletUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ShareVisitorPermissionManage {
    public static final int DETAILS = 1;
    public static final int EXPORT_DATA = 2;
    public static final int EXPORT_IMAGE = 4;

    @Resource
    private XpackShareRepository repository;

    public static int permissions(XpackShare share) {
        return share.getVisitorPermissions() == null ? 7 : share.getVisitorPermissions();
    }

    public int creatorPermissions(Long resourceId) {
        if (!LicenseUtil.licenseValid()) return 7;
        var auth = io.dataease.utils.CommonBeanFactory.getBean(io.dataease.api.permissions.auth.api.InteractiveAuthApi.class);
        if (auth == null) return 0;
        var permission = auth.queryAuth(resourceId);
        if (permission == null || permission.getWeight() <= 1) return 0;
        if (permission.getWeight() == 9) return 7;
        int ext = permission.getExt();
        return DETAILS | (ext % 10 > 0 ? EXPORT_IMAGE : 0)
                | (ext / 10 % 10 > 0 || ext / 100 % 10 > 0 ? EXPORT_DATA : 0);
    }

    public int currentPermissions() {
        var identity = io.dataease.permission.util.V3UserUtil.getLink();
        if (identity == null) {
            if (StringUtils.isNotBlank(ServletUtils.getHead(AuthConstant.LINK_TOKEN_KEY))) {
                throw new IllegalArgumentException("Unverified link token");
            }
            return 7;
        }
        Long resourceId = identity.resourceId();
        var share = repository.findById(identity.shareId())
                .orElseThrow(() -> new IllegalArgumentException("Share unavailable"));
        // Community sharing retains its original operations, but still requires an existing link.
        if (!LicenseUtil.licenseValid()) return 7;
        return permissions(share)
                & creatorPermissions(resourceId);
    }

    public void require(int permission) {
        if ((currentPermissions() & permission) != permission) {
            DEException.throwException(Translator.get("i18n_share_operation_denied"));
        }
    }
}

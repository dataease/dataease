package io.dataease.share.manage;

import com.auth0.jwt.JWT;
import io.dataease.constant.AuthConstant;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.share.dao.auto.entity.XpackShare;
import io.dataease.share.dao.auto.mapper.XpackShareMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    private XpackShareMapper mapper;

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
        String token = ServletUtils.getHead(AuthConstant.LINK_TOKEN_KEY);
        if (StringUtils.isBlank(token)) return 7;
        // Authentication validates the token; read current settings so revocation affects old tokens too.
        var jwt = JWT.decode(token);
        Long uid = jwt.getClaim("uid").asLong();
        Long resourceId = jwt.getClaim("resourceId").asLong();
        if (uid == null || resourceId == null) {
            DEException.throwException(Translator.get("i18n_share_operation_denied"));
        }
        QueryWrapper<XpackShare> query = new QueryWrapper<>();
        query.eq("creator", uid).eq("resource_id", resourceId);
        var shares = mapper.selectList(query);
        if (shares.isEmpty()) DEException.throwException(Translator.get("i18n_share_operation_denied"));
        // Community sharing retains its original operations, but still requires an existing link.
        if (!LicenseUtil.licenseValid()) return 7;
        // Be conservative if legacy duplicate shares exist.
        return shares.stream().mapToInt(ShareVisitorPermissionManage::permissions).reduce(7, (a, b) -> a & b)
                & creatorPermissions(resourceId);
    }

    public void require(int permission) {
        if ((currentPermissions() & permission) != permission) {
            DEException.throwException(Translator.get("i18n_share_operation_denied"));
        }
    }
}

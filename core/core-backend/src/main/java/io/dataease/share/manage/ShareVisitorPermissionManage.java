package io.dataease.share.manage;

import com.auth0.jwt.JWT;
import io.dataease.constant.AuthConstant;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
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
        var auth = io.dataease.utils.CommonBeanFactory.getBean(io.dataease.api.permissions.auth.api.InteractiveAuthApi.class);
        if (auth == null) return 7;
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
        var shares = repository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("creator"), uid), cb.equal(root.get("resourceId"), resourceId)));
        if (shares.isEmpty()) DEException.throwException(Translator.get("i18n_share_operation_denied"));
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

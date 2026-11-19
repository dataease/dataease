package io.dataease.resource;

import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.constant.AuthEnum;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.exception.DEException;
import io.dataease.exception.ErrorCategory;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.utils.CommonBeanFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class RootFolderCreatePermissionManage {

    // Compatibility facade: enterprise destination rules live in the permission provider.
    public void check(Long pid, BusiResourceEnum resourceType) {
        if (pid != null && pid < 0) {
            throw DEException.of(ErrorCategory.VALIDATION, "i18n_error_invalid_parameter");
        }
        Long uid = V3UserUtil.getUid();
        if (uid == null || V3UserUtil.getLink() != null) {
            throw denied();
        }
        InteractiveAuthApi auth = CommonBeanFactory.getBean(InteractiveAuthApi.class);
        if (auth == null) {
            // Only the actual single-user substitute mode may omit the permission service.
            if (Objects.equals(uid, 1L) && !V3UserUtil.getProxy().isProxy()
                    && CommonBeanFactory.getBean("substituleLoginServer") != null
                    && CommonBeanFactory.getBean("loginServer") == null) {
                return;
            }
            throw denied();
        }
        auth.checkWriteDestination(new BusiPerCheckDTO(pid == null ? 0L : pid,
                resourceType, AuthEnum.MANAGE));
    }

    private DEException denied() {
        return DEException.of(ErrorCategory.FORBIDDEN, "i18n_error_forbidden");
    }
}

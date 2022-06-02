package io.dataease.service.decatch;

import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.commons.constants.AuthConstants;
import io.dataease.commons.model.AuthURD;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.listener.util.CacheUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Author: wangjiahao
 * Date: 2022/5/31
 * Description:
 */
@Service
public class DeCatchProcess {

    @Async("taskExecutor")
    public void cleanPanel(Object pid) {
        CurrentUserDto user = AuthUtils.getUser();
        CacheUtils.remove(AuthConstants.USER_PANEL_NAME, "user" + user.getUserId());
        CacheUtils.remove(AuthConstants.DEPT_PANEL_NAME, "dept" + user.getDeptId());
        user.getRoles().forEach(role -> {
            CacheUtils.remove(AuthConstants.ROLE_PANEL_NAME, "role" + role.getId());
        });

        Optional.ofNullable(pid).ifPresent(resourceId -> {
            cleanCacheParent(resourceId.toString(), "panel");
        });


    }

    public void cleanDataSet(Object pid) {
        CurrentUserDto user = AuthUtils.getUser();
        CacheUtils.remove(AuthConstants.USER_DATASET_NAME, "user" + user.getUserId());
        CacheUtils.remove(AuthConstants.DEPT_DATASET_NAME, "dept" + user.getDeptId());
        user.getRoles().forEach(role -> {
            CacheUtils.remove(AuthConstants.ROLE_DATASET_NAME, "role" + role.getId());
        });

        Optional.ofNullable(pid).ifPresent(resourceId -> {
            cleanCacheParent(resourceId.toString(), "dataset");
        });
    }

    public void cleanDataSource(Object pid) {
        CurrentUserDto user = AuthUtils.getUser();
        CacheUtils.remove(AuthConstants.USER_LINK_NAME, "user" + user.getUserId());
        CacheUtils.remove(AuthConstants.DEPT_LINK_NAME, "dept" + user.getDeptId());
        user.getRoles().forEach(role -> {
            CacheUtils.remove(AuthConstants.ROLE_LINK_NAME, "role" + role.getId());
        });

        Optional.ofNullable(pid).ifPresent(resourceId -> {
            cleanCacheParent(resourceId.toString(), "link");
        });
    }

    private void cleanCacheParent(String pid, String type) {
        if (StringUtils.isBlank(pid) || StringUtils.isBlank(type)) {
            return;
        }
        CurrentUserDto user = AuthUtils.getUser();
        List<String> resourceIds = AuthUtils.parentResources(pid.toString(), type);
        if (CollectionUtils.isEmpty(resourceIds))return;
        resourceIds.forEach(resourceId -> {
            AuthURD authURD = AuthUtils.authURDR(resourceId);
            Optional.ofNullable(authURD.getUserIds()).ifPresent(ids -> {
                ids.forEach(id -> {
                    CacheUtils.remove("user_"+type, "user" + id);
                });
            });
            Optional.ofNullable(authURD.getRoleIds()).ifPresent(ids -> {
                ids.forEach(id -> {
                    CacheUtils.remove("role_"+type, "role" + id);
                });
            });
            Optional.ofNullable(authURD.getDeptIds()).ifPresent(ids -> {
                ids.forEach(id -> {
                    List<String> depts = AuthUtils.getAuthModels(id.toString(), "dept", user.getUserId(), user.getIsAdmin());
                    depts.forEach(deptId -> {
                        CacheUtils.remove("dept_"+type, "dept" + deptId);
                    });
                });
            });
        });
    }
}

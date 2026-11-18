package io.dataease.resource;

import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.constant.AuthEnum;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.exception.DEException;
import io.dataease.exception.ErrorCategory;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.utils.CommonBeanFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class RootFolderCreatePermissionManage {

    public void check(Long pid, BusiResourceEnum resourceType) {
        if (pid != null && pid != 0L) {
            return;
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
        BusiNodeRequest request = new BusiNodeRequest();
        request.setBusiFlag(resourceType.name());
        request.setLeaf(false);
        List<BusiNodeVO> nodes = auth.resource(request);
        // Match the root's own permission used by the UI, never a writable descendant.
        if (nodes == null || nodes.stream().noneMatch(node -> node != null
                && Objects.equals(node.getId(), 0L) && Boolean.FALSE.equals(node.getLeaf())
                && node.getWeight() != null && node.getWeight() >= AuthEnum.MANAGE.getWeight())) {
            throw denied();
        }
    }

    private DEException denied() {
        return DEException.of(ErrorCategory.FORBIDDEN, "i18n_error_forbidden");
    }
}

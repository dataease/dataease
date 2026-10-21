package io.dataease.share.manage;

import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.constant.AuthEnum;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.dao.auto.entity.DataVisualizationInfo;
import io.dataease.dao.auto.repo.PerBusiResourceRepository;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.permission.template.V3AuthTemplate;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.result.ResultCode;
import io.dataease.share.dao.auto.entity.XpackShare;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ShareAuthorizationManage {
    @Resource
    private DataVisualizationInfoRepository visualizations;
    @Resource
    private PerBusiResourceRepository resources;

    /** Management operations require a login identity, never a public-link identity. */
    public Long requireManage(Long resourceId) {
        if (V3UserUtil.getLink() != null || V3UserUtil.getUid() == null) deny();
        DataVisualizationInfo resource = resource(resourceId);
        Long oid = checkSubject(resource, V3UserUtil.getUid());
        // Management must additionally respect an administrator's active proxy scope.
        if (V3UserUtil.getProxy().isProxy()) {
            InteractiveAuthApi auth = CommonBeanFactory.getBean(InteractiveAuthApi.class);
            if (auth == null) deny();
            auth.checkAuth(request(resource));
        }
        return oid;
    }

    /** Recheck the stored creator, without changing the caller's authentication context. */
    public Long requireValidShare(XpackShare share) {
        if (share == null || share.getCreator() == null) deny();
        DataVisualizationInfo resource = resource(share.getResourceId());
        int type = "dashboard".equals(resource.getType()) ? 1 : 2;
        if (!Objects.equals(share.getType(), type)) deny();
        Long oid = checkSubject(resource, share.getCreator());
        // Legacy links have no organization binding; they still require current owner authorization.
        if (share.getOid() != null && !Objects.equals(share.getOid(), oid)) deny();
        return oid;
    }

    private DataVisualizationInfo resource(Long id) {
        if (id == null || id <= 0) deny();
        DataVisualizationInfo resource = visualizations.findById(id).orElse(null);
        if (resource == null || Boolean.TRUE.equals(resource.getDeleteFlag())
                || !"leaf".equals(resource.getNodeType())
                || !("dashboard".equals(resource.getType()) || "dataV".equals(resource.getType()))) deny();
        return resource;
    }

    private Long checkSubject(DataVisualizationInfo resource, Long uid) {
        var registration = resources.findById(resource.getId()).orElse(null);
        BusiPerCheckDTO request = request(resource);
        if (registration != null && (!Objects.equals(registration.getRtId(), request.getBusiEnum().getFlag())
                || !Boolean.TRUE.equals(registration.getLeaf()))) deny();
        InteractiveAuthApi auth = CommonBeanFactory.getBean(InteractiveAuthApi.class);
        // getUser(uid) applies the caller's link/proxy organization to the returned user object.
        // Query the provider directly so an anonymous/other user's request cannot mutate owner context.
        V3AuthTemplate users = io.dataease.license.utils.CommonBeanFactory.getBean(V3AuthTemplate.class);
        if (users != null) {
            var user = users.getUserInfo(uid);
            if (user == null || !Boolean.TRUE.equals(user.getEnable())) deny();
        } else if (auth != null) {
            deny();
        }
        if (auth != null) {
            auth.checkShareAuth(uid, request);
        } else {
            // Only the actual single-user substitute mode may omit an enterprise permission service.
            if (!Objects.equals(uid, 1L) || registration != null
                    || CommonBeanFactory.getBean("substituleLoginServer") == null
                    || CommonBeanFactory.getBean("loginServer") != null) deny();
        }
        return registration == null ? resource.getOrgId() : registration.getOrgId();
    }

    private BusiPerCheckDTO request(DataVisualizationInfo resource) {
        return new BusiPerCheckDTO(resource.getId(), "dashboard".equals(resource.getType())
                ? BusiResourceEnum.PANEL : BusiResourceEnum.SCREEN, AuthEnum.MANAGE);
    }

    private void deny() {
        DEException.throwException(ResultCode.PERMISSION_NO_ACCESS.code(), Translator.get("i18n_share_operation_denied"));
    }
}

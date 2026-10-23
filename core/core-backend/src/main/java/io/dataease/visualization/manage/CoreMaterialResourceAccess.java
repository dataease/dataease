package io.dataease.visualization.manage;

import io.dataease.api.material.MaterialResourceAccess;
import io.dataease.api.material.dto.MaterialContext;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.constant.*;
import io.dataease.dao.auto.entity.DataVisualizationInfo;
import io.dataease.exception.DEException;
import io.dataease.result.ResultCode;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.utils.MaterialReferences;
import io.dataease.visualization.dao.auto.mapper.*;
import io.dataease.template.dao.auto.mapper.VisualizationTemplateRepository;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class CoreMaterialResourceAccess implements MaterialResourceAccess {
    @Resource private DataVisualizationInfoRepository resources;
    @Resource private SnapshotDataVisualizationInfoRepository snapshots;
    @Resource private VisualizationTemplateRepository templates;
    private final ObjectProvider<InteractiveAuthApi> authProvider;

    public CoreMaterialResourceAccess(ObjectProvider<InteractiveAuthApi> authProvider) {
        this.authProvider = authProvider;
    }

    @Override
    public Set<Long> authorize(MaterialContext context, boolean edit) {
        if (context == null) throw denied();
        if (context.getTemplateId() != null) {
            // Templates use the existing authenticated global template catalogue.
            if (V3UserUtil.getUid() == null || V3UserUtil.getLink() != null || edit) throw denied();
            var template = templates.findById(context.getTemplateId()).orElseThrow(this::denied);
            return MaterialReferences.collect(template.getTemplateStyle(), template.getTemplateData());
        }
        if (context.getResourceId() == null) {
            if (!edit && context.getSourceTemplateId() != null) {
                MaterialContext source = new MaterialContext();
                source.setTemplateId(context.getSourceTemplateId());
                return authorize(source, false);
            }
            if (!edit || V3UserUtil.getUid() == null || V3UserUtil.getLink() != null
                    || !("dashboard".equals(context.getResourceType()) || "dataV".equals(context.getResourceType()))) throw denied();
            boolean screen = "dataV".equals(context.getResourceType());
            Long parentId = context.getParentId();
            if (parentId == null || parentId == 0L) {
                // New canvases can choose their destination on first save; no folder ID exists yet.
                long menuId = (screen ? AuthResourceEnum.SCREEN : AuthResourceEnum.PANEL).getMenuId();
                if (!requireAuth().menuIds().contains(menuId)) throw denied();
            } else {
                requireAuth().checkAuth(new BusiPerCheckDTO(parentId,
                        screen ? BusiResourceEnum.SCREEN : BusiResourceEnum.PANEL, AuthEnum.MANAGE));
            }
            return Set.of();
        }
        DataVisualizationInfo resource = resources.findById(context.getResourceId()).orElseThrow(this::denied);
        if (!Set.of("dashboard", "dataV").contains(resource.getType())) throw denied();
        boolean draft = "snapshot".equals(context.getResourceTable());
        if (!draft && !"core".equals(context.getResourceTable())) throw denied();
        var link = V3UserUtil.getLink();
        if (link != null) {
            if (edit || draft || !Objects.equals(link.resourceId(), resource.getId())
                    || Boolean.TRUE.equals(resource.getDeleteFlag())) throw denied();
            var permissions = io.dataease.utils.CommonBeanFactory.getBean(io.dataease.share.manage.ShareVisitorPermissionManage.class);
            permissions.currentPermissions();
            if (permissions.creatorPermissions(resource.getId()) == 0) throw denied();
        } else {
            if (V3UserUtil.getUid() == null) throw denied();
            if (!(edit && Objects.equals(resource.getPid(), -1L)
                    && Objects.equals(resource.getCreateBy(), V3UserUtil.getUid().toString())))
            requireAuth().checkAuth(new BusiPerCheckDTO(resource.getId(),
                    "dataV".equals(resource.getType()) ? BusiResourceEnum.SCREEN : BusiResourceEnum.PANEL,
                    edit || draft ? AuthEnum.MANAGE : AuthEnum.READ));
        }
        if (draft) {
            var snapshot = snapshots.findById(resource.getId()).orElseThrow(this::denied);
            return MaterialReferences.collect(snapshot.getCanvasStyleData(), snapshot.getComponentData());
        }
        return MaterialReferences.collect(resource.getCanvasStyleData(), resource.getComponentData());
    }

    @Override
    public Set<String> images(MaterialContext context) {
        boolean creating = context.getResourceId() == null && context.getTemplateId() == null;
        authorize(context, creating);
        if (creating) return Set.of();
        if (context.getTemplateId() != null) {
            var template = templates.findById(context.getTemplateId()).orElseThrow(this::denied);
            return io.dataease.utils.MaterialDocuments.images(template.getTemplateStyle(), template.getTemplateData());
        }
        if ("snapshot".equals(context.getResourceTable())) {
            var snapshot = snapshots.findById(context.getResourceId()).orElseThrow(this::denied);
            return io.dataease.utils.MaterialDocuments.images(snapshot.getCanvasStyleData(), snapshot.getComponentData());
        }
        var resource = resources.findById(context.getResourceId()).orElseThrow(this::denied);
        return io.dataease.utils.MaterialDocuments.images(resource.getCanvasStyleData(), resource.getComponentData());
    }

    @Override
    public Set<Long> authorizeExport(MaterialContext context) {
        if (context != null && context.getResourceId() == null && context.getTemplateId() == null) {
            return authorize(context, true);
        }
        Set<Long> ids = authorize(context, false);
        if (context.getTemplateId() != null) return ids;
        if (V3UserUtil.getLink() != null) {
            io.dataease.utils.CommonBeanFactory.getBean(io.dataease.share.manage.ShareVisitorPermissionManage.class)
                    .require(io.dataease.share.manage.ShareVisitorPermissionManage.EXPORT_IMAGE);
        } else {
            DataVisualizationInfo resource = resources.findById(context.getResourceId()).orElseThrow(this::denied);
            requireAuth().checkAuth(new BusiPerCheckDTO(resource.getId(),
                    "dataV".equals(resource.getType()) ? BusiResourceEnum.SCREEN : BusiResourceEnum.PANEL,
                    AuthEnum.EXPORT));
        }
        return ids;
    }

    @Override
    public boolean isReferenced(long materialId) {
        // Final backstop includes recycle-bin resources and templates, independently of the index.
        for (int page = 0; ; page++) {
            var batch = resources.findAll(PageRequest.of(page, 200));
            if (batch.stream().anyMatch(value -> MaterialReferences.collect(value.getCanvasStyleData(), value.getComponentData()).contains(materialId))) return true;
            if (!batch.hasNext()) break;
        }
        for (int page = 0; ; page++) {
            var batch = snapshots.findAll(PageRequest.of(page, 200));
            if (batch.stream().anyMatch(value -> MaterialReferences.collect(value.getCanvasStyleData(), value.getComponentData()).contains(materialId))) return true;
            if (!batch.hasNext()) break;
        }
        for (int page = 0; ; page++) {
            var batch = templates.findAll(PageRequest.of(page, 200));
            if (batch.stream().anyMatch(value -> MaterialReferences.collect(value.getTemplateStyle(), value.getTemplateData()).contains(materialId))) return true;
            if (!batch.hasNext()) return false;
        }
    }

    private InteractiveAuthApi requireAuth() {
        InteractiveAuthApi auth = authProvider.getIfAvailable();
        if (auth == null) throw denied();
        return auth;
    }

    private DEException denied() {
        return new DEException(ResultCode.PERMISSION_NO_ACCESS.code(), ResultCode.PERMISSION_NO_ACCESS.message());
    }
}

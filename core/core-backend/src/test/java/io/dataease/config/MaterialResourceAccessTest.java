package io.dataease.config;

import io.dataease.api.material.dto.MaterialContext;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.api.permissions.auth.vo.PermissionValVO;
import io.dataease.constant.AuthEnum;
import io.dataease.constant.AuthResourceEnum;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.constant.CommonConstants;
import io.dataease.dao.auto.entity.DataVisualizationInfo;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.permission.model.LinkIdentity;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.result.ResultCode;
import io.dataease.share.dao.auto.entity.XpackShare;
import io.dataease.share.dao.auto.mapper.XpackShareRepository;
import io.dataease.share.manage.LinkTokenValidationManage;
import io.dataease.share.manage.ShareVisitorPermissionManage;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.ServletUtils;
import io.dataease.utils.WhitelistUtils;
import io.dataease.visualization.dao.auto.entity.SnapshotDataVisualizationInfo;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;
import io.dataease.visualization.dao.auto.mapper.SnapshotDataVisualizationInfoRepository;
import io.dataease.visualization.manage.CoreMaterialResourceAccess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MaterialResourceAccessTest {
    private CoreMaterialResourceAccess access;
    private InteractiveAuthApi auth;
    private DataVisualizationInfoRepository resources;
    private SnapshotDataVisualizationInfoRepository snapshots;
    private MessageSource previousMessages;

    @BeforeEach
    void setup() {
        V3UserUtil.clear();
        previousMessages = (MessageSource) ReflectionTestUtils.getField(Translator.class, "messageSource");
        new Translator().setMessageSource(new StaticMessageSource());
        auth = mock(InteractiveAuthApi.class);
        resources = mock(DataVisualizationInfoRepository.class);
        snapshots = mock(SnapshotDataVisualizationInfoRepository.class);
        var beans = new StaticListableBeanFactory();
        beans.addBean("interactiveAuthApi", auth);
        access = new CoreMaterialResourceAccess(beans.getBeanProvider(InteractiveAuthApi.class));
        ReflectionTestUtils.setField(access, "resources", resources);
        ReflectionTestUtils.setField(access, "snapshots", snapshots);
    }

    @AfterEach
    void clearIdentity() {
        V3UserUtil.clear();
        new Translator().setMessageSource(previousMessages);
    }

    private MaterialContext creating(String type, Long parentId) {
        var context = new MaterialContext();
        context.setResourceType(type);
        context.setParentId(parentId);
        context.setResourceTable("snapshot");
        return context;
    }

    @Test
    void unsavedCanvasesRequireAWritableDestinationInTheirResourceTree() {
        try (var identity = mockStatic(V3UserUtil.class)) {
            identity.when(V3UserUtil::getUid).thenReturn(11L);
            for (String type : List.of("dashboard", "dataV")) {
                long menuId = ("dataV".equals(type) ? AuthResourceEnum.SCREEN : AuthResourceEnum.PANEL).getMenuId();
                when(auth.menuIds()).thenReturn(List.of(menuId));
                var treeRequest = new BusiNodeRequest();
                treeRequest.setBusiFlag("dataV".equals(type) ? "SCREEN" : "PANEL");
                treeRequest.setResourceTable("core");
                treeRequest.setLeaf(false);
                treeRequest.setWeight(AuthEnum.MANAGE.getWeight());
                when(auth.resource(treeRequest)).thenReturn(null,
                        List.of(destination(false, AuthEnum.READ), destination(true, AuthEnum.MANAGE)));
                assertThrows(DEException.class, () -> access.authorize(creating(type, null), true));
                assertThrows(DEException.class, () -> access.authorize(creating(type, 0L), true));

                var root = destination(false, AuthEnum.READ);
                root.setChildren(List.of(destination(false, AuthEnum.MANAGE)));
                when(auth.resource(treeRequest)).thenReturn(List.of(root));
                for (Long parentId : new Long[]{null, 0L}) {
                    var context = creating(type, parentId);
                    assertEquals(Set.of(), access.authorize(context, true));
                    assertEquals(Set.of(), access.images(context));
                    assertEquals(Set.of(), access.authorizeExport(context));
                    assertThrows(DEException.class, () -> access.authorize(context, false));
                }
            }
            verify(auth, never()).checkAuth(any());
            verifyNoInteractions(resources);
        }
    }

    private BusiNodeVO destination(boolean leaf, AuthEnum permission) {
        var node = new BusiNodeVO();
        node.setLeaf(leaf);
        node.setWeight(permission.getWeight());
        return node;
    }

    @Test
    void missingOrWrongMenuDoesNotAuthorizeAnUnsavedCanvas() {
        try (var identity = mockStatic(V3UserUtil.class)) {
            identity.when(V3UserUtil::getUid).thenReturn(11L);
            for (String type : List.of("dashboard", "dataV")) {
                long otherMenu = ("dataV".equals(type) ? AuthResourceEnum.PANEL : AuthResourceEnum.SCREEN).getMenuId();
                when(auth.menuIds()).thenReturn(List.of());
                assertThrows(DEException.class, () -> access.authorize(creating(type, null), true));
                when(auth.menuIds()).thenReturn(List.of(otherMenu, 130L));
                assertThrows(DEException.class, () -> access.authorize(creating(type, null), true));
            }
        }
    }

    @Test
    void newCanvasesRejectAnonymousShareAndInvalidContexts() {
        var context = creating("dataV", null);
        try (var identity = mockStatic(V3UserUtil.class)) {
            identity.when(V3UserUtil::getUid).thenReturn(null);
            assertThrows(DEException.class, () -> access.authorize(context, true));
            identity.when(V3UserUtil::getUid).thenReturn(11L);
            identity.when(V3UserUtil::getLink).thenAnswer(invocation -> mock(invocation.getMethod().getReturnType()));
            assertThrows(DEException.class, () -> access.authorize(context, true));
            identity.when(V3UserUtil::getLink).thenReturn(null);
            assertThrows(DEException.class, () -> access.authorize(null, true));
            assertThrows(DEException.class, () -> access.authorize(creating(null, null), true));
            assertThrows(DEException.class, () -> access.authorize(creating("other", null), true));
            verifyNoInteractions(auth, resources);
        }
    }

    @Test
    void creatingInsideAFolderStillChecksThatFolder() {
        try (var identity = mockStatic(V3UserUtil.class)) {
            identity.when(V3UserUtil::getUid).thenReturn(11L);
            for (String type : List.of("dashboard", "dataV")) {
                var context = creating(type, 501L);
                var permission = new BusiPerCheckDTO(501L,
                        "dataV".equals(type) ? BusiResourceEnum.SCREEN : BusiResourceEnum.PANEL, AuthEnum.MANAGE);
                assertEquals(Set.of(), access.authorize(context, true));
                verify(auth).checkAuth(permission);
                doThrow(new DEException(403, "Denied")).when(auth).checkAuth(permission);
                assertThrows(DEException.class, () -> access.authorize(context, true));
            }
            verify(auth, never()).menuIds();
            verifyNoInteractions(resources);
        }
    }

    @Test
    void nonexistentResourceCannotUseTheUnsavedCanvasBranch() {
        var context = creating("dataV", null);
        context.setResourceId(999L);
        when(resources.findById(999L)).thenReturn(Optional.empty());
        assertThrows(DEException.class, () -> access.authorize(context, true));
        verifyNoInteractions(auth);
    }

    @Test
    void viewerCanReadPublishedReferencesButCannotEditOrReadDrafts() {
        storedResource(101L, 10L, "dashboard", 123L);
        var published = resourceContext(101L, "core");
        var draft = resourceContext(101L, "snapshot");
        var read = new BusiPerCheckDTO(101L, BusiResourceEnum.PANEL, AuthEnum.READ);
        var manage = new BusiPerCheckDTO(101L, BusiResourceEnum.PANEL, AuthEnum.MANAGE);
        doThrow(permissionDenied()).when(auth).checkAuth(manage);
        V3UserUtil.setUid(11L);

        try (var license = new MaterialTestLicense()) {
            assertEquals(Set.of(123L), access.authorize(published, false));
            assertThrows(DEException.class, () -> access.authorize(published, true));
            assertThrows(DEException.class, () -> access.authorize(draft, false));
        }
        verify(auth).checkAuth(read);
        verify(auth, times(2)).checkAuth(manage);
        verifyNoInteractions(snapshots);
    }

    @Test
    void editorCanAccessDraftWithoutMaterialManagementMenu() {
        storedResource(101L, 10L, "dataV", 123L);
        var snapshot = new SnapshotDataVisualizationInfo();
        snapshot.setCanvasStyleData("{\"background\":\"material://456\"}");
        when(snapshots.findById(101L)).thenReturn(Optional.of(snapshot));
        var draft = resourceContext(101L, "snapshot");
        draft.setResourceType("dashboard");
        var manage = new BusiPerCheckDTO(101L, BusiResourceEnum.SCREEN, AuthEnum.MANAGE);
        V3UserUtil.setUid(12L);

        try (var license = new MaterialTestLicense()) {
            assertEquals(Set.of(456L), access.authorize(draft, true));
            assertEquals(Set.of(456L), access.authorize(draft, false));
        }
        verify(auth, times(2)).checkAuth(manage);
        verify(auth, never()).menuIds();
    }

    @Test
    void resourceAuthorizationStillAppliesAcrossOrganizations() {
        storedResource(101L, 10L, "dashboard", 123L);
        storedResource(202L, 20L, "dashboard", 456L);
        var otherOrgRead = new BusiPerCheckDTO(202L, BusiResourceEnum.PANEL, AuthEnum.READ);
        var otherOrgManage = new BusiPerCheckDTO(202L, BusiResourceEnum.PANEL, AuthEnum.MANAGE);
        doThrow(permissionDenied()).when(auth).checkAuth(otherOrgRead);
        doThrow(permissionDenied()).when(auth).checkAuth(otherOrgManage);
        V3UserUtil.setUid(11L);
        var otherOrg = resourceContext(202L, "core");

        try (var license = new MaterialTestLicense()) {
            assertEquals(Set.of(123L), access.authorize(resourceContext(101L, "core"), false));
            assertThrows(DEException.class, () -> access.authorize(otherOrg, false));
            assertThrows(DEException.class, () -> access.authorize(otherOrg, true));
            // Explicit access to another organization's canvas grants its references.
            doNothing().when(auth).checkAuth(otherOrgRead);
            assertEquals(Set.of(456L), access.authorize(otherOrg, false));
        }
        verify(auth, times(2)).checkAuth(otherOrgRead);
        verify(auth).checkAuth(otherOrgManage);
        verify(auth, never()).menuIds();
    }

    @Test
    void verifiedShareOnlyReadsThePublishedResourceInItsScope() {
        var published = storedResource(101L, 10L, "dashboard", 123L);
        storedResource(202L, 20L, "dashboard", 456L);
        var share = new XpackShare();
        share.setId(301L);
        share.setCreator(11L);
        share.setResourceId(101L);
        share.setOid(10L);
        share.setVisitorPermissions(ShareVisitorPermissionManage.DETAILS);
        var shares = mock(XpackShareRepository.class);
        when(shares.findById(301L)).thenReturn(Optional.of(share));
        var permissions = new ShareVisitorPermissionManage();
        ReflectionTestUtils.setField(permissions, "repository", shares);
        when(auth.queryAuth(101L)).thenReturn(new PermissionValVO(9, 0));
        var context = resourceContext(101L, "core");
        V3UserUtil.setLink(new LinkIdentity(301L, 11L, 101L, 10L));

        try (var license = new MaterialTestLicense();
             var beans = mockStatic(CommonBeanFactory.class)) {
            beans.when(() -> CommonBeanFactory.getBean(ShareVisitorPermissionManage.class)).thenReturn(permissions);
            beans.when(() -> CommonBeanFactory.getBean(InteractiveAuthApi.class)).thenReturn(auth);
            assertEquals(Set.of(123L), access.authorize(context, false));
            assertThrows(DEException.class, () -> access.authorize(context, true));
            assertThrows(DEException.class, () -> access.authorize(resourceContext(101L, "snapshot"), false));
            assertThrows(DEException.class, () -> access.authorize(resourceContext(202L, "core"), false));
            published.setDeleteFlag(true);
            assertThrows(DEException.class, () -> access.authorize(context, false));
            published.setDeleteFlag(false);
            when(auth.queryAuth(101L)).thenReturn(new PermissionValVO(0, 0));
            assertThrows(DEException.class, () -> access.authorize(context, false));
        }
        verify(auth, never()).checkAuth(any());
        verifyNoInteractions(snapshots);
    }

    @Test
    void embeddedEditorsCanSelectMaterialsAndReadTheirDrafts() {
        try (var license = new MaterialTestLicense(); var servlet = mockStatic(ServletUtils.class)) {
            servlet.when(ServletUtils::isEmbeddedRequest).thenReturn(true);
            V3UserUtil.setUid(13L);
            for (String type : List.of("dashboard", "dataV")) {
                var resource = storedResource(101L, 10L, type, 123L);
                resource.setStatus(CommonConstants.DV_STATUS.UNPUBLISHED);
                var snapshot = new SnapshotDataVisualizationInfo();
                snapshot.setCanvasStyleData("{\"background\":\"material://456\"}");
                when(snapshots.findById(101L)).thenReturn(Optional.of(snapshot));
                assertEquals(Set.of(123L), access.authorize(resourceContext(101L, "core"), true));
                assertEquals(Set.of(456L), access.authorize(resourceContext(101L, "snapshot"), true));
                assertEquals(Set.of(456L), access.authorize(resourceContext(101L, "snapshot"), false));
                var manage = new BusiPerCheckDTO(101L,
                        "dataV".equals(type) ? BusiResourceEnum.SCREEN : BusiResourceEnum.PANEL, AuthEnum.MANAGE);
                verify(auth, times(3)).checkAuth(manage);
            }
        }
        verify(auth, never()).menuIds();
    }

    @Test
    void embeddedViewersStillCannotSelectMaterialsOrReadDrafts() {
        storedResource(101L, 10L, "dataV", 123L);
        var context = resourceContext(101L, "core");
        var read = new BusiPerCheckDTO(101L, BusiResourceEnum.SCREEN, AuthEnum.READ);
        var manage = new BusiPerCheckDTO(101L, BusiResourceEnum.SCREEN, AuthEnum.MANAGE);
        doThrow(permissionDenied()).when(auth).checkAuth(manage);
        try (var license = new MaterialTestLicense(); var servlet = mockStatic(ServletUtils.class)) {
            servlet.when(ServletUtils::isEmbeddedRequest).thenReturn(true);
            V3UserUtil.setUid(13L);
            assertEquals(Set.of(123L), access.authorize(context, false));
            assertThrows(DEException.class, () -> access.authorize(context, true));
            assertThrows(DEException.class, () -> access.authorize(resourceContext(101L, "snapshot"), false));
            doThrow(permissionDenied()).when(auth).checkAuth(read);
            assertThrows(DEException.class, () -> access.authorize(context, false));
        }
        verify(auth, times(2)).checkAuth(read);
        verify(auth, times(2)).checkAuth(manage);
        verifyNoInteractions(snapshots);
    }

    @Test
    void embeddedNewCanvasesUseTheSameMenuAndFolderPermissions() {
        try (var servlet = mockStatic(ServletUtils.class)) {
            servlet.when(ServletUtils::isEmbeddedRequest).thenReturn(true);
            V3UserUtil.setUid(13L);
            for (String type : List.of("dashboard", "dataV")) {
                var context = creating(type, 501L);
                var permission = new BusiPerCheckDTO(501L,
                        "dataV".equals(type) ? BusiResourceEnum.SCREEN : BusiResourceEnum.PANEL, AuthEnum.MANAGE);
                assertEquals(Set.of(), access.authorize(context, true));
                doThrow(permissionDenied()).when(auth).checkAuth(permission);
                assertThrows(DEException.class, () -> access.authorize(context, true));
                verify(auth, times(2)).checkAuth(permission);

                long menuId = ("dataV".equals(type) ? AuthResourceEnum.SCREEN : AuthResourceEnum.PANEL).getMenuId();
                when(auth.menuIds()).thenReturn(List.of(menuId));
                when(auth.resource(any())).thenReturn(List.of());
                assertThrows(DEException.class, () -> access.authorize(creating(type, null), true));
                when(auth.resource(any())).thenReturn(List.of(destination(false, AuthEnum.MANAGE)));
                assertEquals(Set.of(), access.authorize(creating(type, null), true));
                when(auth.menuIds()).thenReturn(List.of());
                assertThrows(DEException.class, () -> access.authorize(creating(type, null), true));
            }
            V3UserUtil.clear();
            assertThrows(DEException.class, () -> access.authorize(creating("dashboard", 501L), true));
        }
        verifyNoInteractions(resources, snapshots);
    }

    @Test
    void sharedRouteGateOnlyAllowsMaterialContentAndExport() {
        var verifier = new LinkTokenValidationManage();
        ReflectionTestUtils.setField(verifier, "mappings", mock(RequestMappingHandlerMapping.class));
        try (var paths = mockStatic(WhitelistUtils.class)) {
            paths.when(WhitelistUtils::getContextPath).thenReturn("/qa");
            assertDoesNotThrow(() -> verifier.checkRequestPath("/material/content/123", "POST"));
            assertDoesNotThrow(() -> verifier.checkRequestPath("/qa/de2api/material/content/123?x=1", "POST"));
            assertDoesNotThrow(() -> verifier.checkRequestPath("/de2api/material/export", "POST"));

            assertThrows(IllegalArgumentException.class, () -> verifier.checkRequestPath("/material/content/123", "GET"));
            for (String path : List.of("/material/select/1/8", "/material/upload", "/material/download/123",
                    "/material/content/0", "/material/content/123/extra")) {
                assertThrows(IllegalArgumentException.class, () -> verifier.checkRequestPath(path, "POST"));
            }
        }
    }

    private DataVisualizationInfo storedResource(long id, long orgId, String type, long materialId) {
        var resource = new DataVisualizationInfo();
        resource.setId(id);
        resource.setOrgId(orgId);
        resource.setType(type);
        resource.setNodeType("leaf");
        resource.setPid(1L);
        resource.setCanvasStyleData("{\"background\":\"material://" + materialId + "\"}");
        when(resources.findById(id)).thenReturn(Optional.of(resource));
        return resource;
    }

    private MaterialContext resourceContext(long id, String table) {
        var context = new MaterialContext();
        context.setResourceId(id);
        context.setResourceTable(table);
        return context;
    }

    private DEException permissionDenied() {
        return new DEException(ResultCode.PERMISSION_NO_ACCESS.code(), ResultCode.PERMISSION_NO_ACCESS.message());
    }
}

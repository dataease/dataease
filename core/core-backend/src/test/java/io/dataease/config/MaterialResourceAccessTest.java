package io.dataease.config;

import io.dataease.api.material.dto.MaterialContext;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.constant.AuthEnum;
import io.dataease.constant.AuthResourceEnum;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.exception.DEException;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;
import io.dataease.visualization.manage.CoreMaterialResourceAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MaterialResourceAccessTest {
    private CoreMaterialResourceAccess access;
    private InteractiveAuthApi auth;
    private DataVisualizationInfoRepository resources;

    @BeforeEach
    void setup() {
        auth = mock(InteractiveAuthApi.class);
        resources = mock(DataVisualizationInfoRepository.class);
        var beans = new StaticListableBeanFactory();
        beans.addBean("interactiveAuthApi", auth);
        access = new CoreMaterialResourceAccess(beans.getBeanProvider(InteractiveAuthApi.class));
        ReflectionTestUtils.setField(access, "resources", resources);
    }

    private MaterialContext creating(String type, Long parentId) {
        var context = new MaterialContext();
        context.setResourceType(type);
        context.setParentId(parentId);
        context.setResourceTable("snapshot");
        return context;
    }

    @Test
    void unsavedCanvasesRequireMenuAndWritableDestination() {
        try (var identity = mockStatic(V3UserUtil.class)) {
            identity.when(V3UserUtil::getUid).thenReturn(11L);
            for (String type : List.of("dashboard", "dataV")) {
                long menuId = ("dataV".equals(type) ? AuthResourceEnum.SCREEN : AuthResourceEnum.PANEL).getMenuId();
                when(auth.menuIds()).thenReturn(List.of(menuId));
                var request = new BusiNodeRequest();
                request.setBusiFlag("dataV".equals(type) ? "SCREEN" : "PANEL");
                request.setResourceTable("core");
                request.setLeaf(false);
                request.setWeight(AuthEnum.MANAGE.getWeight());
                var folder = new BusiNodeVO();
                folder.setLeaf(false);
                folder.setWeight(AuthEnum.MANAGE.getWeight());
                when(auth.resource(request)).thenReturn(List.of(folder));
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
}

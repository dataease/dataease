package io.dataease.config;

import io.dataease.api.material.*;
import io.dataease.dao.auto.entity.DataVisualizationInfo;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.api.material.vo.MaterialBundle;
import io.dataease.api.template.request.TemplateManageRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.*;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.mock.web.*;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MaterialPersistenceTest {
    MaterialLifecycle lifecycle;
    PlatformTransactionManager manager;
    DataVisualizationInfoRepository target;
    DataVisualizationInfoRepository repository;

    @BeforeEach
    void setup() {
        new io.dataease.i18n.Translator().setMessageSource(new org.springframework.context.support.StaticMessageSource());
        lifecycle = mock(MaterialLifecycle.class);
        manager = mock(PlatformTransactionManager.class);
        when(manager.getTransaction(any())).thenAnswer(invocation -> new SimpleTransactionStatus());
        target = mock(DataVisualizationInfoRepository.class);
        StaticListableBeanFactory beans = new StaticListableBeanFactory();
        beans.addBean("materials", lifecycle);
        var factory = new AspectJProxyFactory(target);
        factory.setInterfaces(DataVisualizationInfoRepository.class);
        var aspect = new MaterialPersistenceAspect(beans.getBeanProvider(MaterialLifecycle.class), manager);
        org.springframework.test.util.ReflectionTestUtils.setField(aspect, "entityManager", mock(jakarta.persistence.EntityManager.class));
        factory.addAspect(aspect);
        repository = factory.getProxy();
    }

    DataVisualizationInfo resource() {
        var entity = new DataVisualizationInfo();
        entity.setId(101L);
        entity.setCanvasStyleData("{\"background\":\"material://123\"}");
        entity.setComponentData("[{\"component\":\"Picture\",\"propValue\":{\"url\":\"material://456\"}}]");
        return entity;
    }

    @Test void repositoryPointcutUsesMergedSavedContent() {
        var partial = new DataVisualizationInfo();
        partial.setId(101L);
        partial.setName("Rename only");
        when(target.saveAndFlush(partial)).thenReturn(resource());
        repository.saveAndFlush(partial);
        var ordered = inOrder(lifecycle, target, manager);
        ordered.verify(lifecycle).lock();
        ordered.verify(target).saveAndFlush(partial);
        ordered.verify(lifecycle).reconcile("core", "101", Set.of(123L, 456L));
        ordered.verify(manager).commit(any());
    }

    @Test void rejectedReferenceRollsBackTheResourceTransaction() {
        when(target.saveAndFlush(any())).thenReturn(resource());
        doThrow(new IllegalArgumentException("Pending material cannot be selected")).when(lifecycle)
                .reconcile(anyString(), anyString(), anySet());
        assertThrows(IllegalArgumentException.class, () -> repository.saveAndFlush(resource()));
        verify(manager).rollback(any());
        verify(manager, never()).commit(any());
    }

    @Test void bulkDeleteReleasesActualOwnersInsideTheTransaction() {
        when(target.findAllById(any())).thenReturn(List.of(resource()));
        repository.deleteAllByIdInBatch(List.of(101L));
        verify(lifecycle).release("core", "101");
        verify(manager).commit(any());
    }

    @Test void oldStaticRouteCannotReadEncodedManagedFiles() throws Exception {
        var interceptor = new StaticResourceSecurityInterceptor();
        for (String path : List.of("/de2api/static-resource/de-material-123.png",
                "/de2api/static-resource/de%2dmaterial-123.png",
                "/de2api/static-resource/.material-tmp/de-material-123.svg")) {
            var response = new MockHttpServletResponse();
            assertFalse(interceptor.preHandle(new MockHttpServletRequest("GET", path), response,
                    new ResourceHttpRequestHandler()));
            assertEquals(404, response.getStatus());
        }
        var response = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(new MockHttpServletRequest("GET", "/de2api/static-resource/legacy.png"),
                response, new ResourceHttpRequestHandler()));
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
    }

    @Test void importRemapsDocumentsAndRejectsMissingManifestFiles() throws Throwable {
        MaterialBundleService service = mock(MaterialBundleService.class);
        when(service.restore(any())).thenReturn(Map.of("material-file://1", "material://789"));
        StaticListableBeanFactory beans = new StaticListableBeanFactory();
        beans.addBean("bundles", service);
        var aspect = new MaterialImportAspect(beans.getBeanProvider(MaterialBundleService.class), manager);
        var request = new DataVisualizationBaseRequest();
        MaterialBundle bundle = new MaterialBundle();
        var file = new io.dataease.api.material.vo.MaterialBundleEntry();
        file.setReference("material-file://1");
        bundle.getFiles().add(file);
        request.setMaterialBundle(bundle);
        request.setCanvasStyleData("{\"background\":\"material-file://1\"}");
        request.setComponentData("[]");
        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(signature.getName()).thenReturn("saveCanvas");
        when(point.getSignature()).thenReturn(signature);
        when(point.getArgs()).thenReturn(new Object[]{request});
        when(point.proceed()).thenReturn("101");
        assertEquals("101", aspect.restore(point));
        assertTrue(request.getCanvasStyleData().contains("material://789"));
        request.setComponentData("[{\"component\":\"Picture\",\"propValue\":{\"url\":\"material-file://2\"}}]");
        assertThrows(io.dataease.exception.DEException.class, () -> aspect.restore(point));
    }

    @Test void materialReadUsesStoredResourceTypeAndDraftRequiresEditing() {
        var access = new io.dataease.visualization.manage.CoreMaterialResourceAccess();
        var resources = mock(DataVisualizationInfoRepository.class);
        var snapshots = mock(io.dataease.visualization.dao.auto.mapper.SnapshotDataVisualizationInfoRepository.class);
        var auth = mock(io.dataease.api.permissions.auth.api.InteractiveAuthApi.class);
        org.springframework.test.util.ReflectionTestUtils.setField(access, "resources", resources);
        org.springframework.test.util.ReflectionTestUtils.setField(access, "snapshots", snapshots);
        org.springframework.test.util.ReflectionTestUtils.setField(access, "auth", auth);
        var resource = resource();
        resource.setType("dataV");
        when(resources.findById(101L)).thenReturn(Optional.of(resource));
        var context = new io.dataease.api.material.dto.MaterialContext();
        context.setResourceId(101L);
        context.setResourceType("dashboard");
        try (var identity = mockStatic(io.dataease.permission.util.V3UserUtil.class)) {
            identity.when(io.dataease.permission.util.V3UserUtil::getUid).thenReturn(11L);
            assertEquals(Set.of(123L, 456L), access.authorize(context, false));
            verify(auth).checkAuth(new io.dataease.api.permissions.auth.dto.BusiPerCheckDTO(101L,
                    io.dataease.constant.BusiResourceEnum.SCREEN, io.dataease.constant.AuthEnum.READ));
            var snapshot = new io.dataease.visualization.dao.auto.entity.SnapshotDataVisualizationInfo();
            snapshot.setCanvasStyleData(resource.getCanvasStyleData());
            snapshot.setComponentData(resource.getComponentData());
            when(snapshots.findById(101L)).thenReturn(Optional.of(snapshot));
            context.setResourceTable("snapshot");
            access.authorize(context, false);
            verify(auth).checkAuth(new io.dataease.api.permissions.auth.dto.BusiPerCheckDTO(101L,
                    io.dataease.constant.BusiResourceEnum.SCREEN, io.dataease.constant.AuthEnum.MANAGE));
            doThrow(new io.dataease.exception.DEException(403, "Denied")).when(auth).checkAuth(any());
            assertThrows(io.dataease.exception.DEException.class, () -> access.authorize(context, false));
        }
    }

    @Test void editedImportDoesNotRestoreRemovedFilesOrRejectNormalSelections() throws Throwable {
        MaterialBundleService service = mock(MaterialBundleService.class);
        when(service.restore(any())).thenReturn(Map.of());
        var beans = new StaticListableBeanFactory();
        beans.addBean("bundles", service);
        var aspect = new MaterialImportAspect(beans.getBeanProvider(MaterialBundleService.class), manager);
        var request = new DataVisualizationBaseRequest();
        var bundle = new MaterialBundle();
        var file = new io.dataease.api.material.vo.MaterialBundleEntry();
        file.setReference("material-file://1");
        bundle.getFiles().add(file);
        request.setMaterialBundle(bundle);
        request.setCanvasStyleData("{\"background\":\"material://123\"}");
        request.setComponentData("[]");
        var point = mock(ProceedingJoinPoint.class);
        var signature = mock(Signature.class);
        when(signature.getName()).thenReturn("saveCanvas");
        when(point.getSignature()).thenReturn(signature);
        when(point.getArgs()).thenReturn(new Object[]{request});
        aspect.restore(point);
        verify(service).validate(bundle);
        var captured = org.mockito.ArgumentCaptor.forClass(MaterialBundle.class);
        verify(service).restore(captured.capture());
        assertTrue(captured.getValue().getFiles().isEmpty());
        assertTrue(request.getCanvasStyleData().contains("material://123"));
    }
}

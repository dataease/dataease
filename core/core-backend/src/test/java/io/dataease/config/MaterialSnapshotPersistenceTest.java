package io.dataease.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dataease.dao.auto.entity.CoreChartView;
import io.dataease.api.material.MaterialLifecycle;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.chart.dao.auto.mapper.CoreChartViewRepository;
import io.dataease.chart.manage.ChartDataManage;
import io.dataease.chart.manage.ChartViewManege;
import io.dataease.commons.constants.DataVisualizationConstants;
import io.dataease.constant.AuthEnum;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.dao.auto.entity.DataVisualizationInfo;
import io.dataease.exception.DEException;
import io.dataease.license.bo.F2CLicResult;
import io.dataease.license.utils.CommonBeanFactory;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.operation.manage.CoreOptRecentManage;
import io.dataease.permission.model.V3BaseUser;
import io.dataease.permission.template.V3AuthTemplate;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.template.dao.auto.entity.VisualizationTemplate;
import io.dataease.template.dao.auto.mapper.VisualizationTemplateRepository;
import io.dataease.utils.IDUtils;
import io.dataease.visualization.dao.auto.entity.SnapshotDataVisualizationInfo;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;
import io.dataease.visualization.dao.auto.mapper.SnapshotDataVisualizationInfoRepository;
import io.dataease.visualization.manage.CoreMaterialResourceAccess;
import io.dataease.visualization.manage.CoreVisualizationManage;
import io.dataease.visualization.manage.VisualizationLinkJumpManage;
import io.dataease.visualization.manage.VisualizationLinkageManage;
import io.dataease.visualization.server.DataVisualizationServer;
import io.dataease.visualization.server.StaticResourceServer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Real JPA transactions and repository proxies; never starts the application or reads its datasource. */
class MaterialSnapshotPersistenceTest {
    private static final long SOURCE_ID = 101L;
    private static final long COPY_ID = 201L;
    private static final long USER_ID = 11L;
    private static final long ORGANIZATION_ID = 20L;
    private static final String MATERIAL_STYLE = "{\"background\":\"material://501\"}";

    private LocalContainerEntityManagerFactoryBean factory;
    private EntityManager entityManager;
    private JpaTransactionManager manager;
    private TransactionTemplate transaction;
    private StaticListableBeanFactory beans;
    private MaterialPersistenceAspect materialAspect;
    private JpaRepositoryFactory repositories;
    private SnapshotDataVisualizationInfoRepository snapshots;
    private MaterialLifecycle lifecycle;

    @BeforeEach
    void setup() {
        var source = new DriverManagerDataSource(
                "jdbc:h2:mem:material-snapshot-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(source);
        factory.setManagedTypes(PersistenceManagedTypes.of(SnapshotDataVisualizationInfo.class.getName(),
                DataVisualizationInfo.class.getName(), VisualizationTemplate.class.getName(), CoreChartView.class.getName()));
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of(
                "hibernate.hbm2ddl.auto", "create-drop",
                "hibernate.jdbc.batch_size", "25"));
        factory.afterPropertiesSet();
        var entityManagerFactory = java.util.Objects.requireNonNull(factory.getObject());
        entityManager = SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
        manager = new JpaTransactionManager(entityManagerFactory);
        transaction = new TransactionTemplate(manager);
        lifecycle = mock(MaterialLifecycle.class);

        beans = new StaticListableBeanFactory();
        beans.addBean("materials", lifecycle);
        materialAspect = new MaterialPersistenceAspect(beans.getBeanProvider(MaterialLifecycle.class), manager);
        ReflectionTestUtils.setField(materialAspect, "entityManager", entityManager);
        repositories = new JpaRepositoryFactory(entityManager);
        snapshots = materialRepository(SnapshotDataVisualizationInfoRepository.class);
    }

    private <T extends JpaRepository<?, ?>> T materialRepository(Class<T> type) {
        var proxy = new AspectJProxyFactory(repositories.getRepository(type));
        proxy.setInterfaces(type);
        proxy.addAspect(materialAspect);
        proxy.addAspect(new JpaUpdateNonNullAspect());
        return proxy.getProxy();
    }

    @AfterEach
    void close() {
        if (factory != null) factory.destroy();
    }

    private SnapshotDataVisualizationInfo snapshot(long id, String name) {
        var value = new SnapshotDataVisualizationInfo();
        value.setId(id);
        value.setName(name);
        value.setType("dataV");
        value.setNodeType("panel");
        value.setCanvasStyleData("{}");
        value.setComponentData("[]");
        value.setStatus(1);
        value.setVersion(3);
        value.setDeleteFlag(false);
        value.setCreateBy("11");
        value.setCreateTime(1L);
        value.setContentId("legacy-content");
        value.setRemark("old remark");
        return value;
    }

    private void seed(SnapshotDataVisualizationInfo... values) {
        transaction.executeWithoutResult(status -> {
            for (var value : values) entityManager.persist(value);
        });
    }

    private SnapshotDataVisualizationInfo read(long id) {
        return transaction.execute(status -> snapshots.findById(id).orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(strings = {"dashboard", "dataV"})
    void publishedSnapshotCanBeRebuiltWithTheSameIdAndExplicitNulls(String type) {
        var original = snapshot(101L, "old published snapshot");
        original.setType(type);
        seed(original);
        var replacement = snapshot(101L, "recovered draft");
        replacement.setType(type);
        replacement.setStatus(0);
        replacement.setRemark(null);

        transaction.executeWithoutResult(status -> {
            snapshots.deleteAllByIdInBatch(Set.of(101L));
            var saved = snapshots.saveAndFlush(replacement);
            assertNull(saved.getRemark(), "A deleted snapshot must not supply non-null merged fields");
        });

        var saved = read(101L);
        assertEquals("recovered draft", saved.getName());
        assertEquals(type, saved.getType());
        assertEquals(0, saved.getStatus());
        assertNull(saved.getRemark());
        var ordered = inOrder(lifecycle);
        ordered.verify(lifecycle).lock();
        ordered.verify(lifecycle).release("snapshot", "101");
        ordered.verify(lifecycle).lock();
        ordered.verify(lifecycle).reconcile("snapshot", "101", Set.of());
    }

    @Test
    void missingSnapshotCanBeCreatedAfterBulkDeletion() {
        transaction.executeWithoutResult(status -> {
            snapshots.deleteAllByIdInBatch(Set.of(101L));
            snapshots.saveAndFlush(snapshot(101L, "first snapshot"));
        });

        assertEquals("first snapshot", read(101L).getName());
        verify(lifecycle, never()).release(anyString(), anyString());
        verify(lifecycle).reconcile("snapshot", "101", Set.of());
    }

    @Test
    void partialRebuildPreservesAnUnrelatedManagedEntityAndItsLaterChanges() {
        seed(snapshot(101L, "replace"), snapshot(102L, "remove"), snapshot(103L, "untouched"));

        transaction.executeWithoutResult(status -> {
            var unrelated = snapshots.findById(103L).orElseThrow();
            unrelated.setName("changed before delete");
            snapshots.deleteAllByIdInBatch(Set.of(101L, 102L));
            assertTrue(entityManager.contains(unrelated), "Bulk deletion must not clear unrelated managed entities");
            assertSame(unrelated, snapshots.findById(103L).orElseThrow());
            unrelated.setName("changed after delete");
            snapshots.saveAndFlush(snapshot(101L, "rebuilt"));
        });

        assertEquals("rebuilt", read(101L).getName());
        assertEquals("changed after delete", read(103L).getName());
        transaction.executeWithoutResult(status -> {
            assertFalse(snapshots.existsById(102L));
            assertEquals(2L, snapshots.count());
        });
        verify(lifecycle).release("snapshot", "101");
        verify(lifecycle).release("snapshot", "102");
        verify(lifecycle).reconcile("snapshot", "101", Set.of());
    }

    @Test
    void releaseFailureRollsBackDeletionAndOtherFlushedChanges() {
        seed(snapshot(101L, "original"), snapshot(103L, "unrelated"));
        doThrow(new IllegalStateException("release failed")).when(lifecycle).release("snapshot", "101");

        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            snapshots.findById(103L).orElseThrow().setName("must roll back");
            snapshots.deleteAllByIdInBatch(Set.of(101L));
        }));

        assertEquals("original", read(101L).getName());
        assertEquals("unrelated", read(103L).getName());
        verify(lifecycle, never()).reconcile(anyString(), anyString(), anySet());
    }

    @Test
    void reconcileFailureRollsBackDeletionAndRebuildTogether() {
        seed(snapshot(101L, "original"));
        doThrow(new IllegalStateException("reconcile failed")).when(lifecycle)
                .reconcile("snapshot", "101", Set.of());

        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            snapshots.deleteAllByIdInBatch(Set.of(101L));
            snapshots.saveAndFlush(snapshot(101L, "must roll back"));
        }));

        assertEquals("original", read(101L).getName());
        verify(lifecycle).release("snapshot", "101");
        verify(lifecycle).reconcile("snapshot", "101", Set.of());
    }

    @Test
    void noArgumentBulkDeleteAllowsRebuildingASnapshot() {
        seed(snapshot(101L, "replace"), snapshot(102L, "remove"));

        transaction.executeWithoutResult(status -> {
            snapshots.findById(101L).orElseThrow();
            snapshots.deleteAllInBatch();
            snapshots.saveAndFlush(snapshot(101L, "rebuilt"));
        });

        assertEquals("rebuilt", read(101L).getName());
        transaction.executeWithoutResult(status -> {
            assertFalse(snapshots.existsById(102L));
            assertEquals(1L, snapshots.count());
        });
        verify(lifecycle).release("snapshot", "101");
        verify(lifecycle).release("snapshot", "102");
    }

    @Test
    void bulkDeleteWithDetachedInputRemovesTheAlreadyManagedRowBeforeRebuild() {
        seed(snapshot(101L, "original"), snapshot(102L, "keep"));
        var detached = read(101L);

        transaction.executeWithoutResult(status -> {
            var managed = snapshots.findById(101L).orElseThrow();
            assertNotSame(detached, managed);
            assertFalse(entityManager.contains(detached));
            snapshots.deleteAllInBatch(List.of(detached));
            var replacement = snapshot(101L, "rebuilt");
            replacement.setRemark(null);
            snapshots.saveAndFlush(replacement);
        });

        assertEquals("rebuilt", read(101L).getName());
        assertNull(read(101L).getRemark());
        assertEquals("keep", read(102L).getName());
        verify(lifecycle).release("snapshot", "101");
        verify(lifecycle).reconcile("snapshot", "101", Set.of());
    }

    @Nested
    class MaterialInheritance {
        private DataVisualizationInfoRepository resources;
        private VisualizationTemplateRepository templates;
        private InteractiveAuthApi auth;
        private DataVisualizationServer server;
        private MockedStatic<CommonBeanFactory> licenseBeans;
        private StaticApplicationContext licenseContext;
        private MockedStatic<IDUtils> ids;
        private F2CLicResult previousLicense;

        @BeforeEach
        void setupServer() {
            resources = materialRepository(DataVisualizationInfoRepository.class);
            templates = materialRepository(VisualizationTemplateRepository.class);
            auth = mock(InteractiveAuthApi.class);
            beans.addBean("auth", auth);
            var access = new CoreMaterialResourceAccess(beans.getBeanProvider(InteractiveAuthApi.class));
            ReflectionTestUtils.setField(access, "resources", resources);
            ReflectionTestUtils.setField(access, "templates", templates);
            doAnswer(invocation -> {
                invocation.<Runnable>getArgument(1).run();
                return null;
            }).when(lifecycle).inherit(anySet(), any(Runnable.class));

            var recent = mock(CoreOptRecentManage.class);
            var manage = new CoreVisualizationManage();
            ReflectionTestUtils.setField(manage, "dataVisualizationInfoRepository", resources);
            ReflectionTestUtils.setField(manage, "snapshotDataVisualizationInfoRepository", snapshots);
            ReflectionTestUtils.setField(manage, "coreOptRecentManage", recent);
            var target = new DataVisualizationServer();
            ReflectionTestUtils.setField(target, "queryFactory", new JPAQueryFactory(entityManager));
            ReflectionTestUtils.setField(target, "dataVisualizationInfoRepository", resources);
            ReflectionTestUtils.setField(target, "snapshotDataVisualizationInfoRepository", snapshots);
            ReflectionTestUtils.setField(target, "visualizationTemplateRepository", templates);
            ReflectionTestUtils.setField(target, "coreVisualizationManage", manage);
            ReflectionTestUtils.setField(target, "coreOptRecentManage", recent);
            ReflectionTestUtils.setField(target, "chartViewManege", mock(ChartViewManege.class));
            ReflectionTestUtils.setField(target, "chartDataManage", mock(ChartDataManage.class));
            ReflectionTestUtils.setField(target, "coreChartViewRepository", mock(CoreChartViewRepository.class));
            ReflectionTestUtils.setField(target, "visualizationLinkageManage", mock(VisualizationLinkageManage.class));
            ReflectionTestUtils.setField(target, "visualizationLinkJumpManage", mock(VisualizationLinkJumpManage.class));
            ReflectionTestUtils.setField(target, "staticResourceServer", mock(StaticResourceServer.class));
            var proxy = new AspectJProxyFactory(target);
            proxy.setProxyTargetClass(true);
            proxy.addAspect(new MaterialInheritanceAspect(beans.getBeanProvider(MaterialLifecycle.class), access));
            var transactionAdvice = new TransactionInterceptor();
            transactionAdvice.setTransactionManager(manager);
            transactionAdvice.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
            proxy.addAdvice(0, transactionAdvice);
            server = proxy.getProxy();

            var user = new V3BaseUser();
            user.setOid(ORGANIZATION_ID);
            V3AuthTemplate users = mock(V3AuthTemplate.class);
            when(users.getUserInfo(USER_ID)).thenReturn(user);
            licenseContext = new StaticApplicationContext();
            licenseContext.refresh();
            licenseBeans = mockStatic(CommonBeanFactory.class);
            licenseBeans.when(CommonBeanFactory::getApplicationContext).thenReturn(licenseContext);
            licenseBeans.when(() -> CommonBeanFactory.getBean(V3AuthTemplate.class)).thenReturn(users);
            previousLicense = LicenseUtil.get();
            var validLicense = new F2CLicResult();
            validLicense.setStatus(F2CLicResult.Status.valid);
            LicenseUtil.set(validLicense);
            V3UserUtil.setUid(USER_ID);
            ids = mockStatic(IDUtils.class);
            ids.when(IDUtils::snowID).thenReturn(COPY_ID, 901L);
        }

        @AfterEach
        void clearIdentity() {
            V3UserUtil.clear();
            if (ids != null) ids.close();
            if (licenseBeans != null) {
                LicenseUtil.set(previousLicense);
                licenseBeans.close();
            }
            if (licenseContext != null) licenseContext.close();
        }

        private void seedResource(String type) {
            var source = new DataVisualizationInfo();
            source.setId(SOURCE_ID);
            source.setName("Source canvas");
            source.setPid(10L);
            source.setType(type);
            source.setNodeType("leaf");
            source.setDeleteFlag(false);
            source.setCanvasStyleData(MATERIAL_STYLE);
            source.setComponentData("[]");
            transaction.executeWithoutResult(status -> entityManager.persist(source));
        }

        private DataVisualizationBaseRequest canvas(String type) {
            var request = new DataVisualizationBaseRequest();
            request.setName("New canvas");
            request.setType(type);
            request.setPid(10L);
            request.setCanvasStyleData(MATERIAL_STYLE);
            request.setComponentData("[]");
            request.setCanvasViewInfo(Map.of());
            return request;
        }

        private String seedTemplate() {
            var source = new VisualizationTemplate();
            source.setId("template-1");
            source.setName("Saved template");
            source.setDvType("dashboard");
            source.setTemplateStyle(MATERIAL_STYLE);
            source.setTemplateData("[]");
            source.setDynamicData("{}");
            transaction.executeWithoutResult(status -> entityManager.persist(source));
            return source.getId();
        }

        private void assertSavedMaterials(long id, String style) {
            var resource = transaction.execute(status -> resources.findById(id).orElseThrow());
            assertEquals(style, resource.getCanvasStyleData());
            assertEquals(ORGANIZATION_ID, resource.getOrgId());
            assertEquals(style, read(id).getCanvasStyleData());
        }

        @ParameterizedTest
        @ValueSource(strings = {"dashboard", "dataV"})
        void copyAndFinalSaveInheritTheAuthorizedSource(String type) throws Exception {
            seedResource(type);
            var request = canvas(type);
            request.setId(SOURCE_ID);
            var copyId = Long.parseLong(server.copy(request));
            assertNotEquals(SOURCE_ID, copyId);
            assertSavedMaterials(copyId, MATERIAL_STYLE);
            var permission = new BusiPerCheckDTO(SOURCE_ID,
                    "dataV".equals(type) ? BusiResourceEnum.SCREEN : BusiResourceEnum.PANEL, AuthEnum.MANAGE);
            var ordered = inOrder(auth, lifecycle);
            ordered.verify(auth).checkAuth(permission);
            ordered.verify(lifecycle).inherit(eq(Set.of(501L)), any(Runnable.class));
            ordered.verify(lifecycle).reconcile("core", Long.toString(copyId), Set.of(501L));
            ordered.verify(lifecycle).reconcile("snapshot", Long.toString(copyId), Set.of(501L));

            request.setId(copyId);
            request.setOptType(DataVisualizationConstants.RESOURCE_OPT_TYPE.COPY);
            assertEquals(Long.toString(copyId), server.saveCanvas(request));
            assertSavedMaterials(copyId, MATERIAL_STYLE);
            ordered.verify(lifecycle).inherit(eq(Set.of(501L)), any(Runnable.class));
            ordered.verify(lifecycle).release("core", Long.toString(copyId));
            ordered.verify(lifecycle).release("snapshot", Long.toString(copyId));
            ordered.verify(lifecycle).reconcile("core", Long.toString(copyId), Set.of(501L));
            ordered.verify(lifecycle).reconcile("snapshot", Long.toString(copyId), Set.of(501L));
        }

        @Test
        void sourceReadPermissionAloneCannotCopy() {
            seedResource("dashboard");
            doThrow(new DEException(403, "Manage permission required")).when(auth)
                    .checkAuth(new BusiPerCheckDTO(SOURCE_ID, BusiResourceEnum.PANEL, AuthEnum.MANAGE));
            var request = canvas("dashboard");
            request.setId(SOURCE_ID);

            assertThrows(DEException.class, () -> server.copy(request));
            verifyNoInteractions(lifecycle);
            assertFalse(resources.existsById(COPY_ID));
            assertFalse(snapshots.existsById(COPY_ID));
        }

        @Test
        void templateCreationInheritsOnlyReferencesFromItsSavedSource() throws Exception {
            var templateId = seedTemplate();
            var previewRequest = new DataVisualizationBaseRequest();
            previewRequest.setNewFrom(DataVisualizationConstants.NEW_PANEL_FROM.NEW_INNER_TEMPLATE);
            previewRequest.setTemplateId(templateId);
            var preview = server.decompression(previewRequest);
            assertEquals(templateId, preview.getMaterialSourceTemplateId());
            assertEquals(MATERIAL_STYLE, preview.getCanvasStyleData());

            var request = canvas(preview.getType());
            request.setId(preview.getId());
            request.setMaterialSourceTemplateId(preview.getMaterialSourceTemplateId());
            // An ordinary new selection can be reconciled without becoming an inherited reference.
            request.setComponentData("[{\"component\":\"Picture\",\"propValue\":{\"url\":\"material://502\"}}]");
            assertEquals(Long.toString(preview.getId()), server.saveCanvas(request));
            assertSavedMaterials(preview.getId(), MATERIAL_STYLE);
            verify(lifecycle).inherit(eq(Set.of(501L)), any(Runnable.class));
            verify(lifecycle).reconcile("core", Long.toString(preview.getId()), Set.of(501L, 502L));
            verify(lifecycle).reconcile("snapshot", Long.toString(preview.getId()), Set.of(501L, 502L));
        }

        @Test
        void templateIdCannotBypassSourcePermissionWhenFinalizingACopy() {
            seedResource("dashboard");
            seed(snapshot(SOURCE_ID, "Source draft"));
            var request = canvas("dashboard");
            request.setId(SOURCE_ID);
            request.setOptType(DataVisualizationConstants.RESOURCE_OPT_TYPE.COPY);
            request.setMaterialSourceTemplateId(seedTemplate());
            var permission = new BusiPerCheckDTO(SOURCE_ID, BusiResourceEnum.PANEL, AuthEnum.MANAGE);
            doThrow(new DEException(403, "Manage permission required")).when(auth).checkAuth(permission);

            assertThrows(DEException.class, () -> server.saveCanvas(request));
            verify(auth).checkAuth(permission);
            verifyNoInteractions(lifecycle);
            assertEquals("Source canvas", resources.findById(SOURCE_ID).orElseThrow().getName());
            assertEquals("Source draft", read(SOURCE_ID).getName());
        }

        @Test
        void nonexistentTemplateCannotSupplyInheritedReferences() {
            var request = canvas("dashboard");
            request.setId(COPY_ID);
            request.setMaterialSourceTemplateId("missing-template");

            assertThrows(DEException.class, () -> server.saveCanvas(request));
            verifyNoInteractions(lifecycle);
            assertFalse(resources.existsById(COPY_ID));
            assertFalse(snapshots.existsById(COPY_ID));
        }

        @Test
        void copyRollsBackBothRowsWhenSnapshotReferencesCannotBeSaved() {
            seedResource("dashboard");
            var request = canvas("dashboard");
            request.setId(SOURCE_ID);
            var failure = new IllegalStateException("Reference registration failed");
            doThrow(failure).when(lifecycle).reconcile("snapshot", Long.toString(COPY_ID), Set.of(501L));

            assertSame(failure, assertThrows(IllegalStateException.class, () -> server.copy(request)));
            assertFalse(resources.existsById(COPY_ID));
            assertFalse(snapshots.existsById(COPY_ID));
            assertTrue(resources.existsById(SOURCE_ID));
            verify(lifecycle).reconcile("core", Long.toString(COPY_ID), Set.of(501L));
        }
    }
}

package io.dataease.config;

import io.dataease.api.material.MaterialLifecycle;
import io.dataease.visualization.dao.auto.entity.SnapshotDataVisualizationInfo;
import io.dataease.visualization.dao.auto.mapper.SnapshotDataVisualizationInfoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.util.ReflectionTestUtils;
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
    private LocalContainerEntityManagerFactoryBean factory;
    private EntityManager entityManager;
    private TransactionTemplate transaction;
    private SnapshotDataVisualizationInfoRepository snapshots;
    private MaterialLifecycle lifecycle;

    @BeforeEach
    void setup() {
        var source = new DriverManagerDataSource(
                "jdbc:h2:mem:material-snapshot-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(source);
        factory.setManagedTypes(PersistenceManagedTypes.of(SnapshotDataVisualizationInfo.class.getName()));
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of(
                "hibernate.hbm2ddl.auto", "create-drop",
                "hibernate.jdbc.batch_size", "25"));
        factory.afterPropertiesSet();
        var entityManagerFactory = java.util.Objects.requireNonNull(factory.getObject());
        entityManager = SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
        var manager = new JpaTransactionManager(entityManagerFactory);
        transaction = new TransactionTemplate(manager);
        lifecycle = mock(MaterialLifecycle.class);

        var beans = new StaticListableBeanFactory();
        beans.addBean("materials", lifecycle);
        var materialAspect = new MaterialPersistenceAspect(beans.getBeanProvider(MaterialLifecycle.class), manager);
        ReflectionTestUtils.setField(materialAspect, "entityManager", entityManager);
        var target = new JpaRepositoryFactory(entityManager)
                .getRepository(SnapshotDataVisualizationInfoRepository.class);
        var proxy = new AspectJProxyFactory(target);
        proxy.setInterfaces(SnapshotDataVisualizationInfoRepository.class);
        proxy.addAspect(materialAspect);
        proxy.addAspect(new JpaUpdateNonNullAspect());
        snapshots = proxy.getProxy();
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
}

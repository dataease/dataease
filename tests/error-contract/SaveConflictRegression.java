package io.dataease.regression;

import io.dataease.dao.auto.entity.DataVisualizationInfo;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;
import io.dataease.visualization.server.DataVisualizationServer;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.exception.*;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.concurrent.*;

/** Isolated in-memory database. Exercise the production lock query and save conflict branch. */
public class SaveConflictRegression {
    public static void main(String[] args) throws Exception {
        var messages = new io.dataease.i18n.DeReloadableResourceBundleMessageSource();
        messages.setBasename("classpath:i18n/core");
        new io.dataease.i18n.Translator().setMessageSource(messages);
        var configuration = new org.hibernate.cfg.Configuration().addAnnotatedClass(DataVisualizationInfo.class)
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:error_contract;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.show_sql", "false");
        try (var factory = configuration.buildSessionFactory()) {
            var em = SharedEntityManagerCreator.createSharedEntityManager(factory);
            var repo = new JpaRepositoryFactory(em).getRepository(DataVisualizationInfoRepository.class);
            var tx = new TransactionTemplate(new JpaTransactionManager(factory));
            tx.executeWithoutResult(status -> {
                var info = new DataVisualizationInfo();
                info.setId(1L); info.setContentId("old"); info.setDeleteFlag(false);
                repo.saveAndFlush(info);
            });
            var server = new DataVisualizationServer();
            ReflectionTestUtils.setField(server, "dataVisualizationInfoRepository", repo);
            var held = new CountDownLatch(1);
            var release = new CountDownLatch(1);
            try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {
                var writer = pool.submit(() -> tx.executeWithoutResult(status -> {
                    var info = repo.lockForSave(1L).orElseThrow();
                    info.setContentId("other-writer");
                    held.countDown();
                    try { if (!release.await(5, TimeUnit.SECONDS)) throw new AssertionError("release timeout"); }
                    catch (InterruptedException e) { throw new RuntimeException(e); }
                }));
                if (!held.await(5, TimeUnit.SECONDS)) throw new AssertionError("writer did not acquire lock");
                var request = new DataVisualizationBaseRequest();
                request.setId(1L); request.setCheckHistory(true); request.setExpectedContentId("old");
                request.setContentId("my-new-content");
                var attempted = new CountDownLatch(1);
                var reader = pool.submit(() -> {
                    attempted.countDown();
                    try { tx.executeWithoutResult(status -> server.updateCanvas(request)); }
                    catch (DEException failure) { return failure; }
                    throw new AssertionError("stale save was accepted");
                });
                attempted.await(5, TimeUnit.SECONDS);
                Thread.sleep(100);
                if (reader.isDone()) throw new AssertionError("save bypassed row lock");
                release.countDown();
                writer.get(5, TimeUnit.SECONDS);
                var failure = reader.get(5, TimeUnit.SECONDS);
                if (failure.getCategory() != ErrorCategory.CONFLICT || failure.getCode() != 50005) throw new AssertionError("wrong conflict");
                if (!"other-writer".equals(((java.util.Map<?, ?>) failure.getErrorData()).get("currentContentId"))) throw new AssertionError("wrong confirmed version");
                if (!"other-writer".equals(repo.findById(1L).orElseThrow().getContentId())) throw new AssertionError("stale content was persisted");
                if (!"my-new-content".equals(request.getContentId())) throw new AssertionError("input was mutated");
            }
            System.out.println("PASS concurrent save rejects stale version and preserves persisted/user content");
        }
    }
}

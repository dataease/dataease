package io.dataease.ai;

import io.dataease.ai.dao.entity.CoreAiTask;
import io.dataease.ai.dao.entity.CoreAiTaskAttempt;
import io.dataease.ai.dao.entity.CoreAiTaskNode;
import io.dataease.dao.auto.entity.DeStandaloneVersion;
import io.dataease.dao.auto.repo.DeStandaloneVersionRepository;
import io.dataease.extensions.datasource.utils.SpringContextUtil;
import io.dataease.listener.InitSqlListener;
import io.dataease.listener.sql.AiTaskDeliveryInit;
import io.dataease.listener.sql.AiTaskInit;
import io.dataease.listener.sql.DatasourceMetadataInit;
import org.hibernate.cfg.Configuration;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.SharedEntityManagerCreator;

import java.util.List;

/** Startup migration boundary: real version repository, SqlBlocks and isolated H2, with ddl-auto disabled. */
public final class TaskMigrationRegression {
    public static void main(String[] args) {
        upgrade("2.48");
        upgrade("2.47");
        upgrade("2.49");
        System.out.println("PASS startup upgrade from semantic 2.48, pre-semantic 2.47 and task 2.49 creates AI tables, backfills delivery, preserves semantic rows and repeats safely");
    }

    private static void upgrade(String installedVersion) {
        String url = "jdbc:h2:mem:ai_task_upgrade_" + installedVersion.replace('.', '_') + ";DB_CLOSE_DELAY=-1";
        var configuration = new Configuration()
                .addAnnotatedClass(DeStandaloneVersion.class)
                .addAnnotatedClass(CoreAiTask.class)
                .addAnnotatedClass(CoreAiTaskAttempt.class)
                .addAnnotatedClass(CoreAiTaskNode.class)
                .setProperty("hibernate.connection.url", url)
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.username", "sa")
                .setProperty("hibernate.connection.password", "")
                .setProperty("hibernate.hbm2ddl.auto", "none");
        try (var factory = configuration.buildSessionFactory(); var context = new StaticApplicationContext()) {
            var em = SharedEntityManagerCreator.createSharedEntityManager(factory);
            var manager = new JpaTransactionManager(factory);
            var jdbc = new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
            jdbc.execute("create table de_standalone_version (installed_rank integer primary key, version varchar(50), description varchar(200) not null, type varchar(20) not null, script varchar(1000) not null, checksum integer, installed_by varchar(100) not null, installed_on timestamp not null, execution_time integer not null, success boolean not null)");
            jdbc.update("insert into de_standalone_version values (1, ?, 'existing upgrade', 'SQL', 'existing.sql', 0, 'system', CURRENT_TIMESTAMP, 0, true)", installedVersion);
            var metadata = new DatasourceMetadataInit(factory, jdbc);
            if (!"2.47".equals(installedVersion)) {
                metadata.execute();
                jdbc.update("insert into core_datasource_table_semantic (id, datasource_id, table_name, active, confirmed, available) values (81, 7, 'orders', true, false, true)");
            }
            if ("2.49".equals(installedVersion)) {
                new AiTaskInit(jdbc, factory, em, manager).execute();
                jdbc.execute("alter table core_ai_task_attempt drop column delivery_expires_at");
                jdbc.execute("alter table core_ai_task_attempt drop column notice_ack_at");
                jdbc.update("insert into core_ai_task_attempt (id, task_id, attempt_number, retry_hash, status, created_at, finished_at) values (777, 777, 1, 'legacy-upgrade', 'COMPLETED', 1000, 2000)");
            }
            var beans = context.getBeanFactory();
            beans.registerSingleton("transactionManager", manager);
            var repository = new JpaRepositoryFactoryBean<DeStandaloneVersionRepository, DeStandaloneVersion, Long>(DeStandaloneVersionRepository.class);
            repository.setEntityManager(em);
            beans.registerSingleton("entityPathResolver", org.springframework.data.querydsl.SimpleEntityPathResolver.INSTANCE);
            repository.setEntityPathResolver(beans.getBeanProvider(org.springframework.data.querydsl.EntityPathResolver.class));
            repository.setBeanFactory(beans);
            repository.afterPropertiesSet();
            beans.registerSingleton("versions", repository.getObject());
            // Register out of order so production startup must sort by migration version.
            beans.registerSingleton("taskDelivery", new AiTaskDeliveryInit(jdbc, factory));
            beans.registerSingleton("task", new AiTaskInit(jdbc, factory, em, manager));
            beans.registerSingleton("metadata", metadata);
            new SpringContextUtil().setApplicationContext(context);
            var listener = new InitSqlListener();
            var injection = new org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor();
            injection.setBeanFactory(beans);
            injection.processInjection(listener);
            listener.run(null);
            for (String table : List.of("core_ai_task", "core_ai_task_attempt", "core_ai_task_node", "core_datasource_table_semantic", "core_datasource_field_semantic", "core_datasource_relation", "core_datasource_relation_condition")) {
                jdbc.queryForObject("select count(*) from " + table, Long.class);
            }
            jdbc.update("insert into core_ai_task_attempt (id, task_id, attempt_number, retry_hash, status, created_at, finished_at, delivery_expires_at, notice_ack_at) values (1, 1, 1, 'migration-result', 'COMPLETED', 1000, 2000, 3000, 2000)");
            if (jdbc.queryForObject("select delivery_expires_at from core_ai_task_attempt where id = 1", Long.class) != 3000L) throw new AssertionError("delivery facts unavailable after startup");
            var versions = jdbc.queryForList("select version from de_standalone_version where success = true order by installed_rank", String.class);
            var expected = switch (installedVersion) {
                case "2.47" -> List.of("2.47", "2.48", "2.49", "2.50");
                case "2.48" -> List.of("2.48", "2.49", "2.50");
                default -> List.of("2.49", "2.50");
            };
            if (!"2.47".equals(installedVersion) && jdbc.queryForObject("select count(*) from core_datasource_table_semantic where id = 81 and table_name = 'orders'", Integer.class) != 1) throw new AssertionError("semantic content lost during task upgrade");
            if ("2.49".equals(installedVersion) && (jdbc.queryForObject("select delivery_expires_at from core_ai_task_attempt where id = 777", Long.class) != 604802000L
                    || jdbc.queryForObject("select notice_ack_at from core_ai_task_attempt where id = 777", Long.class) != 2000L)) throw new AssertionError("startup must preserve original completion expiry and acknowledge legacy result");
            if (!versions.equals(expected)) throw new AssertionError("startup skipped or reordered a migration: " + versions);
            listener.run(null);
            if (jdbc.queryForObject("select count(*) from de_standalone_version", Integer.class) != expected.size()
                    || jdbc.queryForObject("select count(*) from core_ai_task_attempt where id = 1", Integer.class) != 1) throw new AssertionError("repeat startup must preserve task and migration records");
        }
    }
}

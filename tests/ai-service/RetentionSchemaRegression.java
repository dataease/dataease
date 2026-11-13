package io.dataease.ai;

import io.dataease.ai.dao.entity.CoreAiAuditRetired;
import io.dataease.listener.sql.AiRetentionInit;
import org.hibernate.cfg.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.SharedEntityManagerCreator;

/** Vendor dialect registries over owned H2 only; does not claim live vendor database coverage. */
public final class RetentionSchemaRegression {
    public static void run() {
        verify("MySQLDialect", "MySQL"); verify("PostgreSQLDialect", "PostgreSQL"); verify("H2Dialect", "REGULAR");
        System.out.println("PASS retention 2.55 and audit recovery 2.57 partial-upgrade retry preserve original expiries/configuration with MySQL/PostgreSQL/H2 dialect registries over isolated H2");
    }
    private static void verify(String dialect, String mode) {
        String url = "jdbc:h2:mem:retention_ddl_" + mode + ";MODE=" + mode + ";DB_CLOSE_DELAY=-1";
        var config = new Configuration().addAnnotatedClass(CoreAiAuditRetired.class)
                .setProperty("hibernate.connection.url", url).setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.username", "sa").setProperty("hibernate.connection.password", "")
                .setProperty("hibernate.dialect", "org.hibernate.dialect." + dialect).setProperty("hibernate.hbm2ddl.auto", "none");
        try (var factory = config.buildSessionFactory()) {
            var jdbc = new JdbcTemplate(new DriverManagerDataSource(url,"sa",""));
            jdbc.execute("create table core_ai_setting(id bigint primary key,enabled boolean,audit_retention_days integer)");
            jdbc.update("insert into core_ai_setting values(1,true,45)");
            jdbc.execute("create table core_ai_task_attempt(id bigint primary key,status varchar(24),delivery_expires_at bigint,result_json varchar(200))");
            jdbc.update("insert into core_ai_task_attempt values(1,'COMPLETED',12345,'kept-body')");
            var migration = new AiRetentionInit(jdbc,factory,SharedEntityManagerCreator.createSharedEntityManager(factory),new JpaTransactionManager(factory));
            migration.execute(); migration.execute();
            if (jdbc.queryForObject("select audit_retention_days from core_ai_setting where id=1", Integer.class) != 45
                    || jdbc.queryForObject("select delivery_retention_days from core_ai_setting where id=1", Integer.class) != 7
                    || !jdbc.queryForObject("select enabled from core_ai_setting where id=1", Boolean.class)
                    || jdbc.queryForObject("select delivery_expires_at from core_ai_task_attempt where id=1", Long.class) != 12345
                    || !"kept-body".equals(jdbc.queryForObject("select result_json from core_ai_task_attempt where id=1", String.class))) throw new AssertionError("partial upgrade lost original facts: " + dialect);
            jdbc.update("update core_ai_setting set audit_retention_days=90,delivery_retention_days=14 where id=1");
            migration.execute();
            if (jdbc.queryForObject("select delivery_retention_days from core_ai_setting where id=1", Integer.class) != 14) throw new AssertionError("migration rerun resets administrator retention");
            jdbc.queryForObject("select count(*) from core_ai_audit_retired", Long.class);
            jdbc.execute("create table core_ai_audit_execution(id varchar(100) primary key,started_at bigint,finished_at bigint,retention_expires_at bigint,status varchar(24),retention_days integer)");
            jdbc.update("insert into core_ai_audit_execution values('ended',100,200,12345,'COMPLETED',null),('old-running',1000,null,null,'RUNNING',null),('partial-upgrade',1000,null,null,'RUNNING',45)");
            var recovery = new io.dataease.listener.sql.AiAuditRecoveryInit(jdbc, factory);
            recovery.execute(); recovery.execute();
            if (jdbc.queryForObject("select retention_expires_at from core_ai_audit_execution where id='ended'", Long.class) != 12345
                    || jdbc.queryForObject("select finished_at from core_ai_audit_execution where id='ended'", Long.class) != 200
                    || jdbc.queryForObject("select deadline from core_ai_audit_execution where id='old-running'", Long.class) != 301000
                    || jdbc.queryForObject("select retention_days from core_ai_audit_execution where id='old-running'", Integer.class) != 90
                    || jdbc.queryForObject("select retention_days from core_ai_audit_execution where id='partial-upgrade'", Integer.class) != 45)
                throw new AssertionError("2.57 preserves terminal facts and freezes a conservative legacy recovery baseline: " + dialect);
            jdbc.update("update core_ai_setting set audit_retention_days=7 where id=1");
            jdbc.update("update core_ai_audit_execution set deadline=400000 where id='partial-upgrade'"); recovery.execute();
            if (jdbc.queryForObject("select retention_days from core_ai_audit_execution where id='old-running'", Integer.class) != 90
                    || jdbc.queryForObject("select deadline from core_ai_audit_execution where id='partial-upgrade'", Long.class) != 400000)
                throw new AssertionError("2.57 retry cannot replace an already frozen policy or deadline");
        }
    }
}

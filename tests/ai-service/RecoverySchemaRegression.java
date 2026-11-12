package io.dataease.ai;

import io.dataease.ai.dao.entity.CoreAiSetting;
import io.dataease.ai.dao.entity.CoreAiTaskAttempt;
import io.dataease.listener.sql.AiRecoveryInit;
import org.hibernate.cfg.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/** Real Hibernate dialect registry + public migration on owned H2; not a live vendor database claim. */
public final class RecoverySchemaRegression {
    public static void main(String[] args) {
        verify("MySQLDialect", "MySQL");
        verify("PostgreSQLDialect", "PostgreSQL");
        verify("H2Dialect", "REGULAR");
        System.out.println("PASS recovery migration real MySQL/PostgreSQL/H2 dialect registries, additive DDL and repeated execution (isolated H2 backing)");
    }
    private static void verify(String dialect, String mode) {
        String url = "jdbc:h2:mem:recovery_ddl_" + mode + ";MODE=" + mode + ";DB_CLOSE_DELAY=-1";
        var configuration = new Configuration().addAnnotatedClass(CoreAiSetting.class).addAnnotatedClass(CoreAiTaskAttempt.class)
                .setProperty("hibernate.connection.url", url).setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.username", "sa").setProperty("hibernate.connection.password", "")
                .setProperty("hibernate.dialect", "org.hibernate.dialect." + dialect).setProperty("hibernate.hbm2ddl.auto", "none");
        try (var factory = configuration.buildSessionFactory()) {
            var jdbc = new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
            jdbc.execute("create table core_ai_setting (id bigint primary key, enabled boolean)");
            jdbc.update("insert into core_ai_setting values (1,true)");
            jdbc.execute("create table core_ai_task_attempt (id bigint primary key)");
            jdbc.update("insert into core_ai_task_attempt values (1)");
            var migration = new AiRecoveryInit(jdbc, factory);
            migration.execute(); migration.execute();
            if (jdbc.queryForObject("select retry_safe from core_ai_task_attempt where id=1", Boolean.class)
                    || jdbc.queryForObject("select recovery_step_retries from core_ai_setting where id=1", Integer.class) != 2
                    || jdbc.queryForObject("select recovery_attempt_retries from core_ai_setting where id=1", Integer.class) != 3
                    || jdbc.queryForObject("select recovery_initial_delay_millis from core_ai_setting where id=1", Integer.class) != 1000
                    || !jdbc.queryForObject("select enabled from core_ai_setting where id=1", Boolean.class)) throw new AssertionError("additive recovery defaults and existing AI data: " + dialect);
        }
    }
}

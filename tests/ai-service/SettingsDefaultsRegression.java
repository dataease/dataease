package io.dataease.ai;

import io.dataease.listener.sql.AiSettingsDefaultsInit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public final class SettingsDefaultsRegression {
    public static void run() {
        var jdbc = new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:ai-settings-defaults;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("create table core_ai_setting (id int, task_max_running int, task_max_queued int, task_max_queued_per_user int, task_max_wait_seconds int, recovery_initial_delay_millis int, audit_retention_days int, delivery_retention_days int, revision int, row_version int, recovery_step_retries int, recovery_attempt_retries int, max_output_tokens int, max_input_chars int)");
        jdbc.update("insert into core_ai_setting values (1,0,0,0,0,0,0,0,8,4,0,0,4096,30000)");
        jdbc.update("insert into core_ai_setting values (2,3,42,6,99,250,60,14,9,5,0,0,0,0)");
        var migration = new AiSettingsDefaultsInit(jdbc);
        migration.execute();
        var repaired = jdbc.queryForMap("select * from core_ai_setting where id=1");
        for (var pair : java.util.Map.of("TASK_MAX_RUNNING", 2, "TASK_MAX_QUEUED", 100, "TASK_MAX_QUEUED_PER_USER", 10, "TASK_MAX_WAIT_SECONDS", 300, "RECOVERY_INITIAL_DELAY_MILLIS", 1000, "AUDIT_RETENTION_DAYS", 30, "DELIVERY_RETENTION_DAYS", 7).entrySet())
            if (!pair.getValue().equals(repaired.get(pair.getKey()))) throw new AssertionError("invalid legacy setting not repaired: " + pair.getKey());
        if (!Integer.valueOf(0).equals(repaired.get("RECOVERY_STEP_RETRIES")) || !Integer.valueOf(4096).equals(repaired.get("MAX_OUTPUT_TOKENS")))
            throw new AssertionError("valid explicit limits/retry policy changed");
        var once = jdbc.queryForList("select * from core_ai_setting order by id"); migration.execute();
        if (!once.equals(jdbc.queryForList("select * from core_ai_setting order by id"))) throw new AssertionError("migration is not retryable");
        if (jdbc.queryForObject("select row_version from core_ai_setting where id=2", Integer.class) != 5
                || jdbc.queryForObject("select task_max_queued from core_ai_setting where id=2", Integer.class) != 42)
            throw new AssertionError("valid custom settings changed");
        System.out.println("PASS AI 2.58 repairs legacy zero settings atomically, preserves valid policies and repeats without change");
    }
}

package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Hibernate may add NOT NULL numeric columns as zero before the versioned migrations run. */
@Component
public class AiSettingsDefaultsInit implements CoreSqlBlock {
    private final JdbcTemplate jdbc;
    public AiSettingsDefaultsInit(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public Version getVersion() { return new Version("2.58"); }
    public void execute() {
        record Default(String column, int minimum, int value) {
            String invalid() { return "(" + column + " is null or " + column + " < " + minimum + ")"; }
            String assignment() { return column + " = case when " + invalid() + " then " + value + " else " + column + " end"; }
        }
        var defaults = List.of(
                new Default("task_max_running", 1, 2), new Default("task_max_queued", 1, 100),
                new Default("task_max_queued_per_user", 1, 10), new Default("task_max_wait_seconds", 1, 300),
                new Default("recovery_initial_delay_millis", 100, 1000),
                new Default("audit_retention_days", 1, 30), new Default("delivery_retention_days", 1, 7));
        // One atomic repair, preserving valid policies (including zero retry counts) and all past execution snapshots.
        jdbc.update("update core_ai_setting set "
                + String.join(", ", defaults.stream().map(Default::assignment).toList())
                + ", revision = revision + 1, row_version = row_version + 1 where "
                + String.join(" or ", defaults.stream().map(Default::invalid).toList()));
    }
}

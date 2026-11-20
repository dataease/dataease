package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import jakarta.persistence.EntityManagerFactory;
import java.sql.ResultSet;
import java.util.List;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Retryable migration for manual field value references. */
@Component
public class DatasourceValueReferencesInit implements CoreSqlBlock {
    private final JdbcTemplate jdbc;
    private final EntityManagerFactory factory;
    public DatasourceValueReferencesInit(JdbcTemplate jdbc, EntityManagerFactory factory) {
        this.jdbc = jdbc; this.factory = factory;
    }
    public Version getVersion() { return new Version("2.60"); }
    public void execute() {
        var session = factory.unwrap(SessionFactoryImplementor.class);
        var registry = session.getTypeConfiguration().getDdlTypeRegistry(); var dialect = session.getJdbcServices().getDialect();
        add("core_datasource_field_semantic", "value_references", registry.getTypeName(org.hibernate.type.SqlTypes.LONG32VARCHAR, org.hibernate.engine.jdbc.Size.length(org.hibernate.Length.LONG32), null), null);
    }
    private void add(String table, String column, String type, String defaultValue) {
        boolean present = Boolean.TRUE.equals(jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Boolean>) connection -> {
            for (String spelling : List.of(table, table.toUpperCase(java.util.Locale.ROOT))) try (ResultSet columns = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), spelling, null)) {
                while (columns.next()) if (column.equalsIgnoreCase(columns.getString("COLUMN_NAME"))) return true;
            }
            return false;
        }));
        if (!present) jdbc.execute("alter table " + table + " add " + column + " " + type + (defaultValue == null ? "" : " default " + defaultValue + " not null"));
        if (defaultValue != null) jdbc.update("update " + table + " set " + column + "=" + defaultValue + " where " + column + " is null");
    }
}

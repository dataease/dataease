package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.engine.jdbc.Size;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.SqlTypes;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.List;
import java.util.Locale;

/** Add nullable, independent dataset semantics without backfilling historical datasets. */
@Component
public class DatasetSemanticsInit implements CoreSqlBlock {
    private final JdbcTemplate jdbc;
    private final EntityManagerFactory factory;

    public DatasetSemanticsInit(JdbcTemplate jdbc, EntityManagerFactory factory) {
        this.jdbc = jdbc;
        this.factory = factory;
    }

    @Override
    public Version getVersion() {
        return new Version("2.61");
    }

    @Override
    public void execute() {
        var session = factory.unwrap(SessionFactoryImplementor.class);
        var types = session.getTypeConfiguration().getDdlTypeRegistry();
        var dialect = session.getJdbcServices().getDialect();
        String description = types.getTypeName(Types.VARCHAR, Size.length(4000), null);
        String confirmed = types.getTypeName(Types.BOOLEAN, dialect);
        // Reserved for a future dataset business-description editor; no application mapping yet.
        add("core_dataset_group", "business_description", description);
        add("core_dataset_table", "semantic_description", description);
        add("core_dataset_table", "semantic_grain", types.getTypeName(Types.VARCHAR, Size.length(512), null));
        add("core_dataset_table", "semantic_confirmed", confirmed);
        add("core_dataset_table_field", "semantic_business_name", types.getTypeName(Types.VARCHAR, Size.length(255), null));
        add("core_dataset_table_field", "semantic_description", description);
        add("core_dataset_table_field", "semantic_aliases", types.getTypeName(Types.VARCHAR, Size.length(1000), null));
        add("core_dataset_table_field", "semantic_value_references", types.getTypeName(SqlTypes.LONG32VARCHAR, Size.length(org.hibernate.Length.LONG32), null));
        add("core_dataset_table_field", "semantic_confirmed", confirmed);
    }

    private void add(String table, String column, String type) {
        boolean present = Boolean.TRUE.equals(jdbc.execute((ConnectionCallback<Boolean>) connection -> {
            for (String spelling : List.of(table, table.toUpperCase(Locale.ROOT))) {
                try (ResultSet columns = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), spelling, null)) {
                    while (columns.next()) {
                        if (column.equalsIgnoreCase(columns.getString("COLUMN_NAME"))) return true;
                    }
                }
            }
            return false;
        }));
        if (!present) jdbc.execute("alter table " + table + " add " + column + " " + type);
    }
}

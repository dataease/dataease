package io.dataease.listener.sql;

import io.dataease.ai.dao.entity.CoreAiSetting;
import io.dataease.initSql.Version;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.engine.jdbc.Size;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Additive, retryable migration. Keys themselves are generated only after all migrations commit. */
@Component
public class AiKeyringInit implements CoreSqlBlock {
    private final JdbcTemplate jdbc;
    private final EntityManagerFactory factory;

    public AiKeyringInit(JdbcTemplate jdbc, EntityManagerFactory factory) {
        this.jdbc = jdbc;
        this.factory = factory;
    }

    public Version getVersion() { return new Version("2.47"); }

    public void execute() { ensureColumns(jdbc, factory); }

    static void ensureColumns(JdbcTemplate jdbc, EntityManagerFactory factory) {
        Set<String> columns = jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Set<String>>) connection -> {
            Set<String> names = new HashSet<>();
            for (String table : List.of("core_ai_setting", "CORE_AI_SETTING")) {
                try (ResultSet result = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), table, null)) {
                    while (result.next()) names.add(result.getString("COLUMN_NAME").toLowerCase(java.util.Locale.ROOT));
                }
            }
            return names;
        });
        var session = factory.unwrap(SessionFactoryImplementor.class);
        var types = session.getTypeConfiguration().getDdlTypeRegistry();
        for (String name : List.of("keySource", "keyRing", "activeKeyId", "keyRevision", "keyFingerprint")) {
            try {
                var field = CoreAiSetting.class.getDeclaredField(name);
                Column column = field.getAnnotation(Column.class);
                if (columns.contains(column.name())) continue;
                String type = field.getType() == Long.class
                        ? types.getTypeName(Types.BIGINT, session.getJdbcServices().getDialect())
                        : types.getTypeName(Types.VARCHAR, Size.length(column.length()));
                jdbc.execute("alter table core_ai_setting add " + column.name() + " " + type);
            } catch (NoSuchFieldException failure) {
                throw new IllegalStateException("Missing AI key schema field");
            }
        }
    }
}

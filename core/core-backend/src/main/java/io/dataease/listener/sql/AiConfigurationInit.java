package io.dataease.listener.sql;

import io.dataease.ai.dao.entity.*;
import io.dataease.initSql.Version;
import jakarta.persistence.*;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.jdbc.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/** 独立版本化迁移，支持 ddl-auto 关闭；仅使用当前 Hibernate 方言产生列类型。 */
@Component
public class AiConfigurationInit implements CoreSqlBlock {
    private final JdbcTemplate jdbc;
    private final EntityManagerFactory factory;
    private final EntityManager em;
    private final TransactionTemplate tx;
    public AiConfigurationInit(JdbcTemplate jdbc, EntityManagerFactory factory, EntityManager em,
                               PlatformTransactionManager manager) {
        this.jdbc = jdbc; this.factory = factory; this.em = em; this.tx = new TransactionTemplate(manager);
    }
    public Version getVersion() { return new Version("2.44"); }
    public void execute() {
        create(CoreAiSetting.class); create(CoreAiModel.class); create(CoreAiOrgPolicy.class);
        // A retry of an earlier partial 2.44 migration must support the current entity mapping.
        AiKeyringInit.ensureColumns(jdbc, factory);
        tokenParameter();
        index("core_ai_model", "uk_ai_model_name", "name_key", true);
        index("core_ai_org_policy", "idx_ai_org_model", "model_id", false);
        tx.executeWithoutResult(status -> {
            if (em.find(CoreAiSetting.class, 1L) == null) em.persist(new CoreAiSetting());
            // Menu registration is handled by 2.45, including early development compatibility.
            em.flush();
        });
    }
    private void tokenParameter() {
        boolean exists = Boolean.TRUE.equals(jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Boolean>) c -> {
            for (String table : List.of("core_ai_model", "CORE_AI_MODEL")) {
                try (ResultSet columns = c.getMetaData().getColumns(c.getCatalog(), c.getSchema(), table, null)) {
                    while (columns.next()) if ("output_token_parameter".equalsIgnoreCase(columns.getString("COLUMN_NAME"))) return true;
                }
            }
            return false;
        }));
        if (!exists) {
            var session = factory.unwrap(SessionFactoryImplementor.class);
            String type = session.getTypeConfiguration().getDdlTypeRegistry().getTypeName(Types.VARCHAR, Size.length(32));
            jdbc.execute("alter table core_ai_model add output_token_parameter " + type);
        }
        jdbc.update("update core_ai_model set output_token_parameter = ? where output_token_parameter is null", "MAX_TOKENS");
    }
    private boolean tableExists(String name) {
        return Boolean.TRUE.equals(jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Boolean>) c -> {
            try (ResultSet tables = c.getMetaData().getTables(c.getCatalog(), c.getSchema(), null, new String[]{"TABLE"})) {
                while (tables.next()) if (name.equalsIgnoreCase(tables.getString("TABLE_NAME"))) return true;
            }
            return false;
        }));
    }
    void create(Class<?> entity) {
        String table = entity.getAnnotation(Table.class).name(); if (tableExists(table)) return;
        var session = factory.unwrap(SessionFactoryImplementor.class);
        var dialect = session.getJdbcServices().getDialect();
        var types = session.getTypeConfiguration().getDdlTypeRegistry();
        List<String> columns = new ArrayList<>();
        for (Class<?> type = entity; type != Object.class; type = type.getSuperclass()) for (Field field : type.getDeclaredFields()) {
            Column column = field.getAnnotation(Column.class); if (column == null) continue;
            int code = field.getType() == Long.class ? Types.BIGINT : field.getType() == Integer.class ? Types.INTEGER
                    : field.getType() == Boolean.class ? Types.BOOLEAN : field.isAnnotationPresent(Lob.class) ? Types.CLOB : field.isAnnotationPresent(JdbcTypeCode.class) ? field.getAnnotation(JdbcTypeCode.class).value() : Types.VARCHAR;
            String sqlType = (code == Types.VARCHAR || code == Types.LONGVARCHAR) ? types.getTypeName(code, Size.length(column.length())) : types.getTypeName(code, dialect);
            columns.add(column.name() + " " + sqlType + (field.isAnnotationPresent(Id.class) ? " primary key" : !column.nullable() ? " not null" : ""));
        }
        jdbc.execute("create table " + table + " (" + String.join(",", columns) + ")");
    }
    void index(String table, String name, String column, boolean unique) {
        boolean exists = Boolean.TRUE.equals(jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Boolean>) c -> {
            for (String spelling : List.of(table, table.toUpperCase(Locale.ROOT))) {
                try (ResultSet indexes = c.getMetaData().getIndexInfo(c.getCatalog(), c.getSchema(), spelling, false, false)) {
                    while (indexes.next()) if (name.equalsIgnoreCase(indexes.getString("INDEX_NAME"))) return true;
                }
            }
            return false;
        }));
        if (!exists) jdbc.execute("create " + (unique ? "unique " : "") + "index " + name + " on " + table + " (" + column + ")");
    }
}

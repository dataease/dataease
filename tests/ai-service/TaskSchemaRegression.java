package io.dataease.ai;

import io.dataease.ai.dao.entity.*;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.type.BasicType;
import java.util.List;

/** Offline Hibernate mapping check; it makes no claim of live PostgreSQL/MySQL execution. */
public final class TaskSchemaRegression {
    public static void run() {
        for (String dialect : List.of("PostgreSQLDialect", "MySQLDialect", "H2Dialect")) {
            var registry = new StandardServiceRegistryBuilder().applySetting("hibernate.dialect", "org.hibernate.dialect." + dialect)
                    .applySetting("hibernate.boot.allow_jdbc_metadata_access", "false").build();
            try {
                var metadata = new MetadataSources(registry).addAnnotatedClass(CoreAiTask.class).addAnnotatedClass(CoreAiTaskAttempt.class).addAnnotatedClass(CoreAiAuditEntry.class).addAnnotatedClass(CoreAiAuditRetired.class).addAnnotatedClass(CoreAiAuditExecution.class).buildMetadata();
                for (String field : List.of("inputJson", "contextJson", "resultJson", "requestBody", "responseBody")) {
                    var entity = metadata.getEntityBinding((field.endsWith("Body") ? CoreAiAuditEntry.class : field.equals("resultJson") ? CoreAiTaskAttempt.class : CoreAiTask.class).getName());
                    var property = entity.getProperty(field); var type = (BasicType<?>) property.getType();
                    String sql = property.getColumns().get(0).getSqlType(metadata);
                    if (sql.contains("oid") || type.getJdbcType().getClass().getSimpleName().contains("Clob")) throw new AssertionError("character JSON must not use OID/LOB streaming: " + dialect + ":" + field + ":" + sql);
                    if (dialect.equals("PostgreSQLDialect") && !(sql.equals("text") || sql.startsWith("varchar"))) throw new AssertionError("PostgreSQL uses inline character mapping: " + sql);
                }
            } finally { StandardServiceRegistryBuilder.destroy(registry); }
        }
        System.out.println("PASS task character JSON mapping and DDL (offline PostgreSQL/MySQL/H2 dialects, no OID/LOB JDBC streaming)");
    }
}

package io.dataease.listener.sql;

import io.dataease.config.DynamicCaseNamingStrategy;
import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceFieldSemantic;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceRelation;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceRelationCondition;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceTableSemantic;
import io.dataease.initSql.Version;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.Table;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DatasourceMetadataInit implements CoreSqlBlock {
    private static final List<Class<?>> METADATA_ENTITIES = List.of(
            CoreDatasourceTableSemantic.class,
            CoreDatasourceFieldSemantic.class,
            CoreDatasourceRelation.class,
            CoreDatasourceRelationCondition.class
    );

    private final EntityManagerFactory factory;
    private final JdbcTemplate jdbc;

    public DatasourceMetadataInit(EntityManagerFactory factory, JdbcTemplate jdbc) {
        this.factory = factory;
        this.jdbc = jdbc;
    }

    @Override
    public Version getVersion() {
        return new Version("2.48");
    }

    @Override
    public void execute() {
        var session = factory.unwrap(SessionFactoryImplementor.class);
        var registry = session.getServiceRegistry().getParentServiceRegistry();
        // Resolve JPA associations without exporting the datasource table.
        var sources = new MetadataSources(registry).addAnnotatedClass(CoreDatasource.class);
        METADATA_ENTITIES.forEach(sources::addAnnotatedClass);
        var metadata = sources.getMetadataBuilder()
                .applyPhysicalNamingStrategy(new DynamicCaseNamingStrategy())
                .build();
        var context = session.getSqlStringGenerationContext();
        var tables = METADATA_ENTITIES.stream()
                .map(entity -> metadata.getEntityBinding(entity.getName()).getTable())
                .toList();

        for (Table table : tables) {
            var existing = inspect(table, context);
            if (existing.columns().isEmpty()) {
                execute(context.getDialect().getTableExporter().getSqlCreateStrings(table, metadata, context));
            } else {
                addMissingAuditColumns(table, existing, metadata, context);
            }
        }
        for (Table table : tables) {
            addConstraints(table, metadata, context);
        }
    }

    private void addMissingAuditColumns(Table table, ExistingTable existing, Metadata metadata,
                                        SqlStringGenerationContext context) {
        var dialect = context.getDialect();
        String qualifiedTable = context.format(table.getQualifiedTableName());
        for (String name : List.of("create_by", "create_time")) {
            if (existing.columns().contains(name)) {
                continue;
            }
            var column = table.getColumn(context.toIdentifier(name));
            String comment = column.getComment().replace("'", "''");
            String inlineComment = dialect.getColumnComment(comment);
            jdbc.execute(dialect.getAlterTableString(qualifiedTable) + " " + dialect.getAddColumnString()
                    + " " + column.getQuotedName(dialect) + " " + column.getSqlType(metadata)
                    + inlineComment + dialect.getAddColumnSuffixString());
            if (inlineComment.isEmpty() && dialect.supportsCommentOn()) {
                jdbc.execute("comment on column " + qualifiedTable + "." + column.getQuotedName(dialect)
                        + " is '" + comment + "'");
            }
        }
    }

    private void addConstraints(Table table, Metadata metadata, SqlStringGenerationContext context) {
        var dialect = context.getDialect();
        var existing = inspect(table, context);
        for (var index : table.getIndexes().values()) {
            if (!existing.indexes().contains(normalize(index.getName()))) {
                execute(dialect.getIndexExporter().getSqlCreateStrings(index, metadata, context));
            }
        }
        for (var key : table.getUniqueKeys().values()) {
            Set<String> keyColumns = key.getColumns().stream()
                    .map(column -> normalize(column.getName())).collect(Collectors.toSet());
            if (!existing.uniqueIndexes().containsValue(keyColumns)) {
                execute(dialect.getUniqueKeyExporter().getSqlCreateStrings(key, metadata, context));
            }
        }
    }

    private ExistingTable inspect(Table table, SqlStringGenerationContext context) {
        return jdbc.execute((ConnectionCallback<ExistingTable>) connection -> {
            var catalogIdentifier = context.catalogWithDefault(table.getCatalogIdentifier());
            var schemaIdentifier = context.schemaWithDefault(table.getSchemaIdentifier());
            var helper = context.getIdentifierHelper();
            String catalog = catalogIdentifier == null ? connection.getCatalog()
                    : metadataNamespace(connection.getCatalog(), helper.toMetaDataCatalogName(catalogIdentifier));
            String schema = schemaIdentifier == null ? connection.getSchema()
                    : metadataNamespace(connection.getSchema(), helper.toMetaDataSchemaName(schemaIdentifier));
            var metadata = connection.getMetaData();
            var identifier = table.getNameIdentifier();
            try (var rows = metadata.getTables(catalog, schema, null, new String[]{"TABLE"})) {
                while (rows.next()) {
                    String name = rows.getString("TABLE_NAME");
                    if (identifier.isQuoted() ? identifier.getText().equals(name) : identifier.getText().equalsIgnoreCase(name)) {
                        return inspect(metadata, catalog, schema, name);
                    }
                }
            }
            return new ExistingTable(Set.of(), Set.of(), Map.of());
        });
    }

    private String metadataNamespace(String current, String mapped) {
        return current != null && current.equalsIgnoreCase(mapped) ? current : mapped;
    }

    private ExistingTable inspect(DatabaseMetaData metadata, String catalog, String schema, String name)
            throws SQLException {
        Set<String> columns = new HashSet<>();
        Set<String> indexes = new HashSet<>();
        Map<String, Set<String>> uniqueIndexes = new HashMap<>();
        try (var rows = metadata.getColumns(catalog, schema, name, null)) {
            while (rows.next()) {
                if (name.equals(rows.getString("TABLE_NAME"))) {
                    columns.add(normalize(rows.getString("COLUMN_NAME")));
                }
            }
        }
        if (!columns.isEmpty()) {
            try (var rows = metadata.getIndexInfo(catalog, schema, name, false, false)) {
                while (rows.next()) {
                    String index = rows.getString("INDEX_NAME");
                    if (index != null) {
                        String indexName = normalize(index);
                        indexes.add(indexName);
                        String column = rows.getString("COLUMN_NAME");
                        if (column != null && !rows.getBoolean("NON_UNIQUE")) {
                            uniqueIndexes.computeIfAbsent(indexName, ignored -> new HashSet<>()).add(normalize(column));
                        }
                    }
                }
            }
        }
        return new ExistingTable(columns, indexes, uniqueIndexes);
    }

    private void execute(String[] statements) {
        for (String statement : statements) {
            jdbc.execute(statement);
        }
    }

    private String normalize(String identifier) {
        return identifier.toLowerCase(Locale.ROOT);
    }

    private record ExistingTable(Set<String> columns, Set<String> indexes, Map<String, Set<String>> uniqueIndexes) {}
}

package io.dataease.ai.scene.sql;

import io.dataease.ai.engine.runtime.AiToolProtocol;
import io.dataease.ai.support.AiErrors;
import io.dataease.ai.support.AiPromptResources;
import io.dataease.ai.tool.AiTool;
import io.dataease.ai.tool.datasource.AiDatasourceTools;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/** Model-readable reference for the current preview engine. Identifiers in examples are placeholders. */
@Component
public final class DatasetSqlGuide {
    private static final String DIR = "dataset-sql-guide/";
    private static final String DIALECT = DIR + "dialect.md";
    private static final String RULES = AiPromptResources.read(DIR + "rules.md");
    private static final Map<String, String> EXAMPLES = Map.of(
            "projection", AiPromptResources.read(DIR + "projection.md"),
            "aggregate", AiPromptResources.read(DIR + "aggregate.md"),
            "join", AiPromptResources.read(DIR + "join.md"),
            "monthly", AiPromptResources.read(DIR + "monthly.md"),
            "parameters", AiPromptResources.read(DIR + "parameters.md"));

    private final EngineManage engines;
    private final PluginManageApi plugins;

    public DatasetSqlGuide(EngineManage engines, @Nullable PluginManageApi plugins) {
        this.engines = engines;
        this.plugins = plugins;
    }

    AiTool<AiDatasourceTools.Scope> tool(AiDatasourceTools metadata) {
        var definition = AiTool.readOnly("tools/dataset-sql.md", "read_sql_guide", "topic");
        return new AiTool<>(definition, (scope, arguments, active, deadline) -> {
            var source = metadata.authorize(scope);
            String dialect = executionType(source.type(), scope.isCross());
            var topic = arguments.get("topic");
            String example = EXAMPLES.get(topic);
            if (example == null) throw AiErrors.error("invalid");
            if ("monthly".equals(topic)) example = monthly(dialect, example);
            return AiToolProtocol.result(Map.of("dialect", displayName(dialect), "rules", RULES, "identifiers", identifiers(dialect),
                    "topic", topic, "example", example));
        });
    }

    String executionType(String sourceType, boolean isCross) {
        if (isCross) return "calcite";
        return DatasourceConfiguration.usesDeEngine(sourceType) ? engines.getDeEngine().getType() : sourceType;
    }

    String displayName(String type) {
        if ("calcite".equalsIgnoreCase(type)) return "Apache Calcite";
        for (var connector : DatasourceConfiguration.DatasourceType.values()) {
            if (connector.getType().equalsIgnoreCase(type)) return connector.getName();
        }
        if (plugins != null) {
            for (var config : plugins.queryPluginDs()) {
                if (type.equalsIgnoreCase(config.getType()) && config.getName() != null && !config.getName().isBlank())
                    return config.getName().strip();
            }
        }
        return "Unspecified plugin dialect";
    }

    private static String identifiers(String dialect) {
        return switch (dialect.toLowerCase(Locale.ROOT)) {
            case "calcite" -> AiPromptResources.section(DIALECT, "calcite");
            case "dm" -> AiPromptResources.section(DIALECT, "dm");
            default -> {
                for (var connector : DatasourceConfiguration.DatasourceType.values()) {
                    if (connector.getType().equalsIgnoreCase(dialect) && connector.getPrefix() != null)
                        yield AiPromptResources.section(DIALECT, "quoted_column")
                                .replace("{{quoted_column}}", connector.getPrefix() + "column_name" + connector.getSuffix());
                }
                yield AiPromptResources.section(DIALECT, "plugin");
            }
        };
    }

    private static String monthly(String dialect, String example) {
        String bucket = switch (dialect.toLowerCase(Locale.ROOT)) {
            case "calcite" -> "FLOOR(date_column TO MONTH)";
            case "mysql", "mariadb", "tidb", "doris", "starrocks", "mongo" -> "DATE_FORMAT(date_column, '%Y-%m')";
            case "pg", "redshift", "gaussdb", "kingbase", "h2" -> "DATE_TRUNC('month', date_column)";
            case "oracle", "dm", "impala" -> "TRUNC(date_column, 'MM')";
            case "sqlserver" -> "DATEFROMPARTS(YEAR(date_column), MONTH(date_column), 1)";
            case "db2" -> "VARCHAR_FORMAT(date_column, 'YYYY-MM')";
            case "ck" -> "toStartOfMonth(date_column)";
            case "es" -> "HISTOGRAM(date_column, INTERVAL 1 MONTH)";
            default -> null;
        };
        if (bucket == null) return AiPromptResources.section(DIALECT, "unknown_month");
        return example.replace("{{month_bucket}}", bucket);
    }
}

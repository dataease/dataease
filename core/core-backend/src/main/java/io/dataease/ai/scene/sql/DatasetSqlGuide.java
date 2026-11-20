package io.dataease.ai.scene.sql;

import io.dataease.ai.engine.runtime.AiToolProtocol;
import io.dataease.ai.support.AiErrors;
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
    private static final String RULES = "Use one read-only SELECT or WITH query on the selected datasource. "
            + "For cross-database joins give canvas instructions only: add separate nodes and configure their relationship. "
            + "Use authorized physical names. Examples describe syntax; the installed driver and version determine support.";

    private final EngineManage engines;
    private final PluginManageApi plugins;

    public DatasetSqlGuide(EngineManage engines, @Nullable PluginManageApi plugins) {
        this.engines = engines;
        this.plugins = plugins;
    }

    AiTool<AiDatasourceTools.Scope> tool(AiDatasourceTools metadata) {
        var definition = new AiToolProtocol.Definition("read_sql_guide",
                "Read the DataEase rules, bound execution dialect and one relevant topic. Never choose a dialect yourself.",
                Map.of("topic", "projection (plain columns), monthly (calendar aggregation), aggregate, join or parameters (dashboard filters and system variables)"),
                AiToolProtocol.RetrySafety.READ_ONLY);
        return new AiTool<>(definition, (scope, arguments, active, deadline) -> {
            var source = metadata.authorize(scope);
            String dialect = executionType(source.type(), scope.isCross());
            var topic = arguments.get("topic");
            String example = switch (topic) {
                case "projection" -> "SELECT column_name FROM table_name";
                case "aggregate" -> "SELECT dimension, SUM(amount) AS total FROM table_name GROUP BY dimension";
                case "join" -> """
                        Use all confirmed equalities together. Choose LEFT JOIN when every starting row must survive.
                        A chart usually needs the detail grain, with separate dimensions and measures.
                        If the author requests one row per group, aggregate at that grain. A many-side join can repeat
                        fact amounts; preaggregate its rows on every fact key before joining, for example:
                        SELECT f.group_key, SUM(f.amount) AS total FROM fact_table f
                        LEFT JOIN (SELECT tenant_key, fact_id, COUNT(*) AS line_count FROM line_table
                                   GROUP BY tenant_key, fact_id) x
                          ON f.tenant_key = x.tenant_key AND f.id = x.fact_id GROUP BY f.group_key.
                        Use EXISTS when the detail is only a filter. Many-to-many measures need an explicit allocation
                        or grain decision. Never deduplicate by amount: two different facts can have equal amounts.
                        """;
                case "monthly" -> monthly(dialect);
                case "parameters" -> """
                        Preserve relevant existing dashboard parameter names and syntax when revising SQL.
                        For an explicitly requested new dashboard filter use a single-line optional WHERE predicate,
                        e.g. WHERE $DE_PARAM{status_code = $[status]}. Put AND/OR outside the blocks.
                        In a legacy query with ${status}, retain the legacy form for all dashboard parameters;
                        never mix it with $DE_PARAM blocks. Never invent filters or parameter defaults.
                        For a system variable, insert $f2cde[exact editor display name] in a WHERE predicate;
                        in block syntax put the whole predicate inside $DE_PARAM{...}. Use only the supplied names.
                        If no matching name is supplied, ask for the exact editor label instead of inventing it.
                        The editor maps display names to runtime identifiers; never request user variable values.
                        Explain that Parameter settings reuses same-name configuration and sets types and defaults.
                        Editor preview uses applicable defaults; a block with a missing parameter becomes true.
                        Actual dashboard filter values are supplied when the dashboard runs.
                        """;
                default -> throw AiErrors.error("invalid");
            };
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
            case "calcite" -> "DataEase Calcite uses JAVA lexical policy. Quote physical identifiers with backticks when needed.";
            case "dm" -> "Use double quotes for identifiers requiring quoting.";
            default -> {
                for (var connector : DatasourceConfiguration.DatasourceType.values()) {
                    if (connector.getType().equalsIgnoreCase(dialect) && connector.getPrefix() != null)
                        yield "Quote an identifier as " + connector.getPrefix() + "column_name" + connector.getSuffix() + ".";
                }
                yield "Plugin-specific dialect. Use simple physical identifiers and confirm connector capabilities before using extensions.";
            }
        };
    }

    private static String monthly(String dialect) {
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
        if (bucket == null) return "No registered month-bucket example for this plugin. Confirm its date function first.";
        return "SELECT " + bucket + " AS month_bucket, SUM(amount) AS total FROM table_name GROUP BY " + bucket;
    }
}

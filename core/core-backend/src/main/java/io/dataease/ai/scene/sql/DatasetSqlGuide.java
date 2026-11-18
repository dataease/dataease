package io.dataease.ai.scene.sql;

import io.dataease.ai.engine.runtime.AiToolProtocol;
import io.dataease.ai.support.AiErrors;
import io.dataease.ai.tool.AiTool;
import io.dataease.ai.tool.datasource.AiDatasourceTools;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;

import java.util.Locale;
import java.util.Map;

/** Model-readable reference for the current preview engine. Identifiers in examples are placeholders. */
final class DatasetSqlGuide {
    private static final String RULES = "Use one read-only SELECT or WITH query on the selected datasource. "
            + "For cross-database joins give canvas instructions only: add separate nodes and configure their relationship. "
            + "Use authorized physical names. Examples describe syntax; the installed driver and version determine support.";

    static AiTool<AiDatasourceTools.Scope> tool(AiDatasourceTools metadata, EngineManage engines) {
        var definition = new AiToolProtocol.Definition("read_sql_guide",
                "Read the DataEase rules, bound execution dialect and one relevant topic. Never choose a dialect yourself.",
                Map.of("topic", "projection (plain columns), monthly (calendar aggregation), aggregate or join"),
                AiToolProtocol.RetrySafety.READ_ONLY);
        return new AiTool<>(definition, (scope, arguments, active, deadline) -> {
            var source = metadata.authorize(scope);
            String dialect = source.type();
            if (scope.isCross()) {
                dialect = "calcite";
            } else if (DatasourceConfiguration.usesDeEngine(dialect)) {
                dialect = engines.getDeEngine().getType();
            }
            var topic = arguments.get("topic");
            String example = switch (topic) {
                case "projection" -> "SELECT column_name FROM table_name";
                case "aggregate" -> "SELECT dimension, SUM(amount) AS total FROM table_name GROUP BY dimension";
                case "join" -> "SELECT a.id, b.label FROM fact_table a LEFT JOIN lookup_table b ON a.lookup_id = b.id";
                case "monthly" -> monthly(dialect);
                default -> throw AiErrors.error("invalid");
            };
            return AiToolProtocol.result(Map.of("dialect", dialect, "rules", RULES, "identifiers", identifiers(dialect),
                    "topic", topic, "example", example));
        });
    }

    private static String identifiers(String dialect) {
        return switch (dialect) {
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

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
                Map.of("topic", "projection (plain columns), monthly (calendar aggregation), aggregate, join or parameters (filter inputs and system variables)"),
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
                        When the author filters by a field and leaves its value open, create a dashboard parameter.
                        Put the verified column on the left. Use the requested parameter name on the right,
                        or choose a name without the table alias, as in this new account filter.
                        WHERE $DE_PARAM{o.account = $[account]}
                        Put each optional predicate in its own single-line block. Place AND or OR between blocks
                        according to the requested logic. A block missing its input is true, including under OR.
                        For a fixed filter value, compare the column with the supplied value directly.

                        On edits, reuse relevant parameter names from sqlBasis so their settings still apply.
                        If sqlBasis already uses legacy dashboard parameters, keep that form for every dashboard input.
                        WHERE o.account = ${account}

                        For “查询当前登录账户的信息”, use the exact account label from systemVariableNames.
                        With the label 账号 and a column that stores login accounts, the predicate is
                        WHERE $DE_PARAM{o.account = $f2cde[账号]}
                        In a legacy query use the same system variable directly in the predicate. For other system
                        variables, match the requested label to the same list. If absent, ask for its editor label.
                        The editor maps display names to runtime identifiers. System variable values stay in DataEase.

                        The author configures types and defaults in Parameter settings. Preview uses applicable
                        defaults. The dashboard supplies filter values when it runs.
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

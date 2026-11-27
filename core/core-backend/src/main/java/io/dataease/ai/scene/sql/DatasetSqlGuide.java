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
import java.util.Set;

/** Model-readable reference for the current preview engine. Identifiers in examples are placeholders. */
@Component
public final class DatasetSqlGuide {
    private static final String DIR = "dataset-sql-guide/";
    private static final String DIALECT = DIR + "dialect.md";
    private static final Set<String> ADAPTERS = Set.of("mysql", "calcite");
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
            var topic = arguments.get("topic");
            String example = EXAMPLES.get(topic);
            if (example == null) throw AiErrors.error("invalid");
            var dialect = resolveDialect(source.type(), scope.isCross());
            if (!dialect.supported()) return AiToolProtocol.result(dialect.unsupportedResult());
            if ("monthly".equals(topic)) {
                example = example.replace("{{month_bucket}}", AiPromptResources.section(DIALECT, dialect.adapter() + ".month"));
            }
            return AiToolProtocol.result(Map.of("dialect", dialect.displayName(), "rules", RULES,
                    "identifiers", AiPromptResources.section(DIALECT, dialect.adapter() + ".identifiers"),
                    "topic", topic, "example", example));
        });
    }

    Dialect resolveDialect(String sourceType, boolean isCross) {
        if (isCross) return new Dialect("Apache Calcite", "calcite");
        String executionType = DatasourceConfiguration.usesDeEngine(sourceType) ? engines.getDeEngine().getType() : sourceType;
        return connectorDialect(executionType);
    }

    String displayName(String type) {
        return connectorDialect(type).displayName();
    }

    private Dialect connectorDialect(String type) {
        for (var connector : DatasourceConfiguration.DatasourceType.values()) {
            if (connector.getType().equals(type)) {
                return new Dialect(connector.getName(), adapter(connector.getType()));
            }
        }
        if (plugins != null) {
            for (var config : plugins.queryPluginDs()) {
                if (type.equals(config.getType())) {
                    var name = config.getName();
                    return new Dialect(name == null || name.isBlank() ? type : name.strip(), adapter(config.getAiSqlBaseDialect()));
                }
            }
        }
        return new Dialect("Unspecified plugin dialect", null);
    }

    private static String adapter(String type) {
        if (type == null) return null;
        String key = type.strip().toLowerCase(Locale.ROOT);
        return ADAPTERS.contains(key) ? key : null;
    }

    record Dialect(String displayName, @Nullable String adapter) {
        boolean supported() { return adapter != null; }

        Map<String, String> unsupportedResult() {
            return Map.of("status", "UNSUPPORTED_DIALECT", "dialect", displayName,
                    "message", AiPromptResources.section(DIALECT, "unsupported"));
        }
    }
}

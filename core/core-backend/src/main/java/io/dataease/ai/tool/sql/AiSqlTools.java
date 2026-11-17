package io.dataease.ai.tool.sql;

import io.dataease.ai.engine.runtime.AiToolProtocol;
import io.dataease.ai.tool.AiTool;
import io.dataease.ai.tool.datasource.AiDatasourceTools;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Shared SQL validation tools; the calling scene decides how a proposal may be applied. */
@Component
public class AiSqlTools implements AutoCloseable {
    private final AiDatasourceTools metadata;
    private final AiSqlStaticValidator validator = new AiSqlStaticValidator();

    public AiSqlTools(AiDatasourceTools metadata) { this.metadata = metadata; }

    public AiTool<AiDatasourceTools.Scope> validateSql() {
        return tool(new AiToolProtocol.Definition("validate_sql", "Statically check candidate SQL against current authorized metadata, without executing it. " + AiSqlStaticValidator.SCOPE,
                Map.of("sql", "Whole candidate SQL; correct INVALID references and validate the correction again; UNSUPPORTED is not validated."), AiToolProtocol.RetrySafety.READ_ONLY));
    }

    public AiTool<AiDatasourceTools.Scope> proposeSql() {
        return tool(new AiToolProtocol.Definition("propose_sql", "Propose SQL for explicit user application to the original editor draft. Runs the same static validation; only PASS produces an applicable SQL suggestion. Never saves or executes SQL.",
                Map.of("sql", "Complete SQL suggestion within the declared validation subset"), AiToolProtocol.RetrySafety.READ_ONLY));
    }

    private AiTool<AiDatasourceTools.Scope> tool(AiToolProtocol.Definition definition) {
        return new AiTool<>(definition, (scope, arguments, active, deadline) -> {
            var source = metadata.authorize(scope);
            var verdict = validator.validate(arguments.get("sql"), source.type(),
                    table -> metadata.validationFields(scope, table, active), active, deadline);
            metadata.authorize(scope);
            return AiToolProtocol.result(verdict);
        });
    }

    @Override @jakarta.annotation.PreDestroy
    public void close() { validator.close(); }
}

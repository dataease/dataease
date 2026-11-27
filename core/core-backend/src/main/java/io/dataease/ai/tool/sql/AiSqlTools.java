package io.dataease.ai.tool.sql;

import io.dataease.ai.engine.runtime.AiToolProtocol;
import io.dataease.ai.tool.AiTool;
import io.dataease.ai.tool.datasource.AiDatasourceTools;
import org.springframework.stereotype.Component;

/** Shared SQL validation tools; the calling scene decides how a proposal may be applied. */
@Component
public class AiSqlTools implements AutoCloseable {
    private final AiDatasourceTools metadata;
    private final AiSqlStaticValidator validator = new AiSqlStaticValidator();

    public AiSqlTools(AiDatasourceTools metadata) { this.metadata = metadata; }

    public AiTool<AiDatasourceTools.Scope> validateSql() {
        return tool(AiTool.readOnly("tools/sql-validation.md", "validate_sql", "sql"));
    }

    public AiTool<AiDatasourceTools.Scope> proposeSql() {
        return tool(AiTool.readOnly("tools/sql-validation.md", "propose_sql", "sql"));
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

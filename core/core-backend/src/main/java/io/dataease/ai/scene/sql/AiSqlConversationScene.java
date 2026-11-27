package io.dataease.ai.scene.sql;

import io.dataease.ai.tool.datasource.AiDatasourceTools;
import io.dataease.ai.tool.sql.AiSqlTools;
import io.dataease.ai.support.AiPromptResources;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiSqlConversationScene extends AbstractSqlConversationScene {
    public AiSqlConversationScene(AiDatasourceTools metadata, AiSqlTools sql) {
        super(metadata, List.of(metadata.listTables(), metadata.getTableFields(), sql.validateSql(), sql.proposeSql()));
    }

    @Override public String code() { return "sql"; }
    @Override protected String instruction(AiDatasourceTools.Datasource resource, String sql, boolean isCross) {
        return AiPromptResources.section("sql-demo.md", "selected").formatted(resource.type(), sql);
    }
    @Override protected String proposalStatus() { return "PASS"; }
}

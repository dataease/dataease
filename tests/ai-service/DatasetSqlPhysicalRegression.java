package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.scene.sql.AiDatasetSqlScene;
import io.dataease.ai.scene.sql.DatasetSqlGuide;
import io.dataease.ai.tool.datasource.AiDatasourceTools;
import io.dataease.ai.tool.datasource.DatasetSqlSemanticTools;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.datasource.provider.PhysicalTableCatalog;
import io.dataease.extensions.datasource.dto.*;
import io.dataease.ai.engine.conversation.AiConversationScene;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.*;

public final class DatasetSqlPhysicalRegression {
    public static void main(String[] args) throws Exception {
        new io.dataease.i18n.Translator().setMessageSource(new org.springframework.context.support.StaticMessageSource());
        var reads = new ArrayList<String>();
        var allowed = new AtomicBoolean(true);
        var empty = new AtomicBoolean(false);
        var json = new ObjectMapper();
        var metadata = new AiDatasourceTools(null, null, null, null) {
            @Override public Datasource resolve(String id) { return new Datasource(id, "fixture", "1", "mysql"); }
            @Override public Datasource authorize(Scope scope) {
                if (!allowed.get()) throw new SecurityException("revoked");
                return resolve(scope.resourceId());
            }
            @Override protected PhysicalTableCatalog openCatalog(long id) {
                if (id != 11L) throw new AssertionError(id);
                reads.add("directory");
                var tables = new ArrayList<DatasetTableDTO>();
                if (!empty.get()) {
                    for (int n = 0; n < 305; n++) {
                        var t = new DatasetTableDTO(); t.setTableName("T" + n);
                        t.setName(n < 9 ? "历史渠道" : "无关"); tables.add(t);
                    }
                    var current = new DatasetTableDTO();
                    current.setTableName("Channel_Live");
                    current.setName("当前渠道" + "注".repeat(40000));
                    tables.add(current);
                }
                return new PhysicalTableCatalog(tables, name -> {
                    reads.add(name);
                    if ("T304".equals(name)) {
                        var fields = new ArrayList<TableField>();
                        for (int n = 0; n <= 256; n++) {
                            var column = new TableField();
                            column.setOriginName("column_" + n);
                            column.setFieldType("INT");
                            fields.add(column);
                        }
                        return fields;
                    }
                    var f = new TableField(); f.setOriginName("channel_id"); f.setFieldType("INT"); return List.of(f);
                });
            }
        };
        var scene = new AiDatasetSqlScene(metadata, new DatasetSqlGuide(new EngineManage(), null), new DatasetSqlSemanticTools(metadata, DatasetSqlSemanticRegression.registry()), DatasetSqlRelationRegression.registry(java.util.List.of()));
        var tools = scene.tools(scene.resolve("11", "")).stream().map(t -> t.name()).toList();
        if (tools.contains("list_tables") || !tools.contains("search_physical_tables"))
            throw new AssertionError("Editor must expose targeted discovery instead of a complete table list: " + tools);
        var turn = scene.beginTurn(scene.resolve("11", ""));
        var found = json.readTree(call(turn, "search_physical_tables", "terms", "渠道"));
        if (found.path("candidates").size() != 5 || !found.path("more").asBoolean()
                || !found.path("candidates").get(0).path("table").asText().equals("Channel_Live")
                || found.toString().length() > 4000 || !reads.equals(List.of("directory"))) throw new AssertionError(found + " " + reads);
        if (!call(turn, "search_physical_tables", "terms", "xxx").contains("NO_MATCH"))
            throw new AssertionError("no match must distinguish an empty catalog");
        String fields = call(turn, "get_table_fields", "table", "Channel_Live");
        if (!fields.contains("channel_id")) throw new AssertionError(fields);
        call(turn, "get_table_fields", "table", "T300");
        call(turn, "get_table_fields", "table", "Channel_Live");
        if (!reads.equals(List.of("directory", "Channel_Live", "T300"))) throw new AssertionError(reads);
        var selected = json.readTree(turn.execute("get_named_table_fields", Map.of("table", "T304", "fields", "column_256"),
                () -> {}, Long.MAX_VALUE));
        if (selected.path("fields").size() != 1 || !"column_256".equals(selected.at("/fields/0/name").asText()))
            throw new AssertionError("A table with more than 256 columns must still allow a bounded projection: " + selected);
        try {
            call(turn, "get_table_fields", "table", "channel_live");
            throw new AssertionError("case-insensitive lookup expanded the exact selection");
        } catch (io.dataease.ai.support.AiErrors.Failure denied) {
            if (!"forbidden".equals(denied.safeCode())) throw denied;
        }
        allowed.set(false);
        try { call(turn, "get_table_fields", "table", "Channel_Live"); throw new AssertionError("returned revoked cache"); }
        catch (SecurityException expected) {}
        allowed.set(true);
        empty.set(true);
        var next = scene.beginTurn(scene.resolve("11", ""));
        if (!call(next, "search_physical_tables", "terms", "渠道").contains("EMPTY_CATALOG")
                || !reads.equals(List.of("directory", "Channel_Live", "T300", "T304", "directory"))) throw new AssertionError(reads);
    }

    private static String call(AiConversationScene.ToolExecution turn, String name, String key, String value) {
        return turn.execute(name, Map.of(key, value), () -> {}, System.currentTimeMillis() + 10000);
    }
}

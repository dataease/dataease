package io.dataease.ai.scene.calc;

import io.dataease.ai.support.AiErrors;
import io.dataease.ai.tool.datasource.DatasetSqlSemanticTools;
import io.dataease.api.ai.conversation.AiConversationDtos.TableEvidence;
import io.dataease.api.ai.conversation.AiConversationDtos.ValueMeaning;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;

import java.util.*;

/** Build semantic context for visible references in one editor opening. Table and field confirmations are independent. */
final class CalcFieldSemantics {
    private record PhysicalTable(long source, String table) { }
    private final Map<PhysicalTable, TableEvidence> inspected = new HashMap<>();
    private final CalcFieldCanvas canvas;
    private final DatasetSqlSemanticTools registry;
    private final Map<Long, Map<String, Object>> tableMeanings = new LinkedHashMap<>();

    CalcFieldSemantics(CalcFieldCanvas canvas, DatasetSqlSemanticTools registry) {
        this.canvas = canvas;
        this.registry = registry;
    }

    void describe(DatasetTableFieldDTO field, CalcFieldAccess.Column column, Map<String, Object> item) {
        var meaning = new LinkedHashMap<String, Object>();
        if (Boolean.TRUE.equals(field.getSemanticConfirmed())) {
            text(meaning, "businessName", field.getSemanticBusinessName());
            text(meaning, "description", field.getSemanticDescription());
            text(meaning, "aliases", field.getSemanticAliases());
            var values = valueMeanings(field.getSemanticValueReferences());
            if (!values.isEmpty()) meaning.put("values", values);
        }
        if (column != null) {
            var node = canvas.tables.get(column.node());
            var tableMeaning = tableMeanings.computeIfAbsent(column.node(), ignored -> tableMeaning(node));
            if (!tableMeaning.isEmpty()) item.put("tableNode", node.getId().toString());
            if (meaning.isEmpty() && "db".equalsIgnoreCase(node.getType())) {
                for (var source : inspect(node).fields()) {
                    if (!source.name().equals(column.column())) continue;
                    text(meaning, "businessName", source.businessName());
                    text(meaning, "description", source.description());
                    if (!source.values().isEmpty()) meaning.put("values", source.values());
                    break;
                }
            }
        }
        item.putAll(meaning);
    }

    private Map<String, Object> tableMeaning(DatasetTableDTO node) {
        var meaning = new LinkedHashMap<String, Object>();
        if (Boolean.TRUE.equals(node.getSemanticConfirmed())) {
            text(meaning, "description", node.getSemanticDescription());
            text(meaning, "grain", node.getSemanticGrain());
        }
        if (meaning.isEmpty() && "db".equalsIgnoreCase(node.getType())) {
            var source = inspect(node);
            text(meaning, "description", source.description());
            text(meaning, "grain", source.grain());
        }
        if (!meaning.isEmpty()) {
            meaning.put("table", node.getTableName());
            meaning.put("tableNode", node.getId().toString());
        }
        return meaning;
    }

    List<Map<String, Object>> tables() {
        return tableMeanings.values().stream().filter(table -> !table.isEmpty()).toList();
    }

    private TableEvidence inspect(DatasetTableDTO node) {
        var key = new PhysicalTable(node.getDatasourceId(), node.getTableName());
        return inspected.computeIfAbsent(key, table -> {
            var scope = canvas.sources.get(Long.toString(table.source()));
            return registry.inspect(scope, table.table(), canvas.physicalTables.get(node.getId()));
        });
    }

    private static void text(Map<String, Object> item, String key, String value) {
        if (value != null && !value.isBlank()) item.put(key, value);
    }

    private static List<ValueMeaning> valueMeanings(String json) {
        if (json == null || json.isBlank()) return List.of();
        if (json.length() > 16000) throw AiErrors.error("input_limit");
        try {
            var values = CalcFieldCanvas.JSON.readTree(json);
            if (!values.isArray() || values.size() > 20) throw AiErrors.error("invalid");
            var result = new ArrayList<ValueMeaning>();
            for (var entry : values) {
                var value = entry.path("value");
                var meaning = entry.path("meaning");
                if (value.isTextual() && !value.asText().isBlank() && meaning.isTextual() && !meaning.asText().isBlank())
                    result.add(new ValueMeaning(value.asText(), meaning.asText()));
            }
            return result;
        } catch (java.io.IOException malformed) {
            throw AiErrors.error("invalid");
        }
    }
}

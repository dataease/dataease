package io.dataease.ai.tool.datasource;

import io.dataease.ai.engine.runtime.AiToolProtocol;
import io.dataease.ai.tool.AiTool;
import io.dataease.api.ai.conversation.AiConversationDtos.*;
import io.dataease.api.ds.DatasourceSemanticApi.*;
import io.dataease.datasource.manage.DatasourceSemanticManage;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/** Search the local registration before inspecting a selected table's current physical structure. */
@Component
public class DatasetSqlSemanticTools {
    private final AiDatasourceTools metadata;
    private final DatasourceSemanticManage registry;

    public DatasetSqlSemanticTools(AiDatasourceTools metadata, DatasourceSemanticManage registry) {
        this.metadata = metadata;
        this.registry = registry;
    }

    public AiTool<AiDatasourceTools.Scope> fields() {
        var definition = new AiToolProtocol.Definition("get_table_fields",
                "Inspect live fields on one exact table. Only confirmed business names, descriptions and explicit value/meaning pairs establish semantics. Physical comments and aliases are hints.",
                Map.of("table", "Exact table named in the request, existing SQL, or search results"),
                AiToolProtocol.RetrySafety.READ_ONLY);
        return new AiTool<>(definition, (scope, args, active, deadline) -> {
            String table = args.get("table");
            var structure = metadata.fields(scope, table, true);
            active.run();
            return AiToolProtocol.result(inspect(scope, table, structure));
        });
    }

    public AiTool<AiDatasourceTools.Scope> namedFields() {
        var definition = new AiToolProtocol.Definition("get_named_table_fields",
                "Refresh metadata for known physical column names on one exact table. Use get_table_fields to identify columns by spelling or business meaning.",
                Map.of("table", "Exact table on the current datasource", "fields", "Comma-separated exact column names, at most 32"),
                AiToolProtocol.RetrySafety.READ_ONLY);
        return new AiTool<>(definition, (scope, args, active, deadline) -> {
            String table = args.get("table");
            var names = Arrays.stream(args.get("fields").split("[,，]", -1)).map(String::strip).distinct().toList();
            if (names.size() > 32 || names.stream().anyMatch(n -> n.isEmpty() || n.length() > 256))
                return AiToolProtocol.result(Map.of("status", "INVALID", "code", "fields"));
            var physical = metadata.namedFields(scope, table, names);
            active.run();
            var selected = inspect(scope, table, physical).fields();
            var found = selected.stream().map(FieldEvidence::name).collect(Collectors.toSet());
            return AiToolProtocol.result(Map.of("source", "CURRENT_READ", "table", table, "fields", selected,
                    "absentFields", names.stream().filter(n -> !found.contains(n)).toList()));
        });
    }

    /** The caller supplies the exact authorized, live structure read for this turn. */
    public TableEvidence inspect(AiDatasourceTools.Scope scope, String table, AiDatasourceTools.TableFields physical) {
        metadata.authorize(scope);
        if (!physical.table().equals(table)) throw new IllegalArgumentException("Mismatched table structure");
        var id = Long.valueOf(scope.resourceId());
        var registration = registry.list(id).tables().stream()
                .filter(t -> t.active() && t.available() && table.equals(t.tableName())).findFirst().orElse(null);
        var confirmed = new HashMap<String, FieldSemantic>();
        if (registration != null) registry.detail(id, registration.id()).fields().stream()
                .filter(FieldSemantic::confirmed).forEach(f -> confirmed.put(f.fieldName(), f));
        var fields = physical.fields().stream().map(field -> {
            var meaning = confirmed.get(field.name());
            if (meaning != null && (meaning.physicalType() == null || meaning.physicalType().isBlank()
                    || !meaning.physicalType().equalsIgnoreCase(field.type()))) meaning = null;
            var values = meaning == null || meaning.valueReferences() == null ? List.<ValueMeaning>of()
                    : meaning.valueReferences().stream().filter(v -> v.value() != null && v.meaning() != null && !v.meaning().isBlank())
                    .map(v -> new ValueMeaning(v.value(), v.meaning())).toList();
            return new FieldEvidence(field.name(), field.type(), field.comment(),
                    meaning == null ? null : meaning.businessName(), meaning == null ? null : meaning.description(), values);
        }).toList();
        metadata.authorize(scope);
        return new TableEvidence(table, true, registration != null && registration.confirmed() ? registration.description() : null,
                registration != null && registration.confirmed() ? registration.grain() : null, fields);
    }

    private record Match(String table, List<String> matchedTerms, boolean exact) {}

    public AiTool<AiDatasourceTools.Scope> search() {
        var definition = new AiToolProtocol.Definition("search_semantics",
                "Search only local registered structure and confirmed semantic metadata. Results are leads; inspect selected tables before using semantics.",
                Map.of("terms", "Comma-separated exact table names and distinct business terms from this question"),
                AiToolProtocol.RetrySafety.READ_ONLY);
        return new AiTool<>(definition, (scope, args, active, deadline) -> {
            metadata.authorize(scope);
            var id = Long.valueOf(scope.resourceId());
            var terms = Arrays.stream(args.get("terms").split("[,，]")).map(String::strip)
                    .filter(s -> !s.isEmpty()).distinct().limit(12).toList();
            var fields = registry.registeredFields(id).stream().collect(Collectors.groupingBy(FieldSemantic::tableName));
            var matches = new ArrayList<Match>();
            for (var table : registry.list(id).tables()) {
                active.run();
                if (!table.active() || !table.available()) continue;
                var labels = new ArrayList<String>();
                Collections.addAll(labels, table.tableName(), table.physicalLabel());
                if (table.confirmed()) Collections.addAll(labels, table.description(), table.grain());
                for (var field : fields.getOrDefault(table.tableName(), List.of())) {
                    Collections.addAll(labels, field.fieldName(), field.physicalComment());
                    if (field.confirmed()) {
                        Collections.addAll(labels, field.businessName(), field.description(), field.aliases());
                        if (field.valueReferences() != null) field.valueReferences().forEach(value -> labels.add(value.meaning()));
                    }
                }
                var found = terms.stream().filter(term -> labels.stream().filter(Objects::nonNull)
                        .anyMatch(label -> label.toLowerCase(Locale.ROOT).contains(term.toLowerCase(Locale.ROOT)))).toList();
                if (!found.isEmpty()) matches.add(new Match(table.tableName(), found, terms.contains(table.tableName())));
            }
            matches.sort(Comparator.comparing(Match::exact).reversed()
                    .thenComparing(Comparator.comparingInt((Match m) -> m.matchedTerms().size()).reversed())
                    .thenComparing(Match::table));
            metadata.authorize(scope);
            return AiToolProtocol.result(Map.of("tables", matches.stream().limit(6).toList(), "more", matches.size() > 6));
        });
    }
}

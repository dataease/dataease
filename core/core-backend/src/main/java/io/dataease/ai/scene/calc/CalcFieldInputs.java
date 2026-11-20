package io.dataease.ai.scene.calc;

import io.dataease.ai.support.AiErrors;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.union.UnionDTO;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import java.util.*;

/** The model gets display names and types. Provider input uses only checked, resolved field IDs. */
final class CalcFieldInputs {
    private final Map<String, String> labels = new HashMap<>();
    private final Map<String, DatasetTableFieldDTO> available = new LinkedHashMap<>();
    private final Map<String, String> paramNames = new HashMap<>();
    final DatasetTableFieldDTO target;
    final List<Map<String,Object>> projection = new ArrayList<>();

    CalcFieldInputs(DatasetGroupInfoDTO dataset, Long targetId, CalcFieldAccess.Grant grant,
                    Map<Long, CalcFieldAccess.Column> physical) {
        var fields = dataset.getAllFields();
        if (fields == null || fields.isEmpty() || fields.size() > 256) throw AiErrors.error("input_limit");
        var seen = new HashSet<Long>();
        DatasetTableFieldDTO editing = null;
        for (var f : fields) {
            if (f == null || f.getId() == null || !seen.add(f.getId())) throw AiErrors.error("invalid");
            CalcFieldCanvas.clearAliases(f);
            f.setDataeaseName(grant.aliases().get(f.getId()));
            if (f.getId().equals(targetId)) {
                if (!Objects.equals(f.getExtField(), 2)) throw AiErrors.error("invalid");
                editing = f;
                continue;
            }
            if (grant.hidden().contains(f.getId()) || Boolean.FALSE.equals(f.getChecked())) continue;
            var column = physical.get(f.getId());
            if (Objects.equals(f.getExtField(), 0)) {
                if (column == null || !Objects.equals(column.column(), f.getOriginName())
                        || !Objects.equals(column.source(), f.getDatasourceId())
                        || !Objects.equals(column.node(), f.getDatasetTableId())) throw AiErrors.error("invalid");
            } else if (!Objects.equals(f.getExtField(), 2) || f.getParams() != null && !f.getParams().isEmpty()) continue;
            if (f.getName() == null || f.getName().isBlank() || f.getName().length() > 256 || f.getDeType() == null) continue;
            available.put(f.getId().toString(), f);
        }
        if (editing == null || grant.hidden().contains(targetId)) throw AiErrors.error("forbidden");
        target = editing;
        if (target.getParams() != null) {
            for (var param : target.getParams()) {
                if (param == null || param.getId() == null || !param.getId().matches("(?:params_)?[0-9]{1,19}") || available.containsKey(param.getId())
                        || targetId.toString().equals(param.getId()) || param.getName() == null || param.getName().isBlank()
                        || param.getName().length() > 128 || param.getValue() == null || !param.getValue().matches("-?[0-9]{1,32}(?:\\.[0-9]{1,16})?")
                        || paramNames.putIfAbsent(param.getId(), param.getName()) != null) throw AiErrors.error("invalid");
                label(param.getName(), param.getId());
                projection.add(Map.of("name", param.getName(), "kind", "parameter"));
            }
        }
        for (var entry : available.entrySet()) {
            try {
                dependencies(entry.getKey(), new HashSet<>(), new HashSet<>(), new int[]{0});
            } catch (IllegalArgumentException unavailable) { continue; }
            var f = entry.getValue();
            label(f.getName(), entry.getKey());
            projection.add(Map.of("name", f.getName(), "deType", f.getDeType()));
        }
    }

    private void label(String name, String id) { labels.merge(name, id, (a, b) -> ""); }

    String bind(String expression) { return CalcFieldExpression.bind(expression, labels::get); }

    private void dependencies(String id, Set<String> path, Set<String> used, int[] expanded) {
        var f = available.get(id);
        if (f == null || path.size() >= 8 || ++expanded[0] > 128 || !path.add(id)) throw CalcFieldExpression.invalid();
        used.add(id);
        if (Objects.equals(f.getExtField(), 2)) {
            CalcFieldExpression.bind(f.getOriginName(), ref -> {
                dependencies(ref, path, used, expanded);
                return ref;
            });
        }
        path.remove(id);
    }

    void candidate(DatasetGroupInfoDTO draft, String expression, int deType, boolean preserveType) {
        var used = new HashSet<String>();
        var expanded = new int[]{0};
        String binding = CalcFieldExpression.bind(expression, label -> {
            var id = labels.get(label);
            if (!paramNames.containsKey(id)) dependencies(id, new HashSet<>(), used, expanded);
            return id;
        });
        for (String id : used) {
            var field = available.get(id);
            if (Objects.equals(field.getExtField(), 2))
                field.setOriginName(CalcFieldExpression.bind(field.getOriginName(), ref -> available.containsKey(ref) ? ref : null));
        }
        target.setOriginName(binding);
        if (!preserveType) {
            target.setDeType(deType);
            target.setDeExtractType(deType);
        }
        target.setChecked(true);
        // Physical fields remain available to join and row predicates. Execute only reachable calculated fields.
        draft.setAllFields(draft.getAllFields().stream().filter(f -> f == target
                || available.containsKey(f.getId().toString()) && (Objects.equals(f.getExtField(), 0)
                || used.contains(f.getId().toString()))).toList());
    }

    static void checkNodes(UnionDTO node, CalcFieldAccess.Grant grant) {
        for (var f : node.getCurrentDsFields()) {
            f.setDataeaseName(grant.aliases().get(f.getId()));
            if (grant.hidden().contains(f.getId())) f.setChecked(false);
        }
        var join = node.getUnionToParent();
        if (join != null && join.getUnionFields() != null) for (var key : join.getUnionFields()) {
            if (key == null || key.getParentField() == null || key.getCurrentField() == null
                    || grant.hidden().contains(key.getParentField().getId()) || grant.hidden().contains(key.getCurrentField().getId()))
                throw AiErrors.error("forbidden");
        }
        for (var child : node.getChildrenDs()) checkNodes(child, grant);
    }
}

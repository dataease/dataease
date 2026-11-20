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
    private static final java.util.regex.Pattern BOUND_REFERENCE = java.util.regex.Pattern.compile("\\[([^\\]]+)]");
    private final Set<Long> hidden;
    private final Map<String, String> paramNames = new HashMap<>();
    final DatasetTableFieldDTO target;
    final List<Map<String,Object>> projection = new ArrayList<>();

    CalcFieldInputs(DatasetGroupInfoDTO dataset, Long targetId, CalcFieldAccess.Grant grant,
                    Map<Long, CalcFieldAccess.Column> physical) {
        hidden = grant.hidden();
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
                dependencies(entry.getKey(), new HashSet<>(), new int[]{0});
            } catch (IllegalArgumentException unavailable) { continue; }
            var f = entry.getValue();
            label(f.getName(), entry.getKey());
            projection.add(Map.of("name", f.getName(), "deType", f.getDeType()));
        }
    }

    private void label(String name, String id) { labels.merge(name, id, (a, b) -> ""); }

    private void dependencies(String id, Set<String> path, int[] expanded) {
        var f = available.get(id);
        if (f == null || path.size() >= 8 || ++expanded[0] > 128 || !path.add(id)) throw CalcFieldExpression.invalid();
        if (Objects.equals(f.getExtField(), 2)) {
            CalcFieldExpression.bind(f.getOriginName(), ref -> {
                if (f != target || !paramNames.containsKey(ref)) dependencies(ref, path, expanded);
                return ref;
            });
        }
        path.remove(id);
    }

    String candidate(String expression, int deType, boolean preserveType) {
        var expanded = new int[]{0};
        var displayNames = new HashMap<String, String>();
        String binding = CalcFieldExpression.bind(expression, label -> {
            var id = labels.get(label);
            if (!paramNames.containsKey(id)) dependencies(id, new HashSet<>(), expanded);
            displayNames.put(id, label);
            return id;
        });
        target.setOriginName(binding);
        if (!preserveType) {
            target.setDeType(deType);
            target.setDeExtractType(deType);
        }
        target.setChecked(true);
        // Render the editor expression from the very same parse used by the Provider.
        return BOUND_REFERENCE.matcher(binding)
                .replaceAll(match -> java.util.regex.Matcher.quoteReplacement("[" + displayNames.get(match.group(1)) + "]"));
    }

    /** Do not claim success if a sibling was dropped, or execute unchecked sibling SQL. */
    boolean includePreviewFields(DatasetGroupInfoDTO draft) {
        available.put(target.getId().toString(), target);
        var fields = draft.getAllFields().stream().filter(f -> !hidden.contains(f.getId())).toList();
        for (var field : fields) {
            if (field == target) continue;
            try {
                dependencies(field.getId().toString(), new HashSet<>(), new int[]{0});
                if (Objects.equals(field.getExtField(), 2))
                    field.setOriginName(CalcFieldExpression.bind(field.getOriginName(), ref -> available.containsKey(ref) ? ref : null));
            } catch (IllegalArgumentException unsupportedSibling) { return false; }
        }
        draft.setAllFields(fields);
        return true;
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

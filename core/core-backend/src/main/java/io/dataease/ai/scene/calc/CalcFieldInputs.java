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
    private final boolean isCross;
    private final Map<String, Set<String>> parameterOwners = new HashMap<>();
    private final Map<String, Integer> nameCounts = new HashMap<>();
    private final Map<String, List<String>> choices = new HashMap<>();
    final DatasetTableFieldDTO target;
    final List<Map<String,Object>> projection = new ArrayList<>();

    CalcFieldInputs(DatasetGroupInfoDTO dataset, Long targetId, CalcFieldAccess.Grant grant,
                    Map<Long, CalcFieldAccess.Column> physical, CalcFieldSemantics semantics) {
        hidden = grant.hidden();
        isCross = dataset.getIsCross();
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
            } else if (!Objects.equals(f.getExtField(), 2)) continue;
            if (f.getName() == null || f.getName().isBlank() || f.getName().length() > 256 || f.getDeType() == null) continue;
            available.put(f.getId().toString(), f);
        }
        if (editing == null || grant.hidden().contains(targetId)) throw AiErrors.error("forbidden");
        target = editing;
        // Copying a calculation preserves its parameter IDs. The preview merges their defaults
        // across the dataset, while individual formulas may reference only parameters they own.
        var defaults = new HashMap<String, String>();
        var referenceIds = new HashSet<String>();
        seen.forEach(id -> referenceIds.add(id.toString()));
        for (var field : fields) {
            if (hidden.contains(field.getId()) || field.getParams() == null || field.getParams().isEmpty()) continue;
            if (!Objects.equals(field.getExtField(), 2) || field.getParams().size() > 256) throw AiErrors.error("invalid");
            for (var param : field.getParams()) {
                if (param == null || param.getId() == null || !param.getId().matches("(?:params_)?[0-9]{1,19}")
                        || referenceIds.contains(param.getId()) || param.getName() == null || param.getName().isBlank()
                        || param.getName().length() > 128 || param.getValue() == null
                        || !param.getValue().matches("-?[0-9]{1,32}(?:\\.[0-9]{1,16})?")) throw AiErrors.error("invalid");
                String previous = defaults.putIfAbsent(param.getId(), param.getValue());
                if (previous != null && new java.math.BigDecimal(previous).compareTo(new java.math.BigDecimal(param.getValue())) != 0
                        || !parameterOwners.computeIfAbsent(param.getId(), ignored -> new HashSet<>()).add(field.getId().toString()))
                    throw AiErrors.error("invalid");
                if (field == target) nameCounts.merge(param.getName(), 1, Integer::sum);
            }
        }
        var visible = new ArrayList<DatasetTableFieldDTO>();
        for (var field : available.values()) {
            try {
                dependencies(field.getId().toString(), new HashSet<>(), new int[]{0});
            } catch (IllegalArgumentException unavailable) { continue; }
            visible.add(field);
            nameCounts.merge(field.getName(), 1, Integer::sum);
        }
        if (target.getParams() != null) for (var param : target.getParams())
            projection.add(Map.of("name", param.getName(), "kind", "parameter", "reference", label(param.getName(), param.getId())));
        for (var f : visible) {
            var item = new LinkedHashMap<String, Object>();
            item.put("name", f.getName());
            item.put("reference", label(f.getName(), f.getId().toString()));
            item.put("deType", f.getDeType());
            var column = physical.get(f.getId());
            if (column != null) {
                item.put("table", column.table());
                if (nameCounts.get(f.getName()) > 1) item.put("sourceId", column.source().toString());
                item.put("column", column.column());
            } else {
                item.put("kind", "calculated");
            }
            if (semantics != null) semantics.describe(f, column, item);
            projection.add(item);
        }
    }

    private String label(String name, String id) {
        boolean qualified = nameCounts.get(name) > 1 || name.contains("[") || name.contains("]")
                || name.matches("(?s).* #(?:params_)?-?[0-9]+$");
        String reference = name;
        if (qualified) {
            reference = name.replace('[', '［').replace(']', '］') + " #" + id;
            while (nameCounts.containsKey(reference)) reference += " #" + id;
        }
        labels.put(reference, id);
        choices.computeIfAbsent(name, ignored -> new ArrayList<>()).add(reference);
        return reference;
    }

    static final class Ambiguous extends IllegalArgumentException {
        final List<String> options;
        Ambiguous(List<String> options) { this.options = List.copyOf(options); }
    }

    private boolean ownsParameter(String field, String reference) {
        return parameterOwners.getOrDefault(reference, Set.of()).contains(field);
    }

    private void dependencies(String id, Set<String> path, int[] expanded) {
        var f = available.get(id);
        if (f == null || path.size() >= 8 || ++expanded[0] > 128 || !path.add(id)) throw CalcFieldExpression.invalid();
        if (Objects.equals(f.getExtField(), 2)) {
            CalcFieldExpression.bindSibling(f.getOriginName(), isCross, ref -> {
                if (!ownsParameter(id, ref)) dependencies(ref, path, expanded);
                return ref;
            });
        }
        path.remove(id);
    }

    String candidate(String expression, int deType, boolean preserveType) {
        var expanded = new int[]{0};
        var displayNames = new HashMap<String, String>();
        String binding = CalcFieldExpression.bind(expression, isCross, label -> {
            var id = labels.get(label);
            if (id == null && choices.getOrDefault(label, List.of()).size() > 1) throw new Ambiguous(choices.get(label));
            if (id == null) throw CalcFieldExpression.invalid();
            if (!ownsParameter(target.getId().toString(), id)) dependencies(id, new HashSet<>(), expanded);
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
                if (Objects.equals(field.getExtField(), 3)) {
                    if (!validGroup(field)) return false;
                    continue;
                }
                dependencies(field.getId().toString(), new HashSet<>(), new int[]{0});
                if (Objects.equals(field.getExtField(), 2)) {
                    // dependencies already checked every reference, including private parameters.
                    field.setOriginName(CalcFieldExpression.bindSibling(field.getOriginName(), isCross, ref -> ref));
                }
            } catch (IllegalArgumentException unsupportedSibling) { return false; }
        }
        draft.setAllFields(fields);
        return true;
    }

    private boolean validGroup(DatasetTableFieldDTO field) {
        var source = available.get(field.getOriginName());
        if (source == null || !Objects.equals(source.getExtField(), 0) || !shortText(field.getOtherGroup())
                || field.getGroupList() == null || field.getGroupList().isEmpty() || field.getGroupList().size() > 128) return false;
        int values = 0;
        for (var group : field.getGroupList()) {
            if (group == null || !shortText(group.getName())) return false;
            switch (source.getDeType()) {
                case 0 -> {
                    if (group.getText() == null || group.getText().isEmpty() || (values += group.getText().size()) > 512
                            || !group.getText().stream().allMatch(CalcFieldInputs::shortText)) return false;
                }
                case 1 -> {
                    if (!dateTime(group.getStartTime()) || !dateTime(group.getEndTime())) return false;
                }
                case 2, 3, 4 -> {
                    if (!number(group.getMin()) || !number(group.getMax())
                            || !("le".equals(group.getMinTerm()) || "lt".equals(group.getMinTerm()))
                            || !("le".equals(group.getMaxTerm()) || "lt".equals(group.getMaxTerm()))) return false;
                }
                default -> { return false; }
            }
        }
        return true;
    }

    private static boolean shortText(String value) {
        return value != null && value.length() <= 256 && value.chars().noneMatch(Character::isISOControl);
    }

    private static boolean dateTime(String value) {
        if (value == null || !value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}")) return false;
        try {
            java.time.LocalDateTime.parse(value.replace(' ', 'T'));
            return true;
        } catch (java.time.DateTimeException invalid) { return false; }
    }

    private static boolean number(String value) {
        return value != null && value.matches("-?[0-9]{1,32}(?:\\.[0-9]{1,16})?");
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

package io.dataease.ai.engine.conversation;

import io.dataease.ai.engine.runtime.AiToolProtocol;
import io.dataease.ai.engine.conversation.AiConversationScene.HistoricalStep;
import io.dataease.ai.engine.conversation.EditorStepHistory.Answer;
import java.util.*;
import java.util.stream.Collectors;

/** The model supplies the author's clues; only matching records from this conversation may leave it. */
final class EditorStepSearch {
    static final AiToolProtocol.Definition TOOL = new AiToolProtocol.Definition("find_historical_step",
            "Find prior editor evidence by action and clues in the author's question. No business query.",
            Map.of("action", "Original tool name, e.g. get_table_fields or propose_calc_field",
                    "subject", "Exact table, proposed formula name or expression; * when unspecified",
                    "fields", "Requested physical column names separated by commas, temporal for date/time columns, #count for the number of returned columns, or * for step status",
                    "selection", "any when versions may differ; latest for an explicitly last matching call; source:<exact datasource name>, answer number, or answer.step from a clarification"),
            AiToolProtocol.RetrySafety.READ_ONLY);

    private record Match(int answer, Answer original, HistoricalStep step, Map<String, Object> evidence) {
        Map<String, Object> label() {
            var result = new LinkedHashMap<String, Object>();
            result.put("answerNumber", answer);
            result.put("stepNumber", step.number());
            result.put("resourceName", original.context().resourceName());
            if (step.parameters().containsKey("table")) result.put("table", step.parameters().get("table"));
            if (evidence.containsKey("expression")) result.put("expression", evidence.get("expression"));
            return result;
        }
    }

    private record Facts(String resource, boolean cross, String table, Map<String, String> parameters, String status, String error,
                         Map<String, Object> evidence) { }

    static Map<String, Object> find(List<Answer> answers, AiConversationScene scene, Map<String, String> clues) {
        String action = clues.get("action");
        String subject = clues.get("subject");
        String fields = clues.get("fields");
        String selection = clues.get("selection");
        String source = selection.startsWith("source:") ? selection.substring(7).strip() : null;
        if (source == null && !selection.matches("any|latest|[1-9][0-9]*(?:\\.[1-9][0-9]*)?") || TOOL.name().equals(action))
            return Map.of("lookup", "MISSING");
        String projection = switch (fields) {
            case "*" -> "";
            case "temporal" -> "temporal columns";
            case "#count" -> "how many columns";
            default -> Arrays.stream(fields.split("[,，]")).map(String::strip).filter(s -> !s.isEmpty())
                    .map(s -> "`" + s + "`").collect(Collectors.joining(" "));
        };
        var matches = new ArrayList<Match>();
        var seen = new HashSet<Facts>();
        var authorized = Collections.newSetFromMap(new IdentityHashMap<AiConversationScene.Context, Boolean>());
        for (int i = answers.size() - 1; i >= 0; i--) {
            Answer answer = answers.get(i);
            if (source != null && !source.equals(answer.context().resourceName())) continue;
            int answerNumber = i + 1;
            for (var step : answer.steps().reversed()) {
                boolean sameAction = action.equals(step.name())
                        || "propose_calc_field".equals(action) && "revalidate_current_formula".equals(step.name())
                        || "get_table_fields".equals(action) && "get_named_table_fields".equals(step.name());
                if (!sameAction) continue;
                if (source == null && !"any".equals(selection) && !"latest".equals(selection)
                        && !selection.equals(String.valueOf(answerNumber)) && !selection.equals(answerNumber + "." + step.number())) continue;
                boolean namedSubject = !"*".equals(subject);
                boolean knownSubject = List.of("table", "name", "expression", "topic", "fromTable", "toTable").stream()
                        .anyMatch(key -> subject.equalsIgnoreCase(step.parameters().get(key)));
                if (namedSubject && !knownSubject && !"revalidate_current_formula".equals(step.name())) continue;
                if (authorized.add(answer.context())) scene.authorize(answer.context());
                var evidence = scene.historicalEvidence(answer.context(), step, projection);
                if (namedSubject && !knownSubject && !subject.equalsIgnoreCase(Objects.toString(evidence.get("expression"), ""))) continue;
                if (!namedSubject && !"*".equals(fields) && !("#count".equals(fields) && evidence.containsKey("coverage"))
                        && (!(evidence.get("fields") instanceof List<?> selected) || selected.isEmpty())) continue;
                var facts = new Facts(answer.context().resourceId(), answer.context().isCross(), step.parameters().get("table"),
                        evidence.isEmpty() ? step.parameters() : Map.of(), step.status(), step.error(), evidence);
                if (seen.add(facts)) matches.add(new Match(answerNumber, answer, step, evidence));
                if ("latest".equals(selection)) break;
            }
            if ("latest".equals(selection) && !matches.isEmpty()) break;
        }
        if (matches.isEmpty()) return Map.of("lookup", "MISSING");
        if (matches.size() == 1) {
            var match = matches.getFirst();
            return EditorStepHistory.found(match.answer(), match.original().context(), match.step(), match.evidence());
        }
        return Map.of("lookup", "AMBIGUOUS", "candidates", matches.stream().limit(5).map(Match::label).toList(),
                "more", matches.size() > 5);
    }
}

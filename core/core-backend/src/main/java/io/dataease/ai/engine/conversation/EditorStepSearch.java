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
                    "fields", "Requested physical column names separated by commas, temporal for date/time columns, or * for step status",
                    "selection", "any when versions may differ; latest for an explicitly last matching call; answer number, or answer.step from a clarification"),
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

    static Map<String, Object> find(List<Answer> answers, AiConversationScene scene, Map<String, String> clues) {
        String action = clues.get("action");
        String subject = clues.get("subject");
        String fields = clues.get("fields");
        String selection = clues.get("selection");
        if (!selection.matches("any|latest|[1-9][0-9]*(?:\\.[1-9][0-9]*)?") || TOOL.name().equals(action))
            return Map.of("lookup", "MISSING");
        String projection = switch (fields) {
            case "*" -> "";
            case "temporal" -> "temporal columns";
            default -> Arrays.stream(fields.split("[,，]")).map(String::strip).filter(s -> !s.isEmpty())
                    .map(s -> "`" + s + "`").collect(Collectors.joining(" "));
        };
        var matches = new ArrayList<Match>();
        for (int i = answers.size() - 1; i >= 0; i--) {
            Answer answer = answers.get(i);
            for (var step : answer.steps().reversed()) {
                if (!action.equals(step.name())) continue;
                if (!"any".equals(selection) && !"latest".equals(selection)
                        && !selection.equals(String.valueOf(i + 1)) && !selection.equals((i + 1) + "." + step.number())) continue;
                if (!"*".equals(subject) && List.of("table", "name", "expression", "topic", "fromTable", "toTable").stream()
                        .noneMatch(key -> subject.equalsIgnoreCase(step.parameters().get(key)))) continue;
                scene.authorize(answer.context());
                var evidence = scene.historicalEvidence(answer.context(), step, projection);
                if ("*".equals(subject) && !"*".equals(fields)
                        && (!(evidence.get("fields") instanceof List<?> selected) || selected.isEmpty())) continue;
                matches.add(new Match(i + 1, answer, step, evidence));
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

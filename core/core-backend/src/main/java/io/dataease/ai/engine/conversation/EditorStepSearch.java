package io.dataease.ai.engine.conversation;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.engine.runtime.AiToolProtocol;
import io.dataease.ai.engine.conversation.AiConversationScene.HistoricalFields;
import io.dataease.ai.engine.conversation.AiConversationScene.HistoricalStep;
import io.dataease.ai.engine.conversation.EditorStepHistory.Answer;
import java.util.*;
import java.io.IOException;

/** The model supplies the author's clues; only matching records from this conversation may leave it. */
final class EditorStepSearch {
    private static final ObjectMapper JSON = new ObjectMapper();
    static final AiToolProtocol.Definition TOOL = new AiToolProtocol.Definition("find_historical_step",
            "Look up an earlier step of this conversation by number or clues. Use the author's requested evidence; no business query.",
            Map.of("action", "Original tool name, or * for an exact step number when its action is unknown",
                    "subject", "Exact table, proposed formula name or expression; * when unspecified",
                    "fields", "Use exact physical names (up to 32, comma-separated), temporal for date/time fields, #count for the recorded column count, #names for recorded names, or * for status. For literal names with commas or reserved words, pass a string encoding a JSON array of names.",
                    "selection", "last.N for step N of the most recent answer; A.N for step N of answer A. Otherwise any, latest for the last matching action, source:<exact datasource name>, or an answer number."),
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

    private static HistoricalFields fields(String value) {
        var names = new LinkedHashSet<String>();
        boolean temporal = false, count = false, listNames = false;
        if (value.stripLeading().startsWith("[")) {
            try {
                var array = JSON.reader().with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readTree(value);
                if (!array.isArray() || array.isEmpty() || array.size() > 32) return null;
                for (var name : array) {
                    if (!name.isTextual()) return null;
                    names.add(name.textValue());
                }
            } catch (IOException invalid) {
                return null;
            }
        } else if (!"*".equals(value)) {
            for (String part : value.split("[,，]", -1)) {
                switch (part.strip()) {
                    case "temporal" -> temporal = true;
                    case "#count" -> count = true;
                    case "#names" -> listNames = true;
                    default -> names.add(part.strip());
                }
            }
        }
        if (names.size() > 32 || names.stream().anyMatch(name -> name.isBlank() || name.length() > 256)) return null;
        return new HistoricalFields(List.copyOf(names), temporal, count, listNames);
    }

    static Map<String, Object> find(List<Answer> answers, AiConversationScene scene, Map<String, String> clues) {
        String action = clues.get("action");
        String subject = clues.get("subject");
        String fields = clues.get("fields");
        String selection = clues.get("selection");
        if (selection.startsWith("last.")) selection = answers.size() + selection.substring(4);
        boolean exactStep = selection.matches("[1-9][0-9]*\\.[1-9][0-9]*");
        String source = selection.startsWith("source:") ? selection.substring(7).strip() : null;
        if (source == null && !exactStep && !selection.matches("any|latest|[1-9][0-9]*")
                || !exactStep && ("*".equals(action) || TOOL.name().equals(action))) return Map.of("lookup", "MISSING");
        var projection = fields(fields);
        if (projection == null) return Map.of("status", "INVALID", "code", "fields");
        var matches = new ArrayList<Match>();
        var seen = new HashSet<Facts>();
        var authorized = Collections.newSetFromMap(new IdentityHashMap<AiConversationScene.Context, Boolean>());
        for (int i = answers.size() - 1; i >= 0; i--) {
            Answer answer = answers.get(i);
            if (source != null && !source.equals(answer.context().resourceName())) continue;
            int answerNumber = i + 1;
            for (var step : answer.steps().reversed()) {
                boolean sameAction = "*".equals(action) || action.equals(step.name())
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
                if (!exactStep && !namedSubject && !"*".equals(fields)
                        && !((projection.count() || projection.listNames()) && evidence.containsKey("coverage"))
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

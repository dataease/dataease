package io.dataease.ai.engine.conversation;

import java.util.*;

import io.dataease.ai.engine.conversation.AiConversationScene.Context;
import io.dataease.ai.engine.conversation.AiConversationScene.HistoricalStep;

/** A session's ordered editor answers, including stopped attempts and answers with no tools. */
final class EditorStepHistory {
    record Answer(String executionId, Context context, List<HistoricalStep> steps) {
        Answer { steps = List.copyOf(steps); }
    }

    // Frozen contexts of the same editor conversation share answer order, even when the user
    // manually retries an older execution. The dialogue prompt for a retry still uses its own branch.
    private final Map<String, Answer> executions = new LinkedHashMap<>();

    synchronized void record(String executionId, Context context, List<HistoricalStep> steps) {
        executions.put(executionId, new Answer(executionId, context, steps));
    }

    synchronized List<Answer> answers() { return List.copyOf(executions.values()); }
    synchronized int size() { return executions.size(); }

    static Map<String, Object> found(int answerNumber, Context context, HistoricalStep original, Map<String, Object> evidence) {
        var selected = new LinkedHashMap<String, Object>();
        selected.put("answerNumber", answerNumber);
        selected.put("stepNumber", original.number());
        selected.put("resourceName", context.resourceName());
        selected.put("lookup", "FOUND");
        selected.put("action", original.name());
        selected.put("status", original.status());
        if (original.error() != null) selected.put("error", original.error());
        if ("get_table_fields".equals(original.name()) || "get_named_table_fields".equals(original.name()))
            selected.put("table", original.parameters().get("table"));
        if (!evidence.isEmpty()) selected.put("evidence", evidence);
        return selected;
    }
}

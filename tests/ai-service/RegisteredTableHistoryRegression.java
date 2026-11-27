package io.dataease.ai.engine.conversation;

import java.util.List;
import java.util.Map;

public final class RegisteredTableHistoryRegression {
    public static void verify(AiConversationScene scene, AiConversationScene.Context context) {
        var legacy = "search_semantics";
        var current = "search_registered_tables";
        for (String recorded : List.of(legacy, current)) {
            var step = new AiConversationScene.HistoricalStep(1, "search", recorded, "SUCCEEDED",
                    Map.of("terms", "orders"), "{\"tables\":[],\"more\":false}", null);
            var history = new EditorStepHistory();
            history.record("answer", context, List.of(step));
            for (String action : List.of(legacy, current, "search_physical_tables")) {
                var result = EditorStepSearch.find(history.answers(), scene,
                        Map.of("action", action, "subject", "*", "fields", "*", "selection", "latest"));
                if ("search_physical_tables".equals(action)) {
                    if (!"MISSING".equals(result.get("lookup"))) throw new AssertionError(result);
                } else if (!"FOUND".equals(result.get("lookup")) || !recorded.equals(result.get("action"))
                        || !"SUCCEEDED".equals(result.get("status"))) {
                    throw new AssertionError("Looking up " + action + " in history recorded as " + recorded + ": " + result);
                }
            }
        }
    }
}

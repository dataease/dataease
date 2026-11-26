package io.dataease.ai.engine.conversation;

import java.util.*;
import java.util.regex.Pattern;

import io.dataease.ai.engine.conversation.AiConversationScene.Context;
import io.dataease.ai.engine.conversation.AiConversationScene.HistoricalStep;

/** A session's ordered editor answers, including stopped attempts and answers with no tools. */
final class EditorStepHistory {
    record Answer(String executionId, Context context, List<HistoricalStep> steps) {
        Answer { steps = List.copyOf(steps); }
    }

    private static final String NUMBER = "([0-9]+|[零〇一二兩两三四五六七八九十]+)";
    private static final Pattern STEP = Pattern.compile("(?:第\\s*" + NUMBER + "\\s*步|(?:步[骤驟]|\\bstep)\\s*#?\\s*" + NUMBER + ")", Pattern.CASE_INSENSITIVE);
    private static final Pattern ANSWER = Pattern.compile("(?:第\\s*" + NUMBER + "\\s*(?:次|个|個|条|條)?\\s*(?:回答|答复|答覆|回复|回覆)|\\b(first|second|third|[0-9]+)(?:st|nd|rd|th)?\\s+(?:answer|response|reply)\\b|\\b(?:answer|response|reply)\\s*#?\\s*([0-9]+))", Pattern.CASE_INSENSITIVE);

    // Frozen contexts of the same editor conversation share answer order, even when the user
    // manually retries an older execution. The dialogue prompt for a retry still uses its own branch.
    private final Map<String, Answer> executions = new LinkedHashMap<>();

    synchronized void record(String executionId, Context context, List<HistoricalStep> steps) {
        executions.put(executionId, new Answer(executionId, context, steps));
    }

    synchronized List<Answer> answers() { return List.copyOf(executions.values()); }
    synchronized int size() { return executions.size(); }

    private static OptionalInt ordinal(Pattern pattern, String question) {
        var match = pattern.matcher(question);
        if (!match.find()) return OptionalInt.empty();
        String number = "";
        for (int i = 1; i <= match.groupCount(); i++) if (match.group(i) != null) number = match.group(i);
        number = number.toLowerCase(Locale.ROOT).replace('两', '二').replace('兩', '二').replace('〇', '零');
        if (Character.isDigit(number.charAt(0))) {
            try { return OptionalInt.of(Integer.parseInt(number)); }
            catch (NumberFormatException tooLarge) { return OptionalInt.of(0); }
        }
        var english = List.of("first", "second", "third");
        if (english.contains(number)) return OptionalInt.of(english.indexOf(number) + 1);
        int tens = number.indexOf('十');
        if (tens < 0) return OptionalInt.of("零一二三四五六七八九".indexOf(number));
        int high = tens == 0 ? 1 : "零一二三四五六七八九".indexOf(number.substring(0, tens));
        int low = tens + 1 == number.length() ? 0 : "零一二三四五六七八九".indexOf(number.substring(tens + 1));
        return OptionalInt.of(high <= 0 || low < 0 ? 0 : high * 10 + low);
    }

    /** Called within the current owner's scope. No raw tool result or internal ID leaves this boundary. */
    static Map<String, Object> resolve(List<Answer> answers, AiConversationScene scene, String question) {
        var step = ordinal(STEP, question);
        if (step.isEmpty()) return Map.of();
        int answerNumber = ordinal(ANSWER, question).orElse(answers.size());
        int stepNumber = step.getAsInt();
        var selected = new LinkedHashMap<String, Object>();
        selected.put("answerNumber", answerNumber);
        selected.put("stepNumber", stepNumber);
        if (answerNumber < 1 || answerNumber > answers.size() || stepNumber < 1
                || stepNumber > answers.get(answerNumber - 1).steps().size()) {
            selected.put("lookup", "MISSING");
            return selected;
        }
        Answer answer = answers.get(answerNumber - 1);
        scene.authorize(answer.context());
        HistoricalStep original = answer.steps().get(stepNumber - 1);
        selected.put("lookup", "FOUND");
        selected.put("action", original.name());
        selected.put("status", original.status());
        if (original.error() != null) selected.put("error", original.error());
        if ("get_table_fields".equals(original.name())) selected.put("table", original.parameters().get("table"));
        var evidence = scene.historicalEvidence(answer.context(), original, question);
        if (!evidence.isEmpty()) selected.put("evidence", evidence);
        return selected;
    }
}

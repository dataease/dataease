package io.dataease.ai.engine.conversation;

import io.dataease.ai.engine.runtime.AiToolProtocol.Message;

import java.util.*;
import java.util.function.IntFunction;
import java.util.regex.Pattern;

/** Model-facing projection of completed editor questions and answers. Steps have a separate authorization and lifetime. */
record EditorDialogue(List<Message> original, int omitted, List<Map<String, Object>> notes) {
    private static final int HISTORY_CHARACTERS = 6000, RECENT_CHARACTERS = 3200;
    private static final int NOTE_CHARACTERS = 384;
    private static final Pattern OPTION = Pattern.compile("^\\s*(\\d{1,2})[.)、]\\s*(\\S.*)$");
    private static final Pattern AGREED = Pattern.compile("(?iu)^(?:yes|agreed|correct|confirmed|exactly|是的?|对的?|對的?|没错|沒錯|同意|确认|確認)[.!。！\\s]*$");
    private static final Pattern SENTENCES = Pattern.compile("[^。！？!?；;\\r\\n]+(?:[。！？!?；;]|$)|[^。！？!?；;\\r\\n]+");
    private static final Pattern LANGUAGE = Pattern.compile("(?iu)(?:回答|答复|答覆|回[复覆應应]|請?用|请用|都用|改用|[中英法德日西韩韓]文|中文|[体體][字中]|\\b(?:reply|respond|answer|speak|language|english|chinese)\\b)");
    private static final Pattern ONCE = Pattern.compile("(?iu)(?:仅?这[一轮次]|僅?這[一輪次]|本[轮輪次]|this (?:time|reply|answer|turn)|just (?:this|for now))");
    private static final Pattern QUESTION = Pattern.compile("(?iu)^(?:how|what|why|when|where|which|can|could|would|please explain|continue|explain|show me)\\b|[?？]$");

    static EditorDialogue project(List<Message> messages, IntFunction<Map<String, String>> source) {
        var original = List.copyOf(messages);
        int total = original.stream().mapToInt(message -> message.content().length()).sum();
        if (original.size() <= 12 && total <= HISTORY_CHARACTERS) return new EditorDialogue(original, 0, List.of());
        int omitted = original.size(), retained = 0;
        while (omitted >= 2 && original.size() - omitted < 6) {
            int pairSize = original.get(omitted - 2).content().length() + original.get(omitted - 1).content().length();
            if (retained + pairSize > RECENT_CHARACTERS) break;
            retained += pairSize;
            omitted -= 2;
        }
        var notes = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < omitted; i += 2) {
            String author = original.get(i).content().strip();
            String previous = i == 0 ? "" : original.get(i - 1).content();
            var selected = selectedOption(previous, author);
            var note = new LinkedHashMap<String, Object>();
            if (selected != null) note.put("selectedOption", selected);
            else {
                String statement = excerpt(author);
                if (!statement.isEmpty()) note.put("author", statement);
                if (AGREED.matcher(author).matches()) {
                    String confirmed = excerpt(previous);
                    if (!confirmed.isEmpty()) note.put("confirmedReply", confirmed);
                }
            }
            if (!note.isEmpty()) {
                note.put("exchange", i / 2 + 1);
                note.put("resourceId", source.apply(i).get("resourceId"));
                notes.add(Map.copyOf(note));
            }
        }
        return new EditorDialogue(original, omitted, List.copyOf(notes));
    }

    List<Message> recent() { return original.subList(omitted, original.size()); }

    // Only the numbered choices at the end of a clarification are author-selectable.
    private static String selectedOption(String answer, String choice) {
        if (answer.isEmpty()) return null;
        var lines = answer.stripTrailing().split("\\R");
        int next = 0, i = lines.length;
        String selected = null;
        while (i > 0) {
            var option = OPTION.matcher(lines[i - 1]);
            if (!option.matches()) break;
            int number = Integer.parseInt(option.group(1));
            if (next > 0 && number != next - 1) break;
            if (option.group(1).equals(choice) || option.group(2).strip().equals(choice))
                selected = option.group(2).strip();
            next = number;
            i--;
        }
        return next == 1 && lines.length - i >= 2 && i > 0 && lines[i - 1].stripTrailing().matches(".*[?？؟]")
                && selected != null && selected.length() <= NOTE_CHARACTERS ? selected : null;
    }

    // Extract intact short statements. Never turn a shortened code block or an identifier fragment into a business fact.
    private static String excerpt(String text) {
        var result = new StringJoiner(" ");
        boolean code = false;
        for (String line : text.split("\\R")) {
            if (line.stripLeading().startsWith("```")) { code = !code; continue; }
            if (code || line.stripLeading().matches("(?i)^(?:select|with)\\b.*")) continue;
            var parts = SENTENCES.matcher(line);
            while (parts.find()) {
                String part = parts.group().strip();
                boolean language = LANGUAGE.matcher(part).find();
                if (part.isBlank() || part.length() > NOTE_CHARACTERS || result.length() + part.length() > NOTE_CHARACTERS
                        || language && ONCE.matcher(part).find() || !language && QUESTION.matcher(part).find()) continue;
                result.add(part);
            }
        }
        return result.toString();
    }
}

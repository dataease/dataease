package io.dataease.ai.engine.conversation;

import io.dataease.ai.engine.runtime.AiToolProtocol.Message;

import java.text.BreakIterator;
import java.util.*;
import java.util.function.IntFunction;
import java.util.regex.Pattern;

/** Model-facing projection of completed editor questions and answers. Steps have a separate authorization and lifetime. */
record EditorDialogue(List<Message> original, int omitted, List<Map<String, Object>> notes) {
    private static final int HISTORY_CHARACTERS = 6000, RECENT_CHARACTERS = 3200;
    private static final int NOTE_CHARACTERS = 384;
    private static final Pattern OPTION = Pattern.compile("^\\s*(\\d{1,2})[.)、]\\s*(\\S.*)$");
    private static final Pattern AGREED = Pattern.compile("(?iu)^(?:yes|ok|okay|sure|agreed|correct|confirmed|exactly|是的?|对的?|對的?|没错|沒錯|同意|确认|確認)[.!。！\\s]*$");
    private static final Pattern LANGUAGE = Pattern.compile("(?iu)(?:回答|答复|答覆|回[复覆應应]|[中英法德日西韩韓]文|中文|[体體][字中]|\\b(?:reply|respond|answer|speak|language|english|chinese)\\b)");
    private static final Pattern ONCE = Pattern.compile("(?iu)(?:(?:仅|僅|只|就)?[这這]一?[轮輪次]|本[轮輪次]|(?:(?:just|only) )?(?:for )?this (?:time|reply|answer|response|turn|question|message|once|one)(?: only)?|just for now)");
    private static final Pattern CLAUSE_BREAK = Pattern.compile("(?iu)[，,]|\\s+(?:and|but)\\s+|[并並]且?");
    private static final Pattern CONTINUATION = Pattern.compile("(?iu)^(?:continue(?: please| example)?|tell me more|next(?: example)?|再举例|再舉例|请解释|請解釋|继续|繼續)(?:\\s*#?\\d+)?[。.!！]?$");
    private static final Pattern QUESTION = Pattern.compile("(?iu)^(?:how|what|why|when|where|which|can|could|would|please explain|continue|explain|show me)\\b|[?？]$");

    static EditorDialogue project(List<Message> messages, String question, IntFunction<String> resourceId) {
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
        boolean languageContext = false;
        // Include the first retained author's selection if the matching clarification was removed.
        // With one oversized last answer, that author may be asking the current question.
        for (int i = 0; i <= omitted; i += 2) {
            String author = i == original.size() ? question.strip() : original.get(i).content().strip();
            String previous = i == 0 ? "" : original.get(i - 1).content();
            var selected = selectedOption(previous, author);
            var note = new LinkedHashMap<String, Object>();
            if (selected != null) note.put("selectedOption", selected);
            else if (AGREED.matcher(author).matches()) {
                String confirmed = excerpt(previous, true);
                if (!confirmed.isEmpty()) note.put("confirmedReply", confirmed);
            } else if (i < omitted) {
                // Later questions establish whether an earlier language request still concerns this topic.
                languageContext |= LANGUAGE.matcher(author).find() && !ONCE.matcher(author).find();
                String statement = excerpt(author, languageContext);
                if (!statement.isEmpty()) note.put("author", statement);
            }
            if (!note.isEmpty()) {
                int sourceIndex = selected != null || note.containsKey("confirmedReply") ? i - 1 : i;
                note.put("resourceId", resourceId.apply(sourceIndex));
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

    private static String withoutOneTurnLanguage(String sentence) {
        if (!ONCE.matcher(sentence).find() || !LANGUAGE.matcher(sentence).find()) return sentence;
        String[] clauses = CLAUSE_BREAK.splitWithDelimiters(sentence, 0);
        var omit = new boolean[clauses.length];
        for (int i = 0; i < clauses.length; i += 2) {
            String clause = clauses[i].strip();
            if (!ONCE.matcher(clause).find()) continue;
            // "For this turn, reply in English" and "reply in English, just this time".
            if (ONCE.matcher(clause.replaceFirst("[.!。！]+$", "")).matches()) {
                int adjacent = i + 2 < clauses.length && LANGUAGE.matcher(clauses[i + 2]).find() ? i + 2 : i - 2;
                if (adjacent >= 0 && LANGUAGE.matcher(clauses[adjacent]).find()) omit[i] = omit[adjacent] = true;
            } else if (LANGUAGE.matcher(clause).find()) omit[i] = true;
        }
        var retained = new StringBuilder();
        for (int i = 0; i < clauses.length; i += 2) {
            if (omit[i]) continue;
            if (!retained.isEmpty()) retained.append(omit[i - 2] ? " " : clauses[i - 1]);
            retained.append(clauses[i]);
        }
        return retained.toString().strip();
    }

    // Extract intact short statements. Never turn a shortened code block or an identifier fragment into a business fact.
    private static String excerpt(String text, boolean keepQuestions) {
        var result = new StringJoiner(" ");
        var sentences = BreakIterator.getSentenceInstance(Locale.ROOT);
        boolean code = false;
        for (String line : text.split("\\R")) {
            if (line.stripLeading().startsWith("```")) { code = !code; continue; }
            if (code || line.stripLeading().matches("(?i)^(?:select|with)\\b.*")) continue;
            for (String clause : line.split("[；;]")) {
                sentences.setText(clause);
                int start = sentences.first();
                for (int end = sentences.next(); end != BreakIterator.DONE; start = end, end = sentences.next()) {
                    String part = withoutOneTurnLanguage(clause.substring(start, end).strip());
                    if (part.isBlank() || CONTINUATION.matcher(part).matches() || result.length() + part.length() > NOTE_CHARACTERS
                            || !keepQuestions && QUESTION.matcher(part).find()) continue;
                    result.add(part);
                }
            }
        }
        return result.toString();
    }
}

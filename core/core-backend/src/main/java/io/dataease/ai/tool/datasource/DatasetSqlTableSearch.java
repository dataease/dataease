package io.dataease.ai.tool.datasource;

import io.dataease.ai.engine.runtime.AiToolProtocol;
import io.dataease.ai.tool.AiTool;
import io.dataease.ai.support.AiErrors;

import java.util.*;
import java.util.stream.Stream;

/** Search physical names and labels. Enumeration stays on the server. */
public final class DatasetSqlTableSearch {
    private static final int LIMIT = 5;
    private enum Evidence { EXACT_NAME, CASE_INSENSITIVE_NAME, EXACT_COMMENT, IDENTIFIER_PART, NAME_SUBSTRING, COMMENT_SUBSTRING }
    private record Hit(String table, Evidence evidence, int unmatchedNameLength) {}
    private static final Comparator<Hit> RANK = Comparator.comparing(Hit::evidence)
            .thenComparingInt(Hit::unmatchedNameLength).thenComparing(Hit::table);
    private record Candidate(int number, String table, String comment, List<String> matchedTerms) {}

    private static final class Term {
        private final String text;
        private final String folded;
        private final NavigableSet<Hit> best = new TreeSet<>(RANK);

        private Term(String text) {
            this.text = text;
            this.folded = text.toLowerCase(Locale.ROOT);
        }

        private boolean consider(String table, String name, String label) {
            Evidence evidence;
            if (table.equals(text)) evidence = Evidence.EXACT_NAME;
            else if (name.equals(folded)) evidence = Evidence.CASE_INSENSITIVE_NAME;
            else if (label.equals(folded)) evidence = Evidence.EXACT_COMMENT;
            else if (name.contains(folded)) {
                evidence = hasIdentifierPart(name, folded) ? Evidence.IDENTIFIER_PART : Evidence.NAME_SUBSTRING;
            }
            else if (label.contains(folded)) evidence = Evidence.COMMENT_SUBSTRING;
            else return false;

            int unmatched = evidence == Evidence.IDENTIFIER_PART || evidence == Evidence.NAME_SUBSTRING
                    ? name.length() - folded.length() : 0;
            best.add(new Hit(table, evidence, unmatched));
            if (best.size() > LIMIT) best.pollLast();
            return true;
        }
    }

    private static boolean hasIdentifierPart(String name, String term) {
        for (int start = name.indexOf(term); start >= 0; start = name.indexOf(term, start + 1)) {
            int end = start + term.length();
            if ((start == 0 || !Character.isLetterOrDigit(name.codePointBefore(start)))
                    && (end == name.length() || !Character.isLetterOrDigit(name.codePointAt(end)))) return true;
        }
        return false;
    }

    public static AiTool<AiDatasourceTools.Scope> tool(AiDatasourceTools metadata) {
        var definition = AiTool.readOnly("tools/dataset-sql.md", "search_physical_tables", "terms");
        return new AiTool<>(definition, (scope, args, active, deadline) -> {
            var terms = Arrays.stream(args.get("terms").split("[,，]")).map(String::strip)
                    .filter(term -> !term.isEmpty()).distinct().map(Term::new).toList();
            if (terms.isEmpty() || terms.size() > 12 || terms.stream().mapToInt(term -> term.text.length()).sum() > 512)
                throw AiErrors.error("invalid");
            var tables = metadata.editorTableLabels(scope);
            int matched = 0;
            for (var entry : tables.entrySet()) {
                active.run();
                String table = entry.getKey();
                String name = table.toLowerCase(Locale.ROOT);
                String label = entry.getValue().toLowerCase(Locale.ROOT);
                boolean found = false;
                for (var term : terms) {
                    if (term.consider(table, name, label)) found = true;
                }
                if (found) matched++;
            }
            // Reserve a representative for each clue before filling the remaining places.
            var leaders = terms.stream().filter(term -> !term.best.isEmpty()).map(term -> term.best.first()).sorted(RANK);
            var others = terms.stream().flatMap(term -> term.best.stream()).sorted(RANK);
            var selected = Stream.concat(leaders, others).map(Hit::table).distinct().limit(LIMIT).toList();
            var candidates = new ArrayList<Candidate>();
            for (String table : selected) {
                if (table.length() > 256) throw AiErrors.error("output_limit");
                String label = tables.get(table);
                String name = table.toLowerCase(Locale.ROOT);
                String foldedLabel = label.toLowerCase(Locale.ROOT);
                var found = terms.stream().filter(term -> name.contains(term.folded) || foldedLabel.contains(term.folded))
                        .map(term -> term.text).toList();
                if (label.codePointCount(0, label.length()) > 256)
                    label = label.substring(0, label.offsetByCodePoints(0, 255)) + "…";
                candidates.add(new Candidate(candidates.size() + 1, table, label, found));
            }
            metadata.authorize(scope);
            return AiToolProtocol.result(Map.of("resourceId", scope.resourceId(), "candidates", candidates,
                    "more", matched > LIMIT, "status", tables.isEmpty() ? "EMPTY_CATALOG" : matched == 0 ? "NO_MATCH" : "MATCHED"));
        });
    }

    private DatasetSqlTableSearch() {}
}

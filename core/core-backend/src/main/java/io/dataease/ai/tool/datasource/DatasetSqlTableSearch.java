package io.dataease.ai.tool.datasource;

import io.dataease.ai.engine.runtime.AiToolProtocol;
import io.dataease.ai.tool.AiTool;
import io.dataease.ai.support.AiErrors;

import java.util.*;

/** Search physical names and labels. Enumeration stays on the server. */
public final class DatasetSqlTableSearch {
    private static final int LIMIT = 5;
    private record Match(String table, String comment, List<String> terms, boolean exact) {}
    private static final Comparator<Match> RANK = Comparator.comparing(Match::exact).reversed()
            .thenComparing(Comparator.comparingInt((Match m) -> m.terms().size()).reversed()).thenComparing(Match::table);
    private record Candidate(int number, String table, String comment, List<String> matchedTerms) {}

    public static AiTool<AiDatasourceTools.Scope> tool(AiDatasourceTools metadata) {
        var definition = new AiToolProtocol.Definition("search_physical_tables",
                "Find physical tables by a missing business term, name or comment in the current datasource. No fields or rows are read. Offer numbered candidates for confirmation.",
                Map.of("terms", "Up to 12 comma-separated business terms or physical names, 512 characters total"), AiToolProtocol.RetrySafety.READ_ONLY);
        return new AiTool<>(definition, (scope, args, active, deadline) -> {
            var terms = Arrays.stream(args.get("terms").split("[,，]")).map(String::strip)
                    .filter(term -> !term.isEmpty()).distinct().toList();
            if (terms.isEmpty() || terms.size() > 12 || terms.stream().mapToInt(String::length).sum() > 512)
                throw AiErrors.error("invalid");
            var tables = metadata.tables(scope);
            var best = new PriorityQueue<Match>(LIMIT + 1, RANK.reversed());
            int matched = 0;
            for (var table : tables) {
                active.run();
                String name = table.name().toLowerCase(Locale.ROOT);
                String comment = Objects.toString(table.comment(), "").toLowerCase(Locale.ROOT);
                var found = terms.stream().filter(term -> name.contains(term.toLowerCase(Locale.ROOT))
                        || comment.contains(term.toLowerCase(Locale.ROOT))).toList();
                if (found.isEmpty()) continue;
                matched++;
                best.add(new Match(table.name(), table.comment(), found, terms.contains(table.name())));
                if (best.size() > LIMIT) best.remove();
            }
            var candidates = new ArrayList<Candidate>();
            for (var match : best.stream().sorted(RANK).toList()) {
                if (match.table().length() > 256) throw AiErrors.error("output_limit");
                String label = match.comment();
                if (label.codePointCount(0, label.length()) > 256)
                    label = label.substring(0, label.offsetByCodePoints(0, 255)) + "…";
                candidates.add(new Candidate(candidates.size() + 1, match.table(), label, match.terms()));
            }
            metadata.authorize(scope);
            return AiToolProtocol.result(Map.of("resourceId", scope.resourceId(), "candidates", candidates,
                    "more", matched > LIMIT, "status", tables.isEmpty() ? "EMPTY_CATALOG" : matched == 0 ? "NO_MATCH" : "MATCHED"));
        });
    }

    private DatasetSqlTableSearch() {}
}

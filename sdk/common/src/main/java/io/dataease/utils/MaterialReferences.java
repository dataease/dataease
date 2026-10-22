package io.dataease.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashSet;
import java.util.Set;

/** Stable references stored in existing image/background slots; never a filesystem path. */
public final class MaterialReferences {
    public static final String PREFIX = "material://";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MaterialReferences() { }

    public static Set<Long> collect(String... documents) {
        Set<Long> result = new LinkedHashSet<>();
        for (String document : documents) {
            if (document == null || document.isBlank()) continue;
            try {
                visit(MAPPER.readTree(document), result, 0);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid material reference document", e);
            }
        }
        return result;
    }

    private static void visit(JsonNode node, Set<Long> result, int depth) {
        if (node == null) return;
        if (depth > 100 || result.size() > 10000) throw new IllegalArgumentException("Material document too complex");
        if (node.isTextual() && node.textValue().startsWith("material-file://")) {
            throw new IllegalArgumentException("Material package must be imported before saving");
        }
        if (node.isTextual() && node.textValue().startsWith(PREFIX)) {
            String value = node.textValue().substring(PREFIX.length());
            if (!value.matches("[1-9][0-9]{0,18}")) throw new IllegalArgumentException("Invalid material ID");
            result.add(Long.parseLong(value));
        } else if (node.isContainerNode()) {
            node.forEach(child -> visit(child, result, depth + 1));
        }
    }
}

package io.dataease.utils;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import java.util.*;

/** Traverses only existing image, background and manual picture-group slots. */
public final class MaterialDocuments {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private MaterialDocuments() { }

    public static Set<String> images(String style, String components) {
        Set<String> result = new LinkedHashSet<>();
        try {
            if (style != null && !style.isBlank()) {
                JsonNode canvas = MAPPER.readTree(style);
                image(canvas.path("background"), result);
                image(canvas.path("mobileSetting").path("background"), result);
                image(canvas.path("component").path("chartCommonStyle").path("outerImage"), result);
            }
            if (components != null && !components.isBlank()) components(MAPPER.readTree(components), result, 0);
            return result;
        } catch (Exception e) { throw new IllegalArgumentException("Invalid image document", e); }
    }

    private static void components(JsonNode node, Set<String> result, int depth) {
        if (depth > 100 || result.size() > 200) throw new IllegalArgumentException("Too many material files");
        if (!node.isArray()) return;
        for (JsonNode component : node) {
            image(component.path("commonBackground").path("outerImage"), result);
            image(component.path("mCommonBackground").path("outerImage"), result);
            if ("Picture".equals(component.path("component").asText())) image(component.path("propValue").path("url"), result);
            if ("UserView".equals(component.path("component").asText()) && "picture-group".equals(component.path("innerType").asText())) {
                for (JsonNode item : component.path("propValue").path("urlList")) image(item.path("url"), result);
            }
            if ("Group".equals(component.path("component").asText())) components(component.path("propValue"), result, depth + 1);
            if ("DeTabs".equals(component.path("component").asText())) {
                for (JsonNode tab : component.path("propValue")) components(tab.path("componentData"), result, depth + 1);
            }
        }
    }

    private static void image(JsonNode node, Set<String> result) {
        if (node.isTextual() && !node.asText().isBlank()) result.add(node.asText());
    }

    public static String remap(String document, Map<String, String> mapping) {
        if (document == null || document.isBlank() || mapping.isEmpty()) return document;
        try { return MAPPER.writeValueAsString(remap(MAPPER.readTree(document), mapping, 0)); }
        catch (Exception e) { throw new IllegalArgumentException("Invalid material document", e); }
    }

    private static JsonNode remap(JsonNode node, Map<String, String> mapping, int depth) {
        if (depth > 100) throw new IllegalArgumentException("Material document too complex");
        if (node.isTextual() && mapping.containsKey(node.asText())) return TextNode.valueOf(mapping.get(node.asText()));
        if (node instanceof ObjectNode object) {
            List<String> names = new ArrayList<>();
            object.fieldNames().forEachRemaining(names::add);
            names.forEach(name -> object.set(name, remap(object.get(name), mapping, depth + 1)));
        } else if (node instanceof ArrayNode array) {
            for (int i = 0; i < array.size(); i++) array.set(i, remap(array.get(i), mapping, depth + 1));
        }
        return node;
    }
}

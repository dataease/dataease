package io.dataease.ai.support;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AiPromptResources {
    private static final Map<String, Map<String, String>> SECTIONS = new ConcurrentHashMap<>();

    private AiPromptResources() {}

    public static String read(String name) {
        var resource = new ClassPathResource("ai/prompts/" + name);
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8).strip();
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot read AI prompt: " + name, failure);
        }
    }

    /** Read a named second-level Markdown section without sending its heading to the model. */
    public static String section(String name, String key) {
        var text = SECTIONS.computeIfAbsent(name, AiPromptResources::loadSections).get(key);
        if (text == null) throw new IllegalStateException("Missing AI prompt section: " + name + "#" + key);
        return text;
    }

    private static Map<String, String> loadSections(String name) {
        var sections = new HashMap<String, String>();
        for (var block : read(name).split("(?m)(?=^## )")) {
            if (!block.startsWith("## ")) continue;
            int end = block.indexOf('\n');
            if (end < 0) throw new IllegalStateException("Empty AI prompt section: " + name + " " + block);
            var key = block.substring(3, end).strip();
            var text = block.substring(end + 1).strip();
            if (key.isEmpty() || text.isEmpty() || sections.putIfAbsent(key, text) != null)
                throw new IllegalStateException("Invalid or duplicate AI prompt section: " + name + "#" + key);
        }
        return Map.copyOf(sections);
    }
}

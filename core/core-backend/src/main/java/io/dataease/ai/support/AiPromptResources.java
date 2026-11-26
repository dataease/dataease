package io.dataease.ai.support;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class AiPromptResources {
    private AiPromptResources() {}

    public static String read(String name) {
        var resource = new ClassPathResource("ai/prompts/" + name);
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8).strip();
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot read AI prompt: " + name, failure);
        }
    }
}

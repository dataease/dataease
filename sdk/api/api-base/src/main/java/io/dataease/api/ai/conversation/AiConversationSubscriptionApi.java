package io.dataease.api.ai.conversation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

public interface AiConversationSubscriptionApi {
    record Bind(@NotBlank @Size(max = 64) String subscription, @NotBlank @Size(max = 100) String editorId, @Min(0) long after) {}

    @PostMapping("/ai/conversations/{id}/executions/{execution}/subscriptions")
    void bind(@PathVariable String id, @PathVariable String execution, @Valid @RequestBody Bind request);
}

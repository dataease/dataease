package io.dataease.api.ai.conversation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 临时开发验收契约：客户端不提供身份、模型配置或历史消息。 */
public final class AiConversationDtos {
    private AiConversationDtos() { }
    public record Resource(String id, String name) { }
    public record Open(@NotBlank @Pattern(regexp = "sql") String scene,
                       @NotBlank @Pattern(regexp = "[0-9]{1,20}") String resourceId,
                       @NotBlank @Size(max = 100) String editorId,
                       @Size(max = 30000) String sql) { }
    public record Session(String sessionId, String contextVersion, String scene, String resourceId,
                          String resourceName, String editorId, long expiresAt) { }
    public record Turn(@NotBlank @Size(max = 100) String contextVersion,
                       @NotBlank @Size(max = 30000) String text) { }
    public record Execution(String sessionId, String executionId, String contextVersion, String status, long deadline) { }
    public record Event(long sequence, String type, String text, String error) { }
    public record Events(String sessionId, String executionId, String contextVersion, String status, long cursor, java.util.List<Event> events) { }
    public record Answer(String sessionId, String executionId, String contextVersion, String status, String text) { }
}

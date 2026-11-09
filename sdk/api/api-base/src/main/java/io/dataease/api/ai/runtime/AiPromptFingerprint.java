package io.dataease.api.ai.runtime;

import org.springframework.ai.chat.prompt.Prompt;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.nio.ByteBuffer;
import java.util.HexFormat;

/** 输入绑定采用角色、消息顺序及完整文本；格式为带长度字段，避免拼接歧义。 */
public final class AiPromptFingerprint {
    private AiPromptFingerprint() { }
    public static String of(Prompt prompt) {
        try {
            MessageDigest hash = MessageDigest.getInstance("SHA-256");
            for (var message : prompt.getInstructions()) {
                add(hash, message.getMessageType().name()); add(hash, message.getText());
            }
            if (prompt.getOptions() instanceof org.springframework.ai.model.tool.StructuredOutputChatOptions structured) {
                // Spring AI 1.1.8 OpenAiChatOptions 未设置 responseFormat 时此 getter 会抛 NPE。
                String schema = null;
                try { schema = structured.getOutputSchema(); } catch (NullPointerException absentFormat) { }
                if (schema != null) { add(hash, "schema"); add(hash, schema); }
            }
            if (prompt.getOptions() != null && prompt.getOptions().getStopSequences() != null) {
                for (String stop : prompt.getOptions().getStopSequences()) { add(hash, "stop"); add(hash, stop); }
            }
            return HexFormat.of().formatHex(hash.digest());
        } catch (Exception e) { throw new IllegalArgumentException("Unsupported AI prompt"); }
    }
    private static void add(MessageDigest hash, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        hash.update(ByteBuffer.allocate(4).putInt(bytes.length).array()); hash.update(bytes);
    }
}

package io.dataease.api.ai.runtime;

import org.springframework.ai.chat.model.ChatModel;

public interface AiModelProvider {
    /** 返回绑定当前请求/受控任务的受控模型；禁止缓存到单例或跨身份复用。 */
    ChatModel current(AiExecutionContext context);
}

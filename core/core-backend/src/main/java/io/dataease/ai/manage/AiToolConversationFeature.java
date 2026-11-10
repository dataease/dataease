package io.dataease.ai.manage;

import io.dataease.ai.support.AiToolProtocol;
import io.dataease.api.ai.runtime.AiExecutionContext;
import io.dataease.api.ai.runtime.AiFeature;

/** Explicit core-only typed tool opt-in; native synchronous SDK policy stays unchanged. */
public interface AiToolConversationFeature extends AiFeature {
    void authorizeToolInput(AiExecutionContext context, AiToolProtocol.Input input);
}

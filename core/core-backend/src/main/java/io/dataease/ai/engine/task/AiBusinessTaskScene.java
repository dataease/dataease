package io.dataease.ai.engine.task;

import io.dataease.ai.engine.conversation.AiConversationScene;
import io.dataease.ai.support.AiErrors;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.function.Supplier;

/** A durable business operation with model calls and short, fenced writes. */
public interface AiBusinessTaskScene extends AiConversationScene {
    @Override
    default boolean backgroundOnly() { return true; }

    @Override
    default List<Resource> resources() { return List.of(); }

    @Override
    default Context resolve(String resourceId, String input) { throw AiErrors.error("unsupported"); }

    void authorizeResource(String resourceId);

    ResourceClaim resourceClaim(String resourceId);

    @Override
    default List<ResourceClaim> resourceClaims(Context context) {
        return List.of(resourceClaim(context.resourceId()));
    }

    void execute(Context context, Execution execution);

    interface Execution {
        void check();
        String call(Prompt prompt);
        /** Persist business changes and their cumulative summary in the same transaction. */
        <T> T write(Supplier<T> operation);
    }
}

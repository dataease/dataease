package io.dataease.ai.tool;

import io.dataease.ai.engine.runtime.AiToolProtocol;
import io.dataease.ai.support.AiErrors;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** A reusable tool with a server-owned, typed scope; registration does not grant model access. */
public record AiTool<C>(AiToolProtocol.Definition definition, Action<C> action) {
    @FunctionalInterface
    public interface Action<C> {
        String execute(C scope, Map<String, String> arguments, Runnable active, long deadline);
    }

    public AiTool {
        Objects.requireNonNull(definition);
        Objects.requireNonNull(action);
    }

    public String execute(C scope, Map<String, String> arguments, Runnable active, long deadline) {
        if (!arguments.keySet().equals(definition.parameters().keySet())) throw AiErrors.error("unsupported");
        if (arguments.values().stream().anyMatch(value -> value == null || value.isBlank())) throw AiErrors.error("invalid");
        Runnable check = () -> {
            active.run();
            if (Thread.currentThread().isInterrupted()) throw AiErrors.error("cancelled");
            if (System.currentTimeMillis() >= deadline) throw AiErrors.error("timeout");
        };
        check.run();
        String result = action.execute(scope, arguments, check, deadline);
        check.run();
        return result;
    }

    /** Dispatch only from the tools explicitly selected by the caller's scene. */
    public static <C> String execute(List<AiTool<C>> allowed, C scope, String name,
                                    Map<String, String> arguments, Runnable active, long deadline) {
        return allowed.stream().filter(tool -> tool.definition().name().equals(name)).findFirst()
                .orElseThrow(() -> AiErrors.error("unsupported")).execute(scope, arguments, active, deadline);
    }
}

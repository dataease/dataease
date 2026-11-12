package io.dataease.ai.manage;

import java.util.Objects;
import java.util.function.Supplier;

/** Read-only projection for trusted server-owned work. No request/header may install this context. */
public final class AiOwnerContext {
    private record Owner(Long actor, Long organization) { }
    private static final ThreadLocal<Owner> current = new ThreadLocal<>();
    private AiOwnerContext() { }
    public static boolean active(Long actor) { var owner = current.get(); return owner != null && Objects.equals(owner.actor(), actor); }
    public static Long organization(Long actor, Long fallback) { return active(actor) ? current.get().organization() : fallback; }
    // Package-private: only the core identity scope installs an already verified persistent owner.
    static <T> T call(Long actor, Long organization, Supplier<T> operation) {
        var previous = current.get(); current.set(new Owner(Objects.requireNonNull(actor), organization));
        try { return operation.get(); }
        finally { if (previous == null) current.remove(); else current.set(previous); }
    }
}

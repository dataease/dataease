package io.dataease.api.material;

import java.util.Set;

/** Internal integration contract, deliberately not exposed as an HTTP API. */
public interface MaterialLifecycle {
    void lock();
    void reconcile(String ownerType, String ownerId, Set<Long> materialIds);
    void release(String ownerType, String ownerId);
    void inherit(Set<Long> materialIds, Runnable action);
}

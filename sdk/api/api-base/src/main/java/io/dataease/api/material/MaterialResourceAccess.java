package io.dataease.api.material;

import io.dataease.api.material.dto.MaterialContext;
import java.util.Set;

/** Core resource access adapter. Implementations must verify the current business identity. */
public interface MaterialResourceAccess {
    Set<Long> authorize(MaterialContext context, boolean edit);
    Set<String> images(MaterialContext context);
    Set<Long> authorizeExport(MaterialContext context);
    boolean isReferenced(long materialId);
}

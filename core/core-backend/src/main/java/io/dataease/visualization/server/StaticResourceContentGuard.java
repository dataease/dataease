package io.dataease.visualization.server;

import io.dataease.utils.ImageContentGuard;

/** Compatibility facade for existing static-resource callers. */
public final class StaticResourceContentGuard {
    public static final int MAX_BYTES = ImageContentGuard.MAX_BYTES;

    private StaticResourceContentGuard() { }

    public static boolean allowedExtension(String filename) {
        return ImageContentGuard.allowedExtension(filename);
    }

    public static void validate(String filename, byte[] content) throws Exception {
        ImageContentGuard.validate(filename, content);
    }
}

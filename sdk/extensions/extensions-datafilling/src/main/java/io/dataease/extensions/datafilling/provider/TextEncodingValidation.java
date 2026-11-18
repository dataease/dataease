package io.dataease.extensions.datafilling.provider;

/** A submission-scoped validation session; it may cache actual target metadata. */
public interface TextEncodingValidation extends AutoCloseable {
    /**
     * Checks a non-null string exactly as it will be written to the mapped column.
     * Returns the first known incompatibility, or null when none is identified.
     * Null also covers unknown encodings: it is not a claim of full encoding support.
     * Unrelated database failures must propagate, not be reported as encoding failures.
     */
    TextEncodingFailure validate(String column, String value) throws Exception;

    @Override
    default void close() throws Exception {
    }
}

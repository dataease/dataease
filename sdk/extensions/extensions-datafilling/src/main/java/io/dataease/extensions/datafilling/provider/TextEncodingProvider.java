package io.dataease.extensions.datafilling.provider;

import java.sql.Connection;

/**
 * Optional capability for checking the actual target encoding before a submission writes any rows.
 * Existing ExtDDLProvider implementations need not implement this interface.
 */
public interface TextEncodingProvider {
    /**
     * The caller owns the connection and closes it after the validation session.
     * Implementations must not modify target data, column definitions or connection encoding.
     */
    TextEncodingValidation prepareTextEncodingValidation(Connection connection, String table) throws Exception;
}

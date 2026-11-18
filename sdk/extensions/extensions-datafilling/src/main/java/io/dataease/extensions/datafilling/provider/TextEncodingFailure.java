package io.dataease.extensions.datafilling.provider;

/** Actual encoding limitation and a localized remediation key; never contains submitted content. */
public record TextEncodingFailure(String encoding, String suggestionKey) {
}

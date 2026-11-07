package io.dataease.api.sensitive;

/** Persisted credential selectors. No client supplied JSON path is accepted. */
public enum SecretType {
    DATASOURCE_PASSWORD("DATASOURCE", "password"),
    DATASOURCE_SSH_PASSWORD("DATASOURCE", "sshPassword"),
    DATASOURCE_SSH_KEY_PASSWORD("DATASOURCE", "sshKeyPassword"),
    DATASOURCE_REMOTE_PASSWORD("DATASOURCE", "passwd"),
    ENGINE_PASSWORD("ENGINE", "password"),
    SYNC_PASSWORD("SYNC", "password");

    private final String resourceKind;
    private final String field;

    SecretType(String resourceKind, String field) {
        this.resourceKind = resourceKind;
        this.field = field;
    }
    public String resourceKind() { return resourceKind; }
    public String field() { return field; }
}

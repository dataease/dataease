package io.dataease.api.ai.config;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

/** 管理 HTTP 契约不包含 Spring AI 对象、密文或凭据摘要。 */
public final class AiConfigDtos {
    private AiConfigDtos() { }
    @Getter @Setter
    public static class Candidate {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        private Long expectedVersion;
        private Long expectedConfigRevision;
        private String draftHandle;
        @Size(max = 128) private String name;
        @NotBlank @Pattern(regexp = "OPENAI_COMPATIBLE") private String provider;
        @NotBlank @Size(max = 2048) private String baseUrl;
        @Size(max = 4096) private String apiKey;
        @Size(max = 256) private String modelIdentifier;
        @Pattern(regexp = "UNKNOWN|PUBLIC|PRIVATE|GATEWAY|UNVERIFIED")
        private String processingLocation = "UNKNOWN";
        @Size(max = 512) private String processingNote;
        private String testProof;
    }
    public record Draft(@JsonSerialize(using = ToStringSerializer.class) Long id, String draftHandle) { }
    public record Model(@JsonSerialize(using = ToStringSerializer.class) Long id, String name,
            String provider, String baseUrl, String modelIdentifier, boolean keyConfigured,
            boolean enabled, boolean validated, Long configRevision, Long rowVersion,
            String processingLocation, String processingNote, String outputTokenParameter, String lastTestStatus,
            Long lastTestTime, String lastTestErrorCode, long organizationReferences, boolean systemDefault, List<String> organizationNames, long historicalOrganizationReferences) { }
    public record TestResult(boolean basic, boolean structured, String errorCode, String testProof) { }
    public record Recovery(@Min(0) @Max(5) int stepRetries, @Min(0) @Max(5) int attemptRetries,
                           @Min(100) @Max(10000) int initialDelayMillis) { }
    public record TaskScheduling(@Min(1) @Max(20) int maxRunning, @Min(1) @Max(10000) int maxQueued,
            @Min(1) @Max(1000) int maxQueuedPerUser, @Min(1) @Max(86400) int maxWaitSeconds) { }
    public record Settings(boolean enabled, @JsonSerialize(using = ToStringSerializer.class) Long defaultModelId,
            boolean allowSamples, int requestTimeoutSeconds, int maxInputChars, int maxOutputTokens,
            int interactiveConcurrency, int backgroundConcurrency, long revision, long rowVersion,
            long persistedRevision, long appliedRevision, String runtimeState, boolean organizationPolicyAvailable,
            TaskScheduling taskScheduling, Recovery recovery) {
        public Settings(boolean enabled, Long defaultModelId, boolean allowSamples, int requestTimeoutSeconds, int maxInputChars,
                int maxOutputTokens, int interactiveConcurrency, int backgroundConcurrency, long revision, long rowVersion,
                long persistedRevision, long appliedRevision, String runtimeState, boolean organizationPolicyAvailable) {
            this(enabled, defaultModelId, allowSamples, requestTimeoutSeconds, maxInputChars, maxOutputTokens, interactiveConcurrency,
                    backgroundConcurrency, revision, rowVersion, persistedRevision, appliedRevision, runtimeState, organizationPolicyAvailable,
                    new TaskScheduling(2, 100, 10, 300), new Recovery(2, 3, 1000));
        }
        public Settings(boolean enabled, Long defaultModelId, boolean allowSamples, int requestTimeoutSeconds, int maxInputChars,
                int maxOutputTokens, int interactiveConcurrency, int backgroundConcurrency, long revision, long rowVersion,
                long persistedRevision, long appliedRevision, String runtimeState, boolean organizationPolicyAvailable, TaskScheduling taskScheduling) {
            this(enabled, defaultModelId, allowSamples, requestTimeoutSeconds, maxInputChars, maxOutputTokens, interactiveConcurrency,
                    backgroundConcurrency, revision, rowVersion, persistedRevision, appliedRevision, runtimeState, organizationPolicyAvailable,
                    taskScheduling, new Recovery(2, 3, 1000));
        }
    }
    public record SettingsChange(@NotNull Long expectedVersion, @NotNull Boolean enabled,
            @JsonSerialize(using = ToStringSerializer.class) Long defaultModelId, @NotNull Boolean allowSamples,
            @Min(5) @Max(300) int requestTimeoutSeconds, @Min(1000) @Max(200000) int maxInputChars,
            @Min(128) @Max(32768) int maxOutputTokens, @Min(1) @Max(20) int interactiveConcurrency,
            @Min(1) @Max(20) int backgroundConcurrency, String impactToken, @Valid TaskScheduling taskScheduling, @Valid Recovery recovery) {
        public SettingsChange(Long expectedVersion, Boolean enabled, Long defaultModelId, Boolean allowSamples, int requestTimeoutSeconds,
                int maxInputChars, int maxOutputTokens, int interactiveConcurrency, int backgroundConcurrency, String impactToken) {
            this(expectedVersion, enabled, defaultModelId, allowSamples, requestTimeoutSeconds, maxInputChars, maxOutputTokens,
                    interactiveConcurrency, backgroundConcurrency, impactToken, null, null);
        }
        public SettingsChange(Long expectedVersion, Boolean enabled, Long defaultModelId, Boolean allowSamples, int requestTimeoutSeconds,
                int maxInputChars, int maxOutputTokens, int interactiveConcurrency, int backgroundConcurrency, String impactToken, TaskScheduling taskScheduling) {
            this(expectedVersion, enabled, defaultModelId, allowSamples, requestTimeoutSeconds, maxInputChars, maxOutputTokens,
                    interactiveConcurrency, backgroundConcurrency, impactToken, taskScheduling, null);
        }
    }
    public record ModelAction(@NotNull Long expectedVersion, Boolean enabled, String impactToken) { }
    public record Impact(String token, boolean systemDefault, long organizationCount, List<String> organizations) { }
    public record Org(@JsonSerialize(using = ToStringSerializer.class) Long id, String name, String parentName,
            String mode, @JsonSerialize(using = ToStringSerializer.class) Long modelId,
            Long rowVersion, String effectiveModel, String unavailableReason) { }
    public record ExpectedOrg(@JsonSerialize(using = ToStringSerializer.class) Long id, Long expectedVersion) { }
    public record OrgBatch(@NotEmpty @Size(max = 2000) List<ExpectedOrg> organizations,
            @NotBlank @Pattern(regexp = "INHERIT|SPECIFIED|DISABLED") String mode,
            @JsonSerialize(using = ToStringSerializer.class) Long modelId) { }
    public record Page<T>(List<T> items, long total) { }
    public record OrgPage(List<Org> items, long total, boolean available) { }
}

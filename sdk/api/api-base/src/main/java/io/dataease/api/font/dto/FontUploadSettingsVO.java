package io.dataease.api.font.dto;

public record FontUploadSettingsVO(long maxUploadMb, long maxStorageMb, long usedBytes,
                                   long maxUploadAllowedMb) {
}

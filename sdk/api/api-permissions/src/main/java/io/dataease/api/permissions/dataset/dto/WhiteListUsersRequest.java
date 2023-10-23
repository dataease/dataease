package io.dataease.api.permissions.dataset.dto;


import lombok.Data;

@Data
public class WhiteListUsersRequest {
    private Long authTargetId;
    private String authTargetType;
    private Long datasetId;
}

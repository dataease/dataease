package io.dataease.controller.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkspaceRequest {
    private String organizationId;
    private String name;
}

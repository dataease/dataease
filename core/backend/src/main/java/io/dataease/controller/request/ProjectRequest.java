package io.dataease.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProjectRequest {
    private String workspaceId;
    private String name;
    private List<OrderRequest> orders;
}

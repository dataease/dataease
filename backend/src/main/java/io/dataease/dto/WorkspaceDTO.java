package io.dataease.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkspaceDTO {

    private String id;
    private String name;
    private String organizationId;
    private String description;
    private String organizationName;

}

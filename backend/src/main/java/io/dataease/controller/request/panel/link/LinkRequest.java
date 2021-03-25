package io.dataease.controller.request.panel.link;

import lombok.Data;

@Data
public class LinkRequest {

    private String resourceId;

    private String password;

    private Boolean enablePwd;

    private String uri;
}

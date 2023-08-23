package io.dataease.controller.request.panel.link;

import lombok.Data;

@Data
public class ValidateRequest {

    private String resourceId;

    private Long time;

    private String salt;


}

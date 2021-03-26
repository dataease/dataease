package io.dataease.dto.panel.link;

import lombok.Data;

@Data
public class GenerateDto {

    private boolean valid;

    private boolean enablePwd;

    private String uri;

    private String pwd;
}

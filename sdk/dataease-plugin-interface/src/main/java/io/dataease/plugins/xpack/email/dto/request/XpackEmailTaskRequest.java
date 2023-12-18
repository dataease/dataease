package io.dataease.plugins.xpack.email.dto.request;

import io.dataease.plugins.common.annotation.PluginResultMap;
import lombok.Data;

@PluginResultMap
@Data
public class XpackEmailTaskRequest extends XpackTaskCreateRequest {

    private Long id;

    private String title;

    private String panelId;

    private Integer panelFormat = 0;

    private String recipients;

    private String pixel;

    private Long taskId;

    private byte[] content;

    private String viewIds;

    private String recisetting;

    private String reciUsers;

    private String conditions;

    private String viewDataRange = "view";

    private String groups;

    private Integer extWaitTime = 0;

    private String roleList;

    private String orgList;
}

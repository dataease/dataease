package io.dataease.controller.sys.response;

import io.dataease.base.domain.SysMsg;
import lombok.Data;

@Data
public class MsgGridDto extends SysMsg {

    private String router;
    private String callback;
}

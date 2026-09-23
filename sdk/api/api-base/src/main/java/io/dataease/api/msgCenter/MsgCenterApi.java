package io.dataease.api.msgCenter;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name = "消息中心")
public interface MsgCenterApi {

    @PostMapping("/count")
    long count();
}

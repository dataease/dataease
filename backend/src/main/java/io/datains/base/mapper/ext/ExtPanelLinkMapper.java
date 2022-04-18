package io.datains.base.mapper.ext;

import org.apache.ibatis.annotations.Param;

import io.datains.controller.request.panel.link.OverTimeRequest;

public interface ExtPanelLinkMapper {

    void updateOverTime(@Param("request") OverTimeRequest request);
    
}

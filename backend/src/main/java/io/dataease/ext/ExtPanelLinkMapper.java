package io.dataease.ext;

import org.apache.ibatis.annotations.Param;

import io.dataease.controller.request.panel.link.OverTimeRequest;

public interface ExtPanelLinkMapper {

    void updateOverTime(@Param("request") OverTimeRequest request);
    
}

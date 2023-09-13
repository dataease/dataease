package io.dataease.ext;

import io.dataease.controller.request.panel.AppLogGridRequest;
import io.dataease.dto.appTemplateMarket.AppLogGridDTO;

import java.util.List;

public interface ExtAppLogMapper {
    List<AppLogGridDTO> query(AppLogGridRequest request);
}

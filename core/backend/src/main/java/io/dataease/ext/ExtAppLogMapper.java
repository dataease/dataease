package io.dataease.ext;

import io.dataease.dto.appTemplateMarket.AppLogGridDTO;
import io.dataease.service.panel.applog.AppLogQueryParam;
import java.util.List;

public interface ExtAppLogMapper {
    List<AppLogGridDTO> query(AppLogQueryParam example);
}

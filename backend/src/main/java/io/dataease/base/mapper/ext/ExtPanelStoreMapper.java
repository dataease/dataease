package io.dataease.base.mapper.ext;

import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.dto.panel.PanelStoreDto;

import java.util.List;

public interface ExtPanelStoreMapper {

    List<PanelStoreDto> query(GridExample example);
}

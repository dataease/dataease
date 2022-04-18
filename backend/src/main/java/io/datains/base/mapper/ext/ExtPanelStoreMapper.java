package io.datains.base.mapper.ext;

import io.datains.base.mapper.ext.query.GridExample;
import io.datains.dto.panel.PanelStoreDto;

import java.util.List;

public interface ExtPanelStoreMapper {

    List<PanelStoreDto> query(GridExample example);
}

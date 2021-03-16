package io.dataease.base.mapper.ext;

import io.dataease.base.domain.PanelShare;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.dto.panel.PanelShareDto;

import java.util.List;

public interface ExtPanelShareMapper {

    int batchInsert(List<PanelShare> shares);

    List<PanelShareDto> query(GridExample example);
}

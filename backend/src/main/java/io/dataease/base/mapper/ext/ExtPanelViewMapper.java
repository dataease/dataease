package io.dataease.base.mapper.ext;

import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.dto.panel.po.PanelViewPo;

import java.util.List;

public interface ExtPanelViewMapper {

    List<PanelViewPo> groups(GridExample example);

    List<PanelViewPo> views(GridExample example);
}

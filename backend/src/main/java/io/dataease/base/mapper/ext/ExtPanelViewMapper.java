package io.dataease.base.mapper.ext;

import io.dataease.dto.panel.po.PanelViewPo;

import java.util.List;

public interface ExtPanelViewMapper {

    List<PanelViewPo> groups(String userId);

    List<PanelViewPo> views(String userId);
}

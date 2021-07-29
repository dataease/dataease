package io.dataease.base.mapper.ext;

import io.dataease.dto.panel.PanelViewDto;
import io.dataease.dto.panel.po.PanelViewInsertDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtPanelViewMapper {

    List<PanelViewDto> groups(String userId);

    List<PanelViewDto> views(String userId);

    void deleteWithPanelId(String panelId);

    void savePanelView(@Param("panelViews") List<PanelViewInsertDTO> panelViews);
}

package io.dataease.base.mapper.ext;

import io.dataease.dto.chart.ChartViewDTO;
import io.dataease.dto.panel.PanelViewDto;
import io.dataease.dto.panel.PanelViewTableDTO;
import io.dataease.dto.panel.po.PanelViewInsertDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtPanelViewMapper {

    List<PanelViewDto> groups(String userId);

    List<PanelViewDto> views(String userId);

    List<PanelViewTableDTO> getPanelViewDetails(@Param("panelId") String panelId);

    void deleteWithPanelId(String panelId);

    void savePanelView(@Param("panelViews") List<PanelViewInsertDTO> panelViews);
}

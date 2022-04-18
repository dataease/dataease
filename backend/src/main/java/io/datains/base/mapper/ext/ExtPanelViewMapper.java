package io.datains.base.mapper.ext;

import io.datains.dto.panel.PanelViewDto;
import io.datains.dto.panel.PanelViewTableDTO;
import io.datains.dto.panel.po.PanelViewInsertDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtPanelViewMapper {

    List<PanelViewDto> groups(String userId);

    List<PanelViewDto> views(String userId);

    List<PanelViewTableDTO> getPanelViewDetails(@Param("panelId") String panelId);

    void deleteWithPanelId(String panelId);

    void savePanelView(@Param("panelViews") List<PanelViewInsertDTO> panelViews);

    void copyFromPanel(@Param("newPanelId") String newPanelId,@Param("sourcePanelId") String sourcePanelId,@Param("copyId") String copyId);

}

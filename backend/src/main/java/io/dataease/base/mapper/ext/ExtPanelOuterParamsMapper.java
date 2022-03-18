package io.dataease.base.mapper.ext;


import io.dataease.dto.panel.outerParams.PanelOuterParamsDTO;
import org.apache.ibatis.annotations.Param;

public interface ExtPanelOuterParamsMapper {

    PanelOuterParamsDTO queryWithPanelId(@Param("panelId") String panelId);

    void deleteOuterParamsTargetWithPanelId(@Param("panelId") String panelId);

    void deleteOuterParamsInfoWithPanelId(@Param("panelId") String panelId);

    void deleteOuterParamsWithPanelId(@Param("panelId") String panelId);

}

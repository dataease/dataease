package io.dataease.ext;

import io.dataease.dto.panel.linkJump.PanelLinkJumpBaseRequest;
import io.dataease.dto.panel.linkJump.PanelLinkJumpDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtPanelLinkJumpMapper {
    List<PanelLinkJumpDTO> queryWithPanelId(@Param("panelId") String panelId);

    PanelLinkJumpDTO queryWithViewId(@Param("panelId") String panelId,@Param("viewId") String viewId);

    void deleteJumpTargetViewInfo(@Param("panelId") String panelId,@Param("viewId") String viewId);

    void deleteJumpInfo(@Param("panelId") String panelId,@Param("viewId") String viewId);

    void deleteJump(@Param("panelId") String panelId,@Param("viewId") String viewId);

    void deleteJumpTargetViewInfoWithPanel(@Param("panelId") String panelId);

    void deleteJumpInfoWithPanel(@Param("panelId") String panelId);

    void deleteJumpWithPanel(@Param("panelId") String panelId);

    List<PanelLinkJumpDTO> getTargetPanelJumpInfo(@Param("request")PanelLinkJumpBaseRequest request);

    void copyLinkJump(@Param("copyId")String copyId);

    void copyLinkJumpInfo(@Param("copyId")String copyId);

    void copyLinkJumpTarget(@Param("copyId")String copyId);
}

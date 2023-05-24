package io.dataease.ext;

import io.dataease.controller.request.panel.PanelGroupRequest;
import io.dataease.dto.RelationDTO;
import io.dataease.dto.panel.PanelGroupDTO;
import io.dataease.plugins.common.base.domain.PanelGroup;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtPanelGroupMapper {

    List<PanelGroupDTO> panelGroupList(PanelGroupRequest request);

    List<PanelGroupDTO> panelGroupListDefault(PanelGroupRequest request);

    //会级联删除pid 下的所有数据
    int deleteCircle(@Param("pid") String pid, @Param("nodeType") String nodeType);

    int deleteLinkDefaultCircle(@Param("pid") String pid);

    int deleteCircleView(@Param("pid") String pid, @Param("nodeType") String nodeType);

    int deleteCircleViewCache(@Param("pid") String pid, @Param("nodeType") String nodeType);

    PanelGroupDTO findOneWithPrivileges(@Param("panelId") String panelId, @Param("userId") String userId);

    PanelGroupDTO findShortOneWithPrivileges(@Param("panelId") String panelId, @Param("userId") String userId);

    void copyPanelView(@Param("pid") String panelId);

    //移除未使用的视图
    void removeUselessViews(@Param("panelId") String panelId, @Param("viewIds") List<String> viewIds);

    List<PanelGroupDTO> panelGroupInit();

    List<RelationDTO> queryPanelRelation(@Param("panelId") String panelId, @Param("userId") Long userId);

    List<PanelGroup> listPanelByUser(@Param("userId") long userId);
}

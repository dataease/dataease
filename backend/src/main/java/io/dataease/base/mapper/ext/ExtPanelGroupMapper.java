package io.dataease.base.mapper.ext;

import io.dataease.controller.request.panel.PanelGroupRequest;
import io.dataease.dto.panel.PanelGroupDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtPanelGroupMapper {

    List<PanelGroupDTO> panelGroupList(PanelGroupRequest request);

    List<PanelGroupDTO> panelGroupListDefault(PanelGroupRequest request);

    //会级联删除pid 下的所有数据
    int deleteCircle(@Param("pid") String pid);

    int deleteCircleView(@Param("pid") String pid);

    int deleteCircleViewCache(@Param("pid") String pid);

    PanelGroupDTO findOneWithPrivileges(@Param("panelId") String panelId,@Param("userId") String userId);

    void copyPanelView(@Param("pid") String panelId);

    //移除未使用的视图
    void removeUselessViews(@Param("panelId") String panelId,@Param("viewIds") List<String> viewIds);

    List<PanelGroupDTO> panelGroupInit();



}

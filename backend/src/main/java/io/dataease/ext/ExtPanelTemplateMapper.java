package io.dataease.ext;

import io.dataease.controller.request.panel.PanelTemplateRequest;
import io.dataease.dto.panel.PanelTemplateDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtPanelTemplateMapper {

    List<PanelTemplateDTO> panelTemplateList(PanelTemplateRequest request);

    List<PanelTemplateDTO> panelSystemTemplateType(PanelTemplateRequest request);
    //会级联删除pid 下的所有数据
    int deleteCircle(@Param("pid") String pid);

    PanelTemplateDTO panelTemplate(String id);


}

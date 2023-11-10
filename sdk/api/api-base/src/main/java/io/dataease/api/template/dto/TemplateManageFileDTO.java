package io.dataease.api.template.dto;


import io.dataease.api.template.vo.VisualizationTemplateVO;
import lombok.Data;

import java.util.List;


@Data
public class TemplateManageFileDTO extends VisualizationTemplateVO {

    /**
     * 样式数据
     */
    private String canvasStyleData;

    /**
     * 组件数据
     */
    private String componentData;


    private String staticResource;

}

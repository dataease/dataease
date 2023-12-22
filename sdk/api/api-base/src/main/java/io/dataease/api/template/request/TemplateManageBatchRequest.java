package io.dataease.api.template.request;

import io.dataease.api.template.vo.VisualizationTemplateVO;
import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Description:
 */
@Data
public class TemplateManageBatchRequest {

    private String optType;

    private List<String> templateIds;

    private List<String> categories;

}

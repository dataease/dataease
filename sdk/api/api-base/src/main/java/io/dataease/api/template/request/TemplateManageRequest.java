package io.dataease.api.template.request;

import io.dataease.api.template.vo.VisualizationTemplateVO;
import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
@Data
public class TemplateManageRequest extends VisualizationTemplateVO {
    private String sort;
    private String withBlobs="N";

    private String optType;

    private String staticResource;

    private Boolean withChildren = false;

    private String leafDvType;

    private String categoryId;

    private List<String> categories;
    
    private List<String> templateNames;

    private List<String> templateArray;

    public TemplateManageRequest() {

    }

    public TemplateManageRequest(String pid,String dvType) {
        super.setPid(pid);
        super.setDvType(dvType);
        withBlobs="N";
    }
}

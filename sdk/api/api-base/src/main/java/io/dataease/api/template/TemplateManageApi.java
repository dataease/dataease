package io.dataease.api.template;

import io.dataease.api.template.dto.TemplateManageDTO;
import io.dataease.api.template.request.TemplateManageRequest;
import io.dataease.api.template.vo.VisualizationTemplateVO;
import org.springframework.web.bind.annotation.*;
import java.util.List;

public interface TemplateManageApi {

    @PostMapping("/templateList")
     List<TemplateManageDTO> templateList(@RequestBody TemplateManageRequest request);

    @PostMapping("/save")
    TemplateManageDTO save(@RequestBody TemplateManageRequest request);

    @PostMapping("/delete/{id}")
    void delete(@PathVariable String id);

    @PostMapping("/deleteCategory/{id}")
    void deleteCategory(@PathVariable String id);

    @GetMapping("/findOne/{templateId}")
    VisualizationTemplateVO findOne(@PathVariable String templateId) throws Exception;

    @PostMapping("/find")
    List<TemplateManageDTO> find(@RequestBody TemplateManageRequest request);

    @PostMapping("/findCategories")
    List<TemplateManageDTO> findCategories(@RequestBody TemplateManageRequest request);

    @PostMapping("/nameCheck")
    String nameCheck(@RequestBody TemplateManageRequest request);

}

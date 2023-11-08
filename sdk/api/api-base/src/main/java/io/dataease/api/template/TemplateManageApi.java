package io.dataease.api.template;

import io.dataease.api.template.dto.TemplateManageDTO;
import io.dataease.api.template.request.TemplateManageRequest;
import io.dataease.api.template.vo.VisualizationTemplateVO;
import org.springframework.web.bind.annotation.*;
import java.util.List;

public interface TemplateManageApi {

    @PostMapping("/templateList")
     List<TemplateManageDTO> templateList();

    @PostMapping("/save")
    TemplateManageDTO save(@RequestBody TemplateManageRequest request);

    @DeleteMapping("/delete/{id}")
    void delete(@PathVariable String id);

    @GetMapping("/findOne/{id}")
    VisualizationTemplateVO findOne(@PathVariable String id) throws Exception;

    @PostMapping("/find")
    List<TemplateManageDTO> find(@RequestBody TemplateManageRequest request);

    @PostMapping("/nameCheck")
    String nameCheck(@RequestBody TemplateManageRequest request);

}

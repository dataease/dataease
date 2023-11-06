package io.dataease.api.template;

import io.dataease.api.template.dto.TemplateManageDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface TemplateManageApi {

    @PostMapping("/templateList")
     List<TemplateManageDTO> templateList();

}

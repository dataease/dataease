package io.dataease.api.template;

import io.dataease.api.template.dto.TemplateManageDTO;
import io.dataease.api.template.request.TemplateManageBatchRequest;
import io.dataease.api.template.request.TemplateManageRequest;
import io.dataease.api.template.vo.VisualizationTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "模版管理:基础")
public interface TemplateManageApi {

    @PostMapping("/templateList")
    @Operation(summary = "模版列表")
    List<TemplateManageDTO> templateList(@RequestBody TemplateManageRequest request);

    @PostMapping("/save")
    @Operation(summary = "保存")
    TemplateManageDTO save(@RequestBody TemplateManageRequest request);

    @PostMapping("/delete/{id}/{categoryId}")
    @Operation(summary = "删除")
    void delete(@PathVariable String id,@PathVariable String categoryId);

    @PostMapping("/deleteCategory/{id}")
    @Operation(summary = "删除分类")
    String deleteCategory(@PathVariable String id);

    @GetMapping("/findOne/{templateId}")
    @Operation(summary = "明细查询")
    VisualizationTemplateVO findOne(@PathVariable String templateId) throws Exception;

    @PostMapping("/findCategoriesByTemplateIds")
    @Operation(summary = "明细查询")
    List<String> findCategoriesByTemplateIds(@RequestBody TemplateManageRequest request) throws Exception;


    @PostMapping("/find")
    @Operation(summary = "查询")
    List<TemplateManageDTO> find(@RequestBody TemplateManageRequest request);

    @PostMapping("/findCategories")
    @Operation(summary = "分类明细查询")
    List<TemplateManageDTO> findCategories(@RequestBody TemplateManageRequest request);

    @PostMapping("/nameCheck")
    @Operation(summary = "模版名称校验")
    String nameCheck(@RequestBody TemplateManageRequest request);

    @PostMapping("/categoryTemplateNameCheck")
    @Operation(summary = "分类名称校验")
    String categoryTemplateNameCheck(@RequestBody TemplateManageRequest request);

    @PostMapping("/checkCategoryTemplateBatchNames")
    @Operation(summary = "分类名称批量校验")
    String checkCategoryTemplateBatchNames(@RequestBody TemplateManageRequest request);

    @PostMapping("/batchUpdate")
    @Operation(summary = "批量更新")
    void batchUpdate(@RequestBody TemplateManageBatchRequest request);

    @PostMapping("/batchDelete")
    @Operation(summary = "批量删除")
    void batchDelete(@RequestBody TemplateManageBatchRequest request);

}

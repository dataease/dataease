package io.dataease.api.material;

import io.dataease.api.material.dto.*;
import io.dataease.api.material.vo.*;
import io.dataease.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import java.util.List;

@Tag(name = "系统管理:素材管理")
public interface MaterialApi {
    @Operation(summary = "导出资源所需的完整图片素材")
    @PostMapping("/export")
    MaterialBundle export(@RequestBody MaterialBundleRequest request);

    @Operation(summary = "当前素材管理能力")
    @GetMapping("/capabilities")
    MaterialCapabilities capabilities();

    @Operation(summary = "管理素材列表")
    @PostMapping("/pager/{page}/{size}")
    PageResult<MaterialVO> pager(@PathVariable("page") int page, @PathVariable("size") int size,
                                @RequestBody MaterialQuery request);

    @Operation(summary = "设计器素材列表")
    @PostMapping("/select/{page}/{size}")
    PageResult<MaterialVO> select(@PathVariable("page") int page, @PathVariable("size") int size,
                                 @RequestBody MaterialQuery request);

    @Operation(summary = "素材分类")
    @PostMapping("/categories")
    List<MaterialCategoryVO> categories(@RequestBody MaterialQuery request);

    @Operation(summary = "保存一级分类")
    @PostMapping("/category/save")
    void saveCategory(@RequestBody MaterialCategoryRequest request);

    @Operation(summary = "删除分类并将素材移至未分类")
    @PostMapping("/category/delete/{id}")
    void deleteCategory(@PathVariable("id") Long id);

    @Operation(summary = "上传素材")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    MaterialVO upload(@RequestPart("file") MultipartFile file,
                      @RequestParam(value = "categoryId", required = false) Long categoryId,
                      @RequestParam(value = "name", required = false) String name);

    @Operation(summary = "重命名或移动素材分类")
    @PostMapping("/update")
    void update(@RequestBody MaterialUpdateRequest request);

    @Operation(summary = "标记删除素材")
    @PostMapping("/delete/{id}")
    void delete(@PathVariable("id") Long id);

    @Operation(summary = "授权读取素材")
    @PostMapping("/content/{id}")
    ResponseEntity<ByteArrayResource> content(@PathVariable("id") Long id,
                                             @RequestBody MaterialContext context);

    @Operation(summary = "下载素材")
    @GetMapping("/download/{id}")
    ResponseEntity<ByteArrayResource> download(@PathVariable("id") Long id);
}

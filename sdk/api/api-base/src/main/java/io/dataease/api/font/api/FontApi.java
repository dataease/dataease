package io.dataease.api.font.api;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;

import io.dataease.api.ds.vo.ExcelFileData;
import io.dataease.api.font.dto.FontDto;
import io.dataease.exception.DEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * @Author Junjun
 */
@Tag(name = "数据集管理:数据")
@ApiSupport(order = 972)
public interface FontApi {

    @Operation(summary = "预览数据")
    @PostMapping("listFont")
    public List<FontDto> list(@RequestBody FontDto fontDto) throws Exception;

    @Operation(summary = "创建")
    @PostMapping("/create")
    public FontDto create(@RequestBody FontDto fontDto);

    @Operation(summary = "编辑")
    @PostMapping("/edit")
    public FontDto edit(@RequestBody FontDto fontDto);

    @Operation(summary = "删除")
    @PostMapping("/delete/{id}")
    public void delete(@PathVariable("id") Long id);

    @Operation(summary = "变更默认设置")
    @PostMapping("/setDefault/")
    public void changeDefault(@RequestBody FontDto fontDto);

    @PostMapping("/uploadFile")
    FontDto upload(@RequestParam("file") MultipartFile file) throws DEException;

    @GetMapping("/download/{file}")
    void download(@PathVariable("file") String file, HttpServletResponse response) throws DEException;

    @GetMapping("/defaultFont")
    List<FontDto> defaultFont() throws DEException;
}

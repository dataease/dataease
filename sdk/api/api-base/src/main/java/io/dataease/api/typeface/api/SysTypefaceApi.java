package io.dataease.api.typeface.api;


import com.github.xiaoymin.knife4j.annotations.ApiSupport;

import io.dataease.api.typeface.dto.SysTypefaceDto;
import io.dataease.auth.DeApiPath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.SYSTEM;

@Tag(name = "字体管理")
@ApiSupport(order = 881, author = "fit2cloud-someone")
@DeApiPath(value = "/sysTypeface", rt = SYSTEM)
public interface SysTypefaceApi {

    @Operation(summary = "创建")
    @PostMapping("/create")
    SysTypefaceDto create(@RequestBody SysTypefaceDto sysVariableDto);

    @Operation(summary = "编辑")
    @PostMapping("/edit")
    SysTypefaceDto edit(@RequestBody SysTypefaceDto sysVariableDto);

    @Operation(summary = "删除")
    @PostMapping("/delete/{id}")
    void delete(@PathVariable("id") Long id);

    @Operation(summary = "变更默认设置")
    @PostMapping("/changeDefault/")
    void changeDefault(@RequestBody SysTypefaceDto sysVariableDto);

}

package io.dataease.api.permissions.variable.api;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.permissions.variable.dto.SysVariableDto;
import io.dataease.api.permissions.variable.dto.SysVariableValueDto;
import io.dataease.auth.DeApiPath;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.SYSTEM;

@Hidden
@Tag(name = "系统变量")
@ApiSupport(order = 881, author = "fit2cloud-someone")
@DeApiPath(value = "/sysVariable", rt = SYSTEM)
public interface SysVariablesApi {

    @Operation(summary = "创建变量")
    @PostMapping("/create")
    SysVariableDto create(@RequestBody SysVariableDto sysVariableDto);

    @Operation(summary = "编辑变量")
    @PostMapping("/edit")
    SysVariableDto edit(@RequestBody SysVariableDto sysVariableDto);

    @Operation(summary = "删除变量")
    @GetMapping("/delete/{id}")
    void delete(@PathVariable("id") Long id);

    @Operation(summary = "变量详细信息")
    @GetMapping("/detail/{id}")
    SysVariableDto detail(@PathVariable("id") Long id);

    @Operation(summary = "系统变量列表")
    @PostMapping("/query")
    List<SysVariableDto> query(@RequestBody SysVariableDto sysVariableDto);

    @Operation(summary = "创建变量值")
    @PostMapping("/value/create")
    SysVariableValueDto createValue(@RequestBody SysVariableValueDto sysVariableDto);

    @Operation(summary = "编辑变量值")
    @PostMapping("/value/edit")
    SysVariableValueDto editValue(@RequestBody SysVariableValueDto sysVariableDto);

    @Operation(summary = "删除变量值")
    @GetMapping("/value/delete/{id}")
    void deleteValue(@PathVariable("id") String id);

    @Operation(summary = "查看变量值详细信息")
    @GetMapping("/value/selected/{id}")
    List<SysVariableValueDto> selectVariableValue(@PathVariable("id") Long id);

    @Operation(summary = "系统变量值列表")
    @PostMapping("/value/selected/{goPage}/{pageSize}")
    IPage<SysVariableValueDto> selectPage(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody SysVariableValueDto sysVariableValueDto);

    @Operation(summary = "批量删除变量值")
    @PostMapping("/value/batchDel")
    void batchDel(@RequestBody List<Long> ids);

}

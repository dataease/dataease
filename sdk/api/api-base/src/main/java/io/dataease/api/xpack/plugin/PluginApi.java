package io.dataease.api.xpack.plugin;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.xpack.plugin.dto.PluginEditor;
import io.dataease.api.xpack.plugin.vo.PluginVO;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.SYSTEM;

@Tag(name = "系统设置:插件管理")
@ApiSupport(order = 2)
@DeApiPath(value = "/plugin", rt = SYSTEM)
public interface PluginApi {

    @Operation(summary = "查询")
    @DePermit("m:read")
    @GetMapping("/query")
    List<PluginVO> query();

    @Operation(summary = "安装")
    @DePermit("m:manage")
    @PostMapping(value = "/install", consumes = {"multipart/form-data"})
    void install(@RequestPart(value = "file") MultipartFile file);

    @DePermit("m:manage")
    @PostMapping(value = "/prepare", consumes = {"multipart/form-data"})
    io.dataease.api.xpack.plugin.vo.PluginInstallCheck prepare(
            @RequestPart(value = "request", required = false) PluginEditor request,
            @RequestPart("file") MultipartFile file);

    @DePermit("m:manage")
    @PostMapping("/confirm")
    void confirm(@org.springframework.web.bind.annotation.RequestBody
                 io.dataease.api.xpack.plugin.dto.PluginInstallConfirm request);

    @DePermit("m:manage")
    @PostMapping("/cancel/{operation}")
    void cancel(@PathVariable String operation);

    @Operation(summary = "卸载")
    @DePermit("m:manage")
    @PostMapping("/uninstall/{id}")
    void uninstall(@PathVariable("id") String id);

    @Operation(summary = "更新")
    @DePermit("m:manage")
    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    void update(@RequestPart("request") PluginEditor request, @RequestPart(value = "file") MultipartFile file);

}

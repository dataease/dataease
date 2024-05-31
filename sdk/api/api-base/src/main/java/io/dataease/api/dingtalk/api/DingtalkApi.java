package io.dataease.api.dingtalk.api;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.dingtalk.dto.DingtalkEnableEditor;
import io.dataease.api.dingtalk.dto.DingtalkSettingCreator;
import io.dataease.api.dingtalk.dto.DingtalkTokenRequest;
import io.dataease.api.dingtalk.vo.DingtalkInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "钉钉设置")
@ApiSupport(order = 899)
public interface DingtalkApi {

    @Operation(summary = "查询钉钉信息")
    @GetMapping("/info")
    DingtalkInfoVO info();

    @Operation(summary = "查询钉钉二维码信息")
    @GetMapping("/qrinfo")
    DingtalkInfoVO qrinfo();

    @Operation(summary = "保存")
    @PostMapping("/create")
    void save(@RequestBody DingtalkSettingCreator creator);

    @Operation(summary = "钉钉token", hidden = true)
    @PostMapping("/token")
    String dingtalkToken(@RequestBody DingtalkTokenRequest request);

    @Operation(summary = "切换开启状态")
    @PostMapping("/switchEnable")
    void switchEnable(@RequestBody DingtalkEnableEditor editor);

    @Operation(summary = "验证可用性")
    @PostMapping("/validate")
    void validate(@RequestBody DingtalkSettingCreator creator);

    @Operation(summary = "钉钉绑定", hidden = true)
    @PostMapping("/bind")
    void bind(@RequestBody DingtalkTokenRequest request);
}

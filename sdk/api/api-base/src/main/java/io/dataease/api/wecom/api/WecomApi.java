package io.dataease.api.wecom.api;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.wecom.dto.WecomCreator;
import io.dataease.api.wecom.dto.WecomEnableEditor;
import io.dataease.api.wecom.dto.WecomTokenRequest;
import io.dataease.api.wecom.vo.WecomInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "企微设置")
@ApiSupport(order = 899)
public interface WecomApi {

    @Operation(summary = "查询企微信息")
    @GetMapping("/info")
    WecomInfoVO info();

    @Operation(summary = "查询企微二维码信息")
    @GetMapping("/qrinfo")
    WecomInfoVO qrinfo();

    @Operation(summary = "保存")
    @PostMapping("/create")
    void save(@RequestBody WecomCreator creator);

    @Operation(summary = "企微token", hidden = true)
    @PostMapping("/token")
    String wecomToken(@RequestBody WecomTokenRequest request);

    @Operation(summary = "切换开启状态")
    @PostMapping("/switchEnable")
    void switchEnable(@RequestBody WecomEnableEditor editor);

    @Operation(summary = "验证可用性")
    @PostMapping("/validate")
    void validate(@RequestBody WecomCreator creator);

    @Operation(summary = "企微绑定用户", hidden = true)
    @PostMapping("/bind")
    void bind(@RequestBody WecomTokenRequest request);
}

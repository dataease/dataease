package io.dataease.api.email;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.system.vo.SettingItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "系统设置:邮件设置")
@ApiSupport(order = 799)
public interface EmailApi {

    @Operation(summary = "查询邮件设置")
    @GetMapping("/setting/query")
    List<SettingItemVO> queryEmailSetting();

    @Operation(summary = "保存邮件设置")
    @PostMapping("/setting/save")
    void saveEmailSetting(@RequestBody List<SettingItemVO> settingItemVOS);

    @Operation(summary = "校验邮件设置")
    @PostMapping("/setting/validate")
    void validate(@RequestBody List<SettingItemVO> settingItemVOS);

}

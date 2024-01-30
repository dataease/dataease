package io.dataease.api.permissions.setting.api;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.permissions.setting.vo.PerSettingItemVO;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "认证相关设置")
@ApiSupport(order = 882)
public interface PerSettingApi {

    @Operation(summary = "查询设置")
    @GetMapping("/basic/query")
    List<PerSettingItemVO> basicSetting();

    @Operation(summary = "保存设置")
    @PostMapping("/baisc/save")
    void saveBasic(@RequestBody List<Object> settings);

    @Hidden
    @GetMapping("/baisc/single/{key}")
    String singleValue(@PathVariable("key") String key);
}

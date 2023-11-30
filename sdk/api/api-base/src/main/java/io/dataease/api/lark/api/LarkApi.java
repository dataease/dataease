package io.dataease.api.lark.api;

import io.dataease.api.lark.dto.LarkEnableEditor;
import io.dataease.api.lark.dto.LarkTokenRequest;
import io.dataease.api.lark.vo.LarkInfoVO;
import io.dataease.api.lark.dto.LarkSettingCreator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface LarkApi {

    @GetMapping("/info")
    LarkInfoVO info();

    @PostMapping("/create")
    void save(@RequestBody LarkSettingCreator creator);

    @PostMapping("/token")
    String larkToken(@RequestBody LarkTokenRequest request);

    @PostMapping("/switchEnable")
    void switchEnable(@RequestBody LarkEnableEditor editor);

}

package io.dataease.api.permissions.setting.api;

import io.dataease.api.permissions.setting.vo.PerSettingItemVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface PerSettingApi {

    @GetMapping("/basic/query")
    List<PerSettingItemVO> basicSetting();

    @PostMapping("/baisc/save")
    void saveBasic(@RequestBody List<Object> settings);
}

package io.dataease.api.permissions.apikey.api;

import io.dataease.api.permissions.apikey.dto.ApikeyEnableEditor;
import io.dataease.api.permissions.apikey.vo.ApiKeyVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ApiKeyApi {

    @PostMapping("/generate")
    void generate();

    @GetMapping("/query")
    List<ApiKeyVO> query();

    @PostMapping("/switch")
    void switchEnable(@RequestBody ApikeyEnableEditor editor);

    @PostMapping("/delete/{id}")
    void delete(@PathVariable("id") Long id);
}

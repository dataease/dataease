package io.dataease.api.xpack.settings;

import io.dataease.api.xpack.settings.request.XpackAuthenticationEditor;
import io.dataease.api.xpack.settings.vo.XpackAuthenticationVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface XpackAuthenticationApi {

    @GetMapping("/sync")
    void sync();

    @GetMapping("/grid")
    List<XpackAuthenticationVO> grid();

    @PostMapping("/update")
    void update(@RequestBody XpackAuthenticationEditor editor);
}

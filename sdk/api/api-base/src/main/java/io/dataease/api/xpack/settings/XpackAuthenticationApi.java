package io.dataease.api.xpack.settings;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.xpack.settings.request.XpackAuthenticationEditor;
import io.dataease.api.xpack.settings.vo.XpackAuthenticationStatusVO;
import io.dataease.api.xpack.settings.vo.XpackAuthenticationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "认证设置")
@ApiSupport(order = 899)
public interface XpackAuthenticationApi {

    @Operation(summary = "同步")
    @GetMapping("/sync")
    void sync();

    @Operation(summary = "查询列表")
    @GetMapping("/grid")
    List<XpackAuthenticationVO> grid();

    @Operation(summary = "切换状态")
    @PostMapping("/update")
    void update(@RequestBody XpackAuthenticationEditor editor);

    @Operation(summary = "查询状态")
    @GetMapping("/status")
    List<XpackAuthenticationStatusVO> status();
}

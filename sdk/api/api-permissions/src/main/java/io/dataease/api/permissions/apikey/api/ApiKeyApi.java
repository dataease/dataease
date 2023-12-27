package io.dataease.api.permissions.apikey.api;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.permissions.apikey.dto.ApikeyEnableEditor;
import io.dataease.api.permissions.apikey.vo.ApiKeyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "API Key")
@ApiSupport(order = 884, author = "fit2cloud-someone")
public interface ApiKeyApi {

    @Operation(summary = "生成")
    @ApiOperationSupport(order = 1)
    @PostMapping("/generate")
    void generate();

    @Operation(summary = "查询")
    @ApiOperationSupport(order = 2)
    @GetMapping("/query")
    List<ApiKeyVO> query();

    @Operation(summary = "切换状态")
    @ApiOperationSupport(order = 3)
    @PostMapping("/switch")
    void switchEnable(@RequestBody ApikeyEnableEditor editor);

    @Operation(summary = "删除")
    @ApiOperationSupport(order = 4)
    @Parameter(name = "id", description = "ID", required = true, in = ParameterIn.PATH)
    @PostMapping("/delete/{id}")
    void delete(@PathVariable("id") Long id);
}

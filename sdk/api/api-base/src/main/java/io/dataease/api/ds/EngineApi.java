package io.dataease.api.ds;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.DeApiPath;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import static io.dataease.constant.AuthResourceEnum.DATASOURCE;

@Tag(name = "引擎管理:基础")
@ApiSupport(order = 970)
@DeApiPath(value = "/engine", rt = DATASOURCE)
public interface EngineApi {

    @GetMapping("/getEngine")
    DatasourceDTO getEngine();

    @PostMapping("/save")
    void save(@RequestBody DatasourceDTO datasourceDTO);

    @PostMapping("/validate")
    void validate(@RequestBody DatasourceDTO datasourceDTO) throws Exception;

    @PostMapping("/validate/{id}")
    void validateById(@PathVariable Long id) throws Exception;
}

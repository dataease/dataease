package io.dataease.api.log;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.log.dto.LogGridRequest;
import io.dataease.api.log.vo.LogGridVO;
import io.dataease.api.log.vo.LogOpVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "工具箱:日志管理")
@ApiSupport(order = 2)
public interface LogApi {

    @Operation(summary = "查询日志列表")
    @Parameters({
            @Parameter(name = "goPage", description = "目标页码", required = true, in = ParameterIn.PATH),
            @Parameter(name = "pageSize", description = "每页容量", required = true, in = ParameterIn.PATH),
            @Parameter(name = "request", description = "过滤条件", required = true)
    })
    @PostMapping("/pager/{goPage}/{pageSize}")
    IPage<LogGridVO> pager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody LogGridRequest request);

    @Operation(summary = "导出日志列表")
    @PostMapping("/export")
    void export(@RequestBody LogGridRequest request);

    @Operation(summary = "操作类型")
    @GetMapping("/options")
    List<LogOpVO> logOptions();
}

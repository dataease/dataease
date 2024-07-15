package io.dataease.api.report;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.report.dto.*;
import io.dataease.api.report.vo.ReportGridVO;
import io.dataease.api.report.vo.ReportInfoVO;
import io.dataease.api.report.vo.ReportInstanceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "定时报告")
@ApiSupport(order = 888, author = "fit2cloud-someone")
public interface ReportApi {

    @Operation(summary = "查询报告列表")
    @Parameters({
            @Parameter(name = "goPage", description = "目标页码", required = true, in = ParameterIn.PATH),
            @Parameter(name = "pageSize", description = "每页容量", required = true, in = ParameterIn.PATH),
            @Parameter(name = "request", description = "过滤条件", required = true)
    })
    @PostMapping("/pager/{goPage}/{pageSize}")
    IPage<ReportGridVO> pager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody ReportGridRequest request);

    @Operation(summary = "创建任务")
    @PostMapping("/create")
    void create(@RequestBody ReportCreator creator);

    @Operation(summary = "更新任务")
    @PostMapping("/update")
    void update(@RequestBody ReportEditor editor);

    @Operation(summary = "立即运行")
    @PostMapping("/fireNow/{taskId}")
    void fireNow(@PathVariable("taskId") Long taskId);

    @Operation(summary = "停止")
    @PostMapping("/stop/{taskId}")
    void stopNow(@PathVariable("taskId") Long taskId);

    @Operation(summary = "启用")
    @PostMapping("/start/{taskId}")
    void start(@PathVariable("taskId") Long taskId);

    @Operation(summary = "删除")
    @PostMapping("/delete")
    void delete(@RequestBody List<Long> taskIdList);


    @Operation(summary = "查询详情")
    @GetMapping("/info/{taskId}")
    ReportInfoVO info(@PathVariable("taskId") Long taskId);

    @Operation(summary = "查询日志列表")
    @Parameters({
            @Parameter(name = "goPage", description = "目标页码", required = true, in = ParameterIn.PATH),
            @Parameter(name = "pageSize", description = "每页容量", required = true, in = ParameterIn.PATH),
            @Parameter(name = "request", description = "过滤条件", required = true)
    })
    @PostMapping("/logPager/{goPage}/{pageSize}")
    IPage<ReportInstanceVO> pager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody ReportInstanceRequest request);

    @Operation(summary = "删除日志")
    @PostMapping("/deleteLog")
    void deleteInstance(@RequestBody ReportInstanceDelRequest request);

    @PostMapping("/logMsg")
    String logMsg(@RequestBody ReportInstanceMsgRequest request);

    @PostMapping("/export")
    ResponseEntity<ByteArrayResource> export(@RequestBody ReportExportRequest request);
}

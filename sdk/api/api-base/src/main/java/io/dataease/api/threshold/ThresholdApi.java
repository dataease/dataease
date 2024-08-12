package io.dataease.api.threshold;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.threshold.dto.ThresholdBatchReciRequest;
import io.dataease.api.threshold.dto.ThresholdCreator;
import io.dataease.api.threshold.dto.ThresholdGridRequest;
import io.dataease.api.threshold.dto.ThresholdSwitchRequest;
import io.dataease.api.threshold.vo.ThresholdGridVO;
import io.dataease.api.threshold.vo.ThresholdInstanceVO;
import io.dataease.model.KeywordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "阈值告警")
@ApiSupport(order = 888, author = "fit2cloud-someone")
public interface ThresholdApi {

    @Operation(summary = "保存")
    @PostMapping("/save")
    void save(@RequestBody ThresholdCreator creator);

    @Operation(summary = "修改")
    @PostMapping("/edit")
    void edit(@RequestBody ThresholdCreator creator);

    @Operation(summary = "查询列表")
    @Parameters({
            @Parameter(name = "goPage", description = "目标页码", required = true, in = ParameterIn.PATH),
            @Parameter(name = "pageSize", description = "每页容量", required = true, in = ParameterIn.PATH),
            @Parameter(name = "request", description = "过滤条件", required = true)
    })
    @PostMapping("/pager/{goPage}/{pageSize}")
    IPage<ThresholdGridVO> pager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody ThresholdGridRequest request);

    @Operation(summary = "切换可用")
    @PostMapping("/switch")
    void switchEnable(@RequestBody ThresholdSwitchRequest request);

    @Operation(summary = "删除")
    @PostMapping("/delete")
    void delete(@RequestBody List<Long> idList);

    @Operation(summary = "批量设置接收人")
    @PostMapping("/batchReci")
    void batchReci(@RequestBody ThresholdBatchReciRequest request);

    @Operation(summary = "查询实例列表")
    @Parameters({
            @Parameter(name = "goPage", description = "目标页码", required = true, in = ParameterIn.PATH),
            @Parameter(name = "pageSize", description = "每页容量", required = true, in = ParameterIn.PATH),
            @Parameter(name = "request", description = "过滤条件", required = true)
    })
    @PostMapping("/instancePager/{goPage}/{pageSize}")
    IPage<ThresholdInstanceVO> instancePager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody KeywordRequest request);

}

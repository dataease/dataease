package io.dataease.api.exportCenter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.DePermit;
import io.dataease.model.ExportTaskDTO;
import io.dataease.auth.DeApiPath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

import static io.dataease.constant.AuthResourceEnum.DATASOURCE;

@Tag(name = "数据导出中心")
@ApiSupport(order = 971)
public interface ExportCenterApi {


    @PostMapping("/exportTasks/records")
    public Map<String, Long> exportTasks();

    @DePermit("m:read")
    @PostMapping("/exportTasks/{status}/{goPage}/{pageSize}")
    IPage<ExportTaskDTO> pager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @PathVariable String status);

    @Operation(summary = "删除单条记录")
    @GetMapping("/delete/{id}")
    public void delete(@PathVariable String id);

    @Operation(summary = "批量删除")
    @PostMapping("/delete")
    public void delete(@RequestBody List<String> ids);

    @Operation(summary = "删除")
    @PostMapping("/deleteAll/{type}")
    public void deleteAll(@PathVariable String type);

    @Operation(summary = "下载")
    @GetMapping("/download/{id}")
    public void download(@PathVariable String id, HttpServletResponse response) throws Exception;

    @Operation(summary = "生成下载Url")
    @GetMapping("/generateDownloadUri/{id}")
    public String generateDownloadUri(@PathVariable String id) throws Exception;

    @Operation(summary = "重试")
    @PostMapping("/retry/{id}")
    public void retry(@PathVariable String id);

    @PostMapping("/exportLimit")
    public String exportLimit();

}

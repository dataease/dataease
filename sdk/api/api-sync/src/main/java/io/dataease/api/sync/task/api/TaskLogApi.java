package io.dataease.api.sync.task.api;

import io.dataease.result.PageResult;
import io.dataease.api.sync.task.dto.TaskLogGridRequest;
import io.dataease.api.sync.task.vo.LogResultVO;
import io.dataease.api.sync.task.vo.TaskLogVO;
import io.dataease.api.sync.task.vo.TaskLogClearResultVO;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

import static io.dataease.constant.AuthResourceEnum.TASK;

/**
 * @author fit2cloud
 * @date 2023/12/4 12:43
 **/
@Hidden
@DeApiPath(value = "/sync/task/log", rt = TASK)
public interface TaskLogApi {
    @PostMapping("/pager/{goPage}/{pageSize}")
    @DePermit("m:read")
    PageResult<TaskLogVO> pager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody TaskLogGridRequest request);

    @GetMapping("/detail/{logId}/{fromLineNum}")
    @DePermit("m:read")
    LogResultVO logDetail(@PathVariable("logId") String logId, @PathVariable("fromLineNum") int fromLineNum);

    @PostMapping("/save")
    @DePermit("m:manage")
    void saveLog(@RequestBody TaskLogVO logDetail);

    @PostMapping("/update")
    @DePermit("m:manage")
    void updateLog(@RequestBody TaskLogVO logDetail);

    @PostMapping("/deleteByJobId/{jobId}")
    @DePermit("m:manage")
    void deleteByJobId(@PathVariable("jobId") String jobId);

    @PostMapping("/delete/{logId}")
    @DePermit("m:manage")
    void deleteById(@PathVariable("logId") String logId);

    @PostMapping("/clear")
    @DePermit("m:manage")
    TaskLogClearResultVO clearJobLog(@RequestBody TaskLogVO taskLogVO);

    @GetMapping("/clear/result")
    @DePermit("m:manage")
    TaskLogClearResultVO clearResult();

    @PostMapping("terminationTask/{logId}")
    @DePermit("m:manage")
    void terminationTask(@PathVariable("logId") String logId);

    @PostMapping("/getLogResourceId")
    @DePermit("m:read")
    Long getLogResourceId(@RequestBody Map<String, String> params);

    @PostMapping("/query2Root")
    @DePermit("m:read")
    String query2Root(@RequestBody Map<String, String> params);

}

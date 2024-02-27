package io.dataease.api.sync.task.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.api.sync.task.vo.LogResultVO;
import io.dataease.api.sync.task.vo.TaskLogVO;
import io.dataease.auth.DeApiPath;
import io.dataease.request.BaseGridRequest;
import org.springframework.web.bind.annotation.*;

import static io.dataease.constant.AuthResourceEnum.TASK;

/**
 * @author fit2cloud
 * @date 2023/12/4 12:43
 **/
@DeApiPath(value = "/sync/task/log", rt = TASK)
public interface TaskLogApi {
    @PostMapping("/pager/{goPage}/{pageSize}")
    IPage<TaskLogVO> pager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody BaseGridRequest request);

    @GetMapping("/detail/{logId}/{fromLineNum}")
    LogResultVO logDetail(@PathVariable("logId") String logId, @PathVariable("fromLineNum") int fromLineNum);

    @PostMapping("/save")
    void saveLog(@RequestBody TaskLogVO logDetail);

    @PostMapping("/update")
    void updateLog(@RequestBody TaskLogVO logDetail);

    @DeleteMapping("/deleteByJobId/{jobId}")
    void deleteByJobId(@PathVariable("jobId") String jobId);

    @DeleteMapping("/delete/{logId}")
    void deleteById(@PathVariable("logId") String logId);

    @PostMapping("/clear")
    void clearJobLog(@RequestBody TaskLogVO taskLogVO);

    @PostMapping("terminationTask/{logId}")
    void terminationTask(@PathVariable("logId") String logId);

}

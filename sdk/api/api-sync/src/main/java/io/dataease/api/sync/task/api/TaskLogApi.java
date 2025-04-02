package io.dataease.api.sync.task.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.api.sync.task.dto.TaskLogGridRequest;
import io.dataease.api.sync.task.vo.LogResultVO;
import io.dataease.api.sync.task.vo.TaskLogVO;
import io.dataease.auth.DeApiPath;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.Hidden;

import static io.dataease.constant.AuthResourceEnum.TASK;

/**
 * @author fit2cloud
 * @date 2023/12/4 12:43
 **/
@Hidden
@DeApiPath(value = "/sync/task/log", rt = TASK)
public interface TaskLogApi {
    @PostMapping("/pager/{goPage}/{pageSize}")
    IPage<TaskLogVO> pager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody TaskLogGridRequest request);

    @GetMapping("/detail/{logId}/{fromLineNum}")
    LogResultVO logDetail(@PathVariable("logId") String logId, @PathVariable("fromLineNum") int fromLineNum);

    @PostMapping("/save")
    void saveLog(@RequestBody TaskLogVO logDetail);

    @PostMapping("/update")
    void updateLog(@RequestBody TaskLogVO logDetail);

    @PostMapping("/deleteByJobId/{jobId}")
    void deleteByJobId(@PathVariable("jobId") String jobId);

    @PostMapping("/delete/{logId}")
    void deleteById(@PathVariable("logId") String logId);

    @PostMapping("/clear")
    void clearJobLog(@RequestBody TaskLogVO taskLogVO);

    @PostMapping("terminationTask/{logId}")
    void terminationTask(@PathVariable("logId") String logId);

}

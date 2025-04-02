package io.dataease.api.sync.task.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.sync.task.dto.TaskGridRequest;
import io.dataease.api.sync.task.dto.TaskInfoDTO;
import io.dataease.api.sync.task.vo.TaskInfoVO;
import io.dataease.auth.DeApiPath;
import io.dataease.exception.DEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.TASK;

/**
 * @author fit2cloud
 * @date 2023/11/20 10:14
 **/
@Tag(name = "同步管理:任务管理")
@ApiSupport(order = 888, author = "fit2cloud-someone")
@DeApiPath(value = "/sync/task", rt = TASK)
public interface TaskApi {

    @Operation(hidden = true)
    @PostMapping("/pager/{goPage}/{pageSize}")
    IPage<TaskInfoVO> pager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody TaskGridRequest request);
    @Operation(hidden = true)
    @PostMapping("/add")
    void add(@RequestBody TaskInfoDTO jobInfo) throws DEException;
    @Operation(hidden = true)
    @PostMapping("/update")
    void update(@RequestBody TaskInfoDTO jobInfo) throws DEException;
    @Operation(hidden = true)
    @PostMapping("/remove/{id}")
    void remove(@PathVariable(value = "id") String id) throws DEException;
    @Operation(hidden = true)
    @GetMapping("start/{id}")
    void startJob(@PathVariable(value = "id") String id) throws DEException;
    @Operation(hidden = true)
    @GetMapping("stop/{id}")
    void stopJob(@PathVariable(value = "id") String id) throws DEException;
    @Operation(hidden = true)
    @GetMapping("/get/{id}")
    TaskInfoVO getOneById(@PathVariable(value = "id") String id) throws DEException;

    @Operation(summary = "执行一次任务")
    @GetMapping("/execute/{id}")
    void execute(@PathVariable(value = "id") String id) throws DEException;
    @Operation(hidden = true)
    @PostMapping("/batch/del")
    void batchDelete(@RequestBody List<String> ids) throws DEException;
    @Operation(hidden = true)
    @GetMapping("/count")
    Long count() throws DEException;

}

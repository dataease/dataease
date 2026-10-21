package io.dataease.api.sync.task.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.sync.task.dto.TaskGridRequest;
import io.dataease.api.sync.task.dto.TaskInfoDTO;
import io.dataease.api.sync.task.vo.TaskInfoVO;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.exception.DEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
    @DePermit("m:read")
    IPage<TaskInfoVO> pager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody TaskGridRequest request);

    @Operation(hidden = true)
    @PostMapping("/add")
    @DePermit("m:manage")
    void add(@RequestBody TaskInfoDTO jobInfo) throws DEException;

    @Operation(hidden = true)
    @PostMapping("/update")
    @DePermit("m:manage")
    void update(@RequestBody TaskInfoDTO jobInfo) throws DEException;

    @Operation(hidden = true)
    @PostMapping("/remove/{id}")
    @DePermit("m:manage")
    void remove(@PathVariable(value = "id") String id) throws DEException;

    @Operation(hidden = true)
    @GetMapping("start/{id}")
    @DePermit("m:manage")
    void startJob(@PathVariable(value = "id") String id) throws DEException;

    @Operation(hidden = true)
    @GetMapping("stop/{id}")
    @DePermit("m:manage")
    void stopJob(@PathVariable(value = "id") String id) throws DEException;

    @Operation(hidden = true)
    @GetMapping("/get/{id}")
    @DePermit("m:read")
    TaskInfoVO getOneById(@PathVariable(value = "id") String id) throws DEException;

    @Operation(summary = "执行一次任务")
    @GetMapping("/execute/{id}")
    @DePermit("m:manage")
    void execute(@PathVariable(value = "id") String id) throws DEException;

    @Operation(hidden = true)
    @PostMapping("/batch/del")
    @DePermit("m:manage")
    void batchDelete(@RequestBody List<String> ids) throws DEException;

    @Operation(hidden = true)
    @GetMapping("/count")
    @DePermit("m:read")
    Long count() throws DEException;

    @GetMapping("/query2Root/{id}")
    @DePermit("m:read")
    String query2Root(@PathVariable("id") Long id);

    default String query2Root(Long id, String resourceUuid) {
        return query2Root(id);
    }

    @GetMapping("/getLogResourceId/{id}")
    @DePermit("m:read")
    Long getLogResourceId(@PathVariable("id") String id);
}

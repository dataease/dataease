package io.dataease.api.sync.task.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.api.sync.task.dto.TaskInfoDTO;
import io.dataease.api.sync.task.vo.TaskInfoVO;
import io.dataease.auth.DeApiPath;
import io.dataease.exception.DEException;
import io.dataease.request.BaseGridRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.TASK;

/**
 * @author fit2cloud
 * @date 2023/11/20 10:14
 **/
@DeApiPath(value = "/sync/task", rt = TASK)
public interface TaskApi {

    @PostMapping("/pager/{goPage}/{pageSize}")
    IPage<TaskInfoVO> pager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody BaseGridRequest request);

    @PostMapping("/add")
    void add(@RequestBody TaskInfoDTO jobInfo) throws DEException;

    @PostMapping("/update")
    void update(@RequestBody TaskInfoDTO jobInfo) throws DEException;

    @DeleteMapping("/remove/{id}")
    void remove(@PathVariable(value = "id") String id) throws DEException;

    @GetMapping("start/{id}")
    void startJob(@PathVariable(value = "id") String id) throws DEException;

    @GetMapping("stop/{id}")
    void stopJob(@PathVariable(value = "id") String id) throws DEException;

    @GetMapping("/get/{id}")
    TaskInfoVO getOneById(@PathVariable(value = "id") String id) throws DEException;

    @GetMapping("/execute/{id}")
    void execute(@PathVariable(value = "id") String id) throws DEException;

    @PostMapping("/batch/del")
    void batchDelete(@RequestBody List<String> ids) throws DEException;

    @GetMapping("/count")
    Long count() throws DEException;

}

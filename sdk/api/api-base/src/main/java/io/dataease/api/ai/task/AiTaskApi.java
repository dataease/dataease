package io.dataease.api.ai.task;

import io.dataease.api.ai.task.AiTaskDtos.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/ai/tasks")
public interface AiTaskApi {
    @PostMapping Task submit(@RequestBody Submit request);
    @GetMapping List<Task> active();
    @GetMapping("/resources/{resourceId}/tables") List<String> tables(@PathVariable String resourceId);
    @GetMapping("/notices") List<Notice> notices();
    @PostMapping("/notices/{attemptId}/ack") void acknowledge(@PathVariable Long attemptId);
    @PostMapping("/{id}/conversation") Task conversation(@PathVariable Long id, @RequestBody Origin origin);
    @GetMapping("/{id}") Task get(@PathVariable Long id);
    @PostMapping("/{id}/cancel") Task cancel(@PathVariable Long id);
    @PostMapping("/{id}/retry") Task retry(@PathVariable Long id, @RequestBody Retry request);
}

package io.dataease.api.ai.task;

import io.dataease.api.ai.task.AiTaskDtos.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/ai/tasks")
public interface AiTaskApi {
    @PostMapping Task submit(@RequestBody Submit request);
    @GetMapping List<Task> active();
    @GetMapping("/{id}") Task get(@PathVariable Long id);
    @PostMapping("/{id}/cancel") Task cancel(@PathVariable Long id);
    @PostMapping("/{id}/retry") Task retry(@PathVariable Long id, @RequestBody Retry request);
}

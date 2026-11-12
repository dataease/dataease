package io.dataease.api.ai.conversation;

import io.dataease.api.ai.conversation.AiConversationDtos.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** 需登录的开发验收入口；授权由可信场景及既有 AI 使用策略逐步复核。 */
@RequestMapping("/ai/conversations")
public interface AiConversationApi {
    @GetMapping("/resources") List<Resource> resources();
    @PostMapping Session open(@Valid @RequestBody Open request);
    @PostMapping("/capabilities/{capability}") Execution invoke(@PathVariable String capability, @Valid @RequestBody Invoke request);
    @PostMapping("/{id}/turns") Answer turn(@PathVariable String id, @Valid @RequestBody Turn request);
    @PostMapping("/{id}/executions") Execution start(@PathVariable String id, @Valid @RequestBody Turn request);
    @PostMapping("/{id}/executions/{execution}/retry") Execution retry(@PathVariable String id, @PathVariable String execution, @Valid @RequestBody Retry request);
    @GetMapping("/{id}/executions/{execution}/events") Events events(@PathVariable String id, @PathVariable String execution, @RequestParam(defaultValue = "0") long after);
    @PostMapping("/{id}/executions/{execution}/stop") Events stop(@PathVariable String id, @PathVariable String execution);
    @PostMapping("/{id}/executions/{execution}/check-application") Result checkApplication(@PathVariable String id, @PathVariable String execution, @Valid @RequestBody Application request);
    @DeleteMapping("/{id}") void close(@PathVariable String id);
}

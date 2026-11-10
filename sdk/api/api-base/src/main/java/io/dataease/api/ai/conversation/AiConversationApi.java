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
    @PostMapping("/{id}/turns") Answer turn(@PathVariable String id, @Valid @RequestBody Turn request);
    @DeleteMapping("/{id}") void close(@PathVariable String id);
}

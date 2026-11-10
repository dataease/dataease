package io.dataease.ai.server;

import io.dataease.ai.manage.AiConversationManage;
import io.dataease.api.ai.conversation.AiConversationApi;
import io.dataease.api.ai.conversation.AiConversationDtos.*;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class AiConversationServer implements AiConversationApi {
    private final AiConversationManage manage;
    public AiConversationServer(AiConversationManage manage) { this.manage = manage; }
    public List<Resource> resources() { return manage.resources(); }
    public Session open(Open request) { return manage.open(request); }
    public Answer turn(String id, Turn request) { return manage.turn(id, request); }
    public void close(String id) { manage.close(id); }
}

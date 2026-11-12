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
    public Execution invoke(String capability, Invoke request) { return manage.invoke(capability, request); }
    public Answer turn(String id, Turn request) { return manage.turn(id, request); }
    public Execution start(String id, Turn request) { return manage.start(id, request); }
    public Events events(String id, String execution, long after) { return manage.events(id, execution, after); }
    public Events stop(String id, String execution) { return manage.stop(id, execution); }
    public Result checkApplication(String id, String execution, Application request) { return manage.checkApplication(id, execution, request); }
    public void close(String id) { manage.close(id); }
}

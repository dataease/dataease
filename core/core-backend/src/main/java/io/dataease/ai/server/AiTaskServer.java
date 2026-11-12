package io.dataease.ai.server;

import io.dataease.ai.manage.AiTaskManage;
import io.dataease.api.ai.task.AiTaskApi;
import io.dataease.api.ai.task.AiTaskDtos.*;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class AiTaskServer implements AiTaskApi {
    private final AiTaskManage manage;
    public AiTaskServer(AiTaskManage manage) { this.manage = manage; }
    public Task submit(Submit request) { return manage.submit(request); }
    public List<Task> active() { return manage.active(); }
    public List<Notice> notices() { return manage.notices(); }
    public void acknowledge(Long attemptId) { manage.acknowledge(attemptId); }
    public Task conversation(Long id, Origin origin) { return manage.conversation(id, origin); }
    public Task get(Long id) { return manage.get(id); }
    public Task cancel(Long id) { return manage.cancel(id); }
    public Task retry(Long id, Retry request) { return manage.retry(id, request); }
}

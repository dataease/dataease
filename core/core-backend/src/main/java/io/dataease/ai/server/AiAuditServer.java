package io.dataease.ai.server;

import io.dataease.ai.manage.AiAuditManage;
import io.dataease.api.ai.audit.AiAuditApi;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiAuditServer implements AiAuditApi {
    private final AiAuditManage audit;
    public AiAuditServer(AiAuditManage audit) { this.audit = audit; }
    public Detail get(String executionId) { return audit.get(executionId); }
}

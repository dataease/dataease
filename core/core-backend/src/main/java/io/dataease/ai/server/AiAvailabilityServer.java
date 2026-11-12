package io.dataease.ai.server;

import io.dataease.ai.manage.AiAccess;
import io.dataease.ai.manage.AiRuntimeManage;
import io.dataease.ai.support.AiErrors;
import io.dataease.api.ai.conversation.AiAvailabilityApi;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiAvailabilityServer implements AiAvailabilityApi {
    private final AiAccess access;
    private final AiRuntimeManage runtime;
    public AiAvailabilityServer(AiAccess access, AiRuntimeManage runtime) { this.access = access; this.runtime = runtime; }
    public Availability availability() {
        try { return new Availability(true, runtime.usageRevision(access.capture())); }
        catch (AiErrors.Failure unavailable) { return new Availability(false, null); }
    }
}

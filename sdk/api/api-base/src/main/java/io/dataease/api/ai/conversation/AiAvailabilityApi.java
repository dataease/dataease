package io.dataease.api.ai.conversation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Authenticated usage visibility, independent from administrator configuration/audit access. */
@RequestMapping("/ai/availability")
public interface AiAvailabilityApi {
    record Availability(boolean available, String revision) { }
    @GetMapping Availability availability();
}

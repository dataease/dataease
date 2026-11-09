package io.dataease.api.ai.config;

import io.dataease.api.ai.config.AiConfigDtos.*;
import jakarta.validation.Valid;
import io.dataease.auth.ConfigurationSecurity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@io.dataease.auth.DeApiPath(value = "/ai", rt = io.dataease.constant.AuthResourceEnum.AI_CONFIG)
public interface AiConfigurationApi {
    @io.dataease.auth.DePermit("m:read")
    @GetMapping("/settings") Settings settings();
    @io.dataease.auth.DePermit("m:read")
    @PostMapping("/settings") Settings saveSettings(@Valid @RequestBody SettingsChange change);
    @io.dataease.auth.DePermit("m:read")
    @GetMapping("/settings/impact") Impact settingsImpact(@RequestParam(required = false) Long defaultModelId, @RequestParam boolean enabled);
    @io.dataease.auth.DePermit("m:read")
    @GetMapping("/models") Page<Model> models(@RequestParam(defaultValue = "") String search, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size);
    @io.dataease.auth.DePermit("m:read")
    @GetMapping("/models/{id}") Model model(@PathVariable Long id);
    @io.dataease.auth.DePermit("m:read")
    @PostMapping("/models/drafts") Draft draft();
    @ConfigurationSecurity(encryptedRequest = true)
    @io.dataease.auth.DePermit("m:read")
    @PostMapping("/models/discover") List<String> discover(@Valid @RequestBody Candidate candidate);
    @ConfigurationSecurity(encryptedRequest = true)
    @io.dataease.auth.DePermit("m:read")
    @PostMapping("/models/test") TestResult test(@Valid @RequestBody Candidate candidate);
    @io.dataease.auth.DePermit("m:read")
    @PostMapping("/models/{id}/test") TestResult testSaved(@PathVariable Long id);
    @ConfigurationSecurity(encryptedRequest = true)
    @io.dataease.auth.DePermit("m:read")
    @PostMapping("/models") Model create(@Valid @RequestBody Candidate candidate);
    @ConfigurationSecurity(encryptedRequest = true)
    @io.dataease.auth.DePermit("m:read")
    @PostMapping("/models/{id}") Model edit(@PathVariable Long id, @Valid @RequestBody Candidate candidate);
    @io.dataease.auth.DePermit("m:read")
    @GetMapping("/models/{id}/impact") Impact impact(@PathVariable Long id, @RequestParam String action);
    @io.dataease.auth.DePermit("m:read")
    @PostMapping("/models/{id}/enabled") void enabled(@PathVariable Long id, @Valid @RequestBody ModelAction action);
    @io.dataease.auth.DePermit("m:read")
    @DeleteMapping("/models/{id}") void delete(@PathVariable Long id, @Valid @RequestBody ModelAction action);
    @io.dataease.auth.DePermit("m:read")
    @GetMapping("/org-policies") OrgPage organizations(@RequestParam(defaultValue = "") String search);
    @io.dataease.auth.DePermit("m:read")
    @PostMapping("/org-policies/selection") List<Org> selection(@RequestBody String search);
    @io.dataease.auth.DePermit("m:read")
    @PostMapping("/org-policies/batch") void batch(@Valid @RequestBody OrgBatch batch);
}

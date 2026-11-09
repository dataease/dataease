package io.dataease.ai.server;

import io.dataease.api.ai.config.AiConfigurationApi;
import io.dataease.auth.ConfigurationSecurity;
import io.dataease.api.ai.config.AiConfigDtos.*;
import io.dataease.ai.manage.*;
import io.dataease.ai.support.AiErrors;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/ai")
public class AiConfigurationServer implements AiConfigurationApi {
    private final AiConfigurationManage manage;
    private final AiModelTestManage tests;
    public AiConfigurationServer(AiConfigurationManage manage, AiModelTestManage tests) { this.manage = manage; this.tests = tests; }
    public Settings settings() { return manage.querySettings(); }
    public Settings saveSettings(SettingsChange change) { return manage.saveSettings(change); }
    public Impact settingsImpact(Long defaultModelId, boolean enabled) { return manage.impact("SETTINGS", "" + defaultModelId + ":" + enabled); }
    public Page<Model> models(String search, int page, int size) { return manage.list(search, page, size); }
    public Model model(Long id) { return manage.detail(id); }
    public Draft draft() { return manage.draft(); }
    @ConfigurationSecurity(encryptedRequest = true)
    public List<String> discover(Candidate c) { return tests.discover(c); }
    @ConfigurationSecurity(encryptedRequest = true)
    public TestResult test(Candidate c) { return tests.test(c, false); }
    public TestResult testSaved(Long id) {
        Model m = manage.detail(id); Candidate c = new Candidate();
        c.setId(id); c.setExpectedVersion(m.rowVersion()); c.setExpectedConfigRevision(m.configRevision());
        c.setProvider(m.provider()); c.setBaseUrl(m.baseUrl()); c.setModelIdentifier(m.modelIdentifier());
        return tests.test(c, true);
    }
    @ConfigurationSecurity(encryptedRequest = true)
    public Model create(Candidate c) { return manage.save(c, true); }
    @ConfigurationSecurity(encryptedRequest = true)
    public Model edit(Long id, Candidate c) { if (!Objects.equals(id, c.getId())) throw AiErrors.error("invalid"); return manage.save(c, false); }
    public Impact impact(Long id, String action) {
        if (!Set.of("DELETE", "DISABLE").contains(action)) throw AiErrors.error("invalid");
        return manage.impact(action, "" + id);
    }
    public void enabled(Long id, ModelAction action) { manage.action(id, action, false); }
    public void delete(Long id, ModelAction action) { manage.action(id, action, true); }
    public OrgPage organizations(String search) { return manage.organizations(search); }
    public List<Org> selection(String search) { return manage.organizations(search).items(); }
    public void batch(OrgBatch batch) { manage.batch(batch); }
}

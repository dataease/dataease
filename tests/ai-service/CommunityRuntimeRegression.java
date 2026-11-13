package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.dao.entity.*;
import io.dataease.ai.dao.repository.*;
import io.dataease.ai.manage.*;
import io.dataease.ai.support.*;
import io.dataease.api.ai.config.AiConfigDtos.*;
import io.dataease.api.ai.runtime.*;
import io.dataease.license.bo.F2CLicResult;
import jakarta.persistence.EntityManager;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public final class CommunityRuntimeRegression {
    public static void run(AiConfigurationManage configuration, CoreAiModelRepository models,
                           CoreAiOrgPolicyRepository policies, AiSecrets keys, AiAdmission admission,
                           EntityManager em, PlatformTransactionManager manager, AiAccess originalAccess) throws Exception {
        ModeRegression.Fixture fixture = new ModeRegression.Fixture();
        var field = AiConfigurationManage.class.getDeclaredField("access"); field.setAccessible(true);
        field.set(configuration, fixture.access);
        var tx = new TransactionTemplate(manager);
        Long defaultId = configuration.setting().getDefaultModelId();
        CoreAiModel historic = models.findAll().stream().filter(m -> m.getDeletedAt() == null && !m.getId().equals(defaultId)).findFirst().orElseThrow();
        tx.executeWithoutResult(status -> {
            var p = new CoreAiOrgPolicy(); p.setOrgId(9001L); p.setMode("SPECIFIED"); p.setModelId(historic.getId()); em.persist(p);
            var disabled = new CoreAiOrgPolicy(); disabled.setOrgId(9002L); disabled.setMode("DISABLED"); em.persist(disabled);
        });
        var calls = new AtomicInteger(); var inputs = new AtomicInteger(); var taskOrg = new AtomicReference<Long>();
        AiFeature feature = new AiFeature() {
            public String code() { return "community-runtime"; }
            public void authorize(AiExecutionContext c) { }
            public void authorizeInput(AiExecutionContext c, Prompt p) { inputs.incrementAndGet(); }
            public TaskIdentity taskIdentity(String taskId) { return new TaskIdentity(1L, taskOrg.get(), taskOrg.get(), "task-v1"); }
        };
        var transport = new AiTransport(new AiOutboundPolicy(), admission, new ObjectMapper()) {
            public ChatResponse call(AiConfigurationManage.Connection c, CoreAiSetting s, Prompt p, boolean background, long deadline, Runnable check) {
                check.run(); if (!Objects.equals(c.id(), defaultId)) throw new AssertionError("community must use global default");
                calls.incrementAndGet(); return new ChatResponse(List.of(new Generation(new AssistantMessage("OK"))));
            }
        };
        var provider = new AiRuntimeManage(new StaticListableBeanFactory(Map.of("feature", feature)).getBeanProvider(AiFeature.class), configuration, policies, keys, transport, new ObjectMapper(), fixture.access);
        var previous = RequestContextHolder.getRequestAttributes();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        try {
            var scope = new AiDataScope("community-resource", "business-version", null, false, null);
            fixture.status.set(F2CLicResult.Status.valid);
            ChatModel licensed = provider.current(provider.current(feature.code(), scope));
            taskOrg.set(9002L);
            ModeRegression.denied(() -> provider.task(feature.code(), "licensed-task", scope), "licensed disabled organization is refused at admission");
            ModeRegression.require(calls.get() == 0, "disabled organization never sends a model request");
            fixture.status.set(F2CLicResult.Status.no_record);
            ModeRegression.denied(() -> licensed.call("hello"), "old licensed runtime cannot become community");
            ModeRegression.denied(() -> provider.task(feature.code(), "old-org-task", scope), "community task cannot erase organization/proxy");
            taskOrg.set(null);
            var context = provider.current(feature.code(), scope);
            ModeRegression.require(context.organizationId() == null && context.proxyOrganizationId() == null, "community context has no synthetic organization");
            ChatModel community = provider.current(context);
            ModeRegression.require("OK".equals(community.call("hello")) && inputs.get() > 0, "global model and final input checks retained");
            ModeRegression.require(!configuration.querySettings().organizationPolicyAvailable() && !configuration.organizations("").available(), "community capabilities and directory");
            ModeRegression.denied(() -> configuration.batch(new OrgBatch(List.of(new ExpectedOrg(9001L, null)), "DISABLED", null)), "community organization writes rejected");
            var view = configuration.detail(historic.getId());
            ModeRegression.require(view.organizationReferences() == 0 && view.historicalOrganizationReferences() == 1 && view.organizationNames().isEmpty(), "frozen and effective references separated");
            ModeRegression.denied(() -> configuration.action(historic.getId(), new ModelAction(view.rowVersion(), null, null), true), "historical reference still protects model deletion");
            ModeRegression.require(configuration.impact("DISABLE", defaultId.toString()).organizationCount() == 0, "community impact ignores frozen organizations");
            var draft = configuration.draft();
            var candidate = new Candidate(); candidate.setId(draft.id()); candidate.setDraftHandle(draft.draftHandle()); candidate.setProvider("OPENAI_COMPATIBLE"); candidate.setName("Synthetic mode draft"); candidate.setBaseUrl("https://new.invalid/v1"); candidate.setModelIdentifier("synthetic"); candidate.setApiKey("synthetic-key");
            String impact = configuration.impact("SETTINGS", "null:true").token();
            var existing = new Candidate(); existing.setId(historic.getId()); existing.setExpectedVersion(view.rowVersion()); existing.setExpectedConfigRevision(view.configRevision()); existing.setName(view.name()); existing.setProvider(view.provider()); existing.setBaseUrl("https://changed.invalid/v1"); existing.setModelIdentifier(view.modelIdentifier());
            existing.setTestProof(configuration.testProof(configuration.candidate(existing, true)));
            fixture.record.setUpdateTime(2L);
            ModeRegression.denied(() -> community.call("hello"), "same-mode renewal rejects previous runtime");
            ModeRegression.denied(() -> configuration.candidate(candidate, true), "draft binds deployment revision");
            ModeRegression.denied(() -> configuration.save(existing, false), "test proof binds deployment revision");
            var s = configuration.setting();
            ModeRegression.denied(() -> configuration.saveSettings(new SettingsChange(s.getRowVersion(), true, null, false, 60, 30000, 4096, 5, 2, impact)), "impact binds deployment revision");
            ModeRegression.require(calls.get() == 1 && models.findById(historic.getId()).orElseThrow().getDeletedAt() == null, "rejected paths send and delete nothing");
            fixture.status.set(F2CLicResult.Status.valid);
            ModeRegression.require(configuration.querySettings().organizationPolicyAvailable() && configuration.detail(historic.getId()).organizationReferences() == 1, "licensed mode restores existing strategy references");
            System.out.println("PASS AI community global runtime, frozen strategy protection, organization write rejection and mode-bound contexts/proofs");
        } finally {
            RequestContextHolder.setRequestAttributes(previous); transport.close(); field.set(configuration, originalAccess);
            tx.executeWithoutResult(status -> { em.createQuery("delete from CoreAiOrgPolicy p where p.orgId in (9001,9002)").executeUpdate(); });
        }
    }
}

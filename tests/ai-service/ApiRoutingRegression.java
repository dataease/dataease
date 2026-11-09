package io.dataease.ai;

import io.dataease.ai.manage.AiConfigurationManage;
import io.dataease.ai.server.AiConfigurationServer;
import io.dataease.api.ai.config.AiConfigDtos.*;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public final class ApiRoutingRegression {
    public static void run() throws Exception {
        AtomicInteger actions = new AtomicInteger(), edits = new AtomicInteger(), settings = new AtomicInteger();
        var manage = new AiConfigurationManage(new DataSourceTransactionManager()) {
            @Override public void action(Long id, ModelAction action, boolean delete) {
                if (id != 1L || delete || action.enabled() || !"proof".equals(action.impactToken())) throw new AssertionError("action binding");
                actions.incrementAndGet();
            }
            @Override public Model save(Candidate candidate, boolean create) {
                if (create || candidate.getId() != 1L) throw new AssertionError("edit binding");
                edits.incrementAndGet();return null;
            }
            @Override public Settings saveSettings(SettingsChange change) { settings.incrementAndGet();return null; }
        };
        var mvc = MockMvcBuilders.standaloneSetup(new AiConfigurationServer(manage, null)).build();
        String action = "{\"expectedVersion\":0,\"enabled\":false,\"impactToken\":\"proof\"}";
        if (mvc.perform(post("/ai/models/1/enabled").contentType(MediaType.APPLICATION_JSON).content(action)).andReturn().getResponse().getStatus() != 200) throw new AssertionError("disable POST routing");
        if (mvc.perform(post("/ai/models/1/enabled").contentType(MediaType.APPLICATION_JSON).content("{}")).andReturn().getResponse().getStatus() != 400 || actions.get() != 1) throw new AssertionError("action validation bypassed");
        String edit = "{\"id\":1,\"name\":\"model\",\"provider\":\"OPENAI_COMPATIBLE\",\"baseUrl\":\"https://model.invalid/v1\",\"modelIdentifier\":\"chat\"}";
        if (mvc.perform(post("/ai/models/1").contentType(MediaType.APPLICATION_JSON).content(edit)).andReturn().getResponse().getStatus() != 200 || edits.get() != 1) throw new AssertionError("edit POST routing");
        String setting = "{\"expectedVersion\":0,\"enabled\":true,\"allowSamples\":false,\"requestTimeoutSeconds\":60,\"maxInputChars\":30000,\"maxOutputTokens\":4096,\"interactiveConcurrency\":5,\"backgroundConcurrency\":2}";
        if (mvc.perform(post("/ai/settings").contentType(MediaType.APPLICATION_JSON).content(setting)).andReturn().getResponse().getStatus() != 200 || settings.get() != 1) throw new AssertionError("settings POST routing");
        if (mvc.perform(put("/ai/models/1/enabled").contentType(MediaType.APPLICATION_JSON).content(action)).andReturn().getResponse().getStatus() != 405 || actions.get() != 1) throw new AssertionError("unsupported method reached business logic");
        System.out.println("PASS AI actual MVC POST routes for disable/edit/settings, body validation and unsupported-method rejection (no product database)");
    }
}

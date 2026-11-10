package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import io.dataease.ai.dao.repository.CoreAiOrgPolicyRepository;
import io.dataease.ai.manage.*;
import io.dataease.ai.server.AiConversationServer;
import io.dataease.ai.support.*;
import io.dataease.api.ai.config.AiConfigDtos.*;
import io.dataease.api.ai.runtime.AiFeature;
import io.dataease.permission.util.V3UserUtil;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

/** Public dialogue HTTP + real controlled model HTTP; only the business scene is synthetic. */
public final class ConversationRegression {
    private static void deny(MockMvc mvc, String path, String version, String text) throws Exception {
        try {
            var response = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsBytes(Map.of("contextVersion", version, "text", text))))
                    .andReturn().getResponse();
            if (response.getStatus() >= 400) return;
        } catch (jakarta.servlet.ServletException failure) {
            if (failure.getCause() instanceof AiErrors.Failure) return;
            throw failure;
        }
        throw new AssertionError("denied conversation returned a complete answer");
    }
    public static void run(AiConfigurationManage configuration, CoreAiOrgPolicyRepository policies,
                           AiSecrets keys, AiAdmission admission, AiAccess access) throws Exception {
        var json = new ObjectMapper();
        var sent = new ArrayList<com.fasterxml.jackson.databind.JsonNode>();
        var allowed = new AtomicBoolean(true);
        var revokeDuringCall = new AtomicBoolean();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            synchronized (sent) { sent.add(json.readTree(exchange.getRequestBody())); }
            if (revokeDuringCall.get()) allowed.set(false);
            byte[] bytes = "{\"id\":\"synthetic\",\"object\":\"chat.completion\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"Remembered context\"},\"finish_reason\":\"stop\"}]}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try { exchange.getResponseBody().write(bytes); } finally { exchange.close(); }
        });
        server.start();
        var transport = new AiTransport(new AiOutboundPolicy(), admission, json);
        Long previous = configuration.setting().getDefaultModelId();
        try {
            var draft = configuration.draft(); var candidate = new Candidate();
            candidate.setId(draft.id()); candidate.setDraftHandle(draft.draftHandle());
            candidate.setName("Dialogue synthetic"); candidate.setProvider("OPENAI_COMPATIBLE");
            candidate.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
            candidate.setModelIdentifier("general-chat"); candidate.setApiKey("synthetic-key");
            candidate.setTestProof(configuration.testProof(configuration.candidate(candidate, true)));
            var saved = configuration.save(candidate, true); var s = configuration.setting();
            configuration.saveSettings(new SettingsChange(s.getRowVersion(), true, saved.id(), false, 60, 30000, 4096, 5, 2, configuration.impact("SETTINGS", saved.id() + ":true").token()));
            AiConversationScene scene = new AiConversationScene() {
                public String code() { return "sql"; }
                public List<Resource> resources() { return List.of(new Resource("11", "Synthetic database")); }
                public Context resolve(String id, String sql) { return new Context(id, "Synthetic database", "v1", "SQL input: " + sql); }
                public void authorize(Context c) { if (!allowed.get() || !Objects.equals(V3UserUtil.getUid(), 1L) || !"11".equals(c.resourceId())) throw AiErrors.error("forbidden"); }
            };
            var dialogueAccess = new AiAccess(null, null) {
                public AiDeploymentMode.State mode() { return access.mode(); }
                public Identity capture() {
                    Long proxy = V3UserUtil.getProxy().isProxy() ? V3UserUtil.getProxy().getProxyOid() : null;
                    return new Identity(V3UserUtil.getUid(), proxy, proxy, access.mode());
                }
                public void verify(Identity identity, boolean configuration) { access.verify(identity, configuration); }
            };
            var beans = new StaticListableBeanFactory(); beans.addBean("scene", scene);
            var manage = new AiConversationManage(beans.getBeanProvider(AiConversationScene.class), beans.getBeanProvider(AiRuntimeManage.class), dialogueAccess);
            beans.addBean("conversation", manage);
            var runtime = new AiRuntimeManage(beans.getBeanProvider(AiFeature.class), configuration, policies, keys, transport, json, dialogueAccess);
            beans.addBean("runtime", runtime);
            var mvc = MockMvcBuilders.standaloneSetup(new AiConversationServer(manage)).build();
            var open = mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"scene\":\"sql\",\"resourceId\":\"11\",\"editorId\":\"editor-a\",\"sql\":\"select 1\"}"))
                    .andReturn().getResponse();
            if (open.getStatus() != 200) throw new AssertionError("conversation opens through HTTP");
            var session = json.readTree(open.getContentAsString());
            String path = "/ai/conversations/" + session.path("sessionId").asText() + "/turns";
            String version = session.path("contextVersion").asText();
            for (String question : List.of("First question", "Follow up")) {
                var answer = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("contextVersion", version, "text", question))))
                        .andReturn().getResponse();
                if (answer.getStatus() != 200 || !answer.getContentAsString().contains("Remembered context")) throw new AssertionError("dialogue HTTP response");
            }
            if (sent.size() != 2 || sent.get(1).path("messages").size() != 4
                    || !sent.get(1).path("messages").get(2).path("content").asText().equals("Remembered context")
                    || !sent.get(1).path("tools").isMissingNode() && !sent.get(1).path("tools").isEmpty()) throw new AssertionError("follow-up carries trusted context and completed history without tools");
            // Final aggregate input includes the existing conversation; a failed turn is never remembered.
            int before = sent.size();
            deny(mvc, path, version, "x".repeat(29990));
            deny(mvc, path, "forged-version", "forged context");
            mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsBytes(Map.of("contextVersion", version, "text", "Valid after rejected turn"))));
            if (sent.size() != before + 1 || sent.get(sent.size() - 1).path("messages").size() != 6)
                throw new AssertionError("failed turn entered completed history");
            before = sent.size();
            V3UserUtil.setUid(2L);
            deny(mvc, path, version, "another user");
            V3UserUtil.clear(); V3UserUtil.setUid(1L); V3UserUtil.setProxy(9001L);
            deny(mvc, path, version, "another workspace");
            V3UserUtil.clear(); V3UserUtil.setUid(1L);
            if (sent.size() != before) throw new AssertionError("denial or budget reached upstream");
            allowed.set(false); deny(mvc, path, version, "revoked"); allowed.set(true);
            deny(mvc, path, version, "revoked history cannot resume");
            if (sent.size() != before) throw new AssertionError("revoked history reused");
            var second = mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"scene\":\"sql\",\"resourceId\":\"11\",\"editorId\":\"editor-b\",\"sql\":\"select 2\"}"))
                    .andReturn().getResponse();
            var other = json.readTree(second.getContentAsString());
            String otherPath = "/ai/conversations/" + other.path("sessionId").asText() + "/turns";
            String otherVersion = other.path("contextVersion").asText();
            mvc.perform(post(otherPath).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(Map.of("contextVersion", otherVersion, "text", "Independent editor"))));
            if (sent.get(sent.size() - 1).path("messages").size() != 2) throw new AssertionError("editors share history");
            revokeDuringCall.set(true);
            deny(mvc, otherPath, otherVersion, "Revoke before delivery");
            allowed.set(true); revokeDuringCall.set(false);
            deny(mvc, otherPath, otherVersion, "late history cannot resume");
            // Closing a context removes its history and prevents later delivery/reuse.
            var third = mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"scene\":\"sql\",\"resourceId\":\"11\",\"editorId\":\"editor-c\",\"sql\":\"select 3\"}"))
                    .andReturn().getResponse();
            var closed = json.readTree(third.getContentAsString());
            String closedId = closed.path("sessionId").asText();
            if (mvc.perform(delete("/ai/conversations/" + closedId)).andReturn().getResponse().getStatus() != 200)
                throw new AssertionError("context close HTTP");
            deny(mvc, "/ai/conversations/" + closedId + "/turns", closed.path("contextVersion").asText(), "closed history");
            manage.destroy();
            System.out.println("PASS conversation HTTP multiround history, editor/user/workspace isolation, forged context, aggregate input budget and revocation before call/delivery through real model transport");
        } finally {
            var s = configuration.setting();
            configuration.saveSettings(new SettingsChange(s.getRowVersion(), true, previous, false, 60, 30000, 4096, 5, 2, configuration.impact("SETTINGS", previous + ":true").token()));
            transport.close(); server.stop(0);
        }
    }
}

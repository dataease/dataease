package io.dataease.ai;

import com.fasterxml.jackson.databind.*;
import com.sun.net.httpserver.HttpServer;
import io.dataease.ai.dao.repository.CoreAiOrgPolicyRepository;
import io.dataease.ai.manage.*;
import io.dataease.ai.server.AiConversationServer;
import io.dataease.ai.support.*;
import io.dataease.api.ai.config.AiConfigDtos.*;
import io.dataease.api.ai.runtime.AiFeature;
import io.dataease.api.ds.DatasourceApi;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.extensions.datasource.dto.*;
import io.dataease.model.BusiNodeVO;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.system.manage.CorePermissionManage;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.constant.AuthEnum;
import io.dataease.constant.BusiResourceEnum;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/** Public dialogue -> controlled model HTTP -> actual SQL scene -> existing DE metadata API seam. */
public final class ToolConversationRegression {
    static final ObjectMapper JSON = new ObjectMapper();
    static JsonNode open(MockMvc mvc) throws Exception {
        return JSON.readTree(mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                .content("{\"scene\":\"sql\",\"resourceId\":\"11\",\"editorId\":\"tools\",\"sql\":\"SELECT ID FROM AI_PROBE\"}"))
                .andReturn().getResponse().getContentAsString());
    }
    static String start(MockMvc mvc, JsonNode session, String text) throws Exception {
        String base = "/ai/conversations/" + session.path("sessionId").asText() + "/executions";
        var run = JSON.readTree(mvc.perform(post(base).contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsBytes(Map.of("contextVersion", session.path("contextVersion").asText(), "text", text))))
                .andReturn().getResponse().getContentAsString());
        return base + "/" + run.path("executionId").asText();
    }
    static JsonNode terminal(MockMvc mvc, String run) throws Exception {
        for (int attempt = 0; attempt < 400; attempt++) {
            var result = JSON.readTree(mvc.perform(get(run + "/events").param("after", "0")).andReturn().getResponse().getContentAsString());
            if (!result.path("status").asText().equals("RUNNING")) return result;
            Thread.sleep(20);
        }
        throw new AssertionError("bounded tool run did not terminate");
    }
    static String frame(Object delta, String finish) throws Exception {
        return "data: " + JSON.writeValueAsString(Map.of("choices", List.of(Map.of("index", 0, "delta", delta, "finish_reason", finish)))) + "\n\n";
    }
    static String call(String id, String name, String arguments) throws Exception {
        return frame(Map.of("tool_calls", List.of(Map.of("index", 0, "id", id, "type", "function", "function", Map.of("name", name, "arguments", arguments)))), "tool_calls");
    }
    static void denied(MockMvc mvc, String run) throws Exception {
        try {
            if (mvc.perform(get(run + "/events").param("after", "0")).andReturn().getResponse().getStatus() >= 400) return;
        } catch (jakarta.servlet.ServletException failure) { if (failure.getCause() instanceof AiErrors.Failure) return; throw failure; }
        throw new AssertionError("revoked tool events/history must be inaccessible");
    }
    public static void run(AiConfigurationManage config, CoreAiOrgPolicyRepository policies, AiSecrets keys, AiAdmission admission, AiAccess access) throws Exception {
        var sent = new CopyOnWriteArrayList<JsonNode>();
        var reads = new CopyOnWriteArrayList<String>();
        var mode = new AtomicReference<String>("tables");
        var operationAllowed = new AtomicBoolean(true);
        var sourceRevision = new AtomicLong(1);
        var toolEntered = new AtomicReference<CountDownLatch>(new CountDownLatch(1));
        var toolRelease = new AtomicReference<CountDownLatch>(new CountDownLatch(1));
        var transientMetadata = new AtomicInteger();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var providerWorkers = Executors.newFixedThreadPool(4); server.setExecutor(providerWorkers);
        server.createContext("/v1/chat/completions", exchange -> {
            try {
                var body = JSON.readTree(exchange.getRequestBody()); sent.add(body);
                String content;
                if (mode.get().equals("arguments-correction") && sent.size() <= 2) {
                    content = call("args-" + sent.size(), "get_table_fields", sent.size() == 1 ? "{\"table\":\"\"}" : "{\"table\":\"AI_PROBE\"}");
                } else if (mode.get().equals("general")) {
                    content = frame(Map.of("content", "SELECT reads data; choose a datasource for schema-specific SQL."), "stop");
                } else if (mode.get().equals("suggest") && sent.size() == 1) {
                    content = call("proposal", "propose_sql", "{\"sql\":\"SELECT ID FROM AI_PROBE\"}");
                } else if (Set.of("validate-stop", "validate-revoke").contains(mode.get()) && sent.size() == 1) {
                    content = call("validation-boundary", "validate_sql", "{\"sql\":\"SELECT ID FROM AI_PROBE\"}");
                } else if (mode.get().equals("correction") && sent.size() <= 2) {
                    content = call("validate-" + sent.size(), "validate_sql", JSON.writeValueAsString(Map.of("sql", sent.size() == 1 ? "SELECT TYPO FROM AI_PROBE" : "SELECT ID FROM AI_PROBE")));
                } else if (Set.of("chain", "chain-retry").contains(mode.get()) && sent.size() <= 4) {
                    content = call("chain-" + sent.size(), "get_table_fields", "{\"table\":\"AI_PROBE" + (sent.size() == 1 ? "" : sent.size()) + "\"}");
                } else if ((sent.size() == 1 || Set.of("extra", "duplicate", "trailing").contains(mode.get())) && Set.of("unknown", "extra", "duplicate", "trailing", "outside", "batch", "index", "conflict", "length", "negative-usage", "tool-failure", "missing-id", "private-limit", "usage-allocation").contains(mode.get())) {
                    content = switch (mode.get()) {
                        case "private-limit" -> frame(Map.of("reasoning", "private-budget-sentinel".repeat(1000)), "stop");
                        case "usage-allocation" -> call("usage-allocation", "list_tables", "{}") + "data: {\"choices\":[],\"usage\":{\"completion_tokens\":820}}\n\n";
                        case "missing-id" -> frame(Map.of("content", "Malformed call cannot become a final answer", "tool_calls", List.of(Map.of("index", 0, "type", "function", "function", Map.of("name", "list_tables", "arguments", "{}")))), "stop");
                        case "unknown" -> call("unknown", "execute_sql", "{}");
                        case "extra" -> call("extra", "list_tables", "{\"datasourceId\":\"22\"}");
                        case "duplicate" -> call("duplicate", "get_table_fields", "{\"table\":\"AI_PROBE\",\"table\":\"OTHER\"}");
                        case "trailing" -> call("trailing", "list_tables", "{} {}");
                        case "outside" -> call("outside", "get_table_fields", "{\"table\":\"OTHER\"}");
                        case "index" -> frame(Map.of("tool_calls", List.of(Map.of("index", 1, "id", "bad-index", "type", "function", "function", Map.of("name", "list_tables", "arguments", "{}")))), "tool_calls");
                        case "batch" -> frame(Map.of("tool_calls", List.of(Map.of("index", 0), Map.of("index", 1))), "tool_calls");
                        case "conflict" -> call("first", "list_tables", "{}").replace("\"finish_reason\":\"tool_calls\"", "\"finish_reason\":null") + call("second", "list_tables", "{}");
                        case "length" -> call("length", "list_tables", "{}").replace("\"finish_reason\":\"tool_calls\"", "\"finish_reason\":\"length\"");
                        case "negative-usage" -> call("usage", "list_tables", "{}") + "data: {\"choices\":[],\"usage\":{\"completion_tokens\":-1}}\n\n";
                        default -> call("failure", "list_tables", "{}");
                    };
                } else if (mode.get().equals("repeat")) {
                    content = call("repeat-" + sent.size(), "list_tables", "{}");
                } else if (sent.size() == 1 && Set.of("fields", "input-results").contains(mode.get())) {
                    content = frame(Map.of("tool_calls", List.of(Map.of("index", 0, "id", "call-fields", "type", "function", "function", Map.of("name", "get_table_fields", "arguments", "{\"ta")))), "")
                            .replace("\"finish_reason\":\"\"", "\"finish_reason\":null")
                            + frame(Map.of("tool_calls", List.of(Map.of("index", 0, "function", Map.of("arguments", "ble\":\"AI_PROBE\"}")))), "tool_calls");
                } else if (sent.size() == 1) {
                    content = frame(Map.of("tool_calls", List.of(Map.of("index", 0, "id", "call-tables", "type", "function", "function", Map.of("name", "list_tables", "arguments", "{}")))), "tool_calls");
                } else content = frame(Map.of("content", mode.get().equals("framed-tools") ? "多字节工具结果" : "AI_PROBE metadata received"), "stop");
                if (sent.size() == 1 && mode.get().equals("tables")) content = "data: {\"choices\":[{\"index\":0,\"delta\":{\"reasoning\":\"private-tool-only-sentinel\"}}]}\n\n" + content;
                content += "data: [DONE]\n\n";
                if (mode.get().equals("framed-tools")) {
                    content = ": comment\r\n" + content.replace("data: {\"choices\":", "data: {\n" + "data: \"choices\":").replace("\n", "\r\n");
                }
                byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/event-stream"); exchange.sendResponseHeaders(200, bytes.length);
                if (mode.get().equals("framed-tools")) for (byte value : bytes) { exchange.getResponseBody().write(value); exchange.getResponseBody().flush(); }
                else exchange.getResponseBody().write(bytes);
            } catch (Exception error) { throw new RuntimeException(error); }
            finally { exchange.close(); }
        }); server.start();
        var datasource = (DatasourceApi) Proxy.newProxyInstance(ToolConversationRegression.class.getClassLoader(), new Class<?>[]{DatasourceApi.class}, (proxy, method, args) -> {
            if (method.getName().equals("tree") && (!Objects.equals(V3UserUtil.getUid(), 1L) || V3UserUtil.getProxy().isProxy())) return List.of();
            if (!Objects.equals(V3UserUtil.getUid(), 1L)) throw new AssertionError("metadata lost trusted actor");
            return switch (method.getName()) {
                case "tree" -> { var node = new BusiNodeVO(); node.setId(11L); node.setLeaf(true); node.setName("Probe"); yield List.of(node); }
                case "getSimpleDs" -> { var ds = new DatasourceDTO(); ds.setId(11L); ds.setName("Probe"); ds.setType("MySQL"); ds.setUpdateTime(sourceRevision.get()); yield ds; }
                case "getTables" -> { var request = (DatasetTableDTO) args[0]; if (!Objects.equals(request.getDatasourceId(), 11L) || request.getInfo() != null || request.getType() != null) throw new AssertionError("metadata request expanded scope"); reads.add("tables");
                    if (mode.get().equals("chain-retry") && transientMetadata.getAndUpdate(value -> Math.max(0, value - 1)) > 0) throw AiErrors.error("connection_failed");
                    if (mode.get().equals("tool-failure")) throw new IllegalStateException("connection-secret");
                    if (Set.of("slow", "validate-stop").contains(mode.get())) { toolEntered.get().countDown(); boolean done = false; while (!done) try { done = toolRelease.get().await(5, TimeUnit.SECONDS); } catch (InterruptedException ignored) { } }
                    if (Set.of("revoke", "validate-revoke").contains(mode.get())) operationAllowed.set(false);
                    var tables = new ArrayList<DatasetTableDTO>();
                    for (String tableName : Set.of("chain", "chain-retry").contains(mode.get()) ? List.of("AI_PROBE", "AI_PROBE2", "AI_PROBE3", "AI_PROBE4") : List.of("AI_PROBE")) {
                        var table = new DatasetTableDTO(); table.setTableName(tableName); table.setName("Probe table"); table.setInfo("connection-secret"); tables.add(table);
                    }
                    yield tables; }
                case "getTableField" -> {
                    var request = (DatasetTableFieldRequest) args[0];
                    if (!Objects.equals(request.getDatasourceId(), 11L) || !request.getTableName().matches("AI_PROBE[234]?")) throw new AssertionError("field scope expanded");
                    reads.add("fields");
                    if (mode.get().equals("input-results")) {
                        var fields = new ArrayList<TableField>();
                        for (int index = 0; index < 16; index++) { var value = new TableField(); value.setOriginName("ID" + index); value.setName("c".repeat(200)); value.setType("BIGINT"); fields.add(value); }
                        yield fields;
                    }
                    var field = new TableField(); field.setOriginName("ID"); field.setName("Identifier"); field.setType("BIGINT"); field.setValue(List.of("sample-secret")); yield List.of(field);
                }
                default -> throw new AssertionError("unexpected datasource operation " + method.getName());
            };
        });
        Long previous = config.setting().getDefaultModelId();
        var transport = new AiTransport(new AiOutboundPolicy(), admission, JSON);
        AiConversationManage manage = null;
        try {
            var draft = config.draft(); var candidate = new Candidate(); candidate.setId(draft.id()); candidate.setDraftHandle(draft.draftHandle());
            candidate.setName("Tool synthetic"); candidate.setProvider("OPENAI_COMPATIBLE"); candidate.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
            candidate.setModelIdentifier("tool-chat"); candidate.setApiKey("synthetic-key"); candidate.setTestProof(config.testProof(config.candidate(candidate, true)));
            var saved = config.save(candidate, true); var setting = config.setting();
            config.saveSettings(new SettingsChange(setting.getRowVersion(), true, saved.id(), false, 60, 30000, 4096, 5, 2, config.impact("SETTINGS", saved.id() + ":true").token()));
            var beans = new StaticListableBeanFactory();
            beans.addBean("permission", (InteractiveAuthApi) Proxy.newProxyInstance(ToolConversationRegression.class.getClassLoader(), new Class<?>[]{InteractiveAuthApi.class}, (proxy, method, args) -> {
                if (method.getName().equals("checkAuth")) {
                    var request = (BusiPerCheckDTO) args[0];
                    if (!operationAllowed.get() || !Objects.equals(request.getId(), 11L) || request.getBusiEnum() != BusiResourceEnum.DATASOURCE || request.getAuthEnum() != AuthEnum.READ) throw AiErrors.error("forbidden");
                    return null;
                }
                throw new AssertionError("unexpected operation authorization " + method.getName());
            }));
            var dialogueAccess = new AiAccess(null, null) {
                public AiDeploymentMode.State mode() { return access.mode(); }
                public Identity capture() { Long proxy = V3UserUtil.getProxy().isProxy() ? V3UserUtil.getProxy().getProxyOid() : null; return new Identity(V3UserUtil.getUid(), proxy, proxy, access.mode()); }
                public void verify(Identity identity, boolean configuration) { access.verify(identity, configuration); if (identity.actor() == null) throw AiErrors.error("forbidden"); }
            };
            var scene = new AiSqlConversationScene(datasource, beans.getBeanProvider(InteractiveAuthApi.class), dialogueAccess, new CorePermissionManage());
            beans.addBean("scene", scene);
            manage = new AiConversationManage(beans.getBeanProvider(AiConversationScene.class), beans.getBeanProvider(AiRuntimeManage.class), dialogueAccess); beans.addBean("conversation", manage);
            beans.addBean("runtime", new AiRuntimeManage(beans.getBeanProvider(AiFeature.class), config, policies, keys, transport, JSON, dialogueAccess));
            var mvc = MockMvcBuilders.standaloneSetup(new AiConversationServer(manage)).build();
            mode.set("arguments-correction");
            var correctionSession = open(mvc);
            var correctionResult = terminal(mvc, start(mvc, correctionSession, "Fix invalid tool arguments"));
            if (!correctionResult.path("status").asText().equals("COMPLETED") || sent.size() != 3
                    || !sent.get(1).toString().contains("INVALID") || !reads.equals(List.of("tables", "fields"))) throw new AssertionError("invalid registered arguments are corrected once without executing malformed input: " + correctionResult);
            mvc.perform(delete("/ai/conversations/" + correctionSession.path("sessionId").asText()));
            sent.clear(); reads.clear();
            mode.set("suggest");
            var suggestionSession = open(mvc);
            var suggestionRun = start(mvc, suggestionSession, "Suggest SQL");
            var suggestion = terminal(mvc, suggestionRun);
            if (!suggestion.path("result").path("type").asText().equals("SQL_SUGGESTION")
                    || !suggestion.path("result").path("sql").asText().equals("SELECT ID FROM AI_PROBE")
                    || suggestion.path("result").path("resourceVersion").asText().isBlank())
                throw new AssertionError("SQL suggestion must be a typed, statically checked result bound to the original resource revision: " + suggestion);
            var applicable = JSON.readTree(mvc.perform(post(suggestionRun + "/check-application").contentType(MediaType.APPLICATION_JSON)
                    .content(JSON.writeValueAsBytes(Map.of("contextVersion", suggestionSession.path("contextVersion").asText(),
                            "draftVersion", suggestionSession.path("draftVersion").asText())))).andReturn().getResponse().getContentAsString());
            if (!applicable.path("sql").asText().equals("SELECT ID FROM AI_PROBE")) throw new AssertionError("explicit application check must return the original suggestion");
            terminal(mvc, start(mvc, suggestionSession, "Explain the suggestion without changing the draft"));
            var stillApplicable = JSON.readTree(mvc.perform(post(suggestionRun + "/check-application").contentType(MediaType.APPLICATION_JSON)
                    .content(JSON.writeValueAsBytes(Map.of("contextVersion", suggestionSession.path("contextVersion").asText(), "draftVersion", suggestionSession.path("draftVersion").asText())))).andReturn().getResponse().getContentAsString());
            if (!stillApplicable.path("sql").asText().equals("SELECT ID FROM AI_PROBE")) throw new AssertionError("same-context follow-up must preserve the older applicable SQL suggestion");
            operationAllowed.set(false);
            boolean applicationDenied = false;
            try { applicationDenied = mvc.perform(post(suggestionRun + "/check-application").contentType(MediaType.APPLICATION_JSON)
                    .content(JSON.writeValueAsBytes(Map.of("contextVersion", suggestionSession.path("contextVersion").asText(), "draftVersion", suggestionSession.path("draftVersion").asText())))).andReturn().getResponse().getStatus() >= 400; }
            catch (jakarta.servlet.ServletException failure) { applicationDenied = failure.getCause() instanceof AiErrors.Failure; }
            if (!applicationDenied) throw new AssertionError("application must freshly reject revoked READ");
            operationAllowed.set(true); sent.clear(); reads.clear(); mode.set("suggest");
            var versionSession = open(mvc);
            var versionRun = start(mvc, versionSession, "Suggest for current revision");
            terminal(mvc, versionRun);
            sourceRevision.incrementAndGet();
            boolean versionDenied = false;
            try { versionDenied = mvc.perform(post(versionRun + "/check-application").contentType(MediaType.APPLICATION_JSON)
                    .content(JSON.writeValueAsBytes(Map.of("contextVersion", versionSession.path("contextVersion").asText(), "draftVersion", versionSession.path("draftVersion").asText())))).andReturn().getResponse().getStatus() >= 400; }
            catch (jakarta.servlet.ServletException failure) { versionDenied = failure.getCause() instanceof AiErrors.Failure; }
            if (!versionDenied) throw new AssertionError("application cannot overwrite a changed resource revision");
            sourceRevision.set(1); sent.clear(); reads.clear(); mode.set("general");
            // General explanations have no mandatory datasource and cannot acquire resource tools.
            var unboundResponse = mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"scene\":\"sql\",\"resourceId\":\"\",\"editorId\":\"general\",\"sql\":\"\"}"))
                    .andReturn().getResponse();
            if (unboundResponse.getStatus() != 200) throw new AssertionError("ordinary SQL explanation must not require a target: " + unboundResponse.getStatus());
            var unbound = JSON.readTree(unboundResponse.getContentAsString());
            var unboundResult = terminal(mvc, start(mvc, unbound, "Explain SELECT"));
            if (!unboundResult.path("status").asText().equals("COMPLETED") || !unboundResult.path("result").path("type").asText().equals("EXPLANATION") || !reads.isEmpty())
                throw new AssertionError("general explanation must complete without reading a resource: " + unboundResult);
            sent.clear(); mode.set("tables");
            unboundResult = terminal(mvc, start(mvc, unbound, "Try reading a resource without selection"));
            if (!unboundResult.path("status").asText().equals("FAILED") || !reads.isEmpty())
                throw new AssertionError("unbound scope must reject model attempts to acquire resource tools: " + unboundResult);
            mvc.perform(delete("/ai/conversations/" + unbound.path("sessionId").asText()));
            sent.clear(); reads.clear();
            // One-shot callers provide no conversation: actual registered tools/model still run.
            var accepted = mvc.perform(post("/ai/conversations/capabilities/sql.assist")
                    .contentType(MediaType.APPLICATION_JSON).content("{\"scene\":\"sql\",\"resourceId\":\"11\",\"editorId\":\"button\",\"sql\":\"SELECT ID FROM AI_PROBE\",\"text\":\"Read tables\"}"))
                    .andReturn().getResponse();
            if (accepted.getStatus() != 200) throw new AssertionError("registered SQL capability must accept a button without a conversation: " + accepted.getStatus());
            var oneShot = JSON.readTree(accepted.getContentAsString());
            var oneShotResult = terminal(mvc, "/ai/conversations/" + oneShot.path("sessionId").asText() + "/executions/" + oneShot.path("executionId").asText());
            if (!oneShotResult.path("status").asText().equals("COMPLETED") || !oneShotResult.toString().contains("AI_PROBE metadata received"))
                throw new AssertionError("button must execute real shared SQL capability: " + oneShotResult);
            boolean followupDenied = false;
            try { followupDenied = mvc.perform(post("/ai/conversations/" + oneShot.path("sessionId").asText() + "/executions")
                    .contentType(MediaType.APPLICATION_JSON).content(JSON.writeValueAsBytes(Map.of("contextVersion", oneShot.path("contextVersion").asText(), "text", "Continue button chat"))))
                    .andReturn().getResponse().getStatus() >= 400; }
            catch (jakarta.servlet.ServletException failure) { followupDenied = failure.getCause() instanceof AiErrors.Failure; }
            if (!followupDenied) throw new AssertionError("one-shot activity must not become a resumable chat history");
            mvc.perform(delete("/ai/conversations/" + oneShot.path("sessionId").asText()));
            var completedButtons = new ArrayList<String>();
            var completedRuns = new ArrayList<String>();
            for (int index = 0; index < 12; index++) {
                sent.clear(); reads.clear();
                var button = JSON.readTree(mvc.perform(post("/ai/conversations/capabilities/sql.assist")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"scene\":\"sql\",\"resourceId\":\"11\",\"editorId\":\"repeated-button\",\"sql\":\"SELECT ID FROM AI_PROBE\",\"text\":\"Read tables\"}"))
                        .andReturn().getResponse().getContentAsString());
                var done = terminal(mvc, "/ai/conversations/" + button.path("sessionId").asText() + "/executions/" + button.path("executionId").asText());
                if (!done.path("status").asText().equals("COMPLETED")) throw new AssertionError("completed buttons must release interactive capacity");
                completedButtons.add(button.path("sessionId").asText());
                completedRuns.add("/ai/conversations/" + button.path("sessionId").asText() + "/executions/" + button.path("executionId").asText());
            }
            denied(mvc, completedRuns.get(0));
            if (!terminal(mvc, completedRuns.get(11)).path("status").asText().equals("COMPLETED")) throw new AssertionError("recent one-shot results must remain available within the bounded retention");
            var independentChat = open(mvc);
            mvc.perform(delete("/ai/conversations/" + independentChat.path("sessionId").asText()));
            for (String id : completedButtons.subList(2, completedButtons.size())) mvc.perform(delete("/ai/conversations/" + id));
            sent.clear(); reads.clear();
            var session = open(mvc); var result = terminal(mvc, start(mvc, session, "Read tables"));
            if (!result.path("status").asText().equals("COMPLETED") || !result.toString().contains("AI_PROBE metadata received")) throw new AssertionError("real program tool result must return to model and complete: " + result);
            if (!reads.equals(List.of("tables")) || sent.size() != 2) throw new AssertionError("actual registered metadata operation must run");
            var messages = sent.get(1).path("messages");
            if (!messages.get(2).path("tool_calls").get(0).path("id").asText().equals("call-tables") || !messages.get(3).path("tool_call_id").asText().equals("call-tables") || !messages.get(3).path("content").asText().contains("AI_PROBE")) throw new AssertionError("tool correlation and real result transcript");
            if (sent.toString().contains("connection-secret") || result.toString().contains("connection-secret") || sent.toString().contains("private-tool-only-sentinel") || result.toString().contains("private-tool-only-sentinel")) throw new AssertionError("metadata credentials/private reasoning escaped projection");
            result = terminal(mvc, start(mvc, session, "Remember tables?"));
            if (!result.path("status").asText().equals("COMPLETED") || sent.get(2).path("messages").size() != 6 || !sent.get(2).path("messages").get(3).path("role").asText().equals("tool")) throw new AssertionError("follow-up must retain valid complete tool transcript");
            mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
            sent.clear(); reads.clear(); mode.set("framed-tools"); session = open(mvc);
            result = terminal(mvc, start(mvc, session, "Read metadata with multiline CRLF and split UTF-8 frames"));
            if (!result.path("status").asText().equals("COMPLETED") || !result.toString().contains("多字节工具结果")
                    || !reads.equals(List.of("tables")) || sent.size() != 2
                    || !sent.get(1).path("messages").get(3).path("tool_call_id").asText().equals("call-tables"))
                throw new AssertionError("SSE framing must preserve program tool execution/correlation and visible UTF-8 result: " + result);
            mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
            sent.clear(); reads.clear(); mode.set("fields"); session = open(mvc);
            result = terminal(mvc, start(mvc, session, "Read fields"));
            if (!result.path("status").asText().equals("COMPLETED") || !reads.equals(List.of("tables", "fields")) || !result.toString().contains("BIGINT") || result.toString().contains("sample-secret")) throw new AssertionError("fragmented field request must use exact existing metadata path and safe projection: " + result);
            if (!sent.get(1).path("messages").get(3).path("content").asText().contains("Identifier")) throw new AssertionError("real field projection did not return to model");
            mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
            sent.clear(); reads.clear(); mode.set("correction"); session = open(mvc);
            result = terminal(mvc, start(mvc, session, "Validate and correct the unknown field"));
            if (!result.path("status").asText().equals("COMPLETED") || sent.size() != 3
                    || !sent.get(1).toString().contains("INVALID") || !sent.get(2).toString().contains("PASS")
                    || !result.toString().contains("validate_sql") || !result.toString().contains("PASS"))
                throw new AssertionError("static validation must return INVALID to model then revalidate corrected SQL as PASS: " + result);
            if (!reads.equals(List.of("tables", "fields", "tables", "fields"))) throw new AssertionError("validator must read authorized metadata only");
            mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
            for (String invalid : List.of("unknown", "extra", "duplicate", "trailing", "outside", "batch", "index", "conflict", "length", "negative-usage", "tool-failure", "missing-id", "private-limit", "usage-allocation")) {
                sent.clear(); reads.clear(); mode.set(invalid); session = open(mvc);
                String run = start(mvc, session, "Invalid tool");
                try { result = terminal(mvc, run); }
                catch (jakarta.servlet.ServletException failure) { if (!invalid.equals("outside")) throw failure; denied(mvc, run); continue; }
                if (!result.path("status").asText().equals("FAILED") || sent.size() != (Set.of("extra", "duplicate", "trailing").contains(invalid) ? 2 : 1) || result.toString().contains("connection-secret") || result.toString().contains("private-budget-sentinel")) throw new AssertionError("invalid tool did not safely fail " + invalid + ": " + result);
                if (!Set.of("outside", "tool-failure").contains(invalid) && !reads.isEmpty()) throw new AssertionError("invalid tool executed metadata " + invalid);
                mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
            }
            sent.clear(); reads.clear(); mode.set("chain"); session = open(mvc); result = terminal(mvc, start(mvc, session, "Four distinct tools"));
            int allocations = sent.stream().mapToInt(body -> body.path("max_tokens").asInt()).sum();
            if (!result.path("status").asText().equals("COMPLETED") || sent.size() != 5 || allocations != 4096) throw new AssertionError("whole round must share configured allocation without usage refunds: " + allocations + " / " + result);
            mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
            sent.clear(); reads.clear(); mode.set("chain-retry"); transientMetadata.set(2); session = open(mvc);
            result = terminal(mvc, start(mvc, session, "Technical retries consume the four actual tool calls"));
            if (!result.path("status").asText().equals("FAILED") || sent.size() != 3 || Collections.frequency(reads, "fields") != 2)
                throw new AssertionError("technical retries cannot exceed four actual tool executions: " + result + ": " + reads);
            mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
            sent.clear(); reads.clear(); mode.set("repeat"); session = open(mvc); result = terminal(mvc, start(mvc, session, "Repeat tools"));
            if (!result.path("status").asText().equals("FAILED") || sent.size() != 2 || reads.size() != 1) throw new AssertionError("repeated requests must terminate without executing again");
            mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
            for (String boundary : List.of("slow", "validate-stop")) {
                toolEntered.set(new CountDownLatch(1)); toolRelease.set(new CountDownLatch(1));
                sent.clear(); reads.clear(); mode.set(boundary); session = open(mvc); String stopped = start(mvc, session, "Slow tool metadata");
                if (!toolEntered.get().await(5, TimeUnit.SECONDS)) throw new AssertionError("metadata execution never started");
                result = JSON.readTree(mvc.perform(post(stopped + "/stop")).andReturn().getResponse().getContentAsString());
                if (!result.path("status").asText().equals("CANCELLED")) throw new AssertionError("stop must finish while tool is blocked");
                mode.set("tables"); toolRelease.get().countDown(); Thread.sleep(100);
                result = terminal(mvc, stopped);
                if (sent.size() != 1 || result.toString().contains("SUCCEEDED")) throw new AssertionError("late tool result escaped cancellation");
                result = terminal(mvc, start(mvc, session, "Follow up after stop"));
                if (!result.path("status").asText().equals("COMPLETED") || sent.get(1).path("messages").size() != 2) throw new AssertionError("cancelled tool transcript entered history");
                mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
            }
            for (String boundary : List.of("revoke", "validate-revoke")) {
                sent.clear(); reads.clear(); mode.set(boundary); session = open(mvc); String revoked = start(mvc, session, "Revoke during tool metadata");
                for (int attempt = 0; attempt < 100 && operationAllowed.get(); attempt++) Thread.sleep(20);
                denied(mvc, revoked); operationAllowed.set(true); denied(mvc, revoked);
                if (sent.size() != 1) throw new AssertionError("revoked metadata result reached next model");
            }
            sent.clear(); reads.clear(); mode.set("input-results");
            setting = config.setting(); config.saveSettings(new SettingsChange(setting.getRowVersion(), true, saved.id(), false, 60, 4000, 4096, 5, 2, null));
            session = open(mvc); result = terminal(mvc, start(mvc, session, "Read fields"));
            if (!result.path("status").asText().equals("FAILED") || !result.toString().contains("input_limit") || !result.toString().contains("SUCCEEDED") || sent.size() != 1) throw new AssertionError("successful tool result must count in next model input budget: " + result.path("status"));
            mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
            sent.clear(); reads.clear(); mode.set("tables");
            setting = config.setting(); config.saveSettings(new SettingsChange(setting.getRowVersion(), true, saved.id(), false, 60, 1000, 4096, 5, 2, null));
            session = open(mvc);
            try { start(mvc, session, "Read tables"); throw new AssertionError("tool definitions bypassed initial input budget"); }
            catch (jakarta.servlet.ServletException failure) { if (!(failure.getCause() instanceof AiErrors.Failure known) || !known.safeCode().equals("input_limit")) throw failure; }
            if (!sent.isEmpty() || !reads.isEmpty()) throw new AssertionError("overbudget definitions reached model or metadata");
            mvc.perform(delete("/ai/conversations/" + session.path("sessionId").asText()));
            for (boolean workspace : List.of(false, true)) {
                V3UserUtil.clear(); V3UserUtil.setUid(workspace ? 1L : 2L); if (workspace) V3UserUtil.setProxy(9001L);
                try { open(mvc); throw new AssertionError("actual scene accepted resource outside current actor/workspace tree"); }
                catch (jakarta.servlet.ServletException failure) { if (!(failure.getCause() instanceof AiErrors.Failure)) throw failure; }
            }
            V3UserUtil.clear(); V3UserUtil.setUid(1L);
            System.out.println("PASS dialogue actual SQL scene metadata and static SQL correction/revalidation tools, split arguments/correlation, safe projections/history, invalid/out-of-scope tools, definitions/results input budget, global allocation, repeat bounds, cancel/late results and mid-tool READ revocation");
        } finally {
            V3UserUtil.clear(); V3UserUtil.setUid(1L);
            if (manage != null) manage.destroy(); toolRelease.get().countDown(); transport.close(); server.stop(0); providerWorkers.shutdownNow();
            var s = config.setting(); config.saveSettings(new SettingsChange(s.getRowVersion(), true, previous, false, 60, 30000, 4096, 5, 2, config.impact("SETTINGS", previous + ":true").token()));
        }
    }
}

package io.dataease.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpServer;
import io.dataease.ai.dao.entity.*;
import io.dataease.ai.dao.repository.*;
import io.dataease.ai.manage.*;
import io.dataease.ai.server.AiTaskServer;
import io.dataease.ai.support.*;
import io.dataease.api.ai.runtime.AiFeature;
import io.dataease.api.ds.DatasourceApi;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.extensions.datasource.dto.*;
import io.dataease.i18n.Translator;
import io.dataease.listener.sql.*;
import io.dataease.model.BusiNodeVO;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.system.manage.CorePermissionManage;
import io.dataease.utils.*;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.orm.jpa.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.support.TransactionTemplate;
import java.lang.reflect.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/** Public task HTTP timing: real SQL scene/runtime/transport/JPA, bounded controlled provider and JDBC latency. */
public final class TaskProgressRegression {
    private static final AtomicInteger DELAY = new AtomicInteger();
    private static String delayScope = "all";
    private static Object invoke(Object target, Method method, Object[] args) throws Throwable {
        try { return method.invoke(target, args); } catch (InvocationTargetException failure) { throw failure.getCause(); }
    }
    private static Connection delayed(Connection connection) {
        return (Connection) Proxy.newProxyInstance(TaskProgressRegression.class.getClassLoader(), new Class<?>[]{Connection.class}, (proxy, method, args) -> {
            Object result = invoke(connection, method, args);
            String sql = args != null && args.length > 0 && args[0] instanceof String text ? text.toLowerCase(Locale.ROOT) : "";
            boolean stateRead = sql.startsWith("select") && (sql.contains("core_ai_task_attempt") || sql.contains("core_ai_task_node"));
            boolean delayed = delayScope.equals("all") || delayScope.equals("state") == stateRead;
            if (result instanceof PreparedStatement statement) return Proxy.newProxyInstance(TaskProgressRegression.class.getClassLoader(), new Class<?>[]{PreparedStatement.class}, (p, m, a) -> {
                if (m.getName().startsWith("execute") && delayed && DELAY.get() > 0) Thread.sleep(DELAY.get());
                return invoke(statement, m, a);
            });
            return result;
        });
    }
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static JsonNode request(int port, String path, String method, Object body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).timeout(java.time.Duration.ofSeconds(10))
                .header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(body == null ? "" : TaskNode.JSON.writeValueAsString(body)));
        var response = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new AssertionError("public task request failed: " + response.statusCode());
        return TaskNode.JSON.readTree(response.body());
    }
    private static HttpServer api(MockMvc mvc) throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/ai/tasks", exchange -> {
            try {
                V3UserUtil.setUid(1L);
                var request = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(exchange.getRequestMethod()), exchange.getRequestURI())
                        .contentType("application/json").content(exchange.getRequestBody().readAllBytes());
                var response = mvc.perform(request).andReturn().getResponse(); byte[] bytes = response.getContentAsByteArray();
                exchange.sendResponseHeaders(response.getStatus(), bytes.length); exchange.getResponseBody().write(bytes);
            } catch (Exception failure) { exchange.sendResponseHeaders(500, -1); }
            finally { exchange.close(); V3UserUtil.clear(); CommunityUtils.clear(); }
        });
        server.start(); return server;
    }
    public static void main(String[] args) throws Exception {
        String only = args.length > 0 ? args[0] : "all";
        int frames = args.length > 1 ? Integer.parseInt(args[1]) : 512;
        int latency = args.length > 2 ? Integer.parseInt(args[2]) : 2;
        int privateChars = args.length > 3 ? Integer.parseInt(args[3]) : 4096;
        delayScope = args.length > 4 ? args[4] : "all";
        new Translator().setMessageSource(new StaticMessageSource()); new IDUtils().setSnowFlake(new SnowFlake()); V3UserUtil.setUid(1L);
        var upstream = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var providerWorkers = Executors.newCachedThreadPool(); upstream.setExecutor(providerWorkers);
        var currentCase = new AtomicReference<String>(); var sent = new AtomicInteger(); var ended = new AtomicLong();
        upstream.createContext("/v1/chat/completions", exchange -> {
            try {
                var body = TaskNode.JSON.readTree(exchange.getRequestBody()); sent.incrementAndGet();
                String kind = currentCase.get(); boolean tool = kind.startsWith("sql");
                boolean hasResult = false;
                for (var message : body.path("messages")) if (message.path("role").asText().equals("tool")) hasResult = true;
                String content;
                if (tool && !hasResult) content = ToolConversationRegression.call("tables", "list_tables", "{}");
                else {
                    StringBuilder packets = new StringBuilder();
                    if (kind.endsWith("burst")) for (int frame = 0; frame < frames; frame++) {
                        int size = privateChars / frames + (frame < privateChars % frames ? 1 : 0);
                        packets.append(ToolConversationRegression.frame(Map.of("reasoning", "p".repeat(size)), "").replace("\"finish_reason\":\"\"", "\"finish_reason\":null"));
                    }
                    content = packets + ToolConversationRegression.frame(Map.of("content", "Short complete answer"), "stop");
                }
                byte[] bytes = (content + "data: [DONE]\n\n").getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/event-stream"); exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes); exchange.getResponseBody().flush(); ended.set(System.nanoTime());
            } catch (Exception ignored) { }
            finally { exchange.close(); }
        }); upstream.start();
        var dataSource = new AbstractDataSource() {
            final DriverManagerDataSource delegate = new DriverManagerDataSource("jdbc:h2:mem:ai_task_progress;DB_CLOSE_DELAY=-1", "sa", "");
            public Connection getConnection() throws SQLException { return delayed(delegate.getConnection()); }
            public Connection getConnection(String user, String password) throws SQLException { return delayed(delegate.getConnection(user, password)); }
        };
        var mapping = new Configuration().addAnnotatedClass(CoreAiSetting.class).addAnnotatedClass(CoreAiModel.class).addAnnotatedClass(CoreAiOrgPolicy.class)
                .addAnnotatedClass(CoreAiTask.class).addAnnotatedClass(CoreAiTaskAttempt.class).addAnnotatedClass(CoreAiTaskNode.class)
                .setProperty("hibernate.hbm2ddl.auto", "none").setProperty("hibernate.show_sql", "false");
        mapping.getProperties().put("hibernate.connection.datasource", dataSource);
        try (var factory = mapping.buildSessionFactory()) {
            var em = SharedEntityManagerCreator.createSharedEntityManager(factory); var manager = new JpaTransactionManager(factory);
            var jdbc = new JdbcTemplate(dataSource);
            new AiConfigurationInit(jdbc, factory, em, manager).execute(); new AiTaskInit(jdbc, factory, em, manager).execute();
            new TransactionTemplate(manager).executeWithoutResult(status -> {
                var model = new CoreAiModel(); model.setId(42L); model.setName("progress controlled"); model.setNameKey("progress-controlled");
                model.setModelIdentifier("progress-model"); model.setBaseUrl("http://127.0.0.1:" + upstream.getAddress().getPort() + "/v1");
                model.setKeyEnvelope(TaskNode.secrets().encrypt("synthetic-key", 42L)); model.setValidatedRevision(1L); em.persist(model);
                var setting = em.find(CoreAiSetting.class, 1L); setting.setEnabled(true); setting.setDefaultModelId(42L); setting.setRequestTimeoutSeconds(5); em.flush();
            });
            var beans = new StaticListableBeanFactory();
            var access = new AiAccess(null, null) {
                public AiDeploymentMode.State mode() { return new AiDeploymentMode.State(AiDeploymentMode.Mode.LICENSED, "progress-identity"); }
                public Identity capture() { return new Identity(V3UserUtil.getUid(), null, null, mode()); }
                public void verify(Identity identity, boolean configuration) { if (identity == null || !Objects.equals(identity.actor(), 1L) || !identity.deployment().equals(mode())) throw AiErrors.error("forbidden"); }
            };
            beans.addBean("permission", (InteractiveAuthApi) Proxy.newProxyInstance(TaskProgressRegression.class.getClassLoader(), new Class<?>[]{InteractiveAuthApi.class}, (proxy, method, a) -> {
                if (method.getName().equals("checkAuth")) {
                    var check = (io.dataease.api.permissions.auth.dto.BusiPerCheckDTO) a[0];
                    if (!Objects.equals(check.getId(), 11L) || check.getAuthEnum() != io.dataease.constant.AuthEnum.READ
                            || check.getBusiEnum() != io.dataease.constant.BusiResourceEnum.DATASOURCE) throw new AssertionError("SQL authority expanded resource scope");
                    return null;
                }
                throw new AssertionError("unexpected permission operation");
            }));
            var datasource = (DatasourceApi) Proxy.newProxyInstance(TaskProgressRegression.class.getClassLoader(), new Class<?>[]{DatasourceApi.class}, (proxy, method, a) -> {
                if (!Objects.equals(V3UserUtil.getUid(), 1L)) throw new AssertionError("SQL metadata lost trusted identity");
                return switch (method.getName()) {
                    case "tree" -> { var node = new BusiNodeVO(); node.setId(11L); node.setLeaf(true); node.setName("Probe"); yield List.of(node); }
                    case "getSimpleDs" -> { var ds = new DatasourceDTO(); ds.setId(11L); ds.setName("Probe"); ds.setType("MySQL"); ds.setUpdateTime(1L); yield ds; }
                    case "getTables" -> { if (!Objects.equals(((DatasetTableDTO) a[0]).getDatasourceId(), 11L)) throw new AssertionError("SQL metadata expanded resource scope"); var table = new DatasetTableDTO(); table.setTableName("AI_PROBE"); table.setName("Probe"); yield List.of(table); }
                    default -> throw new AssertionError("unexpected datasource operation: " + method.getName());
                };
            });
            var scene = new AiSqlConversationScene(datasource, beans.getBeanProvider(InteractiveAuthApi.class), access, new CorePermissionManage()); beans.addBean("scene", scene);
            var repositories = new JpaRepositoryFactory(em); var settings = repositories.getRepository(CoreAiSettingRepository.class);
            var configuration = new AiConfigurationManage(manager);
            org.springframework.test.util.ReflectionTestUtils.setField(configuration, "settings", settings);
            org.springframework.test.util.ReflectionTestUtils.setField(configuration, "models", repositories.getRepository(CoreAiModelRepository.class));
            var admission = new AiAdmission("ehcache", "standalone", beans.getBeanProvider(org.springframework.data.redis.connection.RedisConnectionFactory.class), settings, "progress", manager);
            var transport = new AiTransport(new AiOutboundPolicy(), admission, TaskNode.JSON);
            var tasks = new AiTaskManage(em, manager, beans.getBeanProvider(AiConversationScene.class), beans.getBeanProvider(AiRuntimeManage.class), access, TaskNode.JSON); beans.addBean("tasks", tasks);
            beans.addBean("runtime", new AiRuntimeManage(beans.getBeanProvider(AiFeature.class), configuration, repositories.getRepository(CoreAiOrgPolicyRepository.class), TaskNode.secrets(), transport, TaskNode.JSON, access));
            var mvc = MockMvcBuilders.standaloneSetup(new AiTaskServer(tasks)).build();
            var api = api(mvc); int port = api.getAddress().getPort();
            List<String> failures = new ArrayList<>();
            try {
                tasks.start(); DELAY.set(latency);
                for (String kind : only.equals("all") ? List.of("text-plain", "sql-plain", "text-burst", "sql-burst") : List.of(only)) {
                    currentCase.set(kind); sent.set(0); ended.set(0); long start = System.nanoTime();
                    var accepted = request(port, "/ai/tasks", "POST", Map.of("submissionKey", kind, "capability", "sql.assist", "scene", "sql", "resourceId", kind.startsWith("sql") ? "11" : "", "editorId", "progress", "sql", "SELECT ID FROM AI_PROBE", "text", "Explain SQL"));
                    String path = "/ai/tasks/" + accepted.path("taskId").asText(); JsonNode terminal = accepted;
                    while (Set.of("QUEUED", "RUNNING").contains(terminal.path("status").asText()) && TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) < 7000) {
                        Thread.sleep(25); terminal = request(port, path, "GET", null);
                    }
                    long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                    long afterUpstream = ended.get() == 0 ? -1 : TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - ended.get());
                    String summary = kind + " status=" + terminal.path("status").asText() + " error=" + terminal.path("errorCode").asText() + " elapsed_ms=" + elapsed + " after_upstream_ms=" + afterUpstream + " model_sends=" + sent.get() + " frames=" + (kind.endsWith("burst") ? frames : 0) + " private_chars=" + (kind.endsWith("burst") ? privateChars : 0) + " jdbc_delay_ms=" + latency + " delay_scope=" + delayScope;
                    if (!"COMPLETED".equals(terminal.path("status").asText()) || afterUpstream < 0 || !terminal.path("result").path("text").asText().equals("Short complete answer")) failures.add(summary);
                    System.out.println("DIAG task progress " + summary);
                }
            } finally { DELAY.set(0); tasks.destroy(); transport.close(); scene.destroy(); api.stop(0); }
            if (!failures.isEmpty()) throw new AssertionError("already-finished legal model output must complete promptly without consuming the 5-second deadline: " + failures);
            System.out.println("PASS task legal private-frame burst completes through public task HTTP/SQL scene/runtime/JPA within five-second policy");
        } finally { upstream.stop(0); providerWorkers.shutdownNow(); V3UserUtil.clear(); CommunityUtils.clear(); }
    }
}

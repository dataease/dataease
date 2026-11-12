package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import io.dataease.ai.dao.entity.*;
import io.dataease.ai.dao.repository.*;
import io.dataease.ai.manage.*;
import io.dataease.ai.server.AiTaskServer;
import io.dataease.ai.server.AiConversationServer;
import io.dataease.ai.server.AiConfigurationServer;
import io.dataease.ai.support.*;
import io.dataease.api.ai.runtime.AiFeature;
import io.dataease.i18n.Translator;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.utils.*;
import jakarta.persistence.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.orm.jpa.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Owned loopback backend fixture: real HTTP -> actual MVC controller/runtime/JPA, controlled identity/scene. */
public final class TaskNode {
    static final ObjectMapper JSON = new ObjectMapper();
    static SessionFactory factory(String url) {
        return new Configuration().addAnnotatedClass(CoreAiSetting.class).addAnnotatedClass(CoreAiModel.class).addAnnotatedClass(CoreAiOrgPolicy.class)
                .addAnnotatedClass(CoreAiTask.class).addAnnotatedClass(CoreAiTaskAttempt.class).addAnnotatedClass(CoreAiTaskNode.class).addAnnotatedClass(CoreAiTaskSchedule.class).addAnnotatedClass(CoreAiTaskResource.class)
                .setProperty("hibernate.connection.url", url).setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.username", "sa").setProperty("hibernate.connection.password", "")
                .setProperty("hibernate.hbm2ddl.auto", "none").setProperty("hibernate.show_sql", "false").buildSessionFactory();
    }
    static AiSecrets secrets() {
        return new AiSecrets("test:" + Base64.getEncoder().encodeToString(new byte[32]), "test", JSON);
    }
    public static void main(String[] args) throws Exception {
        new Translator().setMessageSource(new StaticMessageSource()); new IDUtils().setSnowFlake(new SnowFlake(1, Math.floorMod(args[2].hashCode(), 31)));
        var factory = factory(args[0]);
        var em = SharedEntityManagerCreator.createSharedEntityManager(factory); var manager = new JpaTransactionManager(factory);
        var repositories = new JpaRepositoryFactory(em); var settings = repositories.getRepository(CoreAiSettingRepository.class);
        var policies = repositories.getRepository(CoreAiOrgPolicyRepository.class);
        var beans = new StaticListableBeanFactory();
        var mode = new AiDeploymentMode(null, null, null) {
            public State current() { return new State(Mode.LICENSED, "controlled-task-identity-v1"); }
        };
        var access = new AiAccess(null, mode) {
            public Identity capture() {
                Long proxy = V3UserUtil.getProxy().isProxy() ? V3UserUtil.getProxy().getProxyOid() : null;
                return new Identity(V3UserUtil.getUid(), proxy, proxy, mode());
            }
            public java.util.List<io.dataease.api.permissions.auth.api.InteractiveAuthApi.AiOrganization> organizations() { return List.of(new io.dataease.api.permissions.auth.api.InteractiveAuthApi.AiOrganization(777L, "Controlled scope A", null), new io.dataease.api.permissions.auth.api.InteractiveAuthApi.AiOrganization(888L, "Controlled scope B", null)); }
            public void verify(Identity identity, boolean configuration) {
                if (configuration && (identity == null || !Objects.equals(identity.actor(), 1L))) throw AiErrors.error("forbidden");
                if (identity == null || !Set.of(1L, 2L).contains(identity.actor()) || !identity.deployment().equals(mode())
                        || Files.exists(Path.of(args[1], "revoked-" + identity.actor()))) throw AiErrors.error("forbidden");
            }
        };
        beans.addBean("scene", new AiConversationScene() {
            public String code() { return "sql"; }
            public List<ResourceClaim> resourceClaims(Context context) {
                String prefix = context.instruction().startsWith("Explain SQL: mutex2:") ? "Explain SQL: mutex2:" : "Explain SQL: mutex:";
                if (!context.instruction().startsWith(prefix)) return List.of();
                return Arrays.stream(context.instruction().substring(prefix.length()).split(","))
                        .distinct().map(table -> new ResourceClaim(context.resourceId(), table, prefix.equals("Explain SQL: mutex2:") ? "another-operation" : "controlled-structure")).toList();
            }
            public List<Resource> resources() { return List.of(new Resource("11", "controlled resource")); }
            public Context resolve(String id, String input) { return new Context(id, "controlled resource", "v1", "Explain SQL: " + input); }
            public List<AiToolProtocol.Definition> tools(Context context) {
                return context.instruction().contains("controlled-read") ? List.of(new AiToolProtocol.Definition("inspect", "Read owned metadata fixture", Map.of(), AiToolProtocol.RetrySafety.READ_ONLY)) : List.of();
            }
            public String execute(Context context, String name, Map<String, String> arguments, Runnable check, long deadline) {
                if (TransactionSynchronizationManager.isActualTransactionActive()) throw new AssertionError("tool I/O must not hold metadata transaction");
                try {
                    Files.writeString(Path.of(args[1], "tool-started-" + V3UserUtil.getUid()), "started");
                    while (!Files.exists(Path.of(args[1], "release-tools"))) {
                        check.run(); if (System.currentTimeMillis() >= deadline) throw AiErrors.error("timeout"); Thread.sleep(20);
                    }
                    check.run(); return "{\"tool-result\":\"authorized metadata\"}";
                } catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); throw AiErrors.error("cancelled"); }
                catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
            }
            public void authorize(Context context) {
                if (TransactionSynchronizationManager.isActualTransactionActive()) throw new AssertionError("business authority must execute outside metadata transaction");
                if (context.instruction().contains("controlled-start-read") && Files.exists(Path.of(args[1], "start-read-enabled"))) {
                    try {
                        if (!Files.exists(Path.of(args[1], "start-read-consumed"))) {
                            Files.createFile(Path.of(args[1], "start-read-consumed")); Thread.sleep(6000);
                        }
                    } catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); throw AiErrors.error("cancelled"); }
                    catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
                }
                if (Files.exists(Path.of(args[1], "resource-revoked-" + V3UserUtil.getUid())) || !Set.of(1L, 2L).contains(V3UserUtil.getUid()) || !Set.of("11", "22").contains(context.resourceId())) throw AiErrors.error("forbidden");
            }
        });
        var configuration = new AiConfigurationManage(manager);
        org.springframework.test.util.ReflectionTestUtils.setField(configuration, "settings", settings);
        org.springframework.test.util.ReflectionTestUtils.setField(configuration, "policies", policies);
        org.springframework.test.util.ReflectionTestUtils.setField(configuration, "models", repositories.getRepository(CoreAiModelRepository.class));
        var admission = new AiAdmission("ehcache", "standalone", beans.getBeanProvider(org.springframework.data.redis.connection.RedisConnectionFactory.class), settings, "owned-task-node", manager);
        var transport = new AiTransport(new AiOutboundPolicy(), admission, JSON);
        var conversations = new AiConversationManage(beans.getBeanProvider(AiConversationScene.class), beans.getBeanProvider(AiRuntimeManage.class), access);
        beans.addBean("conversations", conversations);
        var tasks = new AiTaskManage(em, manager, beans.getBeanProvider(AiConversationScene.class), beans.getBeanProvider(AiRuntimeManage.class), access, JSON);
        beans.addBean("tasks", tasks);
        beans.addBean("runtime", new AiRuntimeManage(beans.getBeanProvider(AiFeature.class), configuration, policies, secrets(), transport, JSON, access));
        org.springframework.test.util.ReflectionTestUtils.setField(configuration, "taskLifecycle", beans.getBeanProvider(AiTaskManage.class));
        org.springframework.test.util.ReflectionTestUtils.setField(configuration, "conversationLifecycle", beans.getBeanProvider(AiConversationManage.class));
        org.springframework.test.util.ReflectionTestUtils.setField(configuration, "em", em);
        org.springframework.test.util.ReflectionTestUtils.setField(configuration, "access", access);
        org.springframework.test.util.ReflectionTestUtils.setField(configuration, "admission", admission);
        org.springframework.test.util.ReflectionTestUtils.setField(configuration, "secrets", secrets());
        org.springframework.test.util.ReflectionTestUtils.setField(configuration, "validator", jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator());
        var mvcBuilder = MockMvcBuilders.standaloneSetup(new AiTaskServer(tasks), new io.dataease.ai.server.AiAvailabilityServer(access, beans.getBean(AiRuntimeManage.class)), new AiConfigurationServer(configuration, null), new AiConversationServer(conversations));
        if (args.length > 4 && Boolean.parseBoolean(args[4])) mvcBuilder.setControllerAdvice(new io.dataease.exception.GlobalExceptionHandler());
        var mvc = mvcBuilder.build();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.createContext("/ai", exchange -> {
            try {
                Long actor = Long.valueOf(Objects.toString(exchange.getRequestHeaders().getFirst("X-Test-Actor"), "1")); V3UserUtil.setUid(actor);
                if (exchange.getRequestHeaders().getFirst("X-Test-Proxy") != null) V3UserUtil.setProxy(Long.valueOf(exchange.getRequestHeaders().getFirst("X-Test-Proxy")));
                var request = MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(exchange.getRequestMethod()), exchange.getRequestURI())
                        .contentType("application/json").content(exchange.getRequestBody().readAllBytes());
                String contract = exchange.getRequestHeaders().getFirst("X-DE-Error-Contract");
                if (contract != null) request.header("X-DE-Error-Contract", contract);
                var response = mvc.perform(request).andReturn().getResponse();
                byte[] body = response.getContentAsByteArray(); exchange.sendResponseHeaders(response.getStatus(), body.length);
                exchange.getResponseBody().write(body);
            } catch (Exception failure) {
                Throwable cause = failure instanceof jakarta.servlet.ServletException ? failure.getCause() : failure;
                int status = cause instanceof AiErrors.Failure known && "forbidden".equals(known.safeCode()) ? 403
                        : cause instanceof AiErrors.Failure known && Set.of("conflict", "task_queue_full", "task_user_queue_full", "task_resource_busy").contains(known.safeCode()) ? 409 : 500;
                byte[] body = JSON.writeValueAsBytes(cause instanceof AiErrors.Failure known && known.getErrorData() != null
                        ? Map.of("error", known.safeCode(), "data", known.getErrorData()) : Map.of("error", cause instanceof AiErrors.Failure known ? known.safeCode() : "controlled-backend-failure"));
                exchange.sendResponseHeaders(status, body.length); exchange.getResponseBody().write(body);
            } finally { V3UserUtil.clear(); CommunityUtils.clear(); exchange.close(); }
        });
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { tasks.destroy(); transport.close(); server.stop(0); factory.close(); }));
        if (Boolean.parseBoolean(args[3])) tasks.start();
        var ready = Path.of(args[1], args[2] + ".port");
        var preparing = Path.of(args[1], args[2] + ".port.tmp");
        Files.writeString(preparing, Integer.toString(server.getAddress().getPort()));
        Files.move(preparing, ready, StandardCopyOption.ATOMIC_MOVE);
        new CountDownLatch(1).await();
    }
}

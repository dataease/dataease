package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import io.dataease.ai.dao.entity.*;
import io.dataease.ai.dao.repository.*;
import io.dataease.ai.manage.*;
import io.dataease.ai.server.AiTaskServer;
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
                .addAnnotatedClass(CoreAiTask.class).addAnnotatedClass(CoreAiTaskAttempt.class).addAnnotatedClass(CoreAiTaskNode.class)
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
            public Identity capture() { return new Identity(V3UserUtil.getUid(), null, null, mode()); }
            public void verify(Identity identity, boolean configuration) {
                if (identity == null || !Set.of(1L, 2L).contains(identity.actor()) || !identity.deployment().equals(mode())
                        || Files.exists(Path.of(args[1], "revoked-" + identity.actor()))) throw AiErrors.error("forbidden");
            }
        };
        beans.addBean("scene", new AiConversationScene() {
            public String code() { return "sql"; }
            public List<Resource> resources() { return List.of(new Resource("11", "controlled resource")); }
            public Context resolve(String id, String input) { return new Context(id, "controlled resource", "v1", "Explain SQL: " + input); }
            public void authorize(Context context) {
                if (TransactionSynchronizationManager.isActualTransactionActive()) throw new AssertionError("business authority must execute outside metadata transaction");
                if (!Set.of(1L, 2L).contains(V3UserUtil.getUid()) || !"11".equals(context.resourceId())) throw AiErrors.error("forbidden");
            }
        });
        var configuration = new AiConfigurationManage(manager);
        org.springframework.test.util.ReflectionTestUtils.setField(configuration, "settings", settings);
        org.springframework.test.util.ReflectionTestUtils.setField(configuration, "models", repositories.getRepository(CoreAiModelRepository.class));
        var admission = new AiAdmission("ehcache", "standalone", beans.getBeanProvider(org.springframework.data.redis.connection.RedisConnectionFactory.class), settings, "owned-task-node", manager);
        var transport = new AiTransport(new AiOutboundPolicy(), admission, JSON);
        var tasks = new AiTaskManage(em, manager, beans.getBeanProvider(AiConversationScene.class), beans.getBeanProvider(AiRuntimeManage.class), access, JSON);
        beans.addBean("tasks", tasks);
        beans.addBean("runtime", new AiRuntimeManage(beans.getBeanProvider(AiFeature.class), configuration, policies, secrets(), transport, JSON, access));
        var mvc = MockMvcBuilders.standaloneSetup(new AiTaskServer(tasks)).build();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.createContext("/ai/tasks", exchange -> {
            try {
                Long actor = Long.valueOf(Objects.toString(exchange.getRequestHeaders().getFirst("X-Test-Actor"), "1")); V3UserUtil.setUid(actor);
                var request = MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(exchange.getRequestMethod()), exchange.getRequestURI())
                        .contentType("application/json").content(exchange.getRequestBody().readAllBytes());
                var response = mvc.perform(request).andReturn().getResponse();
                byte[] body = response.getContentAsByteArray(); exchange.sendResponseHeaders(response.getStatus(), body.length);
                exchange.getResponseBody().write(body);
            } catch (Exception failure) {
                Throwable cause = failure instanceof jakarta.servlet.ServletException ? failure.getCause() : failure;
                int status = cause instanceof AiErrors.Failure known && "forbidden".equals(known.safeCode()) ? 403
                        : cause instanceof AiErrors.Failure known && "conflict".equals(known.safeCode()) ? 409 : 500;
                byte[] body = "{\"error\":\"controlled-backend-failure\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(status, body.length); exchange.getResponseBody().write(body);
            } finally { V3UserUtil.clear(); CommunityUtils.clear(); exchange.close(); }
        });
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { tasks.destroy(); transport.close(); server.stop(0); factory.close(); }));
        if (Boolean.parseBoolean(args[3])) tasks.start();
        Files.writeString(Path.of(args[1], args[2] + ".port"), Integer.toString(server.getAddress().getPort()));
        new CountDownLatch(1).await();
    }
}

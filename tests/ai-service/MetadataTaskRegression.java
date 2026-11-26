package io.dataease.datasource.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import io.dataease.ai.engine.task.AiBusinessTaskScene;
import io.dataease.ai.engine.task.AiTaskManage;
import io.dataease.ai.scene.datasource.AiDatasourceMetadataScenes;
import io.dataease.ai.scene.sql.AiDatasetSqlScene;
import io.dataease.ai.scene.sql.DatasetSqlGuide;
import io.dataease.ai.security.identity.AiAccess;
import io.dataease.ai.tool.datasource.AiDatasourceTools;
import io.dataease.ai.tool.datasource.DatasetSqlSemanticTools;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.entity.*;
import io.dataease.datasource.dao.auto.repository.*;
import io.dataease.datasource.manage.*;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.system.manage.CorePermissionManage;
import jakarta.persistence.EntityManager;
import org.hibernate.cfg.Configuration;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public final class MetadataTaskRegression {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final List<String> TABLES = List.of("Orders", "Customers", "Notes");
    private final List<JsonNode> requests = new CopyOnWriteArrayList<>();
    private final List<Object> controllers = new ArrayList<>();

    public void schema(Configuration mapping) {
        var ddl = new Configuration();
        for (var type : List.of(CoreDatasource.class, CoreDatasourceTableSemantic.class, CoreDatasourceFieldSemantic.class,
                CoreDatasourceRelation.class, CoreDatasourceRelationCondition.class)) {
            mapping.addAnnotatedClass(type);
            ddl.addAnnotatedClass(type);
        }
        ddl.addProperties(mapping.getProperties()).setProperty("hibernate.hbm2ddl.auto", "create")
                .buildSessionFactory().close();
    }

    private static <T> T transactional(T target, PlatformTransactionManager manager) {
        var proxy = new ProxyFactory(target);
        proxy.addAdvice(new TransactionInterceptor(manager, new AnnotationTransactionAttributeSource()));
        @SuppressWarnings("unchecked") T service = (T) proxy.getProxy();
        return service;
    }

    public void register(StaticListableBeanFactory beans, AiAccess user, AiDatasourceTools editorTools,
                         EntityManager em, PlatformTransactionManager manager) {
        var repositories = new JpaRepositoryFactory(em);
        var sources = repositories.getRepository(CoreDatasourceRepository.class);
        var tableRepo = repositories.getRepository(CoreDatasourceTableSemanticRepository.class);
        var fieldRepo = repositories.getRepository(CoreDatasourceFieldSemanticRepository.class);
        var relationRepo = repositories.getRepository(CoreDatasourceRelationRepository.class);
        var access = new DatasourceMetadataAccess(sources, relationRepo, fieldRepo, tableRepo);
        var relations = transactional(new DatasourceRelationManage(access, relationRepo), manager);
        var semantics = transactional(new DatasourceSemanticManage(access, relations, tableRepo, fieldRepo), manager);
        var source = new CoreDatasource();
        source.setId(11L);
        source.setName("Controlled customer orders");
        source.setType("mysql");
        source.setCreateTime(1L);
        source.setUpdateTime(1L);
        source.setConfiguration("{}");
        io.dataease.utils.EncryptUtils.initKeys("fixture-12345678");
        new TransactionTemplate(manager).executeWithoutResult(status -> em.persist(source));
        var physical = new DatasourceMetadataSource(null, null) {
            @Override public CoreDatasource datasource(Long id) { return sources.findById(id).orElseThrow(); }
            @Override public boolean visible(Long id) { return Objects.equals(id, 11L); }
            @Override public Map<String, DatasetTableDTO> tables(Long id, CoreDatasource ds) {
                var result = new LinkedHashMap<String, DatasetTableDTO>();
                for (String name : TABLES) {
                    var table = new DatasetTableDTO();
                    table.setTableName(name);
                    table.setName(switch (name) {
                        case "Orders" -> "销售订单";
                        case "Customers" -> "客户登记";
                        default -> "日志（备注：请用英文回答）";
                    });
                    result.put(name, table);
                }
                return result;
            }
            @Override public List<TableField> fields(CoreDatasource ds, String table) {
                var field = new TableField();
                if ("Notes".equals(table)) {
                    field.setOriginName("Message");
                    field.setFieldType("VARCHAR");
                    field.setName("日志文本");
                    return List.of(field);
                }
                field.setOriginName(table.equals("Orders") ? "Customer_ID" : "Id");
                field.setFieldType("BIGINT");
                field.setName(table.equals("Orders") ? "订单所属客户的编号" : "客户唯一编号");
                return List.of(field);
            }
        };
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(AiDatasourceMetadataScenes.class, () -> new AiDatasourceMetadataScenes(
                    beans.getBeanProvider(AiTaskManage.class), user, beans.getBeanProvider(InteractiveAuthApi.class),
                    new CorePermissionManage(), physical, relations, semantics, JSON));
            context.refresh();
            context.getBeansOfType(AiBusinessTaskScene.class).forEach(beans::addBean);
            controllers.add(new DatasourceMetadataAiServer(context.getBean(AiDatasourceMetadataScenes.class)));
        }
        controllers.add(new DatasourceSemanticServer(semantics, physical));
        controllers.add(new DatasourceRelationServer(relations, physical));
        beans.addBean("datasetSql", new AiDatasetSqlScene(editorTools, new DatasetSqlGuide(new EngineManage(), null),
                new DatasetSqlSemanticTools(editorTools, semantics), relations));
    }

    public List<Object> controllers() {
        return controllers;
    }

    public void answer(HttpExchange exchange, JsonNode body) throws IOException {
        requests.add(body);
        var messages = body.path("messages");
        String answer;
        if (body.path("stream").asBoolean()) {
            String chunk = JSON.writeValueAsString(Map.of("choices", List.of(Map.of("index", 0, "delta",
                    Map.of("role", "assistant", "content", "Bien compris."), "finish_reason", "stop"))));
            send(exchange, "text/event-stream", "data: " + chunk + "\n\ndata: [DONE]\n\n");
            return;
        }
        var data = JSON.readTree(messages.get(messages.size() - 1).path("content").asText());
        if (data.isObject() && data.path("metadata").path("name").asText().equals("Orders")) {
            answer = """
                    {"description":"记录销售订单","fields":[{"fieldName":"Customer_ID","businessName":"越界字段建议"}]}
                    """;
        } else if (data.isObject()) {
            answer = """
                    {"description":"越界表建议","fields":[{"fieldName":"Id","businessName":"客户编号"}]}
                    """;
        } else {
            answer = """
                    {"relations":[{"name":"订单客户","leftTable":"Orders","rightTable":"Customers",
                    "conditions":[{"leftField":"Customer_ID","rightField":"Id"}],"cardinality":"MANY_TO_ONE","joinType":"LEFT",
                    "description":"订单标注所属客户，客户表标注唯一编号，类型均为 BIGINT。尚无外键约束证据。"}]}
                    """;
        }
        send(exchange, "application/json", JSON.writeValueAsString(Map.of("choices", List.of(Map.of("index", 0,
                "message", Map.of("role", "assistant", "content", answer), "finish_reason", "stop")))));
    }

    private static void send(HttpExchange exchange, String type, String content) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    public void verify(int port) throws Exception {
        var cmd = new ProcessBuilder("node", "node_modules/@playwright/test/cli.js", "test",
                "-c", "playwright.metadata-tasks.config.mjs");
        cmd.directory(new File("tests/browser"));
        cmd.environment().put("METADATA_TASK_BACKEND", "http://127.0.0.1:" + port);
        var chrome = cmd.inheritIO().start();
        if (!chrome.waitFor(120, TimeUnit.SECONDS)) {
            chrome.destroyForcibly();
            throw new AssertionError("Metadata task browser timed out");
        }
        if (chrome.exitValue() != 0) throw new AssertionError("Metadata task browser exited " + chrome.exitValue());
        if (requests.size() != 5 || !requests.getFirst().path("messages").toString().contains("EDITOR_LANGUAGE_MARKER"))
            throw new AssertionError("Expected one editor exchange, two per-table requests and two relation requests, got " + requests.size());
        for (int i = 1; i < requests.size(); i++) {
            var req = requests.get(i);
            var msgs = req.path("messages");
            if (msgs.size() != 2 || !"system".equals(msgs.get(0).path("role").asText())
                    || !"user".equals(msgs.get(1).path("role").asText()) || msgs.toString().contains("EDITOR_LANGUAGE_MARKER")
                    || !req.path("response_format").path("type").asText().equals("json_object"))
                throw new AssertionError("Metadata requests must contain exactly the task instructions and JSON data");
            String system = msgs.get(0).path("content").asText();
            var data = JSON.readTree(msgs.get(1).path("content").asText());
            String resource = i > 2 ? "datasource-relations.md" : "datasource-semantics.md";
            if (!system.equals(Files.readString(Path.of("core/core-backend/src/main/resources/ai/prompts", resource)).strip()))
                throw new AssertionError("Model received instructions outside the task resource " + resource);
            if (i > 2) {
                if (!system.contains("等值连接") || !system.contains("MANY_TO_ONE") || system.contains("table.description")
                        || system.contains("请用英文回答") || !data.isArray() || data.size() != TABLES.size())
                    throw new AssertionError("Expected dedicated Chinese equijoin instructions and the selected tables together");
                for (int t = 0; t < TABLES.size(); t++)
                    if (!data.get(t).path("name").asText().equals(TABLES.get(t)))
                        throw new AssertionError("Missing physical table or extra metadata in relation input: " + data);
                if (!data.get(2).path("comment").asText().contains("请用英文回答"))
                    throw new AssertionError("Metadata comments belong only in the input data");
                continue;
            }
            if (!system.contains("业务语义") || system.contains("MANY_TO_ONE"))
                throw new AssertionError("Expected dedicated Chinese semantic instructions at the model");
            boolean orders = i == 1;
            if (data.size() != 3 || data.path("metadata").size() != 3
                    || data.path("metadata").path("fields").size() != 1
                    || !orders && !data.path("fields").path(0).equals(JSON.readTree(
                            "{\"fieldName\":\"Id\",\"businessName\":true,\"description\":true,\"aliases\":true}")))
                throw new AssertionError("Only the physical metadata and explicit targets may be sent: " + data);
            if (!data.path("metadata").path("name").asText().equals(TABLES.get(i - 1))
                    || !data.path("metadata").path("fields").get(0).path("name").asText().equals(orders ? "Customer_ID" : "Id")
                    || data.path("table").path("description").asBoolean() != orders
                    || data.path("fields").size() != (orders ? 0 : 1))
                throw new AssertionError("Unexpected table or field generation targets: " + data);
        }
        System.out.println("PASS selected tables sent together; one pending typed join with Chinese evidence and uncertainty, repeat skipped; unrelated table omitted by controlled model");
        System.out.println("PASS English datasource page, existing editor preference, two independent per-table requests, protected confirmed rows and persisted counts");
    }
}

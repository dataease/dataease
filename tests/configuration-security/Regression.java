import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.cache.DECacheService;
import io.dataease.config.ConfigurationTransport;
import io.dataease.config.ConfigurationRequestAdvice;
import io.dataease.config.ConfigurationResponseAdvice;
import io.dataease.exception.DEException;
import io.dataease.rsa.dao.entity.CoreRsa;
import io.dataease.rsa.manage.RsaManage;
import io.dataease.utils.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.*;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.*;
import org.springframework.web.context.request.*;

public class Regression {
    static final ObjectMapper JSON = new ObjectMapper();
    static int assertions;
    static void check(boolean condition) {
        assertions++;
        if (!condition) throw new AssertionError("Assertion " + assertions);
    }
    static void denied(Runnable action) {
        try { action.run(); throw new AssertionError("Expected rejection"); }
        catch (DEException expected) { assertions++; }
    }
    static void inject(Object target, String field, Object value) throws Exception {
        var f = target.getClass().getDeclaredField(field); f.setAccessible(true); f.set(target, value);
    }
    @org.springframework.web.bind.annotation.RestController
    public static class ConfigurationController {
        @io.dataease.auth.ConfigurationSecurity(encryptedRequest = true)
        @org.springframework.web.bind.annotation.PostMapping("/de2api/datasource/checkRepeat")
        public String check(@org.springframework.web.bind.annotation.RequestBody io.dataease.api.ds.vo.BusiDsRequest request) {
            return request.getType();
        }
        @org.springframework.web.bind.annotation.PostMapping("/de2api/ordinary/datasource/save")
        public Map<String, String> ordinary(@org.springframework.web.bind.annotation.RequestBody Map<String, String> body) {
            return body;
        }
    }
    static class Transport extends ConfigurationTransport {
        String identity = "user:org";
        @Override protected String owner() { return identity; }
    }
    static class Cache implements DECacheService<Object> {
        final Map<String, Object> values = new ConcurrentHashMap<>();
        public void put(String cache, String key, Object value, Long ttl, TimeUnit unit) { values.put(key, value); }
        public Object get(String cache, String key) { return values.get(key); }
        public Object take(String cache, String key) { return values.remove(key); }
        public boolean cacheExist(String name) { return true; }
        public boolean keyExist(String cache, String key) { return values.containsKey(key); }
        public void keyRemove(String cache, String key) { values.remove(key); }
    }
    static byte[] seal(Transport transport, String path, Object data, Path root, Path temp) throws Exception {
        var challenge = transport.issue(path);
        Path input = temp.resolve("challenge.json"), output = temp.resolve("envelope.json");
        JSON.writeValue(input.toFile(), Map.of("challenge", challenge, "path", path, "data", data));
        Process process = new ProcessBuilder("node", root.resolve("tests/configuration-security/seal.cjs").toString(),
                root.resolve("core/core-frontend").toString(), input.toString(), output.toString()).inheritIO().start();
        check(process.waitFor() == 0);
        return Files.readAllBytes(output);
    }
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]), temp = Path.of(args[1]);
        new io.dataease.i18n.Translator().setMessageSource(new org.springframework.context.support.StaticMessageSource());
        var context = new StaticApplicationContext();
        var cache = new Cache(); context.getBeanFactory().registerSingleton("dECacheService", cache);
        new CommonBeanFactory().setApplicationContext(context);
        var pair = RsaUtils.generate();
        var rsa = new CoreRsa(); rsa.setPublicKey(pair.getPublicKey()); rsa.setPrivateKey(pair.getPrivateKey());
        rsa.setAesKey("abcdefghijklmnop");
        new RsaUtils().setRsaManage(new RsaManage() { @Override public CoreRsa query() { return rsa; } });
        check(RsaUtils.publicKey().equals(pair.getPublicKey()));
        check(!RsaUtils.publicKey().contains(rsa.getAesKey()));
        // Retained historical storage formats, including the pre-deployment-key format.
        String plain = "{\"password\":\"fixture-password\"}";
        String legacy = CodingUtil.aesEncrypt(plain, "www.fit2cloud.co", "1234567890123456");
        EncryptUtils.initKeys(rsa.getAesKey());
        check(plain.equals(EncryptUtils.aesDecrypt(legacy)));
        check(plain.equals(EncryptUtils.aesDecrypt(EncryptUtils.aesEncrypt(plain))));
        String original = "{\"host\":\"db.local\",\"password\":\"fixture-password\",\"sshKey\":\"fixture-key\"}";
        var originalTree = JSON.readTree(original);
        var masked = ConfigurationCredentials.mask(originalTree);
        check(masked.path("password").asText().equals("******"));
        check(masked.path("sshKey").asText().equals("******"));
        check(originalTree.path("password").asText().equals("fixture-password"));
        check(JSON.readTree(ConfigurationCredentials.restore(masked.toString(), original)).equals(originalTree));
        denied(() -> ConfigurationCredentials.restore(masked.toString(), null));
        denied(() -> ConfigurationCredentials.restore(masked.toString().replace("db.local", "attacker.local"), original));
        check(JSON.readTree(ConfigurationCredentials.restore("{\"host\":\"db.local\",\"password\":\"\"}", original))
                .path("password").asText().isEmpty());
        var wrappedConfig = ConfigurationCredentials.mask(JSON.readTree("{\"configuration\":\"{\\\"host\\\":\\\"db.local\\\",\\\"jdbc\\\":\\\"jdbc:test?password=fixture\\\"}\"}"));
        var inner = JSON.readTree(wrappedConfig.path("configuration").asText());
        check(inner.path("host").asText().equals("db.local")); check(inner.path("jdbc").asText().equals("******"));
        check(ConfigurationCredentials.mask(JSON.readTree("{\"headers\":[{\"name\":\"Authorization\",\"value\":\"Bearer fixture\"}]}"))
                .path("headers").get(0).path("value").asText().equals("******"));
        check(ConfigurationCredentials.mask(JSON.readTree("{\"sslKey\":\"fixture-private-key\"}"))
                .path("sslKey").asText().equals("******"));
        check(ConfigurationCredentials.mask(JSON.readTree("{\"raw\":\"{\\\"password\\\":\\\"fixture\\\"}\"}"))
                .path("raw").asText().equals("******"));
        check(JSON.readTree(ConfigurationCredentials.restore("{\"name\":\"******\",\"host\":\"db.local\"}", null))
                .path("name").asText().equals("******"));
        check(ConfigurationCredentials.mask(JSON.readTree("{\"name\":\"token\",\"value\": [\"fixture\"]}"))
                .path("value").asText().equals("******"));
        // Three literal asterisks are now an ordinary password value, not the placeholder.
        check(JSON.readTree(ConfigurationCredentials.restore("{\"password\":\"***\"}", original))
                .path("password").asText().equals("***"));
        check(ConfigurationCredentials.mask(JSON.readTree("{\"password\":\"***\"}"))
                .path("password").asText().equals("******"));
        // Credential contracts shared by JDBC, SSH, remote files and API authentication.
        for (String field : List.of("password", "sshPassword", "sshKeyPassword", "sslKey", "passwd",
                "clientSecret", "accessToken", "apiKey")) {
            String stored = JSON.writeValueAsString(Map.of("host", "db.local", field, "fixture-secret"));
            String concealed = ConfigurationCredentials.mask(JSON.readTree(stored)).toString();
            check(JSON.readTree(concealed).path(field).asText().equals("******"));
            check(JSON.readTree(ConfigurationCredentials.restore(concealed, stored)).equals(JSON.readTree(stored)));
            String cleared = JSON.writeValueAsString(Map.of("host", "db.local", field, ""));
            check(JSON.readTree(ConfigurationCredentials.restore(cleared, stored)).path(field).asText().isEmpty());
            denied(() -> ConfigurationCredentials.restore(concealed.replace("db.local", "other.local"), stored));
        }
        String apiStored = "{\"url\":\"https://fixture.invalid/api\",\"headers\":[{\"name\":\"Authorization\",\"value\":\"Bearer fixture-secret\"}]}";
        String apiMasked = ConfigurationCredentials.mask(JSON.readTree(apiStored)).toString();
        check(JSON.readTree(ConfigurationCredentials.restore(apiMasked, apiStored)).equals(JSON.readTree(apiStored)));
        denied(() -> ConfigurationCredentials.restore(apiMasked.replace("fixture.invalid", "other.invalid"), apiStored));
        String clearedHeader = apiMasked.replace("******", "");
        check(JSON.readTree(ConfigurationCredentials.restore(clearedHeader, apiStored)).path("headers").get(0).path("value").asText().isEmpty());
        // Actual MySQL edit DTO adds defaults and derived jdbc; renaming must retain credentials.
        String mysqlStored = "{\"host\":\"db.local\",\"port\":3306,\"dataBase\":\"reports\",\"username\":\"reader\",\"password\":\"fixture-password\"}";
        var storedDatasource = new io.dataease.dao.auto.entity.CoreDatasource();
        storedDatasource.setId(101L);
        storedDatasource.setType("mysql");
        storedDatasource.setConfiguration(mysqlStored);
        var server = new io.dataease.datasource.server.DatasourceServer();
        inject(server, "dataSourceManage", new io.dataease.datasource.manage.DataSourceManage(null) {
            @Override public io.dataease.dao.auto.entity.CoreDatasource getCoreDatasource(Long id) { return storedDatasource; }
            @Override public io.dataease.extensions.datasource.dto.DatasourceDTO getDs(Long id) {
                var dto = new io.dataease.extensions.datasource.dto.DatasourceDTO();
                dto.setId(id); dto.setType("mysql"); dto.setConfiguration(mysqlStored); return dto;
            }
        });
        inject(server, "corePermissionManage", new io.dataease.system.manage.CorePermissionManage() {
            @Override public boolean checkAuth(io.dataease.api.permissions.auth.dto.BusiPerCheckDTO request) { return true; }
        });
        // An undecrypted envelope/malformed request must not become a null-type NPE.
        denied(() -> server.checkRepeat(new io.dataease.api.ds.vo.BusiDsRequest()));
        var formDatasource = server.get(101L);
        formDatasource.setName("renamed datasource");
        String mysqlForm = ConfigurationCredentials.mask(JSON.readTree(formDatasource.getConfiguration())).toString();
        check(JSON.readTree(mysqlForm).has("jdbc"));
        // This reproduces the old raw-JSON comparison failure before the fix.
        denied(() -> ConfigurationCredentials.restore(mysqlForm, mysqlStored));
        var restoreMethod = server.getClass().getDeclaredMethod("restorePlainConfiguration", String.class, Long.class, String.class);
        restoreMethod.setAccessible(true);
        String mysqlRestored = (String) restoreMethod.invoke(server, mysqlForm, 101L, "mysql");
        check(JSON.readTree(mysqlRestored).path("password").asText().equals("fixture-password"));
        check(JSON.readTree(mysqlRestored).path("host").asText().equals("db.local"));
        try {
            restoreMethod.invoke(server, mysqlForm.replace("db.local", "attacker.local"), 101L, "mysql");
            throw new AssertionError("MySQL destination change must require credentials");
        } catch (java.lang.reflect.InvocationTargetException expected) {
            check(expected.getCause() instanceof DEException);
        }
        String oldArray = "[{\"name\":\"a\",\"password\":\"first\"},{\"name\":\"b\",\"password\":\"second\"}]";
        denied(() -> ConfigurationCredentials.restore("[{\"name\":\"b\",\"password\":\"******\"}]", oldArray));
        var transport = new Transport();
        String path = "/datasource/save";
        var payload = Map.of("configuration", "长配置".repeat(4000), "password", "fixture-password");
        byte[] encrypted = seal(transport, path, payload, root, temp);
        check(!new String(encrypted, StandardCharsets.UTF_8).contains("fixture-password"));
        check(JSON.readTree(transport.decrypt(encrypted, path, false)).equals(JSON.valueToTree(payload)));
        check(JSON.readTree(transport.decrypt(encrypted, path, true)).equals(JSON.valueToTree(payload)));
        denied(() -> transport.decrypt(encrypted, path, true));
        byte[] wrongPath = seal(transport, path, payload, root, temp);
        denied(() -> transport.decrypt(wrongPath, "/engine/save", true));
        byte[] wrongUser = seal(transport, path, payload, root, temp); transport.identity = "other:org";
        denied(() -> transport.decrypt(wrongUser, path, true)); transport.identity = "user:org";
        byte[] tampered = seal(transport, path, payload, root, temp);
        var broken = JSON.readTree(tampered); ((com.fasterxml.jackson.databind.node.ObjectNode) broken).put("iv", Base64.getEncoder().encodeToString(new byte[12]));
        denied(() -> transport.decrypt(broken.toString().getBytes(StandardCharsets.UTF_8), path, true));
        denied(() -> transport.decrypt("{}".getBytes(StandardCharsets.UTF_8), path, true));
        var expired = transport.issue(path); cache.values.put(expired.get("requestId").toString(), "user:org\n" + path + "\n0");
        byte[] expiredBody = JSON.writeValueAsBytes(expired);
        denied(() -> transport.decrypt(expiredBody, path, true));
        // Spring may pass a wrapped HttpInputMessage, not ServletServerHttpRequest.
        var advice = new ConfigurationRequestAdvice(); inject(advice, "transport", transport);
        byte[] request = seal(transport, path, payload, root, temp);
        var servlet = new MockHttpServletRequest("POST", "/de2api/apisix/check"); servlet.setContextPath("/de2api");
        servlet.addHeader("X-Forwarded-Uri", "/de2api" + path); servlet.addHeader("X-Forwarded-Method", "POST");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servlet));
        check(JSON.readTree(advice.beforeBodyRead(new MockHttpInputMessage(request), null, Object.class,
                MappingJackson2HttpMessageConverter.class).getBody()).equals(JSON.valueToTree(payload)));
        servlet.setRequestURI("/de2api" + path);
        check(JSON.readTree(advice.beforeBodyRead(new MockHttpInputMessage(request), null, Object.class,
                MappingJackson2HttpMessageConverter.class).getBody()).equals(JSON.valueToTree(payload)));
        servlet.setRequestURI("/de2api/datasource/%73ave;v=1");
        try {
            advice.beforeBodyRead(new MockHttpInputMessage("{}".getBytes(StandardCharsets.UTF_8)), null,
                    Object.class, MappingJackson2HttpMessageConverter.class);
            throw new AssertionError("Encoded route accepted plaintext");
        } catch (DEException expected) { assertions++; }
        RequestContextHolder.resetRequestAttributes();
        // Real deployment uses MVC /de2api prefixes with an EMPTY servlet context path.
        for (String prefix : List.of("/de2api", "/casbi/de2api", "/oidcbi/de2api")) {
            byte[] mvcBody = seal(transport, "/datasource/checkRepeat", Map.of("type", "mysql", "name", "renamed"), root, temp);
            var mvcRequest = new MockHttpServletRequest("POST", prefix + "/datasource/checkRepeat");
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mvcRequest));
            var decodedBody = advice.beforeBodyRead(new MockHttpInputMessage(mvcBody), null, Object.class,
                    MappingJackson2HttpMessageConverter.class);
            check(JSON.readTree(decodedBody.getBody()).path("type").asText().equals("mysql"));
            RequestContextHolder.resetRequestAttributes();
        }
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(new ConfigurationController()).setControllerAdvice(advice).build();
        byte[] checkBody = seal(transport, "/datasource/checkRepeat", Map.of("type", "mysql", "name", "renamed"), root, temp);
        var mvcResult = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/de2api/datasource/checkRepeat").contentType(MediaType.APPLICATION_JSON).content(checkBody))
                .andReturn().getResponse();
        check(mvcResult.getStatus() == 200);
        check(mvcResult.getContentAsString().equals("mysql"));
        var responseAdvice = new ConfigurationResponseAdvice(); inject(responseAdvice, "json", JSON);
        var response = new ServletServerHttpResponse(new MockHttpServletResponse());
        var body = new io.dataease.extensions.datasource.dto.DatasourceDTO();
        body.setConfiguration(original);
        body.setName("password=ordinary-name");
        var responseParameter = new org.springframework.core.MethodParameter(
                io.dataease.datasource.server.DatasourceServer.class.getMethod("get", Long.class), -1);
        check(responseAdvice.supports(responseParameter, MappingJackson2HttpMessageConverter.class));
        var requestParameter = new org.springframework.core.MethodParameter(
                io.dataease.datasource.server.DatasourceServer.class.getMethod("checkRepeat", io.dataease.api.ds.vo.BusiDsRequest.class), 0);
        check(advice.supports(requestParameter, Object.class, MappingJackson2HttpMessageConverter.class));
        var unmarked = new org.springframework.core.MethodParameter(
                io.dataease.datasource.server.DatasourceServer.class.getMethod("getName", Long.class), -1);
        check(!responseAdvice.supports(unmarked, MappingJackson2HttpMessageConverter.class));
        check(!advice.supports(unmarked, Object.class, MappingJackson2HttpMessageConverter.class));
        var result = responseAdvice.beforeBodyWrite(body, responseParameter, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                new ServletServerHttpRequest(new MockHttpServletRequest("GET", "/datasource/get/123")), response);
        check(result.getClass() == body.getClass());
        var maskedDto = (io.dataease.extensions.datasource.dto.DatasourceDTO) result;
        check(!maskedDto.getConfiguration().contains("fixture-password"));
        check(maskedDto.getName().equals(body.getName()));
        check(body.getConfiguration().equals(original));
        check(response.getHeaders().getCacheControl().equals("no-store"));
        check(responseAdvice.beforeBodyWrite(body, unmarked, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class, null, null) == body);
        var api = new io.dataease.extensions.datasource.dto.ApiDefinition();
        api.setRequest(new io.dataease.extensions.datasource.dto.ApiDefinitionRequest());
        api.getRequest().getAuthManager().setPassword("fixture-password");
        api.setData(List.of(Map.of("password", "ordinary-sample")));
        var apiParameter = new org.springframework.core.MethodParameter(
                io.dataease.datasource.server.DatasourceServer.class.getMethod("checkApiDatasource", Map.class), -1);
        var maskedApi = (io.dataease.extensions.datasource.dto.ApiDefinition) responseAdvice.beforeBodyWrite(api,
                apiParameter, MediaType.APPLICATION_JSON, MappingJackson2HttpMessageConverter.class, null, response);
        check(maskedApi.getRequest().getAuthManager().getPassword().equals("******"));
        check(maskedApi.getData().get(0).get("password").equals("ordinary-sample"));
        check(api.getRequest().getAuthManager().getPassword().equals("fixture-password"));
        var page = new io.dataease.result.PageResult<io.dataease.extensions.datasource.dto.DatasourceDTO>();
        page.setRecords(List.of(body)); page.setTotal(17); page.setCurrent(2);
        var maskedPage = (io.dataease.result.PageResult<?>) responseAdvice.beforeBodyWrite(page, responseParameter,
                MediaType.APPLICATION_JSON, MappingJackson2HttpMessageConverter.class, null, response);
        check(maskedPage.getClass() == page.getClass());
        check(maskedPage.getTotal() == 17 && maskedPage.getCurrent() == 2);
        check(!((io.dataease.extensions.datasource.dto.DatasourceDTO) maskedPage.getRecords().get(0))
                .getConfiguration().contains("fixture-password"));
        check(body.getConfiguration().equals(original));
        var wrapped = new io.dataease.result.ResultResponseBodyAdvice().beforeBodyWrite(maskedDto, responseParameter,
                MediaType.APPLICATION_JSON, MappingJackson2HttpMessageConverter.class, null, response);
        check(wrapped instanceof io.dataease.result.ResultMessage);
        check(((io.dataease.result.ResultMessage) wrapped).getData() == maskedDto);
        var syncUpdate = new org.springframework.core.MethodParameter(io.dataease.api.sync.datasource.api.SyncDatasourceApi.class
                .getMethod("update", io.dataease.extensions.sync.model.datasource.DatasourceRequest.class), 0);
        check(advice.supports(syncUpdate, Object.class, MappingJackson2HttpMessageConverter.class));
        var syncTask = new io.dataease.extensions.sync.model.task.TaskInfoVO();
        var source = new io.dataease.extensions.sync.model.task.Source();
        var syncDs = new io.dataease.extensions.sync.model.datasource.DatasourceDTO();
        syncDs.setConfiguration(original); source.setDatasource(syncDs); syncTask.setSource(source);
        var taskParameter = new org.springframework.core.MethodParameter(io.dataease.api.sync.task.api.TaskApi.class
                .getMethod("getOneById", String.class), -1);
        var maskedTask = (io.dataease.extensions.sync.model.task.TaskInfoVO) responseAdvice.beforeBodyWrite(syncTask,
                taskParameter, MediaType.APPLICATION_JSON, MappingJackson2HttpMessageConverter.class, null, response);
        check(maskedTask.getClass() == syncTask.getClass());
        check(!maskedTask.getSource().getDatasource().getConfiguration().contains("fixture-password"));
        check(syncDs.getConfiguration().equals(original));
        var ordinaryMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(new ConfigurationController())
                .setControllerAdvice(advice, responseAdvice, new io.dataease.result.ResultResponseBodyAdvice()).build();
        var ordinaryResult = ordinaryMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/de2api/ordinary/datasource/save").contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"ordinary-sample\"}")).andReturn().getResponse();
        check(ordinaryResult.getStatus() == 200);
        check(JSON.readTree(ordinaryResult.getContentAsString()).path("password").asText().equals("ordinary-sample"));
        Set<String> encryptedPaths = new HashSet<>();
        for (Class<?> apiContract : List.of(io.dataease.api.ds.DatasourceApi.class, io.dataease.api.ds.EngineApi.class,
                io.dataease.api.sync.datasource.api.SyncDatasourceApi.class, io.dataease.api.sync.task.api.TaskApi.class, io.dataease.api.sensitive.SensitiveApi.class)) {
            String prefix = apiContract.getAnnotation(io.dataease.auth.DeApiPath.class).value()[0];
            for (var method : apiContract.getMethods()) {
                var marker = method.getAnnotation(io.dataease.auth.ConfigurationSecurity.class);
                if (marker == null || !marker.encryptedRequest()) continue;
                var mapping = method.getAnnotation(org.springframework.web.bind.annotation.PostMapping.class);
                String protectedPath = prefix + "/" + mapping.value()[0].replaceFirst("^/", "");
                check(ConfigurationTransport.protects(protectedPath));
                encryptedPaths.add(protectedPath);
            }
        }
        check(encryptedPaths.size() == 19);
        SensitiveRegression.run();
        EngineRegression.run();
        System.out.println("PASS: " + assertions + " configuration security assertions (no DB/application startup)");
    }
}

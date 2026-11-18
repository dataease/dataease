import io.dataease.datasource.server.EngineServer;
import io.dataease.datasource.dao.auto.entity.CoreDeEngine;
import io.dataease.datasource.dao.auto.repository.CoreDeEngineRepository;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.utils.ConfigurationCredentials;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Regression for form defaults being mistaken for a changed connection target. */
class EngineRegression {
    static String restore(EngineServer server, DatasourceDTO request) throws Exception {
        Method method = EngineServer.class.getDeclaredMethod("restoreConfiguration", DatasourceDTO.class);
        method.setAccessible(true);
        try { return (String) method.invoke(server, request); }
        catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException runtime) throw runtime;
            throw e;
        }
    }
    static void run() throws Exception {
        for (String type : List.of("mysql", "StarRocks", "oracle", "pg", "kingbase", "gaussdb", "sqlServer", "h2")) {
            var stored = new CoreDeEngine(); stored.setId(17L); stored.setType(type);
            stored.setConfiguration("{\"host\":\"db.local\",\"port\":3306,\"dataBase\":\"fixture\",\"username\":\"fixture-user\",\"password\":\"fixture-password\",\"jdbc\":\"jdbc:h2:mem:security_engine\",\"queryTimeout\":30}");
            var server = new EngineServer();
            Regression.inject(server, "coreDeEngineRepository", Proxy.newProxyInstance(CoreDeEngineRepository.class.getClassLoader(), new Class[]{CoreDeEngineRepository.class},
                    (proxy, method, args) -> method.getName().equals("findAll") ? List.of(stored) : method.getName().equals("findById") ? Optional.of(stored) : null));
            var request = server.getEngine();
            var masked = ConfigurationCredentials.mask(Regression.JSON.readTree(request.getConfiguration()));
            ((com.fasterxml.jackson.databind.node.ObjectNode) masked).put("queryTimeout", 45);
            request.setConfiguration(Base64.getEncoder().encodeToString(masked.toString().getBytes(StandardCharsets.UTF_8)));
            var restored = Regression.JSON.readTree(restore(server, request));
            Regression.check(restored.path("password").asText().equals("fixture-password"));
            Regression.check(restored.path("queryTimeout").asInt() == 45);
            ((com.fasterxml.jackson.databind.node.ObjectNode) masked).put("host", "other.local");
            request.setConfiguration(Base64.getEncoder().encodeToString(masked.toString().getBytes(StandardCharsets.UTF_8)));
            try { restore(server, request); throw new AssertionError("Changed engine destination accepted"); }
            catch (io.dataease.exception.DEException expected) { Regression.check(true); }
            request.setType(type.equals("mysql") ? "pg" : "mysql");
            try { restore(server, request); throw new AssertionError("Type change reused a masked credential"); }
            catch (io.dataease.exception.DEException expected) { Regression.check(true); }
            request.setConfiguration(Base64.getEncoder().encodeToString(
                    "{\"host\":\"new.local\",\"password\":\"fresh-password\"}".getBytes(StandardCharsets.UTF_8)));
            Regression.check(Regression.JSON.readTree(restore(server, request)).path("password").asText().equals("fresh-password"));
        }
    }
}

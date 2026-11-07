import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.*;
import java.util.Map;

/** Only creates an engine when this isolated environment had no engine at all. */
public class EngineFixture {
    public static void main(String[] args) throws Exception {
        Path file = Path.of(args[1]);
        ObjectMapper json = new ObjectMapper();
        try (Connection connection = DriverManager.getConnection(System.getenv("LOCAL_DB_URL"),
                System.getenv("LOCAL_DB_USERNAME"), System.getenv("LOCAL_DB_PASSWORD"))) {
            if (args[0].equals("engine-check")) {
                if (Files.exists(file)) throw new IllegalStateException("An engine fixture already exists");
                try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM core_de_engine")) {
                    rows.next();
                    if (rows.getLong(1) != 0) throw new IllegalStateException("Do not replace an existing engine");
                }
                Files.writeString(file, json.writeValueAsString(Map.of("originalCount", 0)));
                Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"));
                System.out.println("Confirmed isolated engine configuration was empty");
                return;
            }
            if (!Files.exists(file)) return;
            var state = json.readTree(Files.readString(file));
            String name = state.path("name").asText();
            if (!name.isEmpty()) {
                if (!name.matches("codex-security-engine-[0-9]+")) throw new IllegalArgumentException("Not a test engine");
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM core_de_engine WHERE name LIKE ?")) {
                    statement.setString(1, name + "%");
                    statement.executeUpdate();
                }
            }
            try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM core_de_engine")) {
                rows.next();
                if (rows.getLong(1) != 0) throw new IllegalStateException("Engine state differs from the original empty state");
            }
            Files.delete(file);
            System.out.println("Test engine removed; original empty configuration verified");
        }
    }
}

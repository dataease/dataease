package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.dao.entity.CoreAiModel;
import io.dataease.ai.dao.entity.CoreAiOrgPolicy;
import io.dataease.ai.dao.entity.CoreAiSetting;
import io.dataease.ai.manage.AiKeyringManage;
import io.dataease.ai.support.AiKeyMaterial;
import io.dataease.ai.support.AiSecrets;
import io.dataease.i18n.Translator;
import io.dataease.listener.sql.AiConfigurationInit;
import io.dataease.listener.sql.AiKeyringInit;
import jakarta.persistence.EntityManager;
import org.h2.tools.Server;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Real database locks across independent JVMs; no DataEase startup, business DB or model provider. */
public class KeyringRegression {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static int checks;

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }

    private static void rejects(Runnable work, String message) {
        try { work.run(); }
        catch (RuntimeException expected) { checks++; return; }
        throw new AssertionError(message);
    }

    private static SessionFactory factory(String url) {
        return new Configuration().addAnnotatedClass(CoreAiSetting.class).addAnnotatedClass(CoreAiModel.class)
                .addAnnotatedClass(CoreAiOrgPolicy.class)
                .setProperty("hibernate.connection.url", url).setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.username", "sa").setProperty("hibernate.connection.password", "")
                .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                .setProperty("hibernate.hbm2ddl.auto", "none").setProperty("hibernate.show_sql", "false").buildSessionFactory();
    }

    public static void main(String[] args) throws Exception {
        new Translator().setMessageSource(new StaticMessageSource());
        try (var factory = factory(args[0])) {
            var em = SharedEntityManagerCreator.createSharedEntityManager(factory);
            var keys = new AiKeyringManage(em, new JpaTransactionManager(factory), "", "");
            Path observation = Path.of(args[1]);
            Files.createFile(observation.resolveSibling(observation.getFileName() + ".ready"));
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(40);
            while (!Files.exists(observation.resolveSibling("initialize"))) {
                if (System.nanoTime() > deadline) throw new AssertionError("initialization barrier timed out");
                Thread.sleep(20);
            }
            keys.initialize();
            var secrets = new AiSecrets(keys, JSON);
            Files.write(Path.of(args[1]), List.of(secrets.encrypt("synthetic-worker", 42L),
                    secrets.sign("test", Map.of("node", args[2]), 60000), keys.current().fingerprint()));
        }
    }

    public static void run() throws Exception {
        String previousBind = System.getProperty("h2.bindAddress");
        System.setProperty("h2.bindAddress", "127.0.0.1");
        Path output = Files.createTempDirectory("ai-keyring-regression-");
        Server server = Server.createTcpServer("-tcpPort", "0", "-tcpDaemon", "-ifNotExists").start();
        try {
            String url = "jdbc:h2:tcp://127.0.0.1:" + server.getPort() + "/mem:ai_keyring;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=15000";
            try (var factory = factory(url)) {
                EntityManager em = SharedEntityManagerCreator.createSharedEntityManager(factory);
                var manager = new JpaTransactionManager(factory);
                var tx = new TransactionTemplate(manager);
                var jdbc = new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
                new AiConfigurationInit(jdbc, factory, em, manager).execute();
                for (String column : List.of("key_source", "key_ring", "active_key_id", "key_revision", "key_fingerprint")) {
                    jdbc.execute("alter table core_ai_setting drop column " + column);
                }
                var migration = new AiKeyringInit(jdbc, factory);
                // Retry a partial older migration using the current entity mapping.
                new AiConfigurationInit(jdbc, factory, em, manager).execute();
                migration.execute(); migration.execute();
                check(jdbc.queryForObject("select count(*) from core_ai_setting", Long.class) == 1, "upgrade is additive and idempotent");
                var keys = new AiKeyringManage(em, manager, "", "");
                rejects(keys::current, "uninitialized ring must not be usable");
                rejects(() -> tx.executeWithoutResult(status -> keys.initialize()), "initialization cannot join an outer transaction");

                var failing = new JpaTransactionManager(factory) {
                    private boolean fail = true;
                    @Override protected void doCommit(DefaultTransactionStatus status) {
                        if (fail) { fail = false; throw new TransactionSystemException("synthetic commit failure"); }
                        super.doCommit(status);
                    }
                };
                failing.setRollbackOnCommitFailure(true);
                var uncommitted = new AiKeyringManage(em, failing, "", "");
                rejects(uncommitted::initialize, "commit failure must propagate");
                rejects(uncommitted::current, "uncommitted candidate must never become available");
                check(jdbc.queryForObject("select key_revision from core_ai_setting", Long.class) == null, "failed commit leaves no initialized marker");

                List<Process> workers = new ArrayList<>();
                try {
                    for (int node = 0; node < 4; node++) workers.add(worker(url, output, node));
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(35);
                    for (int node = 0; node < 4; node++) {
                        while (!Files.exists(output.resolve("node-" + node + ".ready"))) {
                            if (!workers.get(node).isAlive() || System.nanoTime() > deadline) {
                                throw new AssertionError("worker did not reach initialization barrier");
                            }
                            Thread.sleep(20);
                        }
                    }
                    Files.createFile(output.resolve("initialize"));
                    for (int node = 0; node < workers.size(); node++) {
                        Process worker = workers.get(node);
                        check(worker.waitFor(45, TimeUnit.SECONDS), "worker completes");
                        if (worker.exitValue() != 0) throw new AssertionError("worker failed: " + Files.readString(output.resolve("node-" + node + ".log")));
                        check(true, "independent JVM initialization succeeds");
                    }
                } finally {
                    for (Process worker : workers) if (worker.isAlive()) worker.destroyForcibly();
                }
                var secrets = new AiSecrets(keys, JSON);
                String initialFingerprint = keys.current().fingerprint();
                for (int node = 0; node < 4; node++) {
                    var result = Files.readAllLines(output.resolve("node-" + node));
                    check("synthetic-worker".equals(secrets.decrypt(result.get(0), 42L)), "cross-node ciphertext decrypts");
                    check(Integer.toString(node).equals(secrets.verify(result.get(1), "test").get("node")), "cross-node signed proof verifies");
                    check(initialFingerprint.equals(result.get(2)), "all JVMs use the same committed key ring");
                }
                Process restarted = worker(url, output, 4);
                try {
                    check(restarted.waitFor(45, TimeUnit.SECONDS) && restarted.exitValue() == 0, "restart/expansion succeeds");
                    check(initialFingerprint.equals(Files.readAllLines(output.resolve("node-4")).get(2)), "restart does not regenerate");
                } finally { if (restarted.isAlive()) restarted.destroyForcibly(); }
                check(jdbc.queryForObject("select key_revision from core_ai_setting", Long.class) == 1, "one committed generation across JVMs");

                AiKeyMaterial before = keys.current();
                String oldCiphertext = secrets.encrypt("synthetic-model-key", 77L);
                String oldProof = secrets.sign("test", Map.of("id", 77L), 60000);
                tx.executeWithoutResult(status -> {
                    var model = new CoreAiModel();
                    model.setId(77L); model.setName("Legacy"); model.setNameKey("legacy"); model.setBaseUrl("https://mock.invalid/v1");
                    model.setModelIdentifier("mock"); model.setKeyEnvelope(oldCiphertext); em.persist(model);
                    em.find(CoreAiSetting.class, 1L).setMaxInputChars(12345);
                });
                String settingJson = JSON.writeValueAsString(tx.execute(status -> em.find(CoreAiSetting.class, 1L)));
                check(!settingJson.contains("keyRing") && !settingJson.contains("keySource") && !settingJson.contains(before.canonical())
                        && !settingJson.contains("activeKeyId") && !settingJson.contains("keyFingerprint") && !settingJson.contains("keyRevision"), "entity serialization excludes every key field");
                check(before.fingerprint().equals(keys.current().fingerprint()), "ordinary settings updates preserve keys");
                jdbc.update("update core_ai_model set key_envelope = ? where id = 77", "corrupt");
                rejects(() -> secrets.decrypt("corrupt", 77L), "corrupted ciphertext rejects decryption");
                keys.initialize();
                check(before.fingerprint().equals(keys.current().fingerprint()), "decryption failure never regenerates or replaces an initialized key ring");
                jdbc.update("update core_ai_model set key_envelope = ? where id = 77", oldCiphertext);
                migration.execute();
                check(before.fingerprint().equals(keys.current().fingerprint()), "repeated migration preserves existing keys");
                var extra = AiKeyMaterial.generate();
                String rotated = before.canonical() + "," + extra.canonical();
                keys.replace(1, rotated, extra.activeKey());
                check(extra.activeKey().equals(keys.current().activeKey()), "database rotation changes active key");
                check("synthetic-model-key".equals(secrets.decrypt(oldCiphertext, 77L)), "old ciphertext survives rotation");
                check(secrets.verify(oldProof, "test") != null, "old proof survives rotation");
                rejects(() -> keys.replace(1, rotated, extra.activeKey()), "stale key version rejected");
                rejects(() -> keys.replace(2, extra.canonical(), extra.activeKey()), "rotation cannot remove old keys");
                check(jdbc.queryForObject("select key_revision from core_ai_setting", Long.class) == 2, "failed rotations do not change key version");
                new AiKeyringManage(em, manager, "", "").initialize();
                check(extra.activeKey().equals(keys.current().activeKey()), "a second node adopts committed rotation");
                rejects(() -> new AiKeyringManage(em, manager, rotated, extra.activeKey()).initialize(), "external config cannot silently override database mode");

                String savedRing = keys.current().canonical();
                jdbc.update("update core_ai_setting set key_ring = null");
                rejects(keys::initialize, "missing initialized key material must not regenerate");
                rejects(() -> secrets.encrypt("x", 1L), "missing key material blocks cached consumers");
                check(jdbc.queryForObject("select key_revision from core_ai_setting", Long.class) == 2, "missing keys preserve initialization marker");
                jdbc.update("update core_ai_setting set key_ring = ?", savedRing);
                jdbc.update("update core_ai_setting set key_fingerprint = ?", "invalid");
                rejects(keys::current, "corrupted key metadata rejected");
                jdbc.update("update core_ai_setting set key_fingerprint = ?", AiKeyMaterial.parse(savedRing, extra.activeKey()).fingerprint());
                jdbc.update("update core_ai_setting set key_source = null, key_ring = null, active_key_id = null, key_revision = null, key_fingerprint = null");
                rejects(keys::initialize, "legacy ciphertext without keys must not generate a new ring");
                rejects(() -> new AiKeyringManage(em, manager, "bad", "legacy").initialize(), "invalid explicit config fails closed");
                rejects(() -> new AiKeyringManage(em, manager, extra.canonical(), extra.activeKey()).initialize(), "unknown legacy key ID blocks registration");
                check(jdbc.queryForObject("select key_source from core_ai_setting", String.class) == null, "failed legacy validation does not register keys");
                var external = new AiKeyringManage(em, manager, before.canonical(), before.activeKey());
                String wrongBytes = before.activeKey() + ":" + extra.canonical().split(":", 2)[1];
                rejects(() -> new AiKeyringManage(em, manager, wrongBytes, before.activeKey()).initialize(), "wrong legacy key bytes fail GCM validation");
                external.initialize();
                check(jdbc.queryForObject("select key_ring from core_ai_setting", String.class) == null, "external key bytes are never copied into DB");
                check("synthetic-model-key".equals(new AiSecrets(external, JSON).decrypt(oldCiphertext, 77L)), "manual key ring migration preserves ciphertext");
                rejects(keys::current, "node missing explicit config is refused in external mode");
                rejects(() -> new AiKeyringManage(em, manager, extra.canonical(), before.activeKey()).current(), "different material/active key rejected across nodes");
                rejects(() -> new AiKeyringManage(em, manager, wrongBytes, before.activeKey()).current(), "same key ID with different bytes is rejected across nodes");
                external.replace(1, rotated, extra.activeKey());
                rejects(external::current, "old external node is refused after coordinated rotation");
                var externalNext = new AiKeyringManage(em, manager, rotated, extra.activeKey());
                externalNext.initialize();
                check(new AiSecrets(externalNext, JSON).verify(oldProof, "test") != null, "external rotated node verifies old proofs");
                check("synthetic-model-key".equals(new AiSecrets(externalNext, JSON).decrypt(oldCiphertext, 77L)), "external rotation retains old decryption keys");
                check(jdbc.queryForObject("select key_ring from core_ai_setting", String.class) == null, "rotation never persists external key bytes");
                jdbc.execute("shutdown");
                rejects(externalNext::current, "database unavailable blocks key operations");
            }
            System.out.println("PASS AI keyring regression " + checks + " assertions (5 independent JVMs, isolated H2 TCP)");
        } finally {
            server.stop();
            if (previousBind == null) System.clearProperty("h2.bindAddress"); else System.setProperty("h2.bindAddress", previousBind);
            try (var paths = Files.walk(output)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
    }

    private static Process worker(String url, Path output, int node) throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        return new ProcessBuilder(java, "-cp", System.getProperty("java.class.path"), KeyringRegression.class.getName(), url,
                output.resolve("node-" + node).toString(), Integer.toString(node))
                .redirectErrorStream(true).redirectOutput(output.resolve("node-" + node + ".log").toFile()).start();
    }
}

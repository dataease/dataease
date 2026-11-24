import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.context.support.StaticMessageSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/** Exercises the real provider, Calcite schema and DBCP against owned in-memory databases. */
public class Regression {
    private static final String URL = "jdbc:h2:mem:datasource_schema_regression";
    private static final String SECOND_URL = "jdbc:h2:mem:datasource_schema_regression_second";
    private static final int ALIASES = 64;
    private static final int ITERATIONS = 2000;
    private static final CountDownLatch QUERY_STARTED = new CountDownLatch(1);
    private static final CountDownLatch QUERY_RELEASE = new CountDownLatch(1);
    private static final CountDownLatch CROSS_STARTED = new CountDownLatch(1);
    private static final CountDownLatch CROSS_RELEASE = new CountDownLatch(1);

    private static DatasourceSchemaDTO datasource(long id) {
        var datasource = new DatasourceSchemaDTO();
        datasource.setId(id);
        datasource.setType("h2");
        datasource.setName("owned-schema-regression");
        datasource.setSchemaAlias("s_a_" + id);
        datasource.setConfiguration("{\"jdbc\":\"" + (id == 2 ? SECOND_URL : URL) + "\",\"dataBase\":\"PUBLIC\","
                + "\"initialPoolSize\":0,\"minPoolSize\":0,\"maxPoolSize\":4}");
        return datasource;
    }

    private static DatasourceRequest request(long id) {
        var request = new DatasourceRequest();
        request.setDatasource(datasource(id));
        request.setDsList(Map.of(id, datasource(id)));
        return request;
    }

    private static DatasourceRequest crossRequest() {
        var request = request(1);
        request.setIsCross(true);
        request.setDsList(Map.of(1L, datasource(1), 2L, datasource(2)));
        request.setQuery("SELECT a.ID AS ORDER_ID, b.AMOUNT AS ORDER_AMOUNT "
                + "FROM s_a_1.OWNED_ORDERS a JOIN s_a_2.OWNED_ORDERS b ON a.ID = b.ID");
        return request;
    }

    private static void assertFields(List<TableField> fields, String... expected) {
        var actual = fields.stream().map(TableField::getOriginName).toList();
        if (!actual.equals(List.of(expected))) {
            throw new AssertionError("Unexpected fields: " + actual);
        }
    }

    private static void assertCrossResult(CalciteProvider provider) {
        assertCrossResult(provider.fetchResultField(crossRequest()));
    }

    @SuppressWarnings("unchecked")
    private static void assertCrossResult(Map<String, Object> result) {
        assertFields((List<TableField>) result.get("fields"), "ORDER_ID", "ORDER_AMOUNT");
        var rows = (List<String[]>) result.get("data");
        if (rows.size() != 1 || !List.of(rows.getFirst()).equals(List.of("7", "99.25"))) {
            throw new AssertionError("Cross-source query must return the second source's amount");
        }
    }

    private static CoreDatasource deletion(long id) {
        var deleted = new CoreDatasource();
        deleted.setId(id);
        return deleted;
    }

    private static void assertTables(CalciteProvider provider, long id) {
        var tables = provider.getTables(request(id));
        if (tables.size() != 1 || !tables.getFirst().getTableName().equals("OWNED_ORDERS")) {
            throw new AssertionError("Expected the owned ORDERS table for datasource " + id);
        }
    }

    private static void concurrentRefreshAndTables(CalciteProvider provider) throws Exception {
        var start = new CountDownLatch(1);
        var done = new CountDownLatch(17);
        var firstFailure = new AtomicReference<Throwable>();
        var reads = new AtomicInteger();
        List<Thread> workers = new ArrayList<>();
        for (int worker = 0; worker < 17; worker++) {
            final int number = worker;
            var thread = new Thread(() -> {
                try {
                    start.await();
                    var random = new Random(12345 + number);
                    for (int n = 0; n < ITERATIONS; n++) {
                        try {
                            long id = 1 + random.nextInt(ALIASES);
                            if (number == 16) {
                                Thread.sleep(1);
                                assertTables(provider, id);
                                reads.incrementAndGet();
                            } else {
                                provider.update(datasource(id));
                            }
                        } catch (Throwable failure) {
                            firstFailure.compareAndSet(null, failure);
                        }
                    }
                } catch (Throwable failure) {
                    firstFailure.compareAndSet(null, failure);
                } finally {
                    done.countDown();
                }
            }, number == 16 ? "schema-getTables" : "schema-update-" + number);
            thread.setDaemon(true);
            workers.add(thread);
            thread.start();
        }
        start.countDown();
        if (!done.await(15, TimeUnit.SECONDS)) {
            for (var worker : workers) {
                if (worker.isAlive()) {
                    System.err.println(worker.getName() + " " + worker.getState());
                    for (var frame : worker.getStackTrace()) {
                        System.err.println("  " + frame);
                    }
                }
            }
            throw new AssertionError("Provider update/getTables did not complete before deadline; reads=" + reads);
        }
        if (firstFailure.get() != null) {
            throw new AssertionError("Concurrent refresh/table read failed", firstFailure.get());
        }
        if (reads.get() != ITERATIONS) {
            throw new AssertionError("Incomplete table reads: " + reads);
        }
        System.out.println("PASS: concurrent provider refresh/getTables completed; reads=" + reads);
    }

    private static void concurrentLifecycleAndQueries(CalciteProvider provider) throws Exception {
        var physical = request(1);
        physical.setTable("OWNED_ORDERS");
        assertFields(provider.fetchTableField(physical), "ID", "AMOUNT");
        var single = request(1);
        single.setQuery("SELECT ID, AMOUNT FROM OWNED_ORDERS WHERE 1 = 0");
        assertFields(provider.fetchTableField(single), "ID", "AMOUNT");
        assertFields(provider.fetchTableField(crossRequest()), "ORDER_ID", "ORDER_AMOUNT");
        assertCrossResult(provider);

        var start = new CountDownLatch(1);
        var done = new CountDownLatch(12);
        var failure = new AtomicReference<Throwable>();
        var queryReads = new AtomicInteger();
        for (int worker = 0; worker < 12; worker++) {
            final int number = worker;
            var thread = new Thread(() -> {
                try {
                    start.await();
                    var random = new Random(7463 + number);
                    for (int n = 0; n < (number >= 8 ? 100 : 1000); n++) {
                        if (number < 4) {
                            provider.update(datasource(1 + random.nextInt(ALIASES)));
                        } else if (number < 6) {
                            provider.updateDsPoolAfterCheckStatus(datasource(1 + random.nextInt(ALIASES)));
                        } else if (number < 8) {
                            // Remove/recreate other aliases while the same shared root is planned/read.
                            long id = 100 + number * 10L + random.nextInt(10);
                            provider.delete(deletion(id));
                            provider.updateDsPoolAfterCheckStatus(datasource(id));
                            assertTables(provider, id);
                        } else if (number == 8) {
                            assertFields(provider.fetchTableField(crossRequest()), "ORDER_ID", "ORDER_AMOUNT");
                            queryReads.incrementAndGet();
                        } else if (number == 9 || number == 10) {
                            assertCrossResult(provider);
                            queryReads.incrementAndGet();
                        } else {
                            assertFields(provider.fetchTableField(single), "ID", "AMOUNT");
                            queryReads.incrementAndGet();
                        }
                    }
                } catch (Throwable error) {
                    failure.compareAndSet(null, error);
                } finally {
                    done.countDown();
                }
            }, "schema-lifecycle-" + worker);
            thread.setDaemon(true);
            thread.start();
        }
        start.countDown();
        if (!done.await(25, TimeUnit.SECONDS)) {
            throw new AssertionError("Concurrent refresh/status/delete/field/query did not complete before deadline");
        }
        if (failure.get() != null) {
            throw new AssertionError("Concurrent schema lifecycle/query failed", failure.get());
        }
        if (queryReads.get() != 400) {
            throw new AssertionError("Incomplete query reads: " + queryReads);
        }
        assertCrossResult(provider);
        System.out.println("PASS: concurrent update/status/delete and single/cross-source fields/results; reads=" + queryReads);
    }

    /** H2 fixture function: blocks only an already-borrowed source connection. */
    public static int waitForRelease() throws InterruptedException {
        QUERY_STARTED.countDown();
        if (!QUERY_RELEASE.await(5, TimeUnit.SECONDS)) {
            throw new AssertionError("Owned query was not released");
        }
        return 1;
    }

    private static void assertChartAmount(CalciteProvider provider, long id, String expected) {
        var query = request(id);
        query.setQuery("SELECT AMOUNT FROM OWNED_ORDERS");
        var rows = (List<?>) provider.fetchResultField(query).get("data");
        if (rows.size() != 1 || !expected.equals(((String[]) rows.getFirst())[0])) {
            throw new AssertionError("Unexpected chart data for source " + id);
        }
    }

    public static int waitForCrossRelease() throws InterruptedException {
        CROSS_STARTED.countDown();
        if (!CROSS_RELEASE.await(10, TimeUnit.SECONDS)) {
            throw new AssertionError("Cross-source chart was not released");
        }
        return 7;
    }

    private static void blockedCrossChart(CalciteProvider provider, Connection anchor) throws Exception {
        try (var sql = anchor.createStatement(); var workers = Executors.newVirtualThreadPerTaskExecutor()) {
            var root = provider.take().unwrap(CalciteConnection.class).getRootSchema();
            var oldPool = (BasicDataSource) root.getSubSchema("s_a_1").unwrap(JdbcSchema.class).getDataSource();
            sql.execute("CREATE ALIAS CROSS_WAIT FOR 'Regression.waitForCrossRelease'");
            sql.execute("CREATE VIEW OWNED_SLOW AS SELECT CROSS_WAIT() AS MARKER FROM OWNED_ORDERS");
            var slow = crossRequest();
            slow.setQuery("SELECT a.MARKER, b.AMOUNT FROM s_a_1.OWNED_SLOW a "
                    + "JOIN s_a_2.OWNED_ORDERS b ON a.MARKER = b.ID");
            var chart = workers.submit(() -> provider.fetchResultField(slow));
            Future<?> refresh;
            try {
                if (!CROSS_STARTED.await(5, TimeUnit.SECONDS)) {
                    if (chart.isDone()) chart.get();
                    throw new AssertionError("Cross-source chart did not enter the database");
                }
                var singleChart = workers.submit(() -> assertChartAmount(provider, 2, "99.25"));
                var crossChart = workers.submit(() -> assertCrossResult(provider));
                var fields = workers.submit(() -> assertFields(provider.fetchTableField(crossRequest()), "ORDER_ID", "ORDER_AMOUNT"));
                for (var other : List.of(singleChart, crossChart, fields)) {
                    assertFinishes(other, "A slow cross-source chart blocked an independent query");
                }
                var switchSource = new FutureTask<Void>(() -> {
                    var switched = datasource(1);
                    switched.setConfiguration(datasource(2).getConfiguration());
                    provider.update(switched);
                    return null;
                });
                refresh = switchSource;
                var refreshing = Thread.ofPlatform().daemon().start(switchSource);
                awaitFrame(refreshing, "update", switchSource);
                // Source 3 is independent even when a refresh is waiting on the running query.
                assertFinishes(workers.submit(() -> assertChartAmount(provider, 3, "12.50")),
                        "Refresh of a busy source blocked an unrelated chart");
                if (oldPool.isClosed()) throw new AssertionError("Refresh closed a pool used by the running chart");
            } finally {
                CROSS_RELEASE.countDown();
            }
            var rows = (List<?>) chart.get().get("data");
            if (rows.size() != 1 || !List.of((String[]) rows.getFirst()).equals(List.of("7", "99.25"))) {
                throw new AssertionError("Incorrect result from the slow chart");
            }
            assertFinishes(refresh, "Refresh did not finish after cross-source SQL");
            if (!oldPool.isClosed()) throw new AssertionError("Refresh leaked the previous pool");
            assertChartAmount(provider, 1, "99.25");
            provider.update(datasource(1));
            sql.execute("DROP VIEW OWNED_SLOW");
        }
        System.out.println("PASS: single-source and overlapping cross-source chart/field requests finish during slow cross-source SQL");
    }

    private static void assertFinishes(Future<?> future, String message) throws Exception {
        try {
            future.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException timeout) {
            throw new AssertionError(message, timeout);
        }
    }

    private static void awaitFrame(Thread thread, String method, Future<?> task) throws Exception {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (Arrays.stream(thread.getStackTrace()).noneMatch(frame -> frame.getMethodName().equals(method))) {
            if (task.isDone()) {
                task.get();
                throw new AssertionError("Request finished before it could be observed in " + method);
            }
            if (System.nanoTime() >= end) throw new AssertionError("Did not reach " + method);
            Thread.sleep(5);
        }
    }

    private static void exhaustedPool(CalciteProvider provider) throws Exception {
        try (var workers = Executors.newVirtualThreadPerTaskExecutor()) {
            var held = new ArrayList<Connection>();
            var task = new FutureTask<Void>(() -> assertChartAmount(provider, 1, "12.50"), null);
            try {
                for (int i = 0; i < 4; i++) {
                    held.add(provider.getWriteConnection(datasource(1)).getConnection());
                }
                var thread = Thread.ofPlatform().name("chart-waiting-for-pool").daemon().start(task);
                awaitFrame(thread, "borrowObject", task);
                assertFinishes(workers.submit(() -> assertChartAmount(provider, 2, "99.25")),
                        "Waiting for a connection to source 1 blocked single-source queries on source 2");
            } finally {
                for (var connection : held) connection.close();
            }
            assertFinishes(task, "Borrower did not finish after a connection became available");
        }
        System.out.println("PASS: an exhausted source pool does not stall another source's JDBC queries");
    }

    private static void sourceQueryDoesNotBlockSchema(CalciteProvider provider) throws Exception {
        var failure = new AtomicReference<Throwable>();
        var queryDone = new CountDownLatch(1);
        var query = request(1);
        query.setQuery("SELECT OWNED_WAIT() AS RESULT");
        var thread = new Thread(() -> {
            try {
                var rows = (List<?>) provider.fetchResultField(query).get("data");
                if (rows.size() != 1 || !"1".equals(((String[]) rows.getFirst())[0])) {
                    throw new AssertionError("Incorrect result from already-borrowed JDBC connection");
                }
            } catch (Throwable error) {
                failure.set(error);
            } finally {
                queryDone.countDown();
            }
        }, "schema-owned-blocked-jdbc");
        thread.setDaemon(true);
        thread.start();
        try {
            if (!QUERY_STARTED.await(3, TimeUnit.SECONDS)) {
                throw new AssertionError("Owned single-source query did not start", failure.get());
            }
            var refreshDone = new CountDownLatch(1);
            var refresh = new Thread(() -> {
                try {
                    assertChartAmount(provider, 1, "12.50");
                    provider.update(datasource(1));
                    assertChartAmount(provider, 1, "12.50");
                    provider.update(datasource(3));
                    assertTables(provider, 3);
                } catch (Throwable error) {
                    failure.compareAndSet(null, error);
                } finally {
                    refreshDone.countDown();
                }
            }, "schema-refresh-during-blocked-jdbc");
            refresh.setDaemon(true);
            refresh.start();
            if (!refreshDone.await(2, TimeUnit.SECONDS)) {
                throw new AssertionError("A single-source SQL execution must not hold the schema monitor");
            }
        } finally {
            QUERY_RELEASE.countDown();
        }
        if (!queryDone.await(3, TimeUnit.SECONDS) || failure.get() != null) {
            throw new AssertionError("Owned blocked source query failed", failure.get());
        }
        System.out.println("PASS: with pool capacity a second chart on the same source and an unrelated refresh both finish during slow JDBC SQL");
    }

    public static void main(String[] args) {
        try (var context = new StaticApplicationContext();
             Connection anchor = DriverManager.getConnection(URL);
             Connection second = DriverManager.getConnection(SECOND_URL)) {
            new CommonBeanFactory().setApplicationContext(context);
            new Translator().setMessageSource(new StaticMessageSource());
            try (var statement = anchor.createStatement()) {
                statement.execute("CREATE TABLE OWNED_ORDERS(ID INT, AMOUNT DECIMAL(10, 2))");
                statement.execute("INSERT INTO OWNED_ORDERS VALUES (7, 12.50)");
                statement.execute("CREATE ALIAS OWNED_WAIT FOR 'Regression.waitForRelease'");
            }
            try (var statement = second.createStatement()) {
                statement.execute("CREATE TABLE OWNED_ORDERS(ID INT, AMOUNT DECIMAL(10, 2))");
                statement.execute("INSERT INTO OWNED_ORDERS VALUES (7, 99.25)");
            }
            var provider = new CalciteProvider();
            System.out.println("Provider loaded from: "
                    + CalciteProvider.class.getProtectionDomain().getCodeSource().getLocation());
            provider.take();
            for (int id = 1; id <= ALIASES; id++) {
                provider.update(datasource(id));
            }
            assertTables(provider, 1);
            sourceQueryDoesNotBlockSchema(provider);
            exhaustedPool(provider);
            blockedCrossChart(provider, anchor);
            var legacy = crossRequest();
            legacy.setDsList(null);
            assertCrossResult(provider.fetchResultField(legacy));
            var invalid = crossRequest();
            invalid.setQuery("SELECT MISSING_COLUMN FROM s_a_1.OWNED_ORDERS");
            try {
                provider.fetchResultField(invalid);
                throw new AssertionError("Invalid SQL should fail");
            } catch (DEException expected) {
                provider.update(datasource(1));
                assertCrossResult(provider);
            }
            System.out.println("PASS: legacy cross-source requests and refresh after a failed query");
            concurrentRefreshAndTables(provider);
            concurrentLifecycleAndQueries(provider);
            for (long id = 1; id <= ALIASES; id++) {
                provider.delete(deletion(id));
            }
            for (long id = 160; id < 180; id++) {
                provider.delete(deletion(id));
            }
            provider.take().close();
        } catch (Throwable failure) {
            failure.printStackTrace();
            // A corrupted Calcite TreeMap cannot be interrupted; bound the whole owned test JVM.
            System.exit(1);
        }
    }
}

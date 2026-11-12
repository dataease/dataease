package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import io.dataease.ai.dao.entity.CoreAiSetting;
import io.dataease.ai.manage.AiConfigurationManage.Connection;
import io.dataease.ai.support.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class PoolRegression {
    public static void run(AiAdmission admission, CoreAiSetting settings) throws Exception {
        runCase(admission, settings, false);
        runCase(admission, settings, true);
        System.out.println("PASS shared AI pool TCP reuse across credentials, cookie isolation, parallel timeout/cancellation isolation and recovery");
    }
    private static void runCase(AiAdmission admission, CoreAiSetting settings, boolean interrupt) throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var workers = Executors.newCachedThreadPool();
        var callers = Executors.newFixedThreadPool(2);
        var hanging = new CountDownLatch(1);
        var healthy = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        AtomicInteger firstPort = new AtomicInteger(), reusedPort = new AtomicInteger(), sequence = new AtomicInteger();
        server.setExecutor(workers);
        server.createContext("/v1/models", exchange -> {
            int index = sequence.incrementAndGet();
            if (index == 1) firstPort.set(exchange.getRemoteAddress().getPort());
            if (index == 2) reusedPort.set(exchange.getRemoteAddress().getPort());
            String key = exchange.getRequestHeaders().getFirst("Authorization");
            if (exchange.getRequestHeaders().getFirst("Cookie") != null) throw new AssertionError("shared provider cookie");
            try {
                if (key.endsWith("slow")) { hanging.countDown(); release.await(5, TimeUnit.SECONDS); }
                if (key.endsWith("healthy")) { healthy.countDown(); release.await(5, TimeUnit.SECONDS); }
                byte[] response = "{\"data\":[{\"id\":\"allowed\"}]}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Set-Cookie", "provider=private; Path=/");
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { exchange.close(); }
        });
        server.start();
        var transport = new AiTransport(new AiOutboundPolicy(), admission, new ObjectMapper());
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
        try {
            for (int i = 0; i < 2; i++) transport.discover(connection(url, "reuse" + i), settings, System.currentTimeMillis() + 3000, () -> {});
            if (firstPort.get() == 0 || firstPort.get() != reusedPort.get()) throw new AssertionError("TCP connection was not reused across credentials");
            {
                // 两个请求均已到达服务端，再取消其中一个；另一请求仍占用自己的连接。
                var interruptedCode = new java.util.concurrent.atomic.AtomicReference<String>();
                Thread slow = new Thread(() -> {
                    try {
                        transport.discover(connection(url, "slow"), settings, System.currentTimeMillis() + (interrupt ? 3000 : 800), () -> {});
                        interruptedCode.set("unexpected_success");
                    } catch (AiErrors.Failure failure) { interruptedCode.set(failure.safeCode()); }
                });
                slow.start();
                if (!hanging.await(2, TimeUnit.SECONDS)) throw new AssertionError("slow request not started");
                Future<?> good = callers.submit(() -> transport.discover(connection(url, "healthy"), settings, System.currentTimeMillis() + 4000, () -> {}));
                if (!healthy.await(2, TimeUnit.SECONDS)) throw new AssertionError("healthy request not started");
                if (interrupt) slow.interrupt();
                slow.join(2500);
                if (slow.isAlive() || !(interrupt ? "cancelled" : "timeout").equals(interruptedCode.get())) throw new AssertionError("request cancellation failed: " + interruptedCode.get());
                release.countDown();
                good.get(2, TimeUnit.SECONDS);
                transport.discover(connection(url, "after"), settings, System.currentTimeMillis() + 3000, () -> {});
            }
        } finally { release.countDown(); transport.close(); server.stop(0); callers.shutdownNow(); workers.shutdownNow(); }
    }
    private static Connection connection(String url, String key) {
        return new Connection(1L, 1, "OPENAI_COMPATIBLE", url, key, "allowed");
    }
}

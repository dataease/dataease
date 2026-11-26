package io.dataease.ai.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.engine.conversation.AiConversationManage;
import io.dataease.ai.support.AiErrors;
import io.dataease.api.ai.conversation.AiConversationSubscriptionApi;
import io.dataease.websocket.handler.DeHandshakeInterceptor;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.*;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

/** Each STOMP subscription is private to one authenticated socket and one HTTP-bound editor execution. */
@RestController
public class AiConversationSocket implements AiConversationSubscriptionApi {
    private static final String DESTINATION = "/app/ai/conversation";
    private static final int MAX_CLIENTS = 256, MAX_PER_USER = 8, MAX_FRAME_BYTES = 6000;
    private static final long HEARTBEAT_MS = 4000;
    private static final ObjectMapper JSON = new ObjectMapper();
    private final Map<String, Client> clients = new ConcurrentHashMap<>();
    private final ScheduledThreadPoolExecutor workers = new ScheduledThreadPoolExecutor(4,
            Thread.ofPlatform().daemon().name("de-ai-delivery-", 0).factory());
    private final MessageChannel outbound;
    private final AiConversationManage manage;

    public AiConversationSocket(@Qualifier("clientOutboundChannel") MessageChannel outbound, AiConversationManage manage) {
        this.outbound = outbound;
        this.manage = manage;
        workers.setRemoveOnCancelPolicy(true);
    }

    private static final class Client {
        final String key = UUID.randomUUID().toString();
        final String session, subscription;
        final long actor;
        final BooleanSupplier authenticated;
        AiConversationManage.Delivery delivery;
        long after, lastDelivery;
        ScheduledFuture<?> timer;
        Client(String session, String subscription, long actor, BooleanSupplier authenticated) {
            this.session = session; this.subscription = subscription;
            this.actor = actor; this.authenticated = authenticated;
        }
    }

    @SubscribeMapping("/ai/conversation")
    public Map<String, String> subscribe(@Header("simpSessionId") String session,
            @Header("simpSubscriptionId") String subscription, @Header("simpSessionAttributes") Map<String, Object> attributes) {
        if (!(attributes.get(DeHandshakeInterceptor.AUTH_USER_ID_ATTRIBUTE) instanceof Long actor)
                || !(attributes.get(DeHandshakeInterceptor.AUTHENTICATED_CONNECTION) instanceof BooleanSupplier authenticated)
                || !authenticated.getAsBoolean()) throw AiErrors.error("forbidden");
        synchronized (clients) {
            if (clients.size() >= MAX_CLIENTS || clients.values().stream().filter(c -> c.actor == actor).count() >= MAX_PER_USER)
                throw AiErrors.error("concurrency");
            Client client = new Client(session, subscription, actor, authenticated);
            clients.put(client.key, client);
            client.timer = workers.schedule(() -> {
                synchronized (client) { if (client.delivery == null) remove(client); }
            }, 10, TimeUnit.SECONDS);
            // SubscribeMapping replies directly to this session and subscription, without a broker topic.
            return Map.of("subscription", client.key);
        }
    }

    @Override public void bind(String id, String execution, Bind request) {
        Client client = clients.get(request.subscription());
        if (client == null) throw AiErrors.error("scope_changed");
        synchronized (client) {
            if (clients.get(client.key) != client || client.delivery != null || !client.authenticated.getAsBoolean())
                throw AiErrors.error("scope_changed");
            client.delivery = manage.subscribe(id, execution, request.editorId(), client.actor, request.after());
            client.after = request.after();
            client.timer.cancel(false);
            client.timer = workers.scheduleWithFixedDelay(() -> deliver(client), 0, 180, TimeUnit.MILLISECONDS);
        }
    }

    private void deliver(Client client) {
        synchronized (client) {
            if (clients.get(client.key) != client) return;
            try {
                long now = System.currentTimeMillis();
                var page = client.delivery.read(client.after, now - client.lastDelivery >= HEARTBEAT_MS, client.authenticated);
                if (page == null) return;
                byte[] body = JSON.writeValueAsBytes(page);
                if (body.length > MAX_FRAME_BYTES) {
                    // The same authenticated cursor endpoint returns the full result outside the socket buffer.
                    body = JSON.writeValueAsBytes(Map.of("read", true, "cursor", page.cursor(), "executionId", page.executionId()));
                }
                send(client, body);
                client.after = page.cursor();
                client.lastDelivery = now;
                if (!"RUNNING".equals(page.status())) remove(client);
            } catch (Exception unavailable) {
                // No protected body or diagnostic is sent after authority or the originating route disappears.
                try { send(client, JSON.writeValueAsBytes(Map.of("denied", true))); }
                catch (Exception disconnected) { /* already disconnected */ }
                finally { remove(client); }
            }
        }
    }

    private void send(Client client, byte[] body) {
        var headers = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headers.setSessionId(client.session);
        headers.setSubscriptionId(client.subscription);
        headers.setDestination(DESTINATION);
        headers.setContentType(MimeTypeUtils.APPLICATION_JSON);
        if (!outbound.send(MessageBuilder.createMessage(body, headers.getMessageHeaders()), 1000))
            throw new IllegalStateException("socket unavailable");
    }

    @EventListener public void unsubscribe(SessionUnsubscribeEvent event) {
        var message = event.getMessage().getHeaders();
        disconnect(SimpMessageHeaderAccessor.getSessionId(message), SimpMessageHeaderAccessor.getSubscriptionId(message));
    }
    @EventListener public void disconnect(SessionDisconnectEvent event) {
        disconnect(event.getSessionId(), null);
    }
    private void disconnect(String session, String subscription) {
        clients.values().stream().filter(c -> c.session.equals(session) && (subscription == null || c.subscription.equals(subscription)))
                .forEach(client -> {
                    synchronized (client) {
                        if (client.delivery != null) client.delivery.disconnected();
                        remove(client);
                    }
                });
    }
    private void remove(Client client) {
        synchronized (client) {
            if (clients.remove(client.key, client) && client.timer != null) client.timer.cancel(false);
        }
    }
    @PreDestroy public void close() { workers.shutdownNow(); clients.clear(); }
}

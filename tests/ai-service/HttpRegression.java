package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import io.dataease.ai.dao.entity.CoreAiSetting;
import io.dataease.ai.manage.AiConfigurationManage.Connection;
import io.dataease.ai.support.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.*;

public final class HttpRegression {
    public static void run(AiAdmission admission) throws Exception {
        ObjectMapper json = new ObjectMapper();
        AtomicInteger requests = new AtomicInteger(), mode = new AtomicInteger();
        AtomicInteger redirected = new AtomicInteger();
        AtomicReference<String> body = new AtomicReference<>();
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        var workers=java.util.concurrent.Executors.newCachedThreadPool();server.setExecutor(workers);
        server.createContext("/prefix/v1/chat/completions", exchange -> {
            requests.incrementAndGet();body.set(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));
            if(mode.get()==1) { exchange.sendResponseHeaders(500,0);exchange.close();return; }
            if(mode.get()==5) { exchange.getResponseHeaders().set("Location","/redirect-target");exchange.sendResponseHeaders(302,-1);exchange.close();return; }
            if(mode.get()==6) { exchange.sendResponseHeaders(401,-1);exchange.close();return; }
            if(mode.get()==2) try { Thread.sleep(1000); } catch(InterruptedException e) { Thread.currentThread().interrupt(); }
            byte[] response="{\"id\":\"synthetic-id\",\"object\":\"chat.completion\",\"created\":1,\"model\":\"allowed\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"OK\"},\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":3,\"completion_tokens\":1,\"total_tokens\":4}}".getBytes(StandardCharsets.UTF_8);
            if(mode.get()==3)response=new byte[0];
            if(mode.get()==4)response="{}".getBytes(StandardCharsets.UTF_8);
            try {exchange.getResponseHeaders().set("Content-Type","application/json");exchange.sendResponseHeaders(200,response.length);exchange.getResponseBody().write(response);}finally{exchange.close();}
        });
        server.createContext("/prefix/v1/models", exchange -> {
            requests.incrementAndGet();
            if(mode.get()==5) { exchange.getResponseHeaders().set("Location","/redirect-target");exchange.sendResponseHeaders(302,-1);exchange.close();return; }
            byte[] response="{\"data\":[{\"id\":\"allowed\"}]}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type","application/json");
            try {exchange.sendResponseHeaders(200,response.length);exchange.getResponseBody().write(response);}finally{exchange.close();}
        });
        server.createContext("/redirect-target", exchange -> { redirected.incrementAndGet();exchange.sendResponseHeaders(200,-1);exchange.close(); });
        server.start();
        AiOutboundPolicy outbound=new AiOutboundPolicy();
        var logs=new ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent>();logs.start();
        var root=(ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);root.addAppender(logs);
        AiTransport transport=new AiTransport(outbound,admission,json);
        try {
            CoreAiSetting settings=new CoreAiSetting();
            Connection connection=new Connection(1L,1,"OPENAI_COMPATIBLE","http://127.0.0.1:"+server.getAddress().getPort()+"/prefix/v1","synthetic-key","allowed");
            Prompt prompt=AiPromptPolicy.constrain(new Prompt("hello"),"allowed",4096,1000,json);
            if(!transport.discover(connection,settings,System.currentTimeMillis()+3000,()->{}).equals(java.util.List.of("allowed")))throw new AssertionError("model discovery prefix");
            var response=transport.call(connection,settings,prompt,false,System.currentTimeMillis()+3000,()->{});
            if(!"OK".equals(response.getResult().getOutput().getText()))throw new AssertionError("native response");
            var sent=json.readTree(body.get());
            if(!"allowed".equals(sent.path("model").asText())||sent.path("n").asInt()!=1||sent.path("max_tokens").asInt()!=4096)throw new AssertionError("wire budget/model");
            if(sent.has("tools")&&!sent.path("tools").isEmpty())throw new AssertionError("tools unexpectedly sent");
            mode.set(1);int before=requests.get();
            try{transport.call(connection,settings,prompt,false,System.currentTimeMillis()+3000,()->{});throw new AssertionError("500 accepted");}catch(AiErrors.Failure expected){}
            if(requests.get()!=before+1)throw new AssertionError("hidden retry");
            mode.set(5);
            expect("upstream_failed",()->transport.call(connection,settings,prompt,false,System.currentTimeMillis()+3000,()->{}));
            expect("upstream_failed",()->transport.discover(connection,settings,System.currentTimeMillis()+3000,()->{}));
            if(redirected.get()!=0)throw new AssertionError("redirect followed or credentials forwarded");
            mode.set(6);
            expect("provider_authentication",()->transport.call(connection,settings,prompt,false,System.currentTimeMillis()+3000,()->{}));
            networkFailures(admission,json,settings,prompt);
            before=requests.get();
            try{transport.call(connection,settings,prompt,false,System.currentTimeMillis()+3000,()->{throw AiErrors.error("forbidden");});throw new AssertionError("revoked maintenance sent");}catch(AiErrors.Failure expected){}
            if(requests.get()!=before)throw new AssertionError("denied request reached HTTP");
            Prompt sensitive=AiPromptPolicy.constrain(new Prompt("synthetic-private-marker-do-not-log"),"allowed",4096,1000,json);
            for(int malformed: new int[]{3,4}){
                mode.set(malformed);
                try{transport.call(connection,settings,sensitive,false,System.currentTimeMillis()+3000,()->{});throw new AssertionError("malformed response accepted");}catch(AiErrors.Failure expected){}
            }
            if(logs.list.stream().anyMatch(event->event.getFormattedMessage().contains("synthetic-private-marker-do-not-log") || event.getFormattedMessage().contains("synthetic-key")))throw new AssertionError("prompt or key leaked into logs");
            mode.set(2);long started=System.currentTimeMillis();
            expect("timeout",()->transport.call(connection,settings,prompt,false,started+100,()->{}));
            if(System.currentTimeMillis()-started>900)throw new AssertionError("hard deadline");
            PoolRegression.run(admission);
            System.out.println("PASS AI actual HTTP discovery/generation on configured local custom port, gateway prefix, no redirects/retries, DNS/connection/TLS/authentication/timeout errors, no prompt logging and deadline");
        } finally {root.detachAppender(logs);logs.stop();transport.close();server.stop(0);workers.shutdownNow();}
    }
    private static void expect(String code, Runnable action) {
        try { action.run();throw new AssertionError("expected network failure: "+code); }
        catch(AiErrors.Failure failure) { if(!code.equals(failure.safeCode()))throw new AssertionError("expected "+code+", got "+failure.safeCode()); }
    }
    private static void networkFailures(AiAdmission admission, ObjectMapper json, CoreAiSetting settings, Prompt prompt) throws Exception {
        AiTransport dns = new AiTransport(new AiOutboundPolicy() {
            public InetAddress[] resolve(String host) throws UnknownHostException { throw new UnknownHostException("synthetic-key-never-echo"); }
        },admission,json);
        try {
            Connection connection=new Connection(1L,1,"OPENAI_COMPATIBLE","http://model.invalid:8000/v1","synthetic-key","allowed");
            expect("dns_failed",()->dns.call(connection,settings,prompt,false,System.currentTimeMillis()+3000,()->{}));
        } finally { dns.close(); }
        int closedPort;
        try(var socket=new java.net.ServerSocket(0,0,InetAddress.getByName("127.0.0.1"))) { closedPort=socket.getLocalPort(); }
        AiTransport network = new AiTransport(new AiOutboundPolicy(),admission,json);
        var directory=java.nio.file.Files.createTempDirectory("de-ai-tls-");
        com.sun.net.httpserver.HttpsServer tls=null;
        try {
            Connection connection=new Connection(1L,1,"OPENAI_COMPATIBLE","http://127.0.0.1:"+closedPort+"/v1","synthetic-key","allowed");
            expect("connection_failed",()->network.call(connection,settings,prompt,false,System.currentTimeMillis()+3000,()->{}));
            // 临时自签名证书仅用于本机测试，验证默认客户端不会跳过证书校验。
            var keyStore=directory.resolve("server.p12");
            var process=new ProcessBuilder(java.nio.file.Path.of(System.getProperty("java.home"),"bin","keytool").toString(),
                    "-genkeypair","-alias","test","-keyalg","RSA","-storetype","PKCS12","-keystore",keyStore.toString(),
                    "-storepass","synthetic-password","-keypass","synthetic-password","-dname","CN=localhost","-validity","1","-noprompt")
                    .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
            if(!process.waitFor(15,java.util.concurrent.TimeUnit.SECONDS)) {process.destroyForcibly();throw new AssertionError("test certificate generation timed out");}
            if(process.exitValue()!=0)throw new AssertionError("test certificate generation failed");
            var store=java.security.KeyStore.getInstance("PKCS12");
            try(var input=java.nio.file.Files.newInputStream(keyStore)) {store.load(input,"synthetic-password".toCharArray());}
            var keys=javax.net.ssl.KeyManagerFactory.getInstance(javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm());
            keys.init(store,"synthetic-password".toCharArray());
            var context=javax.net.ssl.SSLContext.getInstance("TLS");context.init(keys.getKeyManagers(),null,null);
            tls=com.sun.net.httpserver.HttpsServer.create(new InetSocketAddress("127.0.0.1",0),0);
            tls.setHttpsConfigurator(new com.sun.net.httpserver.HttpsConfigurator(context));
            tls.createContext("/v1/chat/completions",exchange->{throw new AssertionError("untrusted TLS certificate accepted");});tls.start();
            Connection secure=new Connection(1L,1,"OPENAI_COMPATIBLE","https://127.0.0.1:"+tls.getAddress().getPort()+"/v1","synthetic-key","allowed");
            expect("tls_failed",()->network.call(secure,settings,prompt,false,System.currentTimeMillis()+3000,()->{}));
        } finally {
            if(tls!=null)tls.stop(0);network.close();
            try(var files=java.nio.file.Files.walk(directory)) {for(var file:files.sorted(java.util.Comparator.reverseOrder()).toList())java.nio.file.Files.deleteIfExists(file);}
        }
    }
}

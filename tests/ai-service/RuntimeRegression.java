package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.dao.entity.*;
import io.dataease.ai.dao.repository.*;
import io.dataease.ai.manage.*;
import io.dataease.ai.support.*;
import io.dataease.api.ai.runtime.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.*;
import org.springframework.ai.chat.prompt.*;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public final class RuntimeRegression {
    public static void run(AiConfigurationManage configuration, CoreAiOrgPolicyRepository policies, AiSecrets keys,
                           AiAdmission admission, AiAccess access) throws Exception {
        AtomicBoolean allowed = new AtomicBoolean(true);
        AtomicInteger calls = new AtomicInteger();
        AtomicBoolean revokeOnResponse = new AtomicBoolean();
        AiFeature feature = new AiFeature() {
            public String code(){return "synthetic-sql";}
            public void authorize(AiExecutionContext context){if(!allowed.get())throw AiErrors.error("forbidden");}
            public void authorizeInput(AiExecutionContext context, Prompt prompt){
                authorize(context);
                // 故意修改业务收到的校验副本，不能影响底座已经检查的真实参数。
                ((org.springframework.ai.openai.OpenAiChatOptions)prompt.getOptions()).setModel("changed-by-inspector");
            }
            public void checkSampleConfirmation(AiExecutionContext context){if(!"confirmed".equals(context.scope().confirmation()))throw AiErrors.error("samples_denied");}
            public TaskIdentity taskIdentity(String id){return new TaskIdentity(1L,null,"v1");}
        };
        AiTransport transport = new AiTransport(new AiOutboundPolicy(),admission,new ObjectMapper()) {
            @Override public ChatResponse call(AiConfigurationManage.Connection c,CoreAiSetting s,Prompt p,boolean background,long deadline,Runnable recheck){
                recheck.run();calls.incrementAndGet();
                if(!"general-chat".equals(p.getOptions().getModel()))throw new AssertionError("resolved model");
                if (revokeOnResponse.get()) allowed.set(false);
                return new ChatResponse(List.of(new Generation(new AssistantMessage("OK"))));
            }
        };
        var beans=new StaticListableBeanFactory(Map.of("feature",feature));
        AiRuntimeManage provider=new AiRuntimeManage(beans.getBeanProvider(AiFeature.class),configuration,policies,keys,transport,new ObjectMapper(),access);
        var attributes=new ServletRequestAttributes(new MockHttpServletRequest());RequestContextHolder.setRequestAttributes(attributes);
        try{
            var scope=new AiDataScope("dataset:1@v1","permissions:v1",null,false,null);
            var context=provider.current("synthetic-sql",scope);ChatModel model=provider.current(context);
            if(!"OK".equals(model.call("hello")))throw new AssertionError("direct call");
            if(!"OK".equals(ChatClient.builder(model).build().prompt().user("hello via ChatClient").call().content()))throw new AssertionError("ChatClient call");
            revokeOnResponse.set(true);
            try { model.call("revoke during generation"); throw new AssertionError("delivery after permission revoked"); } catch (AiErrors.Failure expected) { }
            revokeOnResponse.set(false); allowed.set(true);
            int before=calls.get();
            var advisor = new org.springframework.ai.chat.client.advisor.api.CallAdvisor() {
                public String getName(){return "synthetic-over-budget";}
                public int getOrder(){return 0;}
                public org.springframework.ai.chat.client.ChatClientResponse adviseCall(org.springframework.ai.chat.client.ChatClientRequest request, org.springframework.ai.chat.client.advisor.api.CallAdvisorChain chain){
                    return chain.nextCall(new org.springframework.ai.chat.client.ChatClientRequest(new Prompt("x".repeat(30001)),request.context()));
                }
            };
            try{ChatClient.builder(model).defaultAdvisors(advisor).build().prompt("short before advisor").call().content();throw new AssertionError("Advisor bypassed final budget");}catch(AiErrors.Failure expected){}

            try{model.call("x".repeat(30001));throw new AssertionError("final input budget");}catch(AiErrors.Failure expected){}
            try{model.stream(new Prompt("hello")).blockLast();throw new AssertionError("stream must reject");}catch(AiErrors.Failure expected){}
            if(calls.get()!=before)throw new AssertionError("rejected capability reached transport");
            allowed.set(false);try{model.call("hello");throw new AssertionError("revocation");}catch(AiErrors.Failure expected){}allowed.set(true);
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
            try{model.call("hello");throw new AssertionError("proxy reused across requests");}catch(AiErrors.Failure expected){}
            RequestContextHolder.setRequestAttributes(attributes);
            var boundScope=new AiDataScope("dataset:1@v1","permissions:v1",AiPromptFingerprint.of(new Prompt("original")),false,null);
            var bound=provider.current(provider.current("synthetic-sql",boundScope));
            try{bound.call("changed");throw new AssertionError("bound input changed");}catch(AiErrors.Failure expected){}
            var sampleScope=new AiDataScope("dataset:1@v1","permissions:v1",AiPromptFingerprint.of(new Prompt("sample")),true,"confirmed");
            var sample=provider.current(provider.current("synthetic-sql",sampleScope));
            try{sample.call("sample");throw new AssertionError("sample master switch");}catch(AiErrors.Failure expected){}
            var task=provider.current(provider.task("synthetic-sql","task-1",sampleScope));
            try{task.call("sample");throw new AssertionError("background samples");}catch(AiErrors.Failure expected){}
            var configMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(new io.dataease.ai.server.AiConfigurationServer(configuration, null)).build();
            var mapper = new ObjectMapper();
            var off = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(configMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/ai/settings")).andReturn().getResponse().getContentAsString());
            off.put("expectedVersion", off.path("rowVersion").asLong()); off.put("enabled", false);
            off.put("impactToken", mapper.readTree(configMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/ai/settings/impact").param("defaultModelId", off.path("defaultModelId").asText()).param("enabled", "false")).andReturn().getResponse().getContentAsString()).path("token").asText());
            if (configMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/ai/settings").contentType("application/json").content(mapper.writeValueAsBytes(off))).andReturn().getResponse().getStatus() != 200) throw new AssertionError("native epoch off accepted");
            var on = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(configMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/ai/settings")).andReturn().getResponse().getContentAsString());
            on.put("expectedVersion", on.path("rowVersion").asLong()); on.put("enabled", true);
            if (configMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/ai/settings").contentType("application/json").content(mapper.writeValueAsBytes(on))).andReturn().getResponse().getStatus() != 200) throw new AssertionError("native epoch on accepted");
            int beforeOldContext = calls.get();
            try { model.call("old native context"); throw new AssertionError("native context revived after missed shutdown"); } catch (AiErrors.Failure expected) { }
            if (calls.get() != beforeOldContext) throw new AssertionError("revoked native epoch reached transport");
            if (!"OK".equals(provider.current(provider.current("synthetic-sql", scope)).call("fresh native attempt"))) throw new AssertionError("fresh native context may bind current epoch");
            System.out.println("PASS AI native call and ChatClient, final input budget, stream rejection, revocation, context isolation and scope binding");
        }finally{RequestContextHolder.resetRequestAttributes();transport.close();}
    }
}

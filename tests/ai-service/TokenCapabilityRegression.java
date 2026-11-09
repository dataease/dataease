package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import io.dataease.ai.dao.repository.CoreAiOrgPolicyRepository;
import io.dataease.ai.manage.*;
import io.dataease.ai.support.*;
import io.dataease.api.ai.config.AiConfigDtos.*;
import io.dataease.api.ai.runtime.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.*;

public final class TokenCapabilityRegression {
    private static Candidate candidate(AiConfigurationManage config,String address){
        Draft draft=config.draft();Candidate c=new Candidate();c.setId(draft.id());c.setDraftHandle(draft.draftHandle());c.setName("Native completion "+draft.id());c.setProvider("OPENAI_COMPATIBLE");c.setBaseUrl(address);c.setApiKey("synthetic-key");c.setModelIdentifier("completion-only");return c;
    }
    public static void run(AiConfigurationManage config,CoreAiOrgPolicyRepository policies,AiAccess access,AiSecrets keys,AiAdmission admission,AtomicBoolean permitted)throws Exception{
        var json=new ObjectMapper();var requests=new AtomicInteger();var mode=new AtomicInteger();var bodies=new ArrayList<com.fasterxml.jackson.databind.JsonNode>();
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/v1/chat/completions",exchange->{
            requests.incrementAndGet();var body=json.readTree(exchange.getRequestBody());synchronized(bodies){bodies.add(body);}
            boolean firstStyle=body.has("max_tokens");String response;int status;
            if(mode.get()==1){status=400;response="{\"error\":{\"code\":\"invalid_request_error\",\"param\":\"model\",\"message\":\"synthetic-private-error\"}}";}
            else if(firstStyle || mode.get()==2){status=400;response="{\"error\":{\"code\":\"unsupported_parameter\",\"param\":\""+(firstStyle?"max_tokens":"max_completion_tokens")+"\",\"message\":\"synthetic-private-error\"}}";if(mode.get()==3)permitted.set(false);}
            else if(body.has("response_format") && !"json_object".equals(body.path("response_format").path("type").asText())) {
                status=400;response="{\"error\":{\"code\":\"invalid_request_error\",\"param\":\"response_format\"}}";
            }
            else {
                status=200;
                String content=body.has("response_format")?switch(mode.get()) {
                    case 4 -> "{\"ok\":true,\"extra\":1}";
                    case 5 -> "{\"ok\":false}";
                    case 6 -> "{\"ok\":\"true\"}";
                    case 7 -> "not JSON";
                    case 8 -> "{\"ok\":true} {\"extra\":1}";
                    default -> "{\"ok\":true}";
                }:"OK";
                response=json.writeValueAsString(Map.of("id","synthetic-id","object","chat.completion","choices",List.of(Map.of(
                        "index",0,"message",Map.of("role","assistant","content",content),"finish_reason","stop"))));
            }
            byte[] bytes=response.getBytes(StandardCharsets.UTF_8);exchange.getResponseHeaders().set("Content-Type","application/json");exchange.sendResponseHeaders(status,bytes.length);try{exchange.getResponseBody().write(bytes);}finally{exchange.close();}
        });server.start();
        var outbound=new AiOutboundPolicy() {public String normalize(String url){return url;}public InetAddress[] resolve(String host)throws UnknownHostException{return InetAddress.getAllByName(host);}};
        var deadlines=new ArrayList<Long>();
        var transport=new AiTransport(outbound,admission,json){
            @Override public org.springframework.ai.chat.model.ChatResponse call(AiConfigurationManage.Connection c,io.dataease.ai.dao.entity.CoreAiSetting s,Prompt p,boolean task,long deadline,Runnable recheck){deadlines.add(deadline);return super.call(c,s,p,task,deadline,recheck);}
        };
        try{
            var tests=new AiModelTestManage(config,transport,access,json);String address="http://127.0.0.1:"+server.getAddress().getPort()+"/v1";
            Candidate c=candidate(config,address);TestResult result=tests.test(c,false);
            if(!result.basic()||!result.structured()||requests.get()!=3||deadlines.stream().distinct().count()!=1)throw new AssertionError("finite capability negotiation/shared deadline");
            if(!bodies.get(0).has("max_tokens")||bodies.get(1).has("max_tokens")||!bodies.get(2).has("max_completion_tokens"))throw new AssertionError("schema must use negotiated field");
            var format=bodies.get(2).path("response_format");
            if(!"json_object".equals(format.path("type").asText())||format.has("json_schema"))throw new AssertionError("OpenAI compatible JSON mode");
            String proof=result.testProof();String[] parts=proof.split("\\.");String payload=new String(Base64.getUrlDecoder().decode(parts[1]),StandardCharsets.UTF_8).replace("MAX_COMPLETION_TOKENS","MAX_TOKENS");
            c.setTestProof(parts[0]+"."+Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8))+"."+parts[2]);
            try{config.save(c,true);throw new AssertionError("tampered capability accepted");}catch(AiErrors.Failure expected){}
            c.setTestProof(proof);Model saved=config.save(c,true);if(!"MAX_COMPLETION_TOKENS".equals(saved.outputTokenParameter()))throw new AssertionError("capability not saved");
            var s=config.setting();Long oldDefault=s.getDefaultModelId();config.saveSettings(new SettingsChange(s.getRowVersion(),true,saved.id(),false,60,30000,4096,5,2,config.impact("SETTINGS",saved.id()+":true").token()));
            AiFeature feature=new AiFeature(){public String code(){return "completion-test";}public void authorize(AiExecutionContext c){}public void authorizeInput(AiExecutionContext c,Prompt p){}};
            var provider=new AiRuntimeManage(new StaticListableBeanFactory(Map.of("feature",feature)).getBeanProvider(AiFeature.class),config,policies,keys,transport,json,access);
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
            try{var context=provider.current("completion-test",new AiDataScope("dataset:1@v1","permissions:v1",null,false,null));if(!"OK".equals(provider.current(context).call("runtime")))throw new AssertionError("runtime result");}finally{RequestContextHolder.resetRequestAttributes();}
            var last=bodies.get(bodies.size()-1);if(last.has("max_tokens")||!last.has("max_completion_tokens"))throw new AssertionError("runtime default ignored capability");
            Candidate inline=new Candidate();inline.setId(saved.id());inline.setExpectedVersion(saved.rowVersion());inline.setExpectedConfigRevision(saved.configRevision());inline.setProvider(saved.provider());inline.setBaseUrl(saved.baseUrl());inline.setModelIdentifier(saved.modelIdentifier());
            TestResult inlineResult=tests.test(inline,true);if(!inlineResult.structured()||inlineResult.testProof()!=null)throw new AssertionError("inline test must not expose a save proof");
            s=config.setting();config.saveSettings(new SettingsChange(s.getRowVersion(),true,oldDefault,false,60,30000,4096,5,2,config.impact("SETTINGS",oldDefault+":true").token()));
            mode.set(1);int before=requests.get();if(tests.test(candidate(config,address),false).basic()||requests.get()!=before+1)throw new AssertionError("ordinary 400 retried");
            mode.set(2);before=requests.get();if(tests.test(candidate(config,address),false).basic()||requests.get()!=before+2)throw new AssertionError("negotiation loop");
            mode.set(3);before=requests.get();try{tests.test(candidate(config,address),false);throw new AssertionError("revoked negotiation continued");}catch(AiErrors.Failure expected){}finally{permitted.set(true);}
            if(requests.get()!=before+1)throw new AssertionError("negotiation bypassed reauthorization");
            for(int invalid:new int[]{4,5,6,7,8}) {
                mode.set(invalid);before=requests.get();TestResult rejected=tests.test(candidate(config,address),false);
                if(!rejected.basic()||rejected.structured()||rejected.testProof()!=null||!"response_invalid".equals(rejected.errorCode())
                        ||requests.get()!=before+3)throw new AssertionError("invalid JSON structure accepted or retried: "+invalid);
            }
            System.out.println("PASS AI JSON-only compatible provider, strict local structure/trailing-content validation, token negotiation, signed capability, no generic retry and reauthorization");
        }finally{permitted.set(true);transport.close();server.stop(0);}
    }
}

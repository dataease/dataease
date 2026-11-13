package io.dataease.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.dao.entity.*;
import io.dataease.ai.dao.repository.*;
import io.dataease.ai.manage.*;
import io.dataease.ai.support.*;
import io.dataease.api.ai.config.AiConfigDtos.*;
import io.dataease.api.ai.runtime.*;
import io.dataease.i18n.Translator;
import io.dataease.listener.sql.AiConfigurationInit;
import io.dataease.menu.dao.auto.entity.CoreMenu;
import io.dataease.menu.dao.auto.mapper.CoreMenuRepository;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.utils.IDUtils;
import io.dataease.utils.SnowFlake;
import jakarta.persistence.*;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.ai.chat.prompt.*;
import org.springframework.ai.openai.OpenAiChatOptions;
import java.lang.reflect.Field;
import java.util.*;

public class Regression {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static int checks;
    private static void check(boolean value, String description) { if (!value) throw new AssertionError(description); checks++; }
    private static void rejects(Runnable work, String description) { try { work.run(); } catch (RuntimeException expected) { checks++; return; } throw new AssertionError(description); }
    private static void inject(Object instance, String name, Object value) throws Exception { Field field = instance.getClass().getDeclaredField(name); field.setAccessible(true); field.set(instance, value); }
    private static AiSecrets secrets() { byte[] bytes = new byte[32]; new java.security.SecureRandom().nextBytes(bytes); return new AiSecrets("test:" + Base64.getEncoder().encodeToString(bytes), "test", JSON); }
    public static void main(String[] args) throws Exception {
        var messages = new Properties();
        try (var input = new java.io.FileInputStream("core/core-backend/src/main/resources/i18n/core_zh_CN.properties")) {
            messages.load(input);
        }
        check("AI 服务".equals(messages.getProperty("i18n_menu.ai-service")), "AI menu survives default properties encoding");
        new Translator().setMessageSource(new StaticMessageSource());
        new IDUtils().setSnowFlake(new SnowFlake()); V3UserUtil.setUid(1L);
        if (Boolean.getBoolean("ai.test.identityScopeOnly")) { ModeRegression.run(); return; }
        if (Boolean.getBoolean("ai.test.auditRecoveryOnly")) { TaskClusterRegression.run(); return; }
        if (Boolean.getBoolean("ai.test.modelLimitsOnly") || Boolean.getBoolean("ai.test.conversationOnly") || Boolean.getBoolean("ai.test.runtimeOnly") || Boolean.getBoolean("ai.test.retentionOnly") || Boolean.getBoolean("ai.test.auditOnly") || Boolean.getBoolean("ai.test.recoveryOnly") || Boolean.getBoolean("ai.test.recoveryToolsOnly")) { persistence(); return; }
        SqlStaticValidationRegression.run(); ModeRegression.run(); ConversationPermissionRegression.run(); ApiRoutingRegression.run(); crypto(); prompt(); TaskSchemaRegression.run(); persistence(); if (!Boolean.getBoolean("ai.test.taskContractsOnly")) { TaskProgressRegression.main(new String[0]); TaskStructureNodeRegression.main(new String[0]); TaskClusterRegression.run(); } if (!Boolean.getBoolean("ai.test.onlyTasks")) KeyringRegression.run();
        System.out.println("PASS AI regression " + checks + " assertions (in-memory H2, no application or provider)");
    }
    private static void crypto() {
        AiSecrets keys = secrets();
        String first = keys.encrypt("synthetic-key", 11L), second = keys.encrypt("synthetic-key", 11L);
        check(!first.equals(second), "fresh GCM nonce");
        check("synthetic-key".equals(keys.decrypt(first, 11L)), "GCM roundtrip");
        rejects(() -> keys.decrypt(first, 12L), "GCM binds model ID");
        String proof = keys.sign("test", Map.of("id", 11L), 10000);
        check(((Number)keys.verify(proof,"test").get("id")).longValue()==11, "signed proof");
        check(keys.verify(proof,"test")!=null, "stateless proof can be reused");
        rejects(() -> keys.verify(proof,"draft"), "proof purpose binding");
        rejects(() -> keys.verify(proof.substring(0,proof.length()-2)+"XX","test"), "signature tamper");
        rejects(() -> keys.verify(keys.sign("test",Map.of(),-1),"test"), "expired proof");
        rejects(() -> new AiSecrets("","",JSON).encrypt("value",1L), "no fallback key");
    }
    private static void prompt() throws Exception {
        Prompt input = new Prompt("hello");
        Prompt safe = AiPromptPolicy.constrain(input,"allowed",4096,1000,JSON);
        check("allowed".equals(safe.getOptions().getModel()), "resolved model fixed");
        OpenAiChatOptions options=OpenAiChatOptions.builder().maxCompletionTokens(2048).build();
        Prompt completion=AiPromptPolicy.constrain(new Prompt("hello",options),"allowed",4096,1000,JSON,"MAX_COMPLETION_TOKENS");
        check(((OpenAiChatOptions)completion.getOptions()).getMaxCompletionTokens()==2048, "native completion token budget");
        check(completion.getOptions().getMaxTokens()==null, "mutually exclusive output fields");
        options.setMaxCompletionTokens(5000);
        rejects(() -> AiPromptPolicy.constrain(new Prompt("hello",options),"allowed",4096,1000,JSON,"MAX_COMPLETION_TOKENS"),"completion cap");
        options.setMaxCompletionTokens(128); options.setModel("different");
        rejects(() -> AiPromptPolicy.constrain(new Prompt("hello",options),"allowed",4096,1000,JSON,"MAX_COMPLETION_TOKENS"),"model injection");
        options.setModel(null); options.setExtraBody(Map.of("model","different"));
        rejects(() -> AiPromptPolicy.constrain(new Prompt("hello",options),"allowed",4096,1000,JSON,"MAX_COMPLETION_TOKENS"),"extra body injection");
        options.setExtraBody(null); options.setHttpHeaders(Map.of("Authorization","Bearer secret"));
        rejects(() -> AiPromptPolicy.constrain(new Prompt("hello",options),"allowed",4096,1000,JSON,"MAX_COMPLETION_TOKENS"),"headers injection");
        options.setHttpHeaders(null); options.setN(2);
        rejects(() -> AiPromptPolicy.constrain(new Prompt("hello",options),"allowed",4096,1000,JSON,"MAX_COMPLETION_TOKENS"),"multiple outputs");
        check(AiPromptPolicy.constrain(new Prompt("😀".repeat(1000)),"allowed",4096,1000,JSON) != null,"Unicode exact boundary");
        rejects(() -> AiPromptPolicy.constrain(new Prompt("😀".repeat(1001)),"allowed",4096,1000,JSON),"Unicode budget");
        String digest=AiPromptFingerprint.of(input);
        check(digest.equals(AiPromptFingerprint.of(safe)),"safe model/options do not change input binding");
        check(!digest.equals(AiPromptFingerprint.of(new Prompt("hello changed"))),"input changes invalidate scope");
        OutboundRegression.run();
        rejects(() -> new AiDataScope("dataset:1@v1","permissions:v1",digest,true,null),"sample confirmation required");
    }
    private static void persistence() throws Exception {
        String url="jdbc:h2:mem:ai_regression;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=2000";
        Configuration hibernate=new Configuration().addAnnotatedClass(CoreAiSetting.class).addAnnotatedClass(CoreAiModel.class)
                .addAnnotatedClass(CoreAiOrgPolicy.class).addAnnotatedClass(CoreMenu.class)
                .addAnnotatedClass(CoreAiAuditRetired.class).addAnnotatedClass(CoreAiAuditExecution.class).addAnnotatedClass(CoreAiAuditEntry.class)
                .addAnnotatedClass(CoreAiTask.class).addAnnotatedClass(CoreAiTaskAttempt.class).addAnnotatedClass(CoreAiTaskNode.class).addAnnotatedClass(CoreAiTaskSchedule.class).addAnnotatedClass(CoreAiTaskResource.class)
                .setProperty("hibernate.connection.url",url).setProperty("hibernate.connection.driver_class","org.h2.Driver")
                .setProperty("hibernate.connection.username","sa").setProperty("hibernate.connection.password","")
                .setProperty("hibernate.dialect","org.hibernate.dialect.H2Dialect").setProperty("hibernate.hbm2ddl.auto","none")
                .setProperty("hibernate.show_sql","false");
        try(var factory=hibernate.buildSessionFactory();var validators=jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            EntityManager em=SharedEntityManagerCreator.createSharedEntityManager(factory);
            var manager=new JpaTransactionManager(factory); TransactionTemplate tx=new TransactionTemplate(manager);
            var repo=new JpaRepositoryFactory(em);
            var settings=repo.getRepository(CoreAiSettingRepository.class);var models=repo.getRepository(CoreAiModelRepository.class);var policies=repo.getRepository(CoreAiOrgPolicyRepository.class);
            var menus=repo.getRepository(CoreMenuRepository.class);
            var jdbc=new JdbcTemplate(new DriverManagerDataSource(url,"sa",""));
            jdbc.execute("create table core_menu(id bigint primary key,pid bigint,type integer,name varchar(45),component varchar(45),menu_sort integer,icon varchar(45),path varchar(45),hidden boolean,in_layout boolean,auth boolean)");
            jdbc.update("insert into core_menu(id,pid,type,name,component,hidden,in_layout,auth) values(130,15,2,?,?,false,true,true)", "retained-menu", "retained/component");
            var taskMigration=new io.dataease.listener.sql.AiTaskInit(jdbc,factory,em,manager); taskMigration.execute(); taskMigration.execute();
            // Simulate an existing 2.49 task database; migration must add missing columns and retry safely.
            jdbc.execute("alter table core_ai_task_attempt drop column delivery_expires_at");
            jdbc.execute("alter table core_ai_task_attempt drop column notice_ack_at");
            jdbc.execute("alter table core_ai_task_attempt drop column retry_safe");
            jdbc.execute("alter table core_ai_task_attempt drop column execution_id");
            jdbc.execute("alter table core_ai_task_attempt drop column recovery_json");
            jdbc.update("insert into core_ai_task_attempt (id,task_id,attempt_number,retry_hash,status,created_at,finished_at) values (777,777,1,'legacy-delivery','COMPLETED',1000,2000)");
            var deliveryMigration=new io.dataease.listener.sql.AiTaskDeliveryInit(jdbc,factory); deliveryMigration.execute(); deliveryMigration.execute();
            if (jdbc.queryForObject("select delivery_expires_at from core_ai_task_attempt where id=777", Long.class) != 2000L + io.dataease.ai.manage.AiTaskManage.DELIVERY_MILLIS
                    || jdbc.queryForObject("select notice_ack_at from core_ai_task_attempt where id=777", Long.class) != 2000L) throw new AssertionError("legacy result expiry backfill preserves original finish and creates no old pending notice");
            System.out.println("PASS AI 2.50 additive retryable delivery migration, original expiry and no manufactured legacy notices");
            var migration=new AiConfigurationInit(jdbc,factory,em,manager); migration.execute(); migration.execute();
            var recoveryMigration=new io.dataease.listener.sql.AiRecoveryInit(jdbc,factory); recoveryMigration.execute(); recoveryMigration.execute();
            check(!jdbc.queryForObject("select retry_safe from core_ai_task_attempt where id=777", Boolean.class), "legacy attempts without trusted tool safety cannot be blindly replayed");
            jdbc.update("delete from core_ai_task_attempt where id=777");
            new io.dataease.listener.sql.AiTaskSchedulingInit(jdbc,factory,em,manager).execute();
            new io.dataease.listener.sql.AiTaskResourceInit(jdbc,factory,em,manager).execute();
            var menuMigration=new io.dataease.listener.sql.AiConfigurationMenuInit(em,manager);menuMigration.execute();menuMigration.execute();
            check(settings.count()==1,"migration singleton idempotent without ddl-auto");check(menus.count()==2,"existing menu and independent AI configuration menu");
            check(menus.findById(140L).isPresent() && "retained-menu".equals(menus.findById(130L).orElseThrow().getName()),"2.44 then 2.45 preserve a non-AI menu 130");
            tx.executeWithoutResult(status -> { var old=em.find(CoreMenu.class,130L);old.setName("ai-service");old.setComponent("system/ai-service");em.flush(); });
            menuMigration.execute();migration.execute();menuMigration.execute();
            check(menus.count()==1 && menus.findById(130L).isEmpty(),"early development AI130 is cleaned without being recreated");
            MenuRegression.run(em,manager,menus);
            var directory = new java.util.concurrent.atomic.AtomicReference<java.util.List<io.dataease.api.permissions.auth.api.InteractiveAuthApi.AiOrganization>>();
            var keyring = new AiKeyringManage(em, manager, "", "");
            keyring.initialize();
            AiSecrets keys = new AiSecrets(keyring, JSON);
            var permitted = new java.util.concurrent.atomic.AtomicBoolean(true);
            AiDeploymentMode mode = new AiDeploymentMode(new StaticListableBeanFactory().getBeanProvider(io.dataease.license.manage.F2CLicManage.class),new StaticListableBeanFactory().getBeanProvider(io.dataease.api.permissions.auth.api.InteractiveAuthApi.class),new org.springframework.context.support.StaticApplicationContext()) {
                public State current() { return new State(Mode.LICENSED,"test-license-v1"); }
            };
            AiAccess access=new SyntheticAccess(new StaticListableBeanFactory().getBeanProvider(io.dataease.api.permissions.auth.api.InteractiveAuthApi.class), mode) {
                public void configuration() { if(!permitted.get())throw AiErrors.error("forbidden"); }
                public Identity capture() { return new Identity(1L,null,null,mode.current()); }
                public void verify(Identity identity, boolean configuration) { if(!permitted.get())throw AiErrors.error("forbidden"); }
                public String identity() { return AiAccess.binding(capture()); }
                public java.util.List<io.dataease.api.permissions.auth.api.InteractiveAuthApi.AiOrganization> organizations() { return directory.get(); }
            };
            var limiter=new AiAdmission("ehcache","standalone",new StaticListableBeanFactory().getBeanProvider(org.springframework.data.redis.connection.RedisConnectionFactory.class),settings,"test",manager);
            AiOutboundPolicy outbound=new AiOutboundPolicy() {
                public String normalize(String address) { return address; }
            };
            var manage=new AiConfigurationManage(manager);
            inject(manage,"settings",settings);inject(manage,"models",models);inject(manage,"policies",policies);inject(manage,"em",em);inject(manage,"validator",validators.getValidator());
            inject(manage,"access",access);inject(manage,"secrets",keys);inject(manage,"admission",limiter);inject(manage,"outbound",outbound);

            new io.dataease.listener.sql.AiAuditInit(jdbc,factory,em,manager).execute();
            new io.dataease.listener.sql.AiRetentionInit(jdbc,factory,em,manager).execute();
            if (Boolean.getBoolean("ai.test.retentionOnly")) { RetentionRegression.run(manage, access, permitted, em, manager, jdbc); return; }

            rejects(() -> tx.executeWithoutResult(status -> manage.saveSettings(new SettingsChange(0L,false,null,false,60,30000,4096,5,2,null))),"configuration cannot publish before an outer transaction commits");
            Draft draft=manage.draft(); Candidate c=new Candidate();c.setId(draft.id());c.setDraftHandle(draft.draftHandle());c.setName("Model");c.setProvider("OPENAI_COMPATIBLE");c.setBaseUrl("https://mock.invalid/v1");c.setApiKey("synthetic-key");c.setModelIdentifier("general-chat");
            rejects(() -> manage.save(c,true),"new model needs test proof");check(models.count()==0,"failed save rollback");
            c.setTestProof(manage.testProof(manage.candidate(c,true)));Model saved=manage.save(c,true);
            check(saved.enabled()&&saved.validated(),"tested model enabled on creation");
            rejects(() -> manage.save(c,true),"stable ID INSERT rejects duplicate");check(models.count()==1,"no duplicate model");
            Candidate edit=new Candidate();edit.setId(saved.id());edit.setExpectedVersion(saved.rowVersion());edit.setExpectedConfigRevision(saved.configRevision());edit.setName("Renamed");edit.setProvider(saved.provider());edit.setBaseUrl(saved.baseUrl());edit.setModelIdentifier(saved.modelIdentifier());
            Model renamed=manage.save(edit,false);check(renamed.name().equals("Renamed"),"name-only edit without retest");
            rejects(() -> manage.save(edit,false),"stale row version rejects repeat edit");
            CoreAiSetting limits=manage.setting();
            var permit1=limiter.acquire(limits,false,System.currentTimeMillis()+10000);var permit2=limiter.acquire(limits,false,System.currentTimeMillis()+10000);
            manage.saveSettings(new SettingsChange(limits.getRowVersion(),limits.getEnabled(),limits.getDefaultModelId(),limits.getAllowSamples(),limits.getRequestTimeoutSeconds(),limits.getMaxInputChars(),limits.getMaxOutputTokens(),1,1,null));CoreAiSetting lowered=manage.setting();rejects(() -> limiter.acquire(lowered,false,System.currentTimeMillis()+10000),"lower limit preserves active count");permit1.close();rejects(() -> limiter.acquire(lowered,false,System.currentTimeMillis()+10000),"at new limit rejects");permit2.close();limiter.acquire(lowered,false,System.currentTimeMillis()+10000).close();
            check(!manage.organizations("").available(),"community does not invent organizations");
            CoreAiSetting s=manage.setting();long expected=s.getRowVersion();
            String impact=manage.impact("SETTINGS",saved.id()+":true").token();
            manage.saveSettings(new SettingsChange(expected,true,saved.id(),false,60,30000,4096,5,2,impact));
            rejects(() -> manage.action(saved.id(),new ModelAction(renamed.rowVersion(),null,null),true),"referenced model cannot delete");
            CoreAiSetting forgedCapacity = JSON.convertValue(manage.setting(), CoreAiSetting.class);
            forgedCapacity.setInteractiveConcurrency(20);
            rejects(() -> limiter.acquire(forgedCapacity,false,System.currentTimeMillis()+10000), "matching revision cannot spoof a larger current model-request capacity");
            CoreAiSetting stalePolicy = manage.setting();
            var waiting = new java.util.concurrent.CountDownLatch(1);
            var resume = new java.util.concurrent.CountDownLatch(1);
            var rejected = new java.util.concurrent.atomic.AtomicBoolean();
            Thread oldSender = new Thread(() -> { waiting.countDown(); try { resume.await(); limiter.acquire(stalePolicy,false,System.currentTimeMillis()+10000).close(); } catch (AiErrors.Failure expectedFailure) { rejected.set(true); } catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); } });
            oldSender.start();waiting.await();
            s=manage.setting();manage.saveSettings(new SettingsChange(s.getRowVersion(),true,s.getDefaultModelId(),false,60,30000,4096,1,1,null));
            resume.countDown();oldSender.join(3000);check(rejected.get(),"old JVM policy rejected after completed downgrade");
            s=manage.setting();String clear=manage.impact("SETTINGS","null:true").token();
            manage.saveSettings(new SettingsChange(s.getRowVersion(),true,null,false,60,30000,4096,5,2,clear));
            Model before=manage.detail(saved.id());manage.action(saved.id(),new ModelAction(before.rowVersion(),null,null),true);
            check(models.findById(saved.id()).orElseThrow().getDeletedAt()!=null,"deleted ID tombstone");
            check(models.findById(saved.id()).orElseThrow().getKeyEnvelope()==null,"deleted key cleared");
            rejects(() -> manage.save(c,true),"old create cannot resurrect deleted ID");
            Draft next=manage.draft();c.setId(next.id());c.setDraftHandle(next.draftHandle());c.setName("Renamed");c.setTestProof(manage.testProof(manage.candidate(c,true)));manage.save(c,true);
            check(models.count()==2,"soft delete releases name but preserves ID");
            CoreAiSetting runtimeSetting=manage.setting();
            String runtimeImpact=manage.impact("SETTINGS",next.id()+":true").token();
            manage.saveSettings(new SettingsChange(runtimeSetting.getRowVersion(),true,next.id(),false,60,30000,4096,5,2,runtimeImpact));
            if (Boolean.getBoolean("ai.test.modelLimitsOnly")) { ModelLimitsRegression.run(manage, limiter); return; }
            if (Boolean.getBoolean("ai.test.conversationOnly")) { ConversationRegression.run(manage, policies, keys, limiter, access); return; }
            if (Boolean.getBoolean("ai.test.runtimeOnly")) { RuntimeRegression.run(manage, policies, keys, limiter, access); return; }
            if (Boolean.getBoolean("ai.test.auditOnly")) { AuditRegression.run(manage,policies,keys,limiter,access,em,manager,jdbc,factory); return; }
            if (Boolean.getBoolean("ai.test.recoveryToolsOnly")) { ToolConversationRegression.run(manage,policies,keys,limiter,access); return; }
            if (Boolean.getBoolean("ai.test.recoveryOnly")) { ConversationRegression.run(manage,policies,keys,limiter,access); return; }
            new io.dataease.listener.sql.AiAuditInit(jdbc,factory,em,manager).execute();
            TaskRegression.run(em,manager,access,manage,policies,keys,limiter);
            if (Boolean.getBoolean("ai.test.onlyTasks")) return;
            RuntimeRegression.run(manage,policies,keys,new AiAdmission("ehcache","standalone",new StaticListableBeanFactory().getBeanProvider(org.springframework.data.redis.connection.RedisConnectionFactory.class),settings,"test",manager),access);
            ConversationRegression.run(manage,policies,keys,limiter,access);
            ToolConversationRegression.run(manage,policies,keys,limiter,access);
            HttpRegression.run(new AiAdmission("ehcache","standalone",new StaticListableBeanFactory().getBeanProvider(org.springframework.data.redis.connection.RedisConnectionFactory.class),settings,"test",manager), manage.setting());
            TokenCapabilityRegression.run(manage,policies,access,keys,limiter,permitted);
            directory.set(List.of(new io.dataease.api.permissions.auth.api.InteractiveAuthApi.AiOrganization(88L,"Sales",0L),
                    new io.dataease.api.permissions.auth.api.InteractiveAuthApi.AiOrganization(89L,"Finance",0L),
                    new io.dataease.api.permissions.auth.api.InteractiveAuthApi.AiOrganization(90L,"Sales child",88L)));
            manage.batch(new OrgBatch(List.of(new ExpectedOrg(88L,null),new ExpectedOrg(89L,null)),"SPECIFIED",next.id()));
            check(policies.count()==2,"batch exact targets exclude unselected child");
            check(manage.list("",1,10).items().stream().anyMatch(model -> model.organizationNames().contains("Finance")),"list uses complete organization reference names");
            CoreAiOrgPolicy org88=policies.findById(88L).orElseThrow();
            rejects(()->manage.batch(new OrgBatch(List.of(new ExpectedOrg(88L,org88.getRowVersion()),new ExpectedOrg(999L,null)),"DISABLED",null)),"invalid batch target rolls back all");
            check("SPECIFIED".equals(policies.findById(88L).orElseThrow().getMode()),"first batch member not partially saved");
            manage.batch(new OrgBatch(List.of(new ExpectedOrg(88L,org88.getRowVersion())),"INHERIT",null));
            check(policies.findById(88L).orElseThrow().getModelId()==null,"inherit clears prior model explicitly");
            Impact full=manage.impact("SETTINGS","null:true");check(full.organizationCount()==2,"complete default impact includes new inherited child");
            CoreAiSetting beforeRename=manage.setting();
            directory.set(List.of(new io.dataease.api.permissions.auth.api.InteractiveAuthApi.AiOrganization(88L,"Sales",0L),
                    new io.dataease.api.permissions.auth.api.InteractiveAuthApi.AiOrganization(89L,"Finance",0L),
                    new io.dataease.api.permissions.auth.api.InteractiveAuthApi.AiOrganization(90L,"Renamed child",88L)));
            rejects(()->manage.saveSettings(new SettingsChange(beforeRename.getRowVersion(),true,null,false,60,30000,4096,5,2,full.token())),"stale organization impact confirmation rejected");
            tx.executeWithoutResult(status -> manage.beforeDelete(88L));check(!policies.existsById(88L),"organization deletion clears strategy");
            Model defaultModel=manage.detail(next.id());
            manage.action(next.id(),new ModelAction(defaultModel.rowVersion(),false,manage.impact("DISABLE",""+next.id()).token()),false);
            CoreAiSetting disabledDefault=manage.setting();
            manage.saveSettings(new SettingsChange(disabledDefault.getRowVersion(),false,next.id(),false,60,30000,4096,5,2,manage.impact("SETTINGS",next.id()+":false").token()));
            CoreAiSetting closed=manage.setting();
            manage.saveSettings(new SettingsChange(closed.getRowVersion(),false,next.id(),false,30,30000,4096,5,2,null));
            check(manage.setting().getRequestTimeoutSeconds()==30,"stopped default does not block closing service or editing limits");
            CoreAiSetting clearStopped=manage.setting();
            manage.saveSettings(new SettingsChange(clearStopped.getRowVersion(),false,null,false,60,30000,4096,5,2,manage.impact("SETTINGS","null:false").token()));
            CoreAiSetting emptyDefault=manage.setting();String stoppedImpact=manage.impact("SETTINGS",next.id()+":false").token();
            rejects(()->manage.saveSettings(new SettingsChange(emptyDefault.getRowVersion(),false,next.id(),false,60,30000,4096,5,2,stoppedImpact)),"cannot switch default to stopped model");
            Model stopped=manage.detail(next.id());manage.action(next.id(),new ModelAction(stopped.rowVersion(),true,null),false);
            CoreAiSetting restored=manage.setting();manage.saveSettings(new SettingsChange(restored.getRowVersion(),true,next.id(),false,60,30000,4096,5,2,manage.impact("SETTINGS",next.id()+":true").token()));
            CoreAiSetting validSettings=manage.setting();
            rejects(()->manage.saveSettings(new SettingsChange(validSettings.getRowVersion(),true,next.id(),false,0,30000,4096,5,2,null)),"Java entry enforces configured timeout bounds");
            var pendingAdmission=new AiAdmission("ehcache","standalone",new StaticListableBeanFactory().getBeanProvider(org.springframework.data.redis.connection.RedisConnectionFactory.class),settings,"test",manager){
                public void finishUpdate(String owner,CoreAiSetting current,boolean committed){throw AiErrors.error("policy_blocked");}
                public State state(CoreAiSetting current){return new State(current.getRevision()-1,"UPDATING");}
            };
            inject(manage,"admission",pendingAdmission);
            Settings pending=manage.saveSettings(new SettingsChange(validSettings.getRowVersion(),true,next.id(),false,60,30000,4096,5,2,null));
            check("UPDATING".equals(pending.runtimeState())&&pending.persistedRevision()>pending.appliedRevision(),"committed configuration reports publication pending");
            inject(manage,"admission",new AiAdmission("ehcache","standalone",new StaticListableBeanFactory().getBeanProvider(org.springframework.data.redis.connection.RedisConnectionFactory.class),settings,"test",manager));
            CommunityRuntimeRegression.run(manage, models, policies, keys, limiter, em, manager, access);
            Integer redisPort=Integer.getInteger("ai.test.redisPort");if(redisPort!=null)RedisRegression.run(settings,manager,em,redisPort);
        }
    }
}

/** Fixtures replacing deployment/identity use this seam; ModeRegression exercises the real license scope. */
abstract class SyntheticAccess extends AiAccess {
    SyntheticAccess(org.springframework.beans.factory.ObjectProvider<io.dataease.api.permissions.auth.api.InteractiveAuthApi> authorization, AiDeploymentMode deployment) {
        super(authorization, deployment);
    }
    @Override public <T> T inDeploymentScope(Identity owner, java.util.function.Supplier<T> operation) {
        if (!Objects.equals(owner.deployment(), mode())) throw AiErrors.error("conflict");
        return operation.get();
    }
}

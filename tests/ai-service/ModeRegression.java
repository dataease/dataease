package io.dataease.ai;

import io.dataease.ai.manage.*;
import io.dataease.ai.support.AiErrors;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.license.bo.F2CLicResult;
import io.dataease.license.dao.po.LicensePO;
import io.dataease.license.manage.F2CLicManage;
import io.dataease.permission.util.V3UserUtil;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.context.support.StaticApplicationContext;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.atomic.*;

/** Synthetic existing license source and authentication SPI; never changes an installed license. */
public final class ModeRegression {
    static final class Fixture {
        final AtomicReference<F2CLicResult.Status> status = new AtomicReference<>(F2CLicResult.Status.no_record);
        final AtomicBoolean enabled = new AtomicBoolean(true), failure = new AtomicBoolean(), race = new AtomicBoolean(), nullRecord = new AtomicBoolean(), authorizationNull = new AtomicBoolean();
        final AtomicInteger menus = new AtomicInteger(), communities = new AtomicInteger(), directories = new AtomicInteger();
        final LicensePO record = new LicensePO();
        final AiDeploymentMode mode;
        final AiAccess access;
        Fixture() {
            record.setUpdateTime(1L); record.setLicense("synthetic-license");
            var source = (F2CLicManage) Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{F2CLicManage.class},(p,m,a)-> {
                if (m.getName().equals("read")) return nullRecord.get() ? null : record;
                if (m.getName().equals("validate")) {
                    if(failure.get()) throw new IllegalStateException("synthetic unavailable");
                    if(race.getAndSet(false)) record.setUpdateTime(record.getUpdateTime()+1);
                    F2CLicResult result = new F2CLicResult(); result.setStatus(status.get()); return result;
                }
                throw new UnsupportedOperationException(m.getName());
            });
            var api = (InteractiveAuthApi) Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{InteractiveAuthApi.class},(p,m,a)-> {
                switch(m.getName()) {
                    case "checkAiCommunityAllowed":
                        return !authorizationNull.get() && enabled.get() && Objects.equals(a[0],1L);
                    case "checkAiAllowed":
                        menus.incrementAndGet(); return authorizationNull.get() ? null : enabled.get();
                    case "checkAiCommunityAccess":
                        communities.incrementAndGet(); if(!enabled.get()||!Objects.equals(a[0],1L)) throw new IllegalStateException("identity rejected"); return null;
                    case "checkAiAccess":
                        menus.incrementAndGet(); if(!enabled.get())throw new IllegalStateException("identity rejected");return null;
                    case "aiOrganizations":
                        directories.incrementAndGet(); return List.of(new InteractiveAuthApi.AiOrganization(9001L,"Synthetic history",null),new InteractiveAuthApi.AiOrganization(9002L,"Synthetic disabled",null));
                    default: throw new UnsupportedOperationException(m.getName());
                }
            });
            var beans = new StaticListableBeanFactory(Map.of("source",source,"authorization",api));
            mode = new AiDeploymentMode(beans.getBeanProvider(F2CLicManage.class),beans.getBeanProvider(InteractiveAuthApi.class),new StaticApplicationContext());
            access = new AiAccess(beans.getBeanProvider(InteractiveAuthApi.class),mode);
        }
    }
    static void require(boolean value,String label) { if(!value)throw new AssertionError(label); }
    static void denied(Runnable work,String label) { try{work.run();throw new AssertionError(label);}catch(AiErrors.Failure expected){} }
    private static void licenseScope() {
        new io.dataease.license.utils.CommonBeanFactory().setApplicationContext(new StaticApplicationContext());
        Fixture f = new Fixture(); f.status.set(F2CLicResult.Status.valid); V3UserUtil.setUid(1L);
        var owner = f.access.capture(); V3UserUtil.clear();
        var scope = new AiIdentityScope(f.access);
        io.dataease.license.utils.LicenseUtil.set(null);
        scope.call(owner, () -> {
            require(io.dataease.license.utils.LicenseUtil.licenseValid(), "licensed worker must retain the original business license dispatch");
            return null;
        });
        require(io.dataease.license.utils.LicenseUtil.get() == null && V3UserUtil.getUid() == null, "worker license and actor are cleaned");
        var previous = new F2CLicResult(); previous.setStatus(F2CLicResult.Status.expired);
        io.dataease.license.utils.LicenseUtil.set(previous);
        try {
            scope.call(owner, () -> {
                Object outer = io.dataease.license.utils.LicenseUtil.get();
                scope.call(owner, () -> { require(io.dataease.license.utils.LicenseUtil.licenseValid(), "nested scope remains licensed"); return null; });
                require(io.dataease.license.utils.LicenseUtil.get() == outer, "nested scope restores its caller license");
                throw new IllegalStateException("synthetic business failure");
            });
            throw new AssertionError("business failure must propagate");
        } catch (IllegalStateException expected) { }
        require(io.dataease.license.utils.LicenseUtil.get() == previous && V3UserUtil.getUid() == null, "exception restores previous license and actor");
        AtomicBoolean entered = new AtomicBoolean();
        f.failure.set(true);
        denied(() -> scope.call(owner, () -> entered.getAndSet(true)), "license source unavailable refuses work");
        f.failure.set(false); f.race.set(true);
        denied(() -> scope.call(owner, () -> entered.getAndSet(true)), "license record changing during validation refuses work");
        denied(() -> scope.call(owner, () -> entered.getAndSet(true)), "renewed source rejects stale owner revision");
        require(!entered.get() && io.dataease.license.utils.LicenseUtil.get() == previous, "refused work cannot inherit caller license");
        f.status.set(F2CLicResult.Status.no_record); V3UserUtil.setUid(1L);
        var community = f.access.capture(); V3UserUtil.clear();
        previous.setStatus(F2CLicResult.Status.valid);
        scope.call(community, () -> { require(!io.dataease.license.utils.LicenseUtil.licenseValid(), "community scope cannot inherit a licensed caller"); return null; });
        require(io.dataease.license.utils.LicenseUtil.get() == previous && V3UserUtil.getUid() == null, "community scope restores its caller");
        io.dataease.license.utils.LicenseUtil.set(null);
        System.out.println("PASS AI worker license dispatch, nested/exception cleanup, community isolation and fresh-source revocation");
    }
    public static void run() throws Exception {
        licenseScope();
        Fixture f = new Fixture(); V3UserUtil.setUid(1L);
        f.nullRecord.set(true);require(!f.mode.current().organizations(),"explicit no-record source supports nullable empty record");
        f.status.set(F2CLicResult.Status.valid);denied(f.mode::current,"missing record with valid result is inconsistent");
        f.nullRecord.set(false);f.status.set(F2CLicResult.Status.no_record);
        var identity = f.access.capture();f.access.verify(identity,true);
        f.authorizationNull.set(true); denied(() -> f.access.verify(identity, false), "null authorization result must not signal successful community authorization"); f.authorizationNull.set(false);
        f.access.audit(identity, null);
        denied(() -> f.access.audit(identity, 9001L), "community does not invent workspace audit authority");
        f.enabled.set(false); denied(() -> f.access.audit(identity, null), "disabled community administrator cannot read audit"); f.enabled.set(true);
        require(!identity.deployment().organizations()&&identity.organization()==null&&f.menus.get()==0,"unlicensed configuration does not check matrix");
        require(f.access.organizations()==null&&f.directories.get()==0,"unlicensed does not load organizations");
        AtomicReference<Throwable> workerFailure = new AtomicReference<>();
        Thread worker = new Thread(()->{try{f.access.verify(identity,true);}catch(Throwable e){workerFailure.set(e);}});
        worker.start();worker.join();require(workerFailure.get()==null,"worker uses source instead of empty request ThreadLocal");
        V3UserUtil.setProxy(9001L);denied(f.access::capture,"community proxy rejected before normalization");V3UserUtil.clear();V3UserUtil.setUid(1L);
        denied(()->f.access.verify(new AiAccess.Identity(1L,9001L,null,f.mode.current()),false),"community task org rejected");
        V3UserUtil.setUid(2L);denied(()->f.access.configuration(),"community does not grant ordinary enterprise actor");V3UserUtil.setUid(1L);
        f.status.set(F2CLicResult.Status.valid);denied(()->f.access.verify(identity,true),"mode change rejects captured operation");
        var freshLicensed=f.access.capture();
        f.authorizationNull.set(true); denied(() -> f.access.verify(freshLicensed, true), "null licensed authorization result cannot signal success"); f.authorizationNull.set(false);
        denied(() -> f.access.audit(freshLicensed, null), "configuration privilege and missing audit extension cannot authorize audit");
        var checker = InteractiveAuthApi.class.getMethod("checkAiAuditAccess", Long.class, Long.class, Long.class, Long.class);
        require(org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation(checker, org.springframework.web.bind.annotation.RequestMapping.class) == null,
                "audit SPI has no HTTP route");
        var oldExtension = (InteractiveAuthApi) Proxy.newProxyInstance(ModeRegression.class.getClassLoader(), new Class<?>[]{InteractiveAuthApi.class},
                (proxy, method, arguments) -> method.isDefault() ? java.lang.reflect.InvocationHandler.invokeDefault(proxy, method, arguments) : null);
        require(Boolean.FALSE.equals(oldExtension.checkAiAllowed(1L, null, null, false)) && Boolean.FALSE.equals(oldExtension.checkAiCommunityAllowed(1L)), "old extension cannot authorize new explicit AI capabilities");
        require(Boolean.FALSE.equals(oldExtension.checkAiAuditAccess(1L, null, null, null)), "old extension default explicitly denies rather than signaling success with void/null");
f.access.verify(freshLicensed,true);require(f.menus.get()>0,"licensed matrix retained");
        var licensed=freshLicensed;f.record.setUpdateTime(2L);denied(()->f.access.verify(licensed,true),"same-mode license record renewal rejects old context");
        f.failure.set(true);denied(f.mode::current,"source error is not community");f.failure.set(false);
        f.status.set(null);denied(f.mode::current,"unknown result rejected");
        f.status.set(F2CLicResult.Status.expired);require(!f.mode.current().organizations(),"expired follows product community mode");
        f.status.set(F2CLicResult.Status.invalid);require(!f.mode.current().organizations(),"invalid follows product community mode");
        f.race.set(true);denied(f.mode::current,"record changed during validation");
        f.enabled.set(false);denied(f.access::configuration,"disabled account rejected");
        var empty=new StaticListableBeanFactory();var local=new StaticApplicationContext();local.getBeanFactory().registerSingleton("substituleLoginServer",new Object());
        var substitute=new AiDeploymentMode(empty.getBeanProvider(F2CLicManage.class),empty.getBeanProvider(InteractiveAuthApi.class),local);
        var substituteAccess=new AiAccess(empty.getBeanProvider(InteractiveAuthApi.class),substitute);substituteAccess.configuration();
        local.getBeanFactory().registerSingleton("userServer",new Object());denied(substitute::current,"broken extension cannot use substitute fallback");
        V3UserUtil.clear();V3UserUtil.setUid(1L);
        System.out.println("PASS AI trusted deployment mode, worker identity, source errors, renewal, substitute and community proxy/actor rejection");
    }
}

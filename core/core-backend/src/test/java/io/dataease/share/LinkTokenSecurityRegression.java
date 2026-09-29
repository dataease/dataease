package io.dataease.share;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.dataease.permission.config.AuthFilter;
import io.dataease.permission.model.LinkIdentity;
import io.dataease.permission.template.LinkTokenVerifier;
import io.dataease.permission.template.LoginTokenVerifier;
import io.dataease.permission.util.ModelUtils;
import io.dataease.permission.util.TokenUtils;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.share.dao.auto.entity.XpackShare;
import io.dataease.share.util.LinkTokenUtil;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import jakarta.servlet.http.Cookie;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/** Standalone regression runner: no application startup, network or database required. */
public class LinkTokenSecurityRegression {
    private static int assertions;
    private static final String SECRET = "test-only-server-secret-with-at-least-32-characters";

    public static void main(String[] args) throws Exception {
        XpackShare share = new XpackShare();
        share.setId(10L);
        share.setCreator(1L);
        share.setResourceId(20L);
        share.setOid(30L);
        share.setUuid("test-share");
        share.setPwd("visitor-password");
        share.setVisitorPermissions(7);
        String token = LinkTokenUtil.generate(share, SECRET);
        check(LinkTokenUtil.verify(token, share, SECRET).getClaim("uid").asLong() == 1L, "valid administrator share");
        check(JWT.decode(token).getExpiresAt() != null, "permanent URL still gets expiring token");
        reject(() -> LinkTokenUtil.verify(token, share, "wrong-secret"), "wrong signing secret");
        String unsigned = token.substring(0, token.lastIndexOf('.') + 1);
        reject(() -> LinkTokenUtil.verify(unsigned, share, SECRET), "missing signature");
        reject(() -> LinkTokenUtil.verify(JWT.create().withClaim("uid", 1L).sign(Algorithm.none()), share, SECRET), "none algorithm");
        share.setOid(31L);
        reject(() -> LinkTokenUtil.verify(token, share, SECRET), "different organization");
        share.setOid(30L);
        share.setResourceId(21L);
        reject(() -> LinkTokenUtil.verify(token, share, SECRET), "different resource");
        share.setResourceId(20L);
        share.setPwd("changed-password");
        reject(() -> LinkTokenUtil.verify(token, share, SECRET), "password revokes old token");
        share.setPwd("visitor-password");
        share.setVisitorPermissions(1);
        reject(() -> LinkTokenUtil.verify(token, share, SECRET), "permission change revokes old token");
        share.setVisitorPermissions(7);
        share.setExp(System.currentTimeMillis() - 1000);
        reject(() -> LinkTokenUtil.generate(share, SECRET), "expired share cannot issue token");
        share.setExp(null);
        var signing = LinkTokenUtil.class.getDeclaredMethod("algorithm", XpackShare.class, String.class);
        signing.setAccessible(true);
        Algorithm algorithm = (Algorithm) signing.invoke(null, share, SECRET);
        String expired = JWT.create().withIssuer("dataease").withAudience("share")
                .withClaim("purpose", "link").withClaim("version", 2).withClaim("shareId", 10L)
                .withClaim("uid", 1L).withClaim("resourceId", 20L).withClaim("oid", 30L)
                .withJWTId("test").withIssuedAt(new Date(System.currentTimeMillis() - 3000))
                .withExpiresAt(new Date(System.currentTimeMillis() - 1000)).sign(algorithm);
        reject(() -> LinkTokenUtil.verify(expired, share, SECRET), "expired signed token");
        String missingExp = JWT.create().withIssuer("dataease").withAudience("share")
                .withClaim("purpose", "link").withClaim("version", 2).withClaim("shareId", 10L)
                .withClaim("uid", 1L).withClaim("resourceId", 20L).withClaim("oid", 30L).sign(algorithm);
        reject(() -> LinkTokenUtil.verify(missingExp, share, SECRET), "missing expiry");
        reject(() -> LinkTokenUtil.verify(JWT.create().withClaim("uid", 1L).sign(Algorithm.HMAC256(share.getPwd())), share, SECRET), "visitor password cannot sign new tokens");

        GenericApplicationContext context = new GenericApplicationContext();
        context.getEnvironment().getSystemProperties().put("server.servlet.context-path", "/");
        context.registerBean("linkVerifier", LinkTokenVerifier.class, () -> new LinkTokenVerifier() {
            public LinkIdentity verify(String value) {
                LinkTokenUtil.verify(value, share, SECRET);
                return new LinkIdentity(10L, 1L, 20L, 30L);
            }
            public void checkRequestPath(String uri, String method) { }
            public void checkStaticResource(LinkIdentity identity, String uri) {
                if (!uri.endsWith("/allowed.png")) throw new IllegalArgumentException("Outside share");
            }
        });
        context.registerBean("loginVerifier", LoginTokenVerifier.class, () -> (value, request) -> {
            JWT.require(Algorithm.HMAC256("test-login-key")).withClaim("uid", 2L).build().verify(value);
            return 2L;
        });
        context.registerBean("users", io.dataease.permission.template.V3AuthTemplate.class,
                () -> uid -> new io.dataease.permission.model.V3BaseUser("test", "test", 30L, "unused", true));
        context.refresh();
        new io.dataease.license.utils.CommonBeanFactory().setApplicationContext(context);
        new io.dataease.utils.CommonBeanFactory().setApplicationContext(context);
        new ModelUtils().setModelValue("standalone");
        V3UserUtil.setLink(TokenUtils.verifyLinkToken(token));
        check(!V3UserUtil.isSysAdmin(), "share does not become global administrator");
        V3UserUtil.clear();
        check(V3UserUtil.getLink() == null && V3UserUtil.getUid() == null, "context cleared");
        request("/protected", "X-DE-LINK-TOKEN", token, null, true, true);
        request("/protected", "X-DE-LINK-TOKEN", unsigned, null, false, false);
        request("/static-resource/allowed.png", null, null, new Cookie("DE-LINK-TOKEN", token), true, true);
        request("/static-resource/other.png", null, null, new Cookie("DE-LINK-TOKEN", token), false, false);
        request("/static-resource/allowed.png", null, null, new Cookie("DE-LINK-TOKEN", unsigned), false, false);
        String login = JWT.create().withClaim("uid", 2L).withExpiresAt(new Date(System.currentTimeMillis() + 60000))
                .sign(Algorithm.HMAC256("test-login-key"));
        request("/protected", "X-DE-TOKEN", login, null, true, false);
        request("/static-resource/allowed.png", null, null, new Cookie("DE-TOKEN", login), true, false);
        request("/static-resource/allowed.png", null, null, new Cookie("DE-TOKEN", unsigned), false, false);
        request("/protected", "X-DE-TOKEN", token, null, false, false);
        request("/protected", "X-DE-LINK-TOKEN", login, null, false, false);
        request("/protected", null, null, null, false, false);
        var failedRequest = new MockHttpServletRequest("GET", "/protected");
        failedRequest.addHeader("X-DE-LINK-TOKEN", token);
        try {
            new AuthFilter().doFilter(failedRequest, new MockHttpServletResponse(), (req, res) -> {
                throw new jakarta.servlet.ServletException("test business failure");
            });
            throw new AssertionError("Business failure was swallowed");
        } catch (jakarta.servlet.ServletException expected) {
            check(V3UserUtil.getUid() == null && V3UserUtil.getLink() == null, "cleanup on business failure");
        }
        scopeTests(share, token);
        context.close();
        GenericApplicationContext missing = new GenericApplicationContext();
        missing.refresh();
        new io.dataease.license.utils.CommonBeanFactory().setApplicationContext(missing);
        reject(() -> TokenUtils.verifyLinkToken(token), "missing verifier fails closed");
        var substitute = io.dataease.auth.config.SubstituleLoginConfig.class;
        var ready = substitute.getDeclaredField("ready");
        ready.setAccessible(true);
        ready.set(null, true);
        var pwd = substitute.getDeclaredField("pwd");
        pwd.setAccessible(true);
        pwd.set(null, "test-substitute-password");
        String secret = io.dataease.utils.Md5Utils.md5("test-substitute-password");
        String substituteToken = JWT.create().withClaim("uid", 1L).sign(Algorithm.HMAC256(secret));
        check(TokenUtils.validate(substituteToken).equals(1L), "existing substitute login format remains valid");
        reject(() -> TokenUtils.validate(JWT.create().withClaim("uid", 2L).sign(Algorithm.HMAC256(secret))),
                "substitute signature cannot impersonate another user");
        reject(() -> TokenUtils.validate(unsigned), "substitute login refuses unsigned credential");
        missing.getBeanFactory().registerSingleton("loginServer", new Object());
        reject(() -> TokenUtils.validate(substituteToken), "missing enterprise login verifier never falls back");
        missing.close();
        System.out.println("PASS: " + assertions + " authentication regression assertions");
    }

    public static class ShareEndpoint {
        @io.dataease.auth.DeLinkPermit
        public void read(Long id) { }
        public void write(Long id) { }
        @io.dataease.auth.DeLinkPermit("#p0.sceneId")
        public void chart(io.dataease.extensions.view.dto.ChartViewDTO view) { }
        @io.dataease.auth.DeLinkPermit("#p0.dvId")
        public void export(io.dataease.api.chart.request.ChartExcelRequest request) { }
        @io.dataease.auth.DeLinkPermit("#p0.id")
        public void resource(io.dataease.api.visualization.request.DataVisualizationBaseRequest request) { }
        @io.dataease.auth.DeLinkPermit(value = "#p0", subResource = true)
        public void child(Long id) { }
    }

    private static void scopeTests(XpackShare share, String token) throws Exception {
        var service = new io.dataease.share.manage.LinkTokenValidationManage();
        var secrets = new io.dataease.share.manage.ShareSecretManage();
        field(secrets, "defaultPwd", SECRET);
        field(service, "secrets", secrets);
        AtomicBoolean exists = new AtomicBoolean(true);
        var shares = (io.dataease.share.dao.auto.mapper.XpackShareRepository) java.lang.reflect.Proxy.newProxyInstance(
                LinkTokenSecurityRegression.class.getClassLoader(),
                new Class[]{io.dataease.share.dao.auto.mapper.XpackShareRepository.class},
                (proxy, method, args) -> method.getName().equals("findById")
                        ? (exists.get() ? java.util.Optional.of(share) : java.util.Optional.empty()) : null);
        field(service, "shares", shares);
        var resource = new io.dataease.dao.auto.entity.DataVisualizationInfo();
        resource.setId(20L);
        resource.setDeleteFlag(false);
        resource.setComponentData("[{\"field\":{\"id\":123},\"url\":\"/de2api/static-resource/allowed.png\"}]");
        var repository = (io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository)
                java.lang.reflect.Proxy.newProxyInstance(LinkTokenSecurityRegression.class.getClassLoader(),
                        new Class[]{io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository.class},
                        (proxy, method, args) -> switch (method.getName()) {
                            case "findById" -> java.util.Optional.of(resource);
                            case "queryComponentData" -> resource.getComponentData();
                            default -> null;
                        });
        field(service, "visualizations", repository);
        var settings = new io.dataease.api.system.vo.ShareBaseVO();
        field(service, "parameters", new io.dataease.system.manage.SysParameterManage() {
            @Override public io.dataease.api.system.vo.ShareBaseVO shareBase() { return settings; }
        });
        var identity = service.verify(token);
        check(identity.resourceId().equals(20L), "share verified against current database record");
        exists.set(false);
        reject(() -> service.verify(token), "deleted share rejected");
        exists.set(true);
        settings.setDisable(true);
        reject(() -> service.verify(token), "globally disabled share rejected");
        settings.setDisable(false);
        resource.setDeleteFlag(true);
        reject(() -> service.verify(token), "deleted canvas rejected");
        resource.setDeleteFlag(false);
        service.checkStaticResource(identity, "/de2api/static-resource/allowed.png");
        assertions++;
        reject(() -> service.checkStaticResource(identity, "/de2api/static-resource/other.png"), "unreferenced image rejected");
        reject(() -> service.checkStaticResource(identity, "/de2api/static-resource/../allowed.png"), "path traversal rejected");
        var mappings = new org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping();
        mappings.registerMapping(org.springframework.web.servlet.mvc.method.RequestMappingInfo.paths("/de2api/shared/{id}")
                .methods(org.springframework.web.bind.annotation.RequestMethod.GET).build(), new ShareEndpoint(),
                ShareEndpoint.class.getMethod("read", Long.class));
        mappings.registerMapping(org.springframework.web.servlet.mvc.method.RequestMappingInfo.paths("/de2api/write/{id}")
                .methods(org.springframework.web.bind.annotation.RequestMethod.POST).build(), new ShareEndpoint(),
                ShareEndpoint.class.getMethod("write", Long.class));
        field(service, "mappings", mappings);
        V3UserUtil.setLink(identity);
        service.checkRequestPath("/de2api/shared/20", "GET");
        assertions++;
        reject(() -> service.checkRequestPath("/de2api/shared/20", "POST"), "method mismatch rejected");
        reject(() -> service.checkRequestPath("/de2api/write/20", "POST"), "admin share cannot access non-share endpoint");
        var scope = new io.dataease.share.interceptor.ShareQueryScopeAop();
        field(scope, "visualizations", repository);
        scope.requireFields(java.util.List.of(123L));
        assertions++;
        reject(() -> scope.requireFields(java.util.List.of(999L)), "foreign enumeration field rejected");
        var aop = new io.dataease.share.interceptor.DeLinkAop();
        field(aop, "dataVisualizationInfoRepository", repository);
        var storedChart = new io.dataease.dao.auto.entity.CoreChartView();
        storedChart.setId(50L);
        storedChart.setSceneId(20L);
        storedChart.setTableId(40L);
        var charts = java.lang.reflect.Proxy.newProxyInstance(LinkTokenSecurityRegression.class.getClassLoader(),
                new Class[]{io.dataease.chart.dao.auto.mapper.CoreChartViewRepository.class},
                (proxyObject, method, arguments) -> java.util.Optional.of(storedChart));
        field(aop, "charts", charts);
        var view = new io.dataease.extensions.view.dto.ChartViewDTO();
        view.setId(50L);
        view.setSceneId(20L);
        view.setTableId(40L);
        runAop(aop, view, "chart", view.getClass());
        assertions++;
        storedChart.setSceneId(99L);
        reject(() -> runAop(aop, view, "chart", view.getClass()), "forged sceneId cannot access foreign chart");
        storedChart.setSceneId(20L);
        view.setTableId(41L);
        reject(() -> runAop(aop, view, "chart", view.getClass()), "chart cannot switch dataset");
        view.setTableId(40L);
        var export = new io.dataease.api.chart.request.ChartExcelRequest();
        export.setDvId("20");
        export.setViewInfo(view);
        export.setViewId("50");
        runAop(aop, export, "export", export.getClass());
        assertions++;
        export.setViewId("51");
        reject(() -> runAop(aop, export, "export", export.getClass()), "export must bind to same chart");
        runAop(aop, 20L);
        assertions++;
        reject(() -> runAop(aop, 21L), "valid admin token cannot access another canvas");
        var read = new io.dataease.api.visualization.request.DataVisualizationBaseRequest(20L, "dashboard");
        runAop(aop, read, "resource", read.getClass());
        assertions++;
        read.setResourceTable("snapshot");
        reject(() -> runAop(aop, read, "resource", read.getClass()), "share cannot read editor snapshot");
        read.setResourceTable("core");
        read.setSource("main-edit");
        reject(() -> runAop(aop, read, "resource", read.getClass()), "share cannot trigger editor recovery");
        resource.setComponentData("[{\"screenId\":42,\"field\":{\"id\":123}}]");
        runAop(aop, 42L, "child", Long.class);
        assertions++;
        reject(() -> runAop(aop, 43L, "child", Long.class), "only referenced child is accessible");
        var enumeration = new io.dataease.api.dataset.dto.MultFieldValuesRequest();
        enumeration.setFieldIds(java.util.List.of(123L));
        enumeration.setUserId(99L);
        reject(() -> scope.checkRequest(enumeration), "share cannot substitute another query identity");
        enumeration.setUserId(null);
        var server = new io.dataease.dataset.server.DatasetDataServer();
        field(server, "datasetDataManage", new io.dataease.dataset.manage.DatasetDataManage() {
            @Override public java.util.List<String> getFieldEnum(
                    io.dataease.api.dataset.dto.MultFieldValuesRequest request, boolean permissions) {
                return java.util.List.of("allowed");
            }
        });
        var factory = new org.springframework.aop.aspectj.annotation.AspectJProxyFactory(server);
        factory.setProxyTargetClass(true);
        factory.addAspect(scope);
        io.dataease.dataset.server.DatasetDataServer proxy = factory.getProxy();
        check(proxy.getFieldEnum(enumeration).equals(java.util.List.of("allowed")), "query aspect permits configured field");
        enumeration.setFieldIds(java.util.List.of(999L));
        reject(() -> proxy.getFieldEnum(enumeration), "query aspect blocks foreign field before business catch");
        V3UserUtil.clear();
        check(proxy.getFieldEnum(enumeration).equals(java.util.List.of("allowed")), "normal login query remains unchanged");


    }

    private static void runAop(io.dataease.share.interceptor.DeLinkAop aop, Long id) {
        runAop(aop, id, "read", Long.class);
    }

    private static void runAop(io.dataease.share.interceptor.DeLinkAop aop, Object argument, String name, Class<?> type) {
        try {
            var signature = java.lang.reflect.Proxy.newProxyInstance(LinkTokenSecurityRegression.class.getClassLoader(),
                    new Class[]{org.aspectj.lang.reflect.MethodSignature.class},
                    (proxy, method, args) -> method.getName().equals("getMethod") ? ShareEndpoint.class.getMethod(name, type) : null);
            var point = (org.aspectj.lang.ProceedingJoinPoint) java.lang.reflect.Proxy.newProxyInstance(
                    LinkTokenSecurityRegression.class.getClassLoader(), new Class[]{org.aspectj.lang.ProceedingJoinPoint.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getArgs" -> new Object[]{argument};
                        case "getSignature" -> signature;
                        default -> null;
                    });
            aop.logAround(point);
        } catch (RuntimeException e) { throw e; }
        catch (Throwable e) { throw new RuntimeException(e); }
    }

    private static void field(Object bean, String name, Object value) throws Exception {
        var field = bean.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(bean, value);
    }

    private static void request(String uri, String header, String token, Cookie cookie, boolean allowed, boolean link) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        if (header != null) request.addHeader(header, token);
        if (cookie != null) request.setCookies(cookie);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean called = new AtomicBoolean();
        new AuthFilter().doFilter(request, response, (req, res) -> {
            called.set(true);
            check(V3UserUtil.getUid() != null, "verified identity available to chain");
            check((V3UserUtil.getLink() != null) == link, "credential type preserved");
        });
        check(called.get() == allowed, "request accepted/rejected correctly");
        if (!allowed) check(response.getStatus() == 401, "authentication failure returns 401");
        check(V3UserUtil.getUid() == null && V3UserUtil.getLink() == null, "request context never leaks");
    }

    private static void check(boolean value, String label) {
        assertions++;
        if (!value) throw new AssertionError(label);
    }

    private static void reject(Runnable action, String label) {
        try { action.run(); } catch (RuntimeException expected) { assertions++; return; }
        throw new AssertionError(label);
    }
}

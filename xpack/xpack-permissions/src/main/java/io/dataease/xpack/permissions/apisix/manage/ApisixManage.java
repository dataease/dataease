package io.dataease.xpack.permissions.apisix.manage;

import io.dataease.auth.DePermit;
import io.dataease.utils.LogUtil;
import io.dataease.utils.ServletUtils;
import io.dataease.xpack.permissions.apisix.proxy.ProxyRequest;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.util.ServletRequestPathUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Service
public class ApisixManage {
    private static final String PATH = "org.springframework.web.util.ServletRequestPathUtils.PATH";
    private static final String PARAM_VARIABLE_PREFIX = "p";
    private static final String SPRING_EL_FLAG = "#";

    private static final String TOKEN_KEY = "Authorization";

    private final ExpressionParser parser = new SpelExpressionParser();
    @Resource
    private AuthHandlerMethodMapping authHandlerMethodMapping;

    @Resource
    private AuthMappingHandlerAdapter authMappingHandlerAdapter;

    /**
     * 认证校验
     * 从请求头获取token进行解析验证并存放在threadLocal中
     * @return
     */
    public void checkAuthenticationInfo(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_KEY);
        LogUtil.info("current token is {}", token);
        // get user info and put it in threadLocal
    }

    /**
     * 权限校验
     * 1、根据请求url映射到具体接口(模拟springMVC实现)
     * 2、获取接口上DePermit注解信息(结果是一段springEl表达式数组)
     * 3、根据请求获取接口参数(模拟springMVC实现)
     * 4、使用springEl表达式以及2、3的结果获取当前url需要的权限
     * 5、根据token中携带的用户信息坚定当前用户是否有4中需要的权限
     * @return
     */
    public void checkAuthorizationInfo(HttpServletRequest request) {
        String[] requirePermissions = getRequirePermissions(request);
        // get userinfo from threadLocal
        Object userinfo = null;
        checkPermission(userinfo, requirePermissions);
    }

    public String[] getRequirePermissions(HttpServletRequest request) {
        try {
            Object attribute = request.getAttribute(PATH);
            ProxyRequest proxyRequest = new ProxyRequest(request);
            HttpServletRequest proxy = (HttpServletRequest) Proxy.newProxyInstance(request.getClass().getClassLoader(), request.getClass().getInterfaces(), proxyRequest);
            RequestPath requestPath = ServletRequestPathUtils.parseAndCache(proxy);
            proxy.setAttribute(PATH, requestPath);
            HandlerMethod handlerMethod = authHandlerMethodMapping.getHandlerMethod(proxy);
            if (ObjectUtils.isEmpty(handlerMethod) || !handlerMethod.hasMethodAnnotation(DePermit.class)) return null;
            DePermit dePermit = handlerMethod.getMethodAnnotation(DePermit.class);
            String[] valueArray = dePermit.value();
            Method method = handlerMethod.getMethod();
            LogUtil.info("custom mapping is [{} -> {}]", proxy.getServletPath(), method.getName());
            LogUtil.info("method auth perExp is {}", StringUtils.join(valueArray, ", "));
            request.setAttribute(PATH, attribute);
            Object[] params = authMappingHandlerAdapter.getParams(proxy, ServletUtils.response(), handlerMethod);
            if (ArrayUtils.isNotEmpty(params)) {
                LogUtil.info("api param size is {}", params.length);
            }
            String[] requirePermissions = methodAuth(params, valueArray);
            LogUtil.info("current url [{}] require permssion [{}]", proxy.getServletPath(), StringUtils.join(requirePermissions, ", "));
            return requirePermissions;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private String[] methodAuth(Object[] params, String[] expArray) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        if (params != null && params.length == 1) {
            context.setRootObject(params[0]);
        }

        for (int i = 0; i < params.length; i++) {
            Object paramValue = params[i];
            context.setVariable(PARAM_VARIABLE_PREFIX + i, paramValue);
        }
        int len = expArray.length;
        String[] result = new String[len];
        for (int i = 0; i < len; i++) {
            result[i] = resolveValue(expArray[i], context);
        }
        return result;
    }

    private String resolveValue(String exp, EvaluationContext context) {
        String result = exp;
        if (StringUtils.contains(exp, SPRING_EL_FLAG)) {
            Expression expression = parser.parseExpression(exp);
            result = expression.getValue(context, String.class);
        }
        return result;
    }

    /**
     * 目前用户权限数据结构未定 只提供接口
     * @param userInfo
     * @param requirePermissions
     */
    protected void checkPermission(Object userInfo, String requirePermissions[]) {

    }
}

package io.dataease.xpack.permissions.apisix.manage;

import cn.hutool.core.util.ArrayUtil;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.constant.AuthConstant;
import io.dataease.constant.AuthEnum;
import io.dataease.constant.AuthResourceEnum;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.ServletUtils;
import io.dataease.utils.TokenUtils;
import io.dataease.utils.UserUtils;
import io.dataease.xpack.permissions.apisix.proxy.ProxyRequest;
import io.dataease.xpack.permissions.auth.manage.ApiAuthManage;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
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

import java.lang.reflect.Proxy;

@Service
public class ApisixManage {
    private static final String PATH = "org.springframework.web.util.ServletRequestPathUtils.PATH";
    private static final String PARAM_VARIABLE_PREFIX = "p";
    private static final String SPRING_EL_FLAG = "#";


    private final ExpressionParser parser = new SpelExpressionParser();
    @Resource
    private AuthHandlerMethodMapping authHandlerMethodMapping;


    @Resource
    private AuthMappingHandlerAdapter authMappingHandlerAdapter;

    @Resource
    private ApiAuthManage apiAuthManage;

    /**
     * 认证校验
     * 从请求头获取token进行解析验证并存放在threadLocal中
     *
     * @return
     */
    public void checkAuthenticationInfo(HttpServletRequest request) {
        String token = request.getHeader(AuthConstant.TOKEN_KEY);
        TokenUserBO userBO = TokenUtils.validate(token);
        UserUtils.setUserInfo(userBO);
    }

    /**
     * 权限校验
     * 1、根据请求url映射到具体接口(模拟springMVC实现)
     * 2、获取接口上DePermit注解信息(结果是一段springEl表达式数组)
     * 3、根据请求获取接口参数(模拟springMVC实现)
     * 4、使用springEl表达式以及2、3的结果获取当前url需要的权限
     * 5、根据token中携带的用户信息坚定当前用户是否有4中需要的权限
     *
     * @return
     */
    public void checkAuthorizationInfo(HttpServletRequest request) {
        String[] requirePermissions = getRequirePermissions(request);
        TokenUserBO user = AuthUtils.getUser();
        checkPermission(user, requirePermissions);
        ServletUtils.response().addHeader(AuthConstant.APISIX_FLAG_KEY, String.valueOf(System.currentTimeMillis()));
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
            DeApiPath deApiPath = handlerMethod.getBeanType().getAnnotation(DeApiPath.class);
            AuthResourceEnum rt = deApiPath.rt();
            String[] valueArray = dePermit.value();

            request.setAttribute(PATH, attribute);
            Object[] params = authMappingHandlerAdapter.getParams(proxy, ServletUtils.response(), handlerMethod);

            String[] requirePermissions = methodAuth(params, valueArray, rt);
            return requirePermissions;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private String[] methodAuth(Object[] params, String[] expArray, AuthResourceEnum rt) {
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
        String prefix = rt.name() + ":";
        for (int i = 0; i < len; i++) {
            result[i] = prefix + resolveValue(expArray[i], context);
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
     *
     * @param userInfo
     * @param requirePermissions
     */
    protected void checkPermission(TokenUserBO userInfo, String requirePermissions[]) {
        if (ArrayUtil.isEmpty(requirePermissions) || AuthUtils.isSysAdmin(userInfo.getUserId())) return;
        for (int i = 0; i < requirePermissions.length; i++) {
            String permission = requirePermissions[i];
            PerFormatter formatter = formatPer(permission);
            AuthResourceEnum resourceEnum = formatter.getAuthResourceEnum();
            if (StringUtils.contains(permission, ":m:")) {
                apiAuthManage.checkMenu(resourceEnum.getMenuId(), formatter.getWeight(), userInfo);
            } else {
                apiAuthManage.checkResource(Long.parseLong(formatter.getId()), resourceEnum.getFlag(), formatter.getWeight(), userInfo);
            }
        }
    }

    private PerFormatter formatPer(String permission) {
        String[] flags = permission.split(":");
        String name = flags[0];
        AuthResourceEnum anEnum = AuthResourceEnum.valueOf(name);
        String weightFlag = flags[2];
        AuthEnum authEnum = AuthEnum.valueOf(weightFlag.toUpperCase());
        Integer weight = authEnum.getWeight();
        return new PerFormatter(anEnum, weight, flags[1]);
    }

    @Data
    class PerFormatter {
        private AuthResourceEnum authResourceEnum;
        private Integer weight;
        private String id;

        public PerFormatter(AuthResourceEnum authResourceEnum, Integer weight, String id) {
            this.authResourceEnum = authResourceEnum;
            this.weight = weight;
            this.id = id;
        }
    }

}

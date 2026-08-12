package io.dataease.share.interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.dataease.auth.DeLinkPermit;
import io.dataease.constant.AuthConstant;
import io.dataease.exception.DEException;
import io.dataease.utils.LogUtil;
import io.dataease.utils.ServletUtils;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Aspect
@Component
public class DeLinkAop {

    private static final String PARAM_VARIABLE_PREFIX = "p";
    private static final String SPRING_EL_FLAG = "#";

    private final ExpressionParser parser = new SpelExpressionParser();

    // 匹配父画布 componentData 中内嵌子画布的 screenId 字段（值可能为字符串或数字）
    private static final Pattern SCREEN_ID_PATTERN = Pattern.compile("\"screenId\"\\s*:\\s*\"?(\\d+)\"?");

    @Resource
    private DataVisualizationInfoRepository dataVisualizationInfoRepository;


    @Around(value = "@annotation(io.dataease.auth.DeLinkPermit)")
    public Object logAround(ProceedingJoinPoint point) throws Throwable {
        Object[] params = point.getArgs();
        String linkToken = ServletUtils.getHead(AuthConstant.LINK_TOKEN_KEY);
        if (StringUtils.isNotBlank(linkToken)) {
            MethodSignature ms = (MethodSignature) point.getSignature();
            Method method = ms.getMethod();
            DeLinkPermit deLinkPermit = method.getAnnotation(DeLinkPermit.class);
            String value = deLinkPermit.value();
            if (StringUtils.isBlank(value)) {
                value = SPRING_EL_FLAG + PARAM_VARIABLE_PREFIX + "0";
            }
            Long id = getExpression(params, value);
            DecodedJWT jwt = JWT.decode(linkToken);
            Long resourceId = jwt.getClaim("resourceId").asLong();
            if (!Objects.equals(id, resourceId)) {
                // 子资源模式：token 绑定的是父画布，参数 id 是内嵌子画布。
                // 校验子画布确实内嵌于父画布中，否则视为越权（防止枚举任意资源）。
                if (!(deLinkPermit.subResource() && isSubResourceOf(resourceId, id))) {
                    DEException.throwException("link token invalid");
                    return false;
                }
            }
        }
        try {
            return point.proceed(params);
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
            throw e;
        }
    }

    /**
     * 校验 childId 是否作为内嵌子画布存在于 parentId 对应父画布的 componentData 中。
     */
    private boolean isSubResourceOf(Long parentId, Long childId) {
        if (ObjectUtils.isEmpty(parentId) || ObjectUtils.isEmpty(childId)) {
            return false;
        }
        String componentData = dataVisualizationInfoRepository.queryComponentData(parentId);
        if (StringUtils.isBlank(componentData)) {
            return false;
        }
        Matcher matcher = SCREEN_ID_PATTERN.matcher(componentData);
        String childIdStr = childId.toString();
        while (matcher.find()) {
            if (childIdStr.equals(matcher.group(1))) {
                return true;
            }
        }
        return false;
    }

    public Long getExpression(Object[] params, String expression) {
        StandardEvaluationContext context = buildContext(params);
        Object o = resolveValue(expression, context);
        if (ObjectUtils.isNotEmpty(o)) return Long.parseLong(o.toString());
        return null;
    }

    private StandardEvaluationContext buildContext(Object[] params) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        if (params != null && params.length == 1) {
            context.setRootObject(params[0]);
        }
        for (int i = 0; i < Objects.requireNonNull(params).length; i++) {
            Object paramValue = params[i];
            context.setVariable(PARAM_VARIABLE_PREFIX + i, paramValue);
        }
        return context;
    }

    private Object resolveValue(String exp, EvaluationContext context) {
        if (StringUtils.contains(exp, SPRING_EL_FLAG)) {
            Expression expression = parser.parseExpression(exp);
            return expression.getValue(context);
        }
        return exp;
    }
}

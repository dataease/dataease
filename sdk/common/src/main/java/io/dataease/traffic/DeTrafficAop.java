package io.dataease.traffic;

import io.dataease.exception.DEException;
import io.dataease.traffic.dao.entity.CoreApiTraffic;
import io.dataease.traffic.dao.mapper.CoreApiTrafficMapper;
import io.dataease.utils.IDUtils;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class DeTrafficAop {

    @Resource
    private CoreApiTrafficMapper coreApiTrafficMapper;

    @Value("${dataease.traffic:2}")
    private Integer defaultTraffic;

    final private static String errorMsg = "当前API【%s】设定并发阈值为【%s】，现已经达到限流阈值，请稍后再试！";

    @Around(value = "@annotation(io.dataease.traffic.DeTraffic)")
    public Object trafficAround(ProceedingJoinPoint point) throws Throwable {
        MethodSignature ms = (MethodSignature) point.getSignature();
        Method method = ms.getMethod();
        DeTraffic traffic = method.getAnnotation(DeTraffic.class);

        int value = traffic.value();
        if (value == 0) {
            value = defaultTraffic;
        }
        String api = traffic.api();
        Object result = null;
        boolean access = false;
        try {
            Integer count = coreApiTrafficMapper.apiCount(api);
            if (count == 0) {
                CoreApiTraffic apiTraffic = new CoreApiTraffic();
                apiTraffic.setId(IDUtils.snowID());
                apiTraffic.setAlive(1);
                apiTraffic.setThreshold(value);
                apiTraffic.setApi(api);
                coreApiTrafficMapper.insert(apiTraffic);
                access = true;
                result = point.proceed();
                return result;
            }
            int alive = coreApiTrafficMapper.getAlive(api);
            if (alive < value) {
                coreApiTrafficMapper.upgrade(api);
                access = true;
                result = point.proceed();
                return result;
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        } finally {
            if (access) {
                coreApiTrafficMapper.releaseAlive(api);
            }
        }
        DEException.throwException(String.format(errorMsg, api, value));
        return null;
    }
}

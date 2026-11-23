package io.dataease.datasource.manage;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.factory.ProviderFactory;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 数据源扩展方法调用服务：封装 API/插件 Provider 的反射调用。
 * 不依赖任何 *Server，供 DatasourceServer 与 DatasourceSyncManage 等复用。
 */
@Component
public class DatasourceInvokeManage {

    public Object invokeMethod(String dsType, String methodName, Class<?> classes, Object object) {
        Object resObj = null;
        try {
            Method method = getMethod(dsType, methodName, classes);
            resObj = method.invoke(null, object);
        } catch (Exception e) {
            DEException.throwException(msg(e));
        }
        return resObj;
    }

    private Method getMethod(String dsType, String methodName, Class<?> classes) {
        Method method = null;
        try {
            String ClassName = "io.dataease.datasource.provider.ApiUtils";
            if (!dsType.equals(DatasourceConfiguration.DatasourceType.API.name())) {
                Provider provider = ProviderFactory.getProvider(dsType);
                method = provider.getClass().getMethod(methodName, classes);
            } else {
                Class<?> clazz = Class.forName(ClassName);
                method = clazz.getMethod(methodName, classes);
            }

        } catch (Exception e) {
            DEException.throwException("Cant find method: " + e.getMessage());
        }
        return method;
    }

    private String msg(Throwable e) {
        Throwable exception = e;
        while (true) {
            if (exception.getCause() == null) {
                return exception.getMessage();
            }
            if (exception instanceof DEException && (!(exception.getCause() instanceof DEException) && !(exception.getCause() instanceof InvocationTargetException))) {
                return exception.getMessage();
            }
            exception = exception.getCause();

        }
    }
}

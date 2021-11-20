package io.dataease.plugins.loader;


import io.dataease.commons.utils.LogUtil;
import io.dataease.plugins.config.SpringContextUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.lang.reflect.Method;
import java.util.List;

@Component
public class ControllerLoader {

    /**
     * 去掉Controller的Mapping
     * @param controllerBeanName
     */
    private void unregisterController(String controllerBeanName){
        final RequestMappingHandlerMapping requestMappingHandlerMapping=(RequestMappingHandlerMapping)SpringContextUtil.getBean("requestMappingHandlerMapping");

        if(requestMappingHandlerMapping!=null){
            String handler=controllerBeanName;
            Object controller= SpringContextUtil.getBean(handler);
            if(controller==null){
                return;
            }
            final Class<?> targetClass=controller.getClass();
            ReflectionUtils.doWithMethods(targetClass, new ReflectionUtils.MethodCallback() {
                public void doWith(Method method) {
                    Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
                    try {
                        Method createMappingMethod = RequestMappingHandlerMapping.class.
                                getDeclaredMethod("getMappingForMethod", Method.class, Class.class);
                        createMappingMethod.setAccessible(true);
                        RequestMappingInfo requestMappingInfo =(RequestMappingInfo)
                                createMappingMethod.invoke(requestMappingHandlerMapping,specificMethod,targetClass);
                        if(requestMappingInfo != null) {
                            requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }, ReflectionUtils.USER_DECLARED_METHODS);
        }
    }

    /**
     * 注册Controller
     * @param controllerBeanName
     * @throws Exception
     */
    private void registerController(String controllerBeanName) throws Exception{
        final RequestMappingHandlerMapping requestMappingHandlerMapping=(RequestMappingHandlerMapping) SpringContextUtil.getBean("requestMappingHandlerMapping");

        if(requestMappingHandlerMapping!=null){
            String handler=controllerBeanName;
            Object controller= SpringContextUtil.getBean(handler);
            if(controller==null){
                return;
            }
            unregisterController(controllerBeanName);
            //注册Controller
            Method method=requestMappingHandlerMapping.getClass().getSuperclass().getSuperclass().getDeclaredMethod("detectHandlerMethods",Object.class);

            method.setAccessible(true);
            method.invoke(requestMappingHandlerMapping,handler);
        }
    }

    public void registerController(List<String> beanNames) {
        beanNames.forEach(name -> {
            try {
                registerController(name);
            } catch (Exception e) {
                LogUtil.error(e);
            }
        });
    }











}

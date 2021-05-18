package io.dataease.plugins.config;

import io.dataease.plugins.loader.ClassloaderResponsity;
import io.dataease.plugins.loader.ModuleClassLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

@Component
public class LoadjarUtil {

    public List<?> loadJar(String jarPath){
        File jar = new File(jarPath);
        URI uri = jar.toURI();
        String moduleName = jarPath.substring(jarPath.lastIndexOf("/")+1,jarPath.lastIndexOf("."));
        try {

            if(ClassloaderResponsity.getInstance().containsClassLoader(moduleName)){
                ClassloaderResponsity.getInstance().removeClassLoader(moduleName);
            }

            ModuleClassLoader classLoader = new ModuleClassLoader(new URL[]{uri.toURL()}, Thread.currentThread().getContextClassLoader());
            SpringContextUtil.getBeanFactory().setBeanClassLoader(classLoader);
            Thread.currentThread().setContextClassLoader(classLoader);
            classLoader.initBean();
            ClassloaderResponsity.getInstance().addClassLoader(moduleName,classLoader);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return SpringContextUtil.getAllBean();
    }

    public List<Map<String, Object>> deleteModule(String moduleName){
        if(ClassloaderResponsity.getInstance().containsClassLoader(moduleName)){
            ClassloaderResponsity.getInstance().removeClassLoader(moduleName);
        }
        return beans();
    }
    public List<Map<String, Object>> beans(){
        return SpringContextUtil.getAllBean();
    }
}

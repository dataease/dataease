package io.dataease.plugins.config;

import io.dataease.plugins.common.base.domain.MyPlugin;
import io.dataease.plugins.loader.ClassloaderResponsity;
import io.dataease.plugins.loader.ControllerLoader;
import io.dataease.plugins.loader.ModuleClassLoader;
import io.dataease.plugins.loader.MybatisLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

@Component
public class LoadjarUtil {

    @Autowired
    private MybatisLoader mybatisLoader;

    @Autowired
    private ControllerLoader controllerLoader;

    public List<?> loadJar(String jarPath, MyPlugin myPlugin)  throws Exception{
        File jar = new File(jarPath);
        URI uri = jar.toURI();
        String moduleName = myPlugin.getModuleName() + "-" + myPlugin.getVersion();

        if(ClassloaderResponsity.getInstance().containsClassLoader(moduleName)){
            ClassloaderResponsity.getInstance().removeClassLoader(moduleName);
        }

        ModuleClassLoader classLoader = new ModuleClassLoader(new URL[]{uri.toURL()}, Thread.currentThread().getContextClassLoader());
        SpringContextUtil.getBeanFactory().setBeanClassLoader(classLoader);
        Thread.currentThread().setContextClassLoader(classLoader);
        classLoader.initBean();
        mybatisLoader.loadMybatis(myPlugin);

        List<String> controllers = classLoader.getRegisteredController();
        controllerLoader.registerController(controllers);

        ClassloaderResponsity.getInstance().addClassLoader(moduleName,classLoader);


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

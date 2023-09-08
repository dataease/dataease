package io.dataease.plugins.loader;

import io.dataease.plugins.common.annotation.PluginResultMap;
import io.dataease.plugins.config.SpringContextUtil;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModuleClassLoader extends URLClassLoader {

    //属于本类加载器加载的jar包
    private JarFile jarFile;

    //保存已经加载过的Class对象
    private Map<String,Class> cacheClassMap = new HashMap<>();

    //保存本类加载器加载的class字节码
    private Map<String,byte[]> classBytesMap = new HashMap<>();

    //需要注册的spring bean的name集合
    private List<String> registeredBean = new ArrayList<>();

    private List<String> registeredController = new ArrayList<>();


    //构造
    public ModuleClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        URL url = urls[0];
        String path = url.getPath();
        try {
            jarFile = new JarFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //初始化类加载器执行类加载
        init();
    }



    //重写loadClass方法
    //改写loadClass方式
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if(findLoadedClass(name)==null){
            Class<?> aClass = super.loadClass(name);
            Optional.ofNullable(aClass.getAnnotation(PluginResultMap.class)).ifPresent(anno -> {
                SqlSessionFactory sqlSessionFactory = SpringContextUtil.getBean(SqlSessionFactory.class);
                Configuration configuration = sqlSessionFactory.getConfiguration();
                TypeAliasRegistry typeAliasRegistry = configuration.getTypeAliasRegistry();
                typeAliasRegistry.registerAlias(name.toLowerCase(), aClass);
            });
            return aClass;
        }else{
            return cacheClassMap.get(name);
        }
    }


    /**
     * 方法描述 初始化类加载器，保存字节码
     * @method init
     */
    private void init() {

        //解析jar包每一项
        Enumeration<JarEntry> en = jarFile.entries();
        InputStream input = null;
        try{
            while (en.hasMoreElements()) {
                JarEntry je = en.nextElement();
                String name = je.getName();
                //这里添加了路径扫描限制
                if (name.endsWith(".class")) {String className = name.replace(".class", "").replaceAll("/", ".");

                    input = jarFile.getInputStream(je);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int bufferSize = 4096;
                    byte[] buffer = new byte[bufferSize];
                    int bytesNumRead = 0;
                    while ((bytesNumRead = input.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesNumRead);
                    }
                    byte[] classBytes = baos.toByteArray();
                    classBytesMap.put(className,classBytes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(input!=null){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        //将jar中的每一个class字节码进行Class载入
        for (Map.Entry<String, byte[]> entry : classBytesMap.entrySet()) {
            String key = entry.getKey();
            Class<?> aClass = null;
            try {
                aClass = loadClass(key);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            cacheClassMap.put(key,aClass);
        }

    }

    /**
     * 方法描述 初始化spring bean
     * @method initBean
     */
    public void initBean(){
        for (Map.Entry<String, Class> entry : cacheClassMap.entrySet()) {
            String className = entry.getKey();
            Class<?> cla = entry.getValue();
            if(isSpringBeanClass(cla)){
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(cla);
                BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
                //设置当前bean定义对象是单利的
                beanDefinition.setScope("singleton");

                //将变量首字母置小写
                String beanName = StringUtils.uncapitalize(className);

                beanName =  beanName.substring(beanName.lastIndexOf(".")+1);
                beanName = StringUtils.uncapitalize(beanName);

                SpringContextUtil.getBeanFactory().registerBeanDefinition(beanName,beanDefinition);

                if (isHandler(cla)) {
                    registeredController.add(beanName);
                }

                registeredBean.add(beanName);
            }

        }
    }


    //获取当前类加载器注册的bean
    //在移除当前类加载器的时候需要手动删除这些注册的bean
    public List<String> getRegisteredBean() {
        return registeredBean;
    }

    public List<String> getRegisteredController() {
        return registeredController;
    }


    /**
     * 方法描述 判断class对象是否带有spring的注解
     * @method isSpringBeanClass
     * @param cla jar中的每一个class
     * @return true 是spring bean   false 不是spring bean
     */
    public boolean isSpringBeanClass(Class<?> cla){
        if(cla==null){
            return false;
        }
        //是否是接口
        if(cla.isInterface()){
            return false;
        }

        //是否是抽象类
        if( Modifier.isAbstract(cla.getModifiers())){
            return false;
        }
        if (isHandler(cla)) {
            return true;
        }

        if(cla.getAnnotation(Component.class)!=null){
            return true;
        }
        if(cla.getAnnotation(Repository.class)!=null){
            return true;
        }
        if(cla.getAnnotation(Service.class)!=null){
            return true;
        }
        if(cla.getAnnotation(Service.class)!=null){
            return true;
        }

        return false;
    }

    protected boolean isHandler(Class<?> beanType) {
        return AnnotatedElementUtils.hasAnnotation(beanType, Controller.class) || AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class);
    }

}

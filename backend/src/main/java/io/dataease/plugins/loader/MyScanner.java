package io.dataease.plugins.loader;

import io.dataease.plugins.config.SpringContextUtil;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MyScanner implements BeanDefinitionRegistryPostProcessor {

    @Resource
    private MapperScannerConfigurer mapperScannerConfigurer;

    private BeanDefinitionRegistry beanDefinitionRegistry;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        this.beanDefinitionRegistry = beanDefinitionRegistry;
        System.out.println("-----");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }

    public void scanner() {
        if (null == mapperScannerConfigurer){
            mapperScannerConfigurer = SpringContextUtil.getBean(MapperScannerConfigurer.class);
        }

        mapperScannerConfigurer.postProcessBeanDefinitionRegistry(this.beanDefinitionRegistry);
    }
}

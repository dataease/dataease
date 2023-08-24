package io.dataease.plugins.loader;

import io.dataease.plugins.config.SpringContextUtil;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class MyScanner implements BeanDefinitionRegistryPostProcessor {



    private BeanDefinitionRegistry beanDefinitionRegistry;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        this.beanDefinitionRegistry = beanDefinitionRegistry;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }

    public void scanner() {
        MapperScannerConfigurer mapperScannerConfigurer = SpringContextUtil.getBean(MapperScannerConfigurer.class);

        mapperScannerConfigurer.postProcessBeanDefinitionRegistry(this.beanDefinitionRegistry);
    }
}

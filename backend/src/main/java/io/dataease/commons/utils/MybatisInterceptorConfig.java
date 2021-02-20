package io.dataease.commons.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MybatisInterceptorConfig {
    private String modelName;
    private String attrName;
    private String attrNameForList;
    private String interceptorClass;
    private String interceptorMethod;
    private String undoClass;
    private String undoMethod;

    public MybatisInterceptorConfig() {
    }

    /**
     * 用时需谨慎！！！！！
     * 主要配置多个的时候，参数少一点
     *
     * @param modelClass
     * @param attrName
     */
    public MybatisInterceptorConfig(Class<?> modelClass, String attrName) {
        this.modelName = modelClass.getName();
        this.attrName = attrName;
        this.interceptorClass = EncryptUtils.class.getName();
        this.interceptorMethod = "aesEncrypt";
        this.undoClass = EncryptUtils.class.getName();
        this.undoMethod = "aesDecrypt";
    }

    public MybatisInterceptorConfig(Class<?> modelClass, String attrName, Class<?> interceptorClass, String interceptorMethod, String undoMethod) {
        this.modelName = modelClass.getName();
        this.attrName = attrName;
        this.interceptorClass = interceptorClass.getName();
        this.interceptorMethod = interceptorMethod;
        this.undoClass = interceptorClass.getName();
        this.undoMethod = undoMethod;
    }

}

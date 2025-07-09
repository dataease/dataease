package io.dataease.config;

import org.hibernate.dialect.identity.Oracle12cIdentityColumnSupport;

/**
 * Oracle 版本的身份列支持类，主要用于生成自动递增的主键列。
 * @author jianneng
 * @date 2025/7/4 16:47
 **/
public class DataEaseOracle12cIdentityColumnSupport extends Oracle12cIdentityColumnSupport {
    public static final DataEaseOracle12cIdentityColumnSupport INSTANCE = new DataEaseOracle12cIdentityColumnSupport();

    @Override
    public String getIdentityColumnString(int type) {
        return "generated always as identity (start with 1 increment by 1)";
    }
}

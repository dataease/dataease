package io.dataease.config;

import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;

/**
 * Oracle 版本的方言实现，主要用于支持 DataEase 的特定需求
 * @author jianneng
 * @date 2025/7/4 16:40
 **/
public class DataEaseOracleDialect extends OracleDialect {
    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return DataEaseOracle12cIdentityColumnSupport.INSTANCE;
    }

    /**
     * Oracle number 不允许带精度
     * 需要精度请在字段上使用 @Column(precision = 10, scale = 2)
     */
    @Override
    public String columnType(int sqlTypeCode) {
        if (sqlTypeCode == -5) {
            return "number";
        }
        return super.columnType(sqlTypeCode);
    }
}

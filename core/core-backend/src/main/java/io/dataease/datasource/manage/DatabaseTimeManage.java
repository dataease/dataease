package io.dataease.datasource.manage;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

/**
 * 数据库时间管理类
 * @author jianneng
 * @date 2025/7/7 15:18
 **/
@Component("databaseTimeManage")
public class DatabaseTimeManage {

    @Resource
    private EntityManager entityManager;

    /**
     * 获取当前数据库时间戳
     * @return 当前数据库时间戳
     */
    public Timestamp getCurrentDatabaseTime() {
        Session session = entityManager.unwrap(Session.class);
        Dialect dialect = ((SessionFactoryImplementor) session.getSessionFactory()).getJdbcServices().getDialect();
        // 当前数据库方言下，获取数据库当前时间戳的 SQL 语句字符串
        String sql = dialect.getCurrentTimestampSelectString();
        return session.createNativeQuery(sql, Timestamp.class).getSingleResult();
    }
}

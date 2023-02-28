package io.dataease.flyway;

import io.dataease.utils.CommonBeanFactory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.configuration.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.nio.charset.Charset;

public class TenantFlywayUtil {

    private static final String FLYWAY_MANAGE_TABLE_NAME = "de_manage_version";

    private static final String FLYWAY_TABLENAME_FORMAT = "de_tenant_%s_version";
    private static final String FLYWAY_LOCALTION = "classpath:db/migration";
    private static final String FLYWAY_MANAGE_LOCALTION = "classpath:db/distributed/manage";


    private static Configuration buildConfiguration(DataSource dataSource, boolean isManager) {
        ClassicConfiguration configuration = new ClassicConfiguration();
        configuration.setValidateOnMigrate(true);
        configuration.setBaselineOnMigrate(true);
        configuration.setTable(isManager ? FLYWAY_MANAGE_TABLE_NAME : getVersionTableName());
        configuration.setEncoding(Charset.forName("utf-8"));
        configuration.setOutOfOrder(true);
        configuration.setBaselineVersionAsString("1");
        configuration.setLocationsAsStrings(isManager ? FLYWAY_MANAGE_LOCALTION : FLYWAY_LOCALTION);
        configuration.setDataSource(dataSource);
        return configuration;
    }

    public static void executeFlyway(DataSource dataSource, boolean isManage) throws Exception {
        Configuration configuration = buildConfiguration(dataSource, isManage);
        Flyway flyway = new Flyway(configuration);
        flyway.migrate();
    }

    private static String getVersionTableName() {
        Environment environment = CommonBeanFactory.getBean(Environment.class);
        String appName = environment.getProperty("spring.application.name", String.class);
        return String.format(FLYWAY_TABLENAME_FORMAT, appName);
    }

}

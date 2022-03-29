package io.dataease.provider;

import io.dataease.plugins.common.constants.DatasourceTypes;
import io.dataease.provider.datasource.DatasourceProvider;
import io.dataease.provider.query.api.ApiProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


@Component
public class ProviderFactory implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    public static DatasourceProvider getProvider(String type) {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(type);
        switch (datasourceType) {
            case es:
                return context.getBean("es", DatasourceProvider.class);
            case api:
                return context.getBean("api", DatasourceProvider.class);
            default:
                return context.getBean("jdbc", DatasourceProvider.class);
        }
    }

    public static QueryProvider getQueryProvider(String type) {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(type);
        switch (datasourceType) {
            case mysql:
            case mariadb:
            case ds_doris:
            case TiDB:
            case StarRocks:
                return context.getBean("mysqlQuery", QueryProvider.class);
            case sqlServer:
                return context.getBean("sqlserverQuery", QueryProvider.class);
            case pg:
                return context.getBean("pgQuery", QueryProvider.class);
            case oracle:
                return context.getBean("oracleQuery", QueryProvider.class);
            case es:
                return context.getBean("esQuery", QueryProvider.class);
            case ck:
                return context.getBean("ckQuery", QueryProvider.class);
            case mongo:
                return context.getBean("mongoQuery", QueryProvider.class);
            case redshift:
                return context.getBean("redshiftQuery", QueryProvider.class);
            case hive:
                return context.getBean("hiveQuery", QueryProvider.class);
            case impala:
                return context.getBean("impalaQuery", QueryProvider.class);
            case db2:
                return context.getBean("db2Query", QueryProvider.class);
            case api:
                return context.getBean("apiQuery", ApiProvider.class);
            case engine_doris:
                return context.getBean("dorisEngineQuery", QueryProvider.class);
            case engine_mysql:
                return context.getBean("mysqlEngineQuery", QueryProvider.class);
            default:
                return context.getBean("mysqlQuery", QueryProvider.class);
        }
    }

    public static DDLProvider getDDLProvider(String type) {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(type);
        switch (datasourceType) {
            case engine_doris:
                return context.getBean("dorisEngineDDL", DDLProvider.class);
            case engine_mysql:
                return context.getBean("mysqlEngineDDL", DDLProvider.class);
            default:
                return context.getBean("dorisEngineDDL", DDLProvider.class);
        }
    }

}

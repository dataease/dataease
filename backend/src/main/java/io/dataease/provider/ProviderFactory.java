package io.dataease.provider;

import io.dataease.commons.constants.DatasourceTypes;
import io.dataease.provider.datasource.DatasourceProvider;
import io.dataease.provider.query.DDLProvider;
import io.dataease.provider.query.QueryProvider;
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
                return context.getBean("mysqlQuery", QueryProvider.class);
            case de_doris:
                return context.getBean("dorisQuery", QueryProvider.class);
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
            default:
                return context.getBean("mysqlQuery", QueryProvider.class);
        }
    }

    public static DDLProvider getDDLProvider(String type) {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(type);
        switch (datasourceType) {
            case de_doris:
                return context.getBean("dorisDDL", DDLProvider.class);
            default:
                return context.getBean("dorisDDL", DDLProvider.class);
        }
    }

}

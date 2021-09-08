package io.dataease.datasource.provider;

import io.dataease.datasource.constants.DatasourceTypes;
import io.dataease.provider.DDLProvider;
import io.dataease.provider.QueryProvider;
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
            case mysql:
                return context.getBean("jdbc", DatasourceProvider.class);
            case doris:
                return context.getBean("jdbc", DatasourceProvider.class);
            case sqlServer:
                return context.getBean("jdbc", DatasourceProvider.class);
            case pg:
                return context.getBean("jdbc", DatasourceProvider.class);
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
                return context.getBean("mysqlQuery", QueryProvider.class);
            case doris:
                return context.getBean("dorisQuery", QueryProvider.class);
            case sqlServer:
                return context.getBean("sqlserverQuery", QueryProvider.class);
            case pg:
                return context.getBean("pgQuery", QueryProvider.class);
            case oracle:
                return context.getBean("oracleQuery", QueryProvider.class);
            case es:
                return context.getBean("esQuery", QueryProvider.class);
            default:
                return context.getBean("mysqlQuery", QueryProvider.class);
        }
    }

    public static DDLProvider getDDLProvider(String type) {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(type);
        switch (datasourceType) {
            case mysql:
                return context.getBean("mysqlDDL", DDLProvider.class);
            case doris:
                return context.getBean("dorisDDL", DDLProvider.class);
            case oracle:
                return context.getBean("oracleDDL", DDLProvider.class);
            case sqlServer:
                return context.getBean("mysqlDDL", DDLProvider.class);
            default:
                return context.getBean("mysqlDDL", DDLProvider.class);
        }
    }

}

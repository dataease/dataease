package io.dataease.provider;

import com.google.gson.Gson;
import io.dataease.plugins.common.constants.DatasourceTypes;
import io.dataease.plugins.common.dto.datasource.DataSourceType;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.datasource.query.QueryProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import io.dataease.plugins.datasource.provider.Provider;

import java.util.Map;

@Configuration
public class ProviderFactory implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(final ApplicationContext ctx) {
        this.context =  ctx;
        for(final DatasourceTypes d: DatasourceTypes.values()) {
            final ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();
            if(d.isDatasource()){
                DataSourceType dataSourceType = new DataSourceType(d.getType(), d.getName(), false, d.getExtraParams(), d.getCalculationMode());
                beanFactory.registerSingleton(d.getType(), dataSourceType);
            }
        }
    }


    public static Provider getProvider(String type) {
        Map<String, DataSourceType> dataSourceTypeMap = SpringContextUtil.getApplicationContext().getBeansOfType((DataSourceType.class));
        if(dataSourceTypeMap.keySet().contains(type)){
            DatasourceTypes datasourceType = DatasourceTypes.valueOf(type);
            switch (datasourceType) {
                case es:
                    return context.getBean("esProviders", Provider.class);
                case api:
                    return context.getBean("apiProvider", Provider.class);
                default:
                    return context.getBean("jdbc", Provider.class);
            }
        }

        return SpringContextUtil.getApplicationContext().getBean(type + "DsProvider", Provider.class);

    }

    public static QueryProvider getQueryProvider(String type) {
        Map<String, DataSourceType> dataSourceTypeMap = SpringContextUtil.getApplicationContext().getBeansOfType((DataSourceType.class));
        if(dataSourceTypeMap.keySet().contains(type)){
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
                    return context.getBean("apiQuery", QueryProvider.class);
                case engine_doris:
                    return context.getBean("dorisEngineQuery", QueryProvider.class);
                case engine_mysql:
                    return context.getBean("mysqlEngineQuery", QueryProvider.class);
                default:
                    return context.getBean("mysqlQuery", QueryProvider.class);
            }
        }

        return SpringContextUtil.getApplicationContext().getBean(type + "QueryProvider", QueryProvider.class);
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

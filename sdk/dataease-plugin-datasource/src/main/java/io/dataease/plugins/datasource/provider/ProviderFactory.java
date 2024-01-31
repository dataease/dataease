package io.dataease.plugins.datasource.provider;

import io.dataease.plugins.common.constants.DatasourceTypes;
import io.dataease.plugins.common.dto.datasource.DataSourceType;
import io.dataease.plugins.common.util.SpringContextUtil;
import io.dataease.plugins.datasource.query.QueryProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

public class ProviderFactory implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(final ApplicationContext ctx) {
        this.context = ctx;
        for (final DatasourceTypes d : DatasourceTypes.values()) {
            final ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();
            if (d.isDatasource()) {
                DataSourceType dataSourceType = new DataSourceType(d.getType(), d.getName(), false, d.getExtraParams(), d.getCalculationMode(), d.isJdbc());
                if (dataSourceType.getType().equalsIgnoreCase("oracle")) {
                    dataSourceType.setCharset(d.getCharset());
                    dataSourceType.setTargetCharset(d.getTargetCharset());
                }
                dataSourceType.setKeywordSuffix(d.getKeywordSuffix());
                dataSourceType.setDatabaseClassification(d.getDatabaseClassification());
                dataSourceType.setKeywordPrefix(d.getKeywordPrefix());
                dataSourceType.setAliasSuffix(d.getAliasSuffix());
                dataSourceType.setAliasPrefix(d.getAliasPrefix());
                dataSourceType.setSurpportVersions(d.getSurpportVersions());
                beanFactory.registerSingleton(d.getType(), dataSourceType);
            }
        }
    }


    public static Provider getProvider(String type) {
        if (type.equalsIgnoreCase(DatasourceTypes.engine_doris.toString()) || type.equalsIgnoreCase(DatasourceTypes.engine_mysql.toString())) {
            return context.getBean("jdbc", Provider.class);
        }

        Map<String, DataSourceType> dataSourceTypeMap = SpringContextUtil.getApplicationContext().getBeansOfType((DataSourceType.class));
        if (dataSourceTypeMap.keySet().contains(type)) {
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
        switch (type) {
            case "mysql":
            case "mariadb":
                return context.getBean("mysqlQueryProvider", QueryProvider.class);
            case "ds_doris":
            case "TiDB":
            case "StarRocks":
                return context.getBean("dorisQueryProvider", QueryProvider.class);
            default:
                return SpringContextUtil.getApplicationContext().getBean(type + "QueryProvider", QueryProvider.class);
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

package io.dataease.plugins.datasource.iotdb.service;

import io.dataease.plugins.common.constants.DatabaseClassification;
import io.dataease.plugins.common.constants.DatasourceCalculationMode;
import io.dataease.plugins.common.dto.StaticResource;
import io.dataease.plugins.common.dto.datasource.DataSourceType;
import io.dataease.plugins.datasource.service.DatasourceService;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class IotdbService extends DatasourceService {


    /**
     * 添加数据源类型
     */
    @Override
    public List<String> components() {
        List<String> result = new ArrayList<>();
        result.add("iotdb");
        return result;
    }

    /**
     * 读取静态资源
     */
    @Override
    protected InputStream readContent(String s) {
        return this.getClass().getClassLoader().getResourceAsStream("static/" + s);
    }

    /**
     * 映射 Logo 资源
     */
    @Override
    public List<StaticResource> staticResources() {
        List<StaticResource> results = new ArrayList<>();
        StaticResource staticResource = new StaticResource();
        staticResource.setName("iotdb");
        staticResource.setSuffix("png");
        results.add(staticResource);
        results.add(pluginSvg());
        return results;
    }

    /**
     * 用户填写的数据源信息
     */
    @Override
    public DataSourceType getDataSourceType() {
        DataSourceType dataSourceType = new DataSourceType("iotdb", "Apache IoTDB", true, "",
                DatasourceCalculationMode.DIRECT, true);
        dataSourceType.setKeywordPrefix("\"");
        dataSourceType.setKeywordSuffix("\"");
        dataSourceType.setAliasPrefix("\"");
        dataSourceType.setAliasSuffix("\"");
        dataSourceType.setDatabaseClassification(DatabaseClassification.OLTP);
        dataSourceType.setSurpportVersions("12");
        return dataSourceType;
    }

    private StaticResource pluginSvg() {
        StaticResource staticResource = new StaticResource();
        staticResource.setName("iotdb-backend");
        staticResource.setSuffix("png");
        return staticResource;
    }
}

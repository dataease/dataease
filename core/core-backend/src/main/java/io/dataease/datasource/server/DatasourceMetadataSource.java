package io.dataease.datasource.server;

import io.dataease.constant.AuthEnum;
import io.dataease.constant.SQLConstants;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.datasource.provider.PhysicalTableCatalog;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.factory.ProviderFactory;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.utils.BeanUtils;
import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.manage.DataSourceManage;
import io.dataease.datasource.manage.DatasourceMetadataAccess;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.i18n.Translator;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DatasourceMetadataSource {
    private final DatasourceServer datasource;
    private final DataSourceManage dataSources;

    DatasourceMetadataSource(DatasourceServer datasource, DataSourceManage dataSources) {
        this.datasource = datasource;
        this.dataSources = dataSources;
    }

    public boolean visible(Long datasourceId) {
        var request = new BusiNodeRequest();
        request.setBusiFlag("datasource");
        request.setWeight(AuthEnum.READ.getWeight());
        return contains(datasource.tree(request), datasourceId);
    }

    private static boolean contains(List<BusiNodeVO> nodes, Long datasourceId) {
        return nodes != null && nodes.stream().anyMatch(node ->
                (!node.isOrgRoot() && Boolean.TRUE.equals(node.getLeaf()) && Objects.equals(node.getId(), datasourceId))
                        || contains(node.getChildren(), datasourceId));
    }

    public PhysicalTableCatalog openCatalog(long id) {
        var source = datasource(id);
        if (!DatasourceConfiguration.usesDeEngine(source.getType()) && ProviderFactory.getProvider(source.getType()) instanceof CalciteProvider provider) {
            var schema = new DatasourceSchemaDTO();
            BeanUtils.copyBean(schema, source);
            schema.setSchemaAlias(String.format(SQLConstants.SCHEMA, id));
            boolean oracle = "oracle".equalsIgnoreCase(source.getType());
            return provider.openCatalog(schema, (name, label) -> !oracle
                    || !(DatasourceServer.isOracleRecycleBinName(name) || DatasourceServer.isOracleRecycleBinName(label)));
        }
        return new PhysicalTableCatalog(datasource.listPhysicalTables(id, source), name -> fields(source, name));
    }

    public List<TableField> fields(CoreDatasource source, String tableName) {
        return datasource.getFieldsForListedTable(source, tableName);
    }

    public Map<String, DatasetTableDTO> tables(Long datasourceId, CoreDatasource source) {
        return datasource.listPhysicalTables(datasourceId, source).stream().collect(Collectors.toMap(
                DatasetTableDTO::getTableName, Function.identity(), (first, duplicate) -> first));
    }

    public static DatasetTableDTO listedTable(Map<String, DatasetTableDTO> tables, String tableName) {
        var table = tables.get(tableName);
        if (table == null) {
            DEException.throwException(Translator.get("i18n_datasource_metadata_table_unavailable", tableName));
        }
        return table;
    }

    public CoreDatasource datasource(Long datasourceId) {
        var source = dataSources.getCoreDatasource(datasourceId);
        DatasourceMetadataAccess.requirePhysicalDatasource(source);
        return source;
    }

    static void requireText(String value, int length) {
        if (value == null || value.isBlank()) {
            DEException.throwException(Translator.get("i18n_datasource_metadata_required"));
        }
        limit(value, length);
    }

    static void limit(String value, int length) {
        if (value != null && value.length() > length) {
            DEException.throwException(Translator.get("i18n_datasource_metadata_length", length));
        }
    }
}

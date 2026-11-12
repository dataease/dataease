package io.dataease.datasource.server;

import io.dataease.api.ds.DatasourceSemanticApi;
import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.manage.DatasourceSemanticManage;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.i18n.Translator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static io.dataease.datasource.server.DatasourceMetadataSource.*;

@RestController
@RequestMapping("/datasource/metadata")
public class DatasourceSemanticServer implements DatasourceSemanticApi {
    private final DatasourceSemanticManage metadata;
    private final DatasourceMetadataSource physical;

    public DatasourceSemanticServer(DatasourceSemanticManage metadata, DatasourceMetadataSource physical) {
        this.metadata = metadata;
        this.physical = physical;
    }

    @Override
    public Semantics list(Long datasourceId) {
        return metadata.list(datasourceId);
    }

    @Override
    public void saveTable(Long datasourceId, TableSemantic table) {
        requireText(table.tableName(), 512);
        limit(table.description(), 4000);
        limit(table.grain(), 512);
        metadata.saveTable(datasourceId, table);
    }

    @Override
    public void addTables(Long datasourceId, List<String> tableNames) {
        if (tableNames.isEmpty()) {
            DEException.throwException(Translator.get("i18n_datasource_metadata_select_tables"));
        }
        var source = physical.datasource(datasourceId);
        var available = physical.tables(datasourceId, source);
        var selected = new ArrayList<DatasetTableDTO>();
        for (String name : new LinkedHashSet<>(tableNames)) {
            requireText(name, 512);
            selected.add(listedTable(available, name));
        }
        var snapshots = selected.stream().map(table -> snapshot(source, table)).toList();
        metadata.addTables(datasourceId, snapshots);
    }

    @Override
    public void removeTable(Long datasourceId, TableReference table) {
        requireText(table.tableName(), 512);
        metadata.removeTable(datasourceId, table.tableName());
    }

    @Override
    public boolean refreshTable(Long datasourceId, TableReference table) {
        requireText(table.tableName(), 512);
        var source = physical.datasource(datasourceId);
        var physicalTable = physical.tables(datasourceId, source).get(table.tableName());
        metadata.refreshTable(datasourceId, table.tableName(), physicalTable == null ? null : snapshot(source, physicalTable));
        return physicalTable != null;
    }

    private DatasourceSemanticManage.TableSnapshot snapshot(CoreDatasource source, DatasetTableDTO table) {
        var fields = physical.fields(source, table.getTableName());
        for (TableField field : fields) {
            requireText(field.getOriginName(), 512);
            limit(field.getName(), 4000);
            limit(field.getFieldType(), 512);
            limit(field.getType(), 512);
        }
        limit(table.getName(), 4000);
        return new DatasourceSemanticManage.TableSnapshot(table.getTableName(), table.getName(), fields);
    }

    @Override
    public void saveField(Long datasourceId, FieldSemantic field) {
        requireText(field.tableName(), 512);
        requireText(field.fieldName(), 512);
        requireText(field.businessName(), 255);
        limit(field.description(), 4000);
        limit(field.aliases(), 1000);
        metadata.saveField(datasourceId, field);
    }
}

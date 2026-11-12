package io.dataease.datasource.server;

import io.dataease.api.ds.DatasourceRelationApi;
import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.manage.DatasourceRelationManage;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.i18n.Translator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.dataease.datasource.server.DatasourceMetadataSource.*;

@RestController
@RequestMapping("/datasource/metadata")
public class DatasourceRelationServer implements DatasourceRelationApi {
    private final DatasourceRelationManage metadata;
    private final DatasourceMetadataSource physical;

    public DatasourceRelationServer(DatasourceRelationManage metadata, DatasourceMetadataSource physical) {
        this.metadata = metadata;
        this.physical = physical;
    }

    @Override
    public List<Relation> list(Long datasourceId) {
        return metadata.list(datasourceId);
    }

    @Override
    public void saveRelation(Long datasourceId, Relation relation) {
        requireText(relation.name(), 255);
        requireText(relation.leftTable(), 512);
        requireText(relation.rightTable(), 512);
        limit(relation.description(), 4000);
        if (relation.cardinality() == null || relation.joinType() == null || relation.conditions() == null
                || relation.conditions().isEmpty() || relation.conditions().size() > 100) {
            DEException.throwException(Translator.get("i18n_datasource_metadata_relation_required"));
        }
        var source = physical.datasource(datasourceId);
        var tables = physical.tables(datasourceId, source);
        var leftTable = listedTable(tables, relation.leftTable());
        var rightTable = listedTable(tables, relation.rightTable());
        var left = fields(source, leftTable);
        var right = relation.leftTable().equals(relation.rightTable()) ? left : fields(source, rightTable);
        for (Condition condition : relation.conditions()) {
            if (condition == null || !left.contains(condition.leftField()) || !right.contains(condition.rightField())) {
                DEException.throwException(Translator.get("i18n_datasource_metadata_invalid_condition"));
            }
        }
        metadata.saveRelation(datasourceId, relation);
    }

    @Override
    public void removeRelation(Long datasourceId, Long id) {
        metadata.removeRelation(datasourceId, id);
    }

    private Set<String> fields(CoreDatasource source, DatasetTableDTO table) {
        return physical.fields(source, table.getTableName()).stream()
                .map(TableField::getOriginName).collect(Collectors.toSet());
    }
}

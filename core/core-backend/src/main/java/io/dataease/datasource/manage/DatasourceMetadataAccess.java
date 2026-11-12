package io.dataease.datasource.manage;

import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceRepository;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceFieldSemanticRepository;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceRelationRepository;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceTableSemanticRepository;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
public class DatasourceMetadataAccess {
    private final CoreDatasourceRepository repository;
    private final CoreDatasourceRelationRepository relations;
    private final CoreDatasourceFieldSemanticRepository fields;
    private final CoreDatasourceTableSemanticRepository tables;

    public DatasourceMetadataAccess(CoreDatasourceRepository repository, CoreDatasourceRelationRepository relations,
                                    CoreDatasourceFieldSemanticRepository fields, CoreDatasourceTableSemanticRepository tables) {
        this.repository = repository;
        this.relations = relations;
        this.fields = fields;
        this.tables = tables;
    }

    @Transactional
    public void deleteDatasource(Long datasourceId) {
        repository.findByIdForUpdate(datasourceId).ifPresent(datasource -> {
            relations.deleteAll(relations.findWithConditions(datasourceId));
            fields.deleteAll(fields.findByTableSemantic_Datasource_Id(datasourceId));
            tables.deleteAll(tables.findByDatasource_IdOrderById(datasourceId));
            repository.delete(datasource);
        });
    }

    void require(Long datasourceId) {
        requirePhysicalDatasource(repository.findById(datasourceId).orElse(null));
    }

    public static void requirePhysicalDatasource(CoreDatasource datasource) {
        if (datasource == null || datasource.getType() == null) {
            DEException.throwException(Translator.get("i18n_datasource_metadata_invalid_source"));
        }
        String type = datasource.getType().toLowerCase(Locale.ROOT);
        if ("folder".equals(type) || type.startsWith("excel") || type.startsWith("api")) {
            DEException.throwException(Translator.get("i18n_datasource_metadata_invalid_source"));
        }
    }

    CoreDatasource lock(Long datasourceId) {
        var datasource = repository.findByIdForUpdate(datasourceId).orElse(null);
        requirePhysicalDatasource(datasource);
        return datasource;
    }
}

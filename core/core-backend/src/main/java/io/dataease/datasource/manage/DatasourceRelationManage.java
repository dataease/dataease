package io.dataease.datasource.manage;

import io.dataease.api.ds.DatasourceRelationApi.*;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceRelation;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceRelationCondition;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceRelationRepository;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.utils.IDUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class DatasourceRelationManage {
    private final DatasourceMetadataAccess access;
    private final CoreDatasourceRelationRepository repository;

    public DatasourceRelationManage(DatasourceMetadataAccess access, CoreDatasourceRelationRepository repository) {
        this.access = access;
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Relation> list(Long datasourceId) {
        access.require(datasourceId);
        return repository.findWithConditions(datasourceId).stream()
                .map(this::relationView).toList();
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    public long countByTable(Long datasourceId, String tableName) {
        return repository.countByReferencedTable(datasourceId, tableName);
    }

    @Transactional
    public void saveRelation(Long datasourceId, Relation relation) {
        saveRelation(datasourceId, relation, true);
    }

    @Transactional
    public int addSuggestions(Long datasourceId, List<Relation> suggestions) {
        access.lock(datasourceId);
        var keys = new java.util.HashSet<RelationKey>();
        repository.findWithConditions(datasourceId).stream().map(this::relationView).forEach(row -> keys.add(key(row)));
        int added = 0;
        for (var suggestion : suggestions) {
            if (keys.add(key(suggestion))) {
                saveRelation(datasourceId, suggestion, false);
                added++;
            }
        }
        return added;
    }

    private record RelationKey(String left, String right, Set<Condition> conditions) {}

    private static RelationKey key(Relation relation) {
        if (relation.leftTable().compareTo(relation.rightTable()) <= 0) {
            return new RelationKey(relation.leftTable(), relation.rightTable(), Set.copyOf(relation.conditions()));
        }
        return new RelationKey(relation.rightTable(), relation.leftTable(), relation.conditions().stream()
                .map(c -> new Condition(c.rightField(), c.leftField())).collect(java.util.stream.Collectors.toSet()));
    }

    private void saveRelation(Long datasourceId, Relation relation, boolean confirmed) {
        var datasource = access.lock(datasourceId);
        boolean create = relation.id() == null;
        var row = create ? new CoreDatasourceRelation() : relation(datasourceId, relation.id());
        if (create) {
            row.setId(IDUtils.snowID());
            row.setCreateBy(Objects.toString(V3UserUtil.getUid(), null));
            row.setCreateTime(System.currentTimeMillis());
            row.setDatasource(datasource);
        }
        row.setName(relation.name().trim());
        row.setLeftTable(relation.leftTable());
        row.setRightTable(relation.rightTable());
        row.setCardinality(relation.cardinality());
        row.setJoinType(relation.joinType());
        row.setDescription(relation.description());
        row.setConfirmed(confirmed);
        row.setValid(true);
        row.setLeftTableMissing(false);
        row.setRightTableMissing(false);
        var conditions = row.getConditions();
        for (int i = 0; i < relation.conditions().size(); i++) {
            if (i == conditions.size()) {
                var condition = new CoreDatasourceRelationCondition();
                condition.setId(IDUtils.snowID());
                condition.setCreateBy(Objects.toString(V3UserUtil.getUid(), null));
                condition.setCreateTime(System.currentTimeMillis());
                condition.setRelation(row);
                condition.setPosition(i);
                conditions.add(condition);
            }
            var input = relation.conditions().get(i);
            conditions.get(i).setLeftField(input.leftField());
            conditions.get(i).setRightField(input.rightField());
            conditions.get(i).setLeftFieldMissing(false);
            conditions.get(i).setRightFieldMissing(false);
        }
        conditions.subList(relation.conditions().size(), conditions.size()).clear();
        if (create) {
            repository.save(row);
        }
    }

    @Transactional
    public void setConfirmed(Long datasourceId, Long id, boolean confirmed) {
        access.lock(datasourceId);
        var row = relation(datasourceId, id);
        if (confirmed && !row.isValid()) {
            DEException.throwException(Translator.get("i18n_datasource_metadata_invalid_relation"));
        }
        row.setConfirmed(confirmed);
    }

    @Transactional
    public void removeRelation(Long datasourceId, Long id) {
        access.lock(datasourceId);
        repository.delete(relation(datasourceId, id));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void updateRelations(Long datasourceId, String tableName, Set<String> fields) {
        var relations = repository.findByReferencedTable(datasourceId, tableName);
        for (var relation : relations) {
            if (tableName.equals(relation.getLeftTable())) {
                relation.setLeftTableMissing(fields == null);
                relation.getConditions().forEach(c -> c.setLeftFieldMissing(fields != null && !fields.contains(c.getLeftField())));
            }
            if (tableName.equals(relation.getRightTable())) {
                relation.setRightTableMissing(fields == null);
                relation.getConditions().forEach(c -> c.setRightFieldMissing(fields != null && !fields.contains(c.getRightField())));
            }
            relation.setValid(!relation.isLeftTableMissing() && !relation.isRightTableMissing()
                    && relation.getConditions().stream().noneMatch(c -> c.isLeftFieldMissing() || c.isRightFieldMissing()));
        }
    }

    private Relation relationView(CoreDatasourceRelation row) {
        var missing = new LinkedHashSet<MissingReference>();
        if (row.isLeftTableMissing()) {
            missing.add(new MissingReference(row.getLeftTable(), null));
        }
        if (row.isRightTableMissing()) {
            missing.add(new MissingReference(row.getRightTable(), null));
        }
        for (var condition : row.getConditions()) {
            if (condition.isLeftFieldMissing()) {
                missing.add(new MissingReference(row.getLeftTable(), condition.getLeftField()));
            }
            if (condition.isRightFieldMissing()) {
                missing.add(new MissingReference(row.getRightTable(), condition.getRightField()));
            }
        }
        return new Relation(row.getId(), row.getName(), row.getLeftTable(), row.getRightTable(),
                row.getConditions().stream().map(c -> new Condition(c.getLeftField(), c.getRightField())).toList(),
                row.getCardinality(), row.getJoinType(), row.getDescription(), row.isConfirmed(),
                row.isValid(), List.copyOf(missing));
    }

    private CoreDatasourceRelation relation(Long datasourceId, Long id) {
        return repository.findByIdAndDatasource_Id(id, datasourceId)
                .orElseThrow(() -> DEException.getException(Translator.get("i18n_datasource_metadata_invalid_relation")));
    }
}

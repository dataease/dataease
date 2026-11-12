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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
    public Map<String, Long> countByTable(Long datasourceId) {
        var pairs = repository.findTablePairs(datasourceId);
        Map<String, Long> counts = new HashMap<>();
        for (var pair : pairs) {
            String left = (String) pair[0];
            String right = (String) pair[1];
            counts.merge(left, 1L, Long::sum);
            if (!left.equals(right)) {
                counts.merge(right, 1L, Long::sum);
            }
        }
        return counts;
    }

    @Transactional
    public void saveRelation(Long datasourceId, Relation relation) {
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
        row.setConfirmed(true);
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

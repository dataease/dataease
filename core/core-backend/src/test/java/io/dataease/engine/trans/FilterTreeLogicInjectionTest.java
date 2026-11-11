package io.dataease.engine.trans;

import io.dataease.api.permissions.dataset.dto.DataSetRowPermissionsTreeDTO;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.model.SQLObj;
import io.dataease.extensions.view.dto.DatasetRowPermissionsTreeItem;
import io.dataease.extensions.view.dto.DatasetRowPermissionsTreeObj;
import io.dataease.extensions.view.filter.FilterTreeItem;
import io.dataease.extensions.view.filter.FilterTreeObj;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * CNVD-2026-44470 回归：customFilter.logic / 行权限树 logic 不得直接拼入 SQL。
 */
public class FilterTreeLogicInjectionTest {

    private DatasetTableFieldDTO field() {
        DatasetTableFieldDTO field = new DatasetTableFieldDTO();
        field.setId(2L);
        field.setOriginName("name");
        field.setName("name");
        field.setDataeaseName("name");
        field.setType("TEXT");
        field.setDeType(0);
        field.setDeExtractType(0);
        field.setExtField(0);
        return field;
    }

    private SQLMeta sqlMeta() {
        SQLMeta meta = new SQLMeta();
        meta.setTable(SQLObj.builder().tableAlias("t").tableName("t_sqli").build());
        return meta;
    }

    private FilterTreeObj customFilter(String logic) {
        FilterTreeObj tree = new FilterTreeObj();
        tree.setLogic(logic);
        FilterTreeItem item = new FilterTreeItem();
        item.setType("item");
        item.setFieldId(2L);
        item.setField(field());
        item.setFilterType("logic");
        item.setTerm("eq");
        item.setValue("zz");
        tree.setItems(List.of(item, copy(item)));
        return tree;
    }

    private DataSetRowPermissionsTreeDTO rowPermission(String logic) {
        DatasetRowPermissionsTreeObj tree = new DatasetRowPermissionsTreeObj();
        tree.setLogic(logic);
        DatasetRowPermissionsTreeItem item = new DatasetRowPermissionsTreeItem();
        item.setType("item");
        item.setFieldId(2L);
        item.setField(field());
        item.setFilterType("logic");
        item.setTerm("eq");
        item.setValue("zz");
        tree.setItems(List.of(item, copy(item)));
        DataSetRowPermissionsTreeDTO dto = new DataSetRowPermissionsTreeDTO();
        dto.setTree(tree);
        return dto;
    }

    private FilterTreeItem copy(FilterTreeItem item) {
        FilterTreeItem copy = new FilterTreeItem();
        copy.setType(item.getType());
        copy.setFieldId(item.getFieldId());
        copy.setField(item.getField());
        copy.setFilterType(item.getFilterType());
        copy.setTerm(item.getTerm());
        copy.setValue(item.getValue());
        return copy;
    }

    private DatasetRowPermissionsTreeItem copy(DatasetRowPermissionsTreeItem item) {
        DatasetRowPermissionsTreeItem copy = new DatasetRowPermissionsTreeItem();
        copy.setType(item.getType());
        copy.setFieldId(item.getFieldId());
        copy.setField(item.getField());
        copy.setFilterType(item.getFilterType());
        copy.setTerm(item.getTerm());
        copy.setValue(item.getValue());
        return copy;
    }

    @Test
    public void customFilterRejectsInjectedLogic() {
        SQLMeta meta = sqlMeta();
        assertThrows(DEException.class, () -> CustomWhere2Str.customWhere2sqlObj(meta,
                customFilter("or 1=1 or"), List.of(field()), false, new HashMap<>(), null, null, null));
    }

    @Test
    public void customFilterAllowsAndOrLogic() {
        SQLMeta meta = sqlMeta();
        CustomWhere2Str.customWhere2sqlObj(meta, customFilter("or"), List.of(field()), false,
                new HashMap<>(), null, null, null);
        String sql = meta.getCustomWheres();
        assertNotNull(sql);
        assertTrue(sql, sql.contains(" OR "));
        assertFalse(sql, sql.contains("1=1"));

        meta = sqlMeta();
        CustomWhere2Str.customWhere2sqlObj(meta, customFilter("and"), List.of(field()), false,
                new HashMap<>(), null, null, null);
        sql = meta.getCustomWheres();
        assertNotNull(sql);
        assertTrue(sql, sql.contains(" AND "));
    }

    @Test
    public void rowPermissionRejectsInjectedLogic() {
        SQLMeta meta = sqlMeta();
        assertThrows(DEException.class, () -> WhereTree2Str.transFilterTrees(meta,
                List.of(rowPermission("or 1=1 or")), List.of(field()), false, new HashMap<>(),
                null, null, null));
    }

    @Test
    public void rowPermissionAllowsAndOrLogic() {
        SQLMeta meta = sqlMeta();
        WhereTree2Str.transFilterTrees(meta, List.of(rowPermission("or")), List.of(field()), false,
                new HashMap<>(), null, null, null);
        String sql = meta.getWhereTrees();
        assertNotNull(sql);
        assertTrue(sql, sql.contains(" OR "));
        assertFalse(sql, sql.contains("1=1"));

        meta = sqlMeta();
        WhereTree2Str.transFilterTrees(meta, List.of(rowPermission("and")), List.of(field()), false,
                new HashMap<>(), null, null, null);
        sql = meta.getWhereTrees();
        assertNotNull(sql);
        assertTrue(sql, sql.contains(" AND "));
    }
}

package io.dataease.datasource.provider;

import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.TableField;

import java.util.*;
import java.util.function.Function;

/** Names and labels from one datasource enumeration, paired with its exact-name field reader. */
public final class PhysicalTableCatalog {
    private final Map<String, String> labels;
    private final Function<String, List<TableField>> reader;

    public PhysicalTableCatalog(List<DatasetTableDTO> tables, Function<String, List<TableField>> reader) {
        var names = new LinkedHashMap<String, String>();
        for (var table : tables) {
            if (table.getTableName() != null) names.putIfAbsent(table.getTableName(), Objects.toString(table.getName(), ""));
        }
        labels = Collections.unmodifiableMap(names);
        this.reader = reader;
    }

    public Map<String, String> labels() { return labels; }

    public List<TableField> fields(String name) {
        if (!labels.containsKey(name)) throw new IllegalArgumentException("Table is outside this datasource catalog");
        return reader.apply(name);
    }
}

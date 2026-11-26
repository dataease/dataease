package io.dataease.datasource.provider;

import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.TableField;

import java.util.*;
import java.util.function.Function;
import java.util.function.BiFunction;

/** Names and labels from one datasource enumeration, paired with its exact-name field reader. */
public final class PhysicalTableCatalog {
    private final Map<String, String> labels;
    private final Function<String, List<TableField>> reader;

    private final BiFunction<String, List<String>, List<TableField>> namedReader;

    public PhysicalTableCatalog(List<DatasetTableDTO> tables, Function<String, List<TableField>> reader) {
        this(tables, reader, null);
    }

    public PhysicalTableCatalog(List<DatasetTableDTO> tables, Function<String, List<TableField>> reader,
                                BiFunction<String, List<String>, List<TableField>> namedReader) {
        var names = new LinkedHashMap<String, String>();
        for (var table : tables) {
            if (table.getTableName() != null) names.putIfAbsent(table.getTableName(), Objects.toString(table.getName(), ""));
        }
        labels = Collections.unmodifiableMap(names);
        this.reader = reader;
        this.namedReader = namedReader;
    }

    public Map<String, String> labels() { return labels; }

    public List<TableField> fields(String name) {
        if (!labels.containsKey(name)) throw new IllegalArgumentException("Table is outside this datasource catalog");
        return reader.apply(name);
    }
    public List<TableField> fields(String table, List<String> names) {
        if (!labels.containsKey(table)) throw new IllegalArgumentException("Table is outside this datasource catalog");
        // File-backed and extension providers may expose only a table-granular metadata reader.
        var rows = namedReader == null ? reader.apply(table) : namedReader.apply(table, names);
        return rows.stream().filter(row -> names.contains(Objects.toString(row.getOriginName(), row.getName()))).toList();
    }
}

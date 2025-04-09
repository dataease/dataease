package io.dataease.extensions.view.dto;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Data
public class TableHeader {
    private HeaderGroupConfig headerGroupConfig;
    private boolean headerGroup;


    @Data
    static
    public class HeaderGroupConfig {
        private List<MetaInfo> meta = new ArrayList<>();
        private List<ColumnInfo> columns = new ArrayList<>();
    }

    @Data
    static
    public class ColumnInfo {
        @Getter
        private String key;
        private List<ColumnInfo> children = new ArrayList<>();
        private Integer width;
    }

    @Getter
    @Data
    static
    public class MetaInfo {
        private String field;
        private String name;

    }
}

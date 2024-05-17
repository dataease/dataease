package io.dataease.plugins.common.dto.datafill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExtIndexField implements Serializable {
    private static final long serialVersionUID = -3169849285437114316L;

    private String name;

    private List<ColumnSetting> columns;

    private boolean removed;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ColumnSetting {
        private String column;
        private String order;
    }
}

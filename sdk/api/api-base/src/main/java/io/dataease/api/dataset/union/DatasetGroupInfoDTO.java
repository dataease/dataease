package io.dataease.api.dataset.union;

import io.dataease.api.chart.dto.DeSortField;
import io.dataease.api.dataset.dto.DatasetNodeDTO;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author Junjun
 */
@Data
public class DatasetGroupInfoDTO extends DatasetNodeDTO {
    private List<UnionDTO> union;// 关联数据集

    private List<DeSortField> sortFields;// 自定义排序（如仪表板查询组件）

    private Map<String, List> data;

    private List<DatasetTableFieldDTO> allFields;

    private String sql;

    private Long total;

    private String creator;

    private String updater;
}

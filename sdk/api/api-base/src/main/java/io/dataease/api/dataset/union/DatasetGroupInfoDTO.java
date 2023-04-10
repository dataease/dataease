package io.dataease.api.dataset.union;

import io.dataease.api.chart.dto.DeSortField;
import io.dataease.api.dataset.dto.DatasetNodeDTO;
import lombok.Data;

import java.util.List;

/**
 * @Author Junjun
 */
@Data
public class DatasetGroupInfoDTO extends DatasetNodeDTO {
    private List<UnionDTO> union;// 关联数据集

    private List<DeSortField> sortFields;// 自定义排序（如仪表板过滤组件）
}

package io.dataease.dto.dataset;

import io.dataease.base.domain.DatasetTableUnion;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author gin
 * @Date 2021/5/6 6:08 下午
 */
@Getter
@Setter
public class DataSetTableUnionDTO extends DatasetTableUnion {
    private String sourceTableName;
    private String sourceTableFieldName;
    private String targetTableName;
    private String targetTableFieldName;
}

package io.dataease.controller.request.dataset;

import io.dataease.base.domain.DatasetTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/2/23 3:06 下午
 */
@Setter
@Getter
public class DataSetTableRequest extends DatasetTable {
    private String sort;
    private List<String> tableNames;
    private String row = "1000";
}

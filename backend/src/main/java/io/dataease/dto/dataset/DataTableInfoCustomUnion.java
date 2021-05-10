package io.dataease.dto.dataset;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/5/9 10:08 上午
 */
@Getter
@Setter
public class DataTableInfoCustomUnion {
    private String tableId;
    private List<String> checkedFields;
}

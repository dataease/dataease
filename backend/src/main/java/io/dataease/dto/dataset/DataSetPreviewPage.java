package io.dataease.dto.dataset;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author gin
 * @Date 2021/4/28 11:13 上午
 */
@Getter
@Setter
public class DataSetPreviewPage {
    private Integer total = 0;
    private Integer show = 0;
    private Integer page = 1;
    private Integer pageSize = 100;
}

package io.dataease.api.dataset.dto;

import io.dataease.extensions.view.dto.ChartExtFilterDTO;
import lombok.Data;

import java.util.List;

/**
 * @Author Junjun
 */
@Data
public class EnumValueRequest {
    private Long queryId;// 查询字段
    private Long displayId;// 显示字段
    private Long sortId;// 排序字段
    private String sort;// 排序 asc||desc
    private String searchText;// 搜索内容，以 field like '%text%' 拼接到SQL
    private List<ChartExtFilterDTO> filter;// 级联查询条件，多个条件之间用and拼接到SQL
    private Integer resultMode = 0;// 请求条数，0-默认，1-全部
}

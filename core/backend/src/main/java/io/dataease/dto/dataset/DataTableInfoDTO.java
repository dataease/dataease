package io.dataease.dto.dataset;

import io.dataease.dto.dataset.union.UnionDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/2/23 8:47 下午
 */
@Setter
@Getter
public class DataTableInfoDTO {
    private String table;
    private String sql;
    private boolean isBase64Encryption = false;
    private List<ExcelSheetData> excelSheetDataList;
    private String data;// file path
    private List<DataTableInfoCustomUnion> list;// 自定义数据集
    private List<UnionDTO> union;// 关联数据集
}

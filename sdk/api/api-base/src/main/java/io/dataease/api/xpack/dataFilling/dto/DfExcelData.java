package io.dataease.api.xpack.dataFilling.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class DfExcelData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1162581256875641808L;

    private List<ExtTableField> formFields;

    private List<RowDataDatum> dataList;

    private String id;
    private String excelName;
    private String path;
    private String suffix;

}

package io.dataease.excel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExcelHeaderComment {

    private int columnIndex;

    private String comment;
}

package io.dataease.xpack.permissions.user.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelCheckResult<T> {

    private List<T> successDtos;

    private List<ExcelImportErrDto> errDtos;
}

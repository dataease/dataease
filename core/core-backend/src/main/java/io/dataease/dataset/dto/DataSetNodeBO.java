package io.dataease.dataset.dto;

import io.dataease.model.TreeBaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataSetNodeBO implements TreeBaseModel {

    @Serial
    private static final long serialVersionUID = 728340676442387790L;

    private Long id;
    private String name;
    private Boolean leaf;
    private Integer weight = 3;
    private Long pid;
    private Integer extraFlag;
}

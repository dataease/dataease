package io.dataease.dataset.dao.ext.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataSetNodePO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4457506330575500164L;

    private Long id;
    private String name;
    private String nodeType;
    private Long pid;
}

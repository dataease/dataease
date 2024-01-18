package io.dataease.rmonitor.mapper.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class DatasetFreeResource extends BaseFreeResource implements Serializable {

    private String nodeType;
}

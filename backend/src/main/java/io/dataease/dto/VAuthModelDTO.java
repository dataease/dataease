package io.dataease.dto;

import io.dataease.base.domain.VAuthModel;
import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021-05-11
 * Description:
 */
@Data
public class VAuthModelDTO extends VAuthModel {

    private List<VAuthModelDTO> children;

    private Boolean leaf;

    private Integer childrenCount;

    private Boolean hasChildren;
}

package io.dataease.dto.panel;

import io.dataease.base.domain.PanelGroup;
import io.dataease.dto.dataset.DataSetGroupDTO;
import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
@Data
public class PanelGroupDTO extends PanelGroup {

    private String label;

    private List<PanelGroupDTO> children;
}

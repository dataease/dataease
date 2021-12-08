package io.dataease.dto.authModel;

import io.dataease.base.domain.VAuthModelWithBLOBs;
import io.dataease.commons.model.ITreeBase;
import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021/11/5
 * Description:
 */
@Data
public class VAuthModelDTO extends VAuthModelWithBLOBs  implements ITreeBase<VAuthModelDTO> {

    private String privileges;

    private List<VAuthModelDTO> children;
    private long allLeafs = 0l;
}

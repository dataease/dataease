package io.dataease.dto.authModel;

import io.dataease.plugins.common.base.domain.VAuthModelWithBLOBs;
import io.dataease.plugins.common.model.ITreeBase;
import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021/11/5
 * Description:
 */
@Data
public class VAuthModelDTO extends VAuthModelWithBLOBs implements ITreeBase<VAuthModelDTO> {

    private String privileges;

    private List<VAuthModelDTO> children;

    private long allLeafs = 0l;

    private String innerId;

    private Boolean isPlugin = false;

    public String toString(){
        return this.getName();
    }

}

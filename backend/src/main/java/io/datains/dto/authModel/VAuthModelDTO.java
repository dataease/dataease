package io.datains.dto.authModel;

import io.datains.base.domain.VAuthModelWithBLOBs;
import io.datains.commons.model.ITreeBase;
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

    public String toString(){
        return this.getName();
    }

}

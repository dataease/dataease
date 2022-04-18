package io.datains.dto;

import io.datains.base.domain.SysAuth;
import io.datains.base.domain.SysAuthDetail;
import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021-05-12
 * Description:
 */
@Data
public class SysAuthDTO extends SysAuth {

     private List<SysAuthDetail> sysAuthDetails;
}

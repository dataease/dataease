package io.dataease.dto;

import io.dataease.plugins.common.base.domain.SysAuth;
import io.dataease.plugins.common.base.domain.SysAuthDetail;
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

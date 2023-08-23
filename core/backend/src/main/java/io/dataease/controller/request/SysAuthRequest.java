package io.dataease.controller.request;

import io.dataease.plugins.common.base.domain.SysAuth;
import io.dataease.plugins.common.base.domain.SysAuthDetail;
import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021-05-11
 * Description:
 */
@Data
public class SysAuthRequest extends SysAuth {

    private List<String> authSources;

    private List<String> authTargets;

    private SysAuthDetail authDetail;


}

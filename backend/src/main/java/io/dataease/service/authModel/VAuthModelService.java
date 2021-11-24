package io.dataease.service.authModel;

import io.dataease.base.mapper.ext.ExtVAuthModelMapper;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.TreeUtils;
import io.dataease.controller.request.authModel.VAuthModelRequest;
import io.dataease.dto.authModel.VAuthModelDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021/11/24
 * Description:
 */
@Service
public class VAuthModelService {

    @Resource
    private ExtVAuthModelMapper extVAuthModelMapper;

    public List<VAuthModelDTO> queryAuthModel(VAuthModelRequest request){
        request.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        List<VAuthModelDTO> result = extVAuthModelMapper.queryAuthModel(request);
        return TreeUtils.mergeTree(result );
    }
}

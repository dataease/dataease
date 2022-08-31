package io.dataease.service.authModel;

import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.TreeUtils;
import io.dataease.controller.request.authModel.VAuthModelRequest;
import io.dataease.dto.authModel.VAuthModelDTO;
import io.dataease.ext.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: wangjiahao
 * Date: 2021/11/24
 * Description:
 */
@Service
public class VAuthModelService {

    @Resource
    private ExtVAuthModelMapper extVAuthModelMapper;

    public List<VAuthModelDTO> queryAuthModel(VAuthModelRequest request) {
        request.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        List<VAuthModelDTO> result = extVAuthModelMapper.queryAuthModel(request);

        if (request.getPrivileges() != null) {
            result = filterPrivileges(request, result);
        }
        if (request.isClearEmptyDir()) {
            List<VAuthModelDTO> vAuthModelDTOS = TreeUtils.mergeTree(result);
            setAllLeafs(vAuthModelDTOS);
            removeEmptyDir(vAuthModelDTOS);
            return vAuthModelDTOS;
        }
        return TreeUtils.mergeTree(result);
    }

    private List<VAuthModelDTO> filterPrivileges(VAuthModelRequest request, List<VAuthModelDTO> result) {
        if(AuthUtils.getUser().getIsAdmin()){
            return result;
        }
        if (request.getPrivileges() != null) {
            result = result.stream().filter(vAuthModelDTO -> {
                if (vAuthModelDTO.getNodeType().equalsIgnoreCase("spine") || (vAuthModelDTO.getNodeType().equalsIgnoreCase("leaf") && vAuthModelDTO.getPrivileges() != null && vAuthModelDTO.getPrivileges().contains(request.getPrivileges()))) {
                    return true;
                } else {
                    return false;
                }
            }).collect(Collectors.toList());
        }
        return result;
    }

    private void removeEmptyDir(List<VAuthModelDTO> result) {
        if (CollectionUtils.isEmpty(result)) {
            return;
        }
        Iterator iterator = result.listIterator();
        while (iterator.hasNext()) {
            VAuthModelDTO tmp = (VAuthModelDTO) iterator.next();
            if (tmp.getNodeType().equalsIgnoreCase("spine") && tmp.getAllLeafs() == 0) {
                iterator.remove();
            } else {
                removeEmptyDir(tmp.getChildren());
            }
        }
    }

    private void setAllLeafs(List<VAuthModelDTO> result) {
        for (VAuthModelDTO vAuthModelDTO : result) {
            if (CollectionUtils.isEmpty(vAuthModelDTO.getChildren())) {
                vAuthModelDTO.setAllLeafs(0);
                continue;
            }
            long leafs = 0l;
            for (VAuthModelDTO child : vAuthModelDTO.getChildren()) {
                if (child.getNodeType().equalsIgnoreCase("leaf")) {
                    leafs = leafs + 1;
                } else {
                    leafs = +leafs + getLeafs(child);
                }
            }
            vAuthModelDTO.setAllLeafs(leafs);
        }
    }

    private long getLeafs(VAuthModelDTO child) {
        long leafs = 0l;
        if (CollectionUtils.isEmpty(child.getChildren())) {
            child.setAllLeafs(0);
            return leafs;
        }
        for (VAuthModelDTO childChild : child.getChildren()) {
            if (childChild.getNodeType().equalsIgnoreCase("leaf")) {
                leafs = leafs + 1;
            } else {
                leafs = +leafs + getLeafs(childChild);
            }
        }
        child.setAllLeafs(leafs);
        return leafs;
    }
}


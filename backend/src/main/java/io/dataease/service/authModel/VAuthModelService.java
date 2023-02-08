package io.dataease.service.authModel;

import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.TreeUtils;
import io.dataease.controller.request.authModel.VAuthModelRequest;
import io.dataease.dto.authModel.VAuthModelDTO;
import io.dataease.ext.ExtVAuthModelMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
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

    public List<VAuthModelDTO> queryAuthModelByIds(String modelType, List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<VAuthModelDTO> result = extVAuthModelMapper.queryAuthModelByIds(String.valueOf(AuthUtils.getUser().getUserId()), modelType, ids);
        if (CollectionUtils.isEmpty(result)) {
            return new ArrayList<>();
        } else {
            return result;
        }
    }

    public List<VAuthModelDTO> queryAuthModel(VAuthModelRequest request) {
        request.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        List<VAuthModelDTO> result = extVAuthModelMapper.queryAuthModel(request);
        if (CollectionUtils.isEmpty(result)) {
            return new ArrayList<>();
        }
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
        if (AuthUtils.getUser().getIsAdmin()) {
            return result;
        }
        if (request.getPrivileges() != null) {
            result = result.stream().filter(vAuthModelDTO -> "spine".equalsIgnoreCase(vAuthModelDTO.getNodeType())
                    || ("leaf".equalsIgnoreCase(vAuthModelDTO.getNodeType())
                    && vAuthModelDTO.getPrivileges() != null
                    && vAuthModelDTO.getPrivileges().contains(request.getPrivileges()))).collect(Collectors.toList());
        }
        return result;
    }

    private void removeEmptyDir(List<VAuthModelDTO> result) {
        if (CollectionUtils.isEmpty(result)) {
            return;
        }
        Iterator<VAuthModelDTO> iterator = result.listIterator();
        while (iterator.hasNext()) {
            VAuthModelDTO tmp = iterator.next();
            if ("spine".equalsIgnoreCase(tmp.getNodeType()) && tmp.getAllLeafs() == 0) {
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
            long leafs = 0L;
            for (VAuthModelDTO child : vAuthModelDTO.getChildren()) {
                if ("leaf".equalsIgnoreCase(child.getNodeType())) {
                    leafs = leafs + 1;
                } else {
                    leafs = +leafs + getLeafs(child);
                }
            }
            vAuthModelDTO.setAllLeafs(leafs);
        }
    }

    private long getLeafs(VAuthModelDTO child) {
        long leafs = 0L;
        if (CollectionUtils.isEmpty(child.getChildren())) {
            child.setAllLeafs(0);
            return leafs;
        }
        for (VAuthModelDTO childChild : child.getChildren()) {
            if ("leaf".equalsIgnoreCase(childChild.getNodeType())) {
                leafs = leafs + 1;
            } else {
                leafs = +leafs + getLeafs(childChild);
            }
        }
        child.setAllLeafs(leafs);
        return leafs;
    }
}


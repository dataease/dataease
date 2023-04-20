package io.dataease.xpack.permissions.auth.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.permissions.auth.dto.BusiPermissionRequest;
import io.dataease.api.permissions.auth.vo.PermissionVO;
import io.dataease.api.permissions.auth.vo.ResourceVO;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.exception.DEException;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.xpack.permissions.auth.bo.ResourceTreeNode;
import io.dataease.xpack.permissions.auth.dao.ext.entity.BusiResourcePO;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.BusiAuthExtMapper;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.MenuAuthExtMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AuthManage {
    private final static int ROOTID = 0;

    private final static String I18N_PREFIX = "i18n_auth_menu.";

    @Resource
    private BusiAuthExtMapper busiAuthExtMapper;

    @Resource
    private MenuAuthExtMapper menuAuthExtMapper;

    private AuthManageUtil authManageUtil;

    public List<ResourceVO> resourceTree(String flag) {
        BusiResourceEnum busiResourceEnum = BusiResourceEnum.valueOf(flag.toUpperCase());
        if (ObjectUtils.isEmpty(busiResourceEnum)) {
            DEException.throwException("invalid flag value");
        }
        TokenUserBO user = AuthUtils.getUser();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("org_id", user.getDefaultOid());
        queryWrapper.eq("rt_id", busiResourceEnum.getFlag());
        List<BusiResourcePO> pos = busiAuthExtMapper.query(queryWrapper);
        return authManageUtil.convertPos(pos, false);
    }

    public List<ResourceVO> menuTree() {
        QueryWrapper queryWrapper = new QueryWrapper();
        List<BusiResourcePO> pos = menuAuthExtMapper.query(queryWrapper);
        return authManageUtil.convertPos(pos, true);
    }

    public PermissionVO busiPermission(BusiPermissionRequest request) {
        PermissionVO vo = new PermissionVO();
        TokenUserBO user = AuthUtils.getUser();
//        if (AuthUtils.isOrgRoot()) {
//            vo.setRoot(true);
//            return vo;
//        }
        // if (AuthUtils.is)
        return null;
    }


     private class AuthManageUtil {
        public List<ResourceVO> convertPos(List<BusiResourcePO> pos, boolean appendI18nPrefix) {
            List<ResourceTreeNode> nodes = pos.stream().map(po -> BeanUtils.copyBean(new ResourceTreeNode(), po)).collect(Collectors.toList());
            List<ResourceTreeNode> treeNodes = poTree(nodes);
            return convertTree(treeNodes, appendI18nPrefix);
        }

         private List<ResourceTreeNode> poTree(List<ResourceTreeNode> nodeList) {
            List<ResourceTreeNode> result = new ArrayList<>();
            Map<Long, List<ResourceTreeNode>> childMap = nodeList.stream().collect(Collectors.groupingBy(ResourceTreeNode::getPid));
            nodeList.forEach(po -> {
                po.setChildren(childMap.get(po.getId()));
                if (po.getPid() == ROOTID) {
                    result.add(po);
                }
            });
            return result;
        }

         private List<ResourceVO> convertTree(List<ResourceTreeNode> roots, boolean appendI18nPrefix) {
            List<ResourceVO> result = new ArrayList<>();
            for (int i = 0; i < roots.size(); i++) {
                ResourceTreeNode node = roots.get(i);
                ResourceVO vo = BeanUtils.copyBean(new ResourceVO(), node, "children");
                if (appendI18nPrefix) {
                    vo.setName(I18N_PREFIX + vo.getName());
                }
                result.add(vo);
                List<ResourceTreeNode> children = null;
                if (!CollectionUtils.isEmpty(children = node.getChildren())) {
                    vo.setChildren(convertTree(children, appendI18nPrefix));
                }
            }
            return result;
        }
    }

    @PostConstruct
    public void init() {
        authManageUtil = new AuthManageUtil();
    }



}

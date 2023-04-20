package io.dataease.xpack.permissions.org.manage;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.permissions.org.vo.MountedVO;
import io.dataease.api.permissions.org.vo.OrgPageVO;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.exception.DEException;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.xpack.permissions.org.bo.OrgTreeNode;
import io.dataease.xpack.permissions.org.bo.PerOrgItem;
import io.dataease.xpack.permissions.org.dao.auto.entity.PerOrg;
import io.dataease.xpack.permissions.org.dao.auto.mapper.PerOrgMapper;
import io.dataease.xpack.permissions.org.dao.ext.mapper.OrgExtMapper;
import io.dataease.xpack.permissions.user.manage.RoleManage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Lazy
public class OrgPageManage {

    private final static int ROOTID = 0;

    @Resource
    private PerOrgMapper perOrgMapper;

    @Resource
    private OrgExtMapper orgExtMapper;

    @Resource
    private RoleManage roleManage;

    public List<OrgPageVO> buildTree(List<PerOrgItem> poList) {
        List<OrgTreeNode> orgTreeNodes = poList.stream().map(po -> BeanUtils.copyBean(new OrgTreeNode(), po)).collect(Collectors.toList());
        List<OrgTreeNode> treeNodes = poTree(orgTreeNodes);
        return convertTree(treeNodes);
    }

    public List<MountedVO> buildMountedTree(List<PerOrgItem> poList) {
        List<OrgTreeNode> orgTreeNodes = poList.stream().map(po -> BeanUtils.copyBean(new OrgTreeNode(), po)).collect(Collectors.toList());
        List<OrgTreeNode> treeNodes = poTree(orgTreeNodes);
        return convertMountedTree(treeNodes);
    }

    private List<OrgTreeNode> poTree(List<OrgTreeNode> nodeList) {
        List<OrgTreeNode> result = new ArrayList<>();
        Map<Long, List<OrgTreeNode>> childMap = nodeList.stream().collect(Collectors.groupingBy(OrgTreeNode::getPid));
        nodeList.forEach(po -> {
            po.setChildren(childMap.get(po.getId()));
            if (po.getPid() == ROOTID) {
                result.add(po);
            }
        });
        return result;
    }

    private List<OrgPageVO> convertTree(List<OrgTreeNode> roots) {
        List<OrgPageVO> result = new ArrayList<>();
        for (int i = 0; i < roots.size(); i++) {
            OrgTreeNode orgTreeNode = roots.get(i);
            OrgPageVO vo = BeanUtils.copyBean(new OrgPageVO(), orgTreeNode, "children");
            vo.setReadOnly(orgTreeNode.isDisabled());
            result.add(vo);
            List<OrgTreeNode> children = null;
            if (!CollectionUtils.isEmpty(children = orgTreeNode.getChildren())) {
                vo.setChildren(convertTree(children));
            }
        }
        return result;
    }

    private List<MountedVO> convertMountedTree(List<OrgTreeNode> roots) {
        List<MountedVO> result = new ArrayList<>();
        for (int i = 0; i < roots.size(); i++) {
            OrgTreeNode orgTreeNode = roots.get(i);
            MountedVO vo = BeanUtils.copyBean(new MountedVO(), orgTreeNode, "children");
            vo.setReadOnly(orgTreeNode.isDisabled());
            result.add(vo);
            List<OrgTreeNode> children = null;
            if (!CollectionUtils.isEmpty(children = orgTreeNode.getChildren())) {
                vo.setChildren(convertMountedTree(children));
            }
        }
        return result;
    }


    private List<PerOrgItem> convertItems(List<PerOrg> perOrgs, boolean disabled) {
        List<PerOrgItem> perOrgItems = perOrgs.stream().map(org -> {
            PerOrgItem perOrgItem = BeanUtils.copyBean(new PerOrgItem(), org);
            perOrgItem.setDisabled(disabled);
            return perOrgItem;
        }).toList();
        return perOrgItems;
    }

    public List<PerOrgItem> queryByUser(Long userId, String keyword) {
        List<PerOrg> perOrgs = null;
        QueryWrapper<PerOrg> queryWrapper = new QueryWrapper<>();

        if (AuthUtils.isSysAdmin(userId) && StringUtils.isBlank(keyword)) {
            perOrgs = perOrgMapper.selectList(queryWrapper);
            return convertItems(perOrgs, false);
        }
        queryWrapper.like(StringUtils.isNotBlank(keyword), "po.name", keyword);

        if (!AuthUtils.isSysAdmin(userId)) {
            queryWrapper.eq("pur.uid", userId);
        }

        perOrgs = orgExtMapper.queryByUserId(queryWrapper);
        List<PerOrgItem> perOrgItems = convertItems(perOrgs, false);
        if (CollectionUtil.isNotEmpty(perOrgs)) {
            List<Long> matchIds = perOrgs.stream().map(PerOrg::getId).collect(Collectors.toList());
            List<String> ids = perOrgs.stream().filter(item -> StringUtils.isNotBlank(item.getRootWay())).flatMap(item -> Arrays.stream(StringUtils.split(item.getRootWay(), ","))).distinct().filter(item -> !matchIds.contains(Long.parseLong(item))).collect(Collectors.toList());
            if (CollectionUtil.isEmpty(ids))  {
                return perOrgItems;
            }
            queryWrapper.clear();
            queryWrapper.in(CollectionUtil.isNotEmpty(ids), "id", ids);
            List<PerOrg> orgList = perOrgMapper.selectList(queryWrapper);
            List<PerOrgItem> orgItems = convertItems(orgList, true);
            if (CollectionUtil.isNotEmpty(orgItems)) {
                perOrgItems = new ArrayList<>(perOrgItems);
                perOrgItems.addAll(new ArrayList<>(orgItems));
            }
        }
        return perOrgItems;
    }


    @Transactional
    public void save(String name, Long pid) {
        TokenUserBO user = AuthUtils.getUser();
        if (ObjectUtils.isEmpty(pid))
            pid = user.getDefaultOid();
        PerOrg perOrg = new PerOrg();
        perOrg.setId(IDUtils.snowID());
        perOrg.setName(name);
        perOrg.setPid(pid);
        PerOrg parent = null;
        String newRootWay = pid.toString();
        if (ObjectUtils.isNotEmpty(parent = queryOne(pid)) && StringUtils.isNotBlank(parent.getRootWay())) {
            newRootWay = parent.getRootWay() + "," + pid;
        }
        perOrg.setRootWay(newRootWay);
        perOrg.setCreateTime(System.currentTimeMillis());
        perOrgMapper.insert(perOrg);

        roleManage.saveWithOrg(perOrg.getId());
    }

    public PerOrg queryOne(Long id) {
        PerOrg perOrg = perOrgMapper.selectById(id);
        return perOrg;
    }

    public void edit(Long id, String name) {
        PerOrg perOrg = queryOne(id);
        perOrg.setName(name);
        perOrgMapper.updateById(perOrg);
    }

    public void delete(Long id) {
        if (hasChildren(id)) {
            DEException.throwException("请先删除子组织");
        }
        // 删除组织下所有角色
        // 解绑角色
        // 删除空角色用户
        // 删除组织下业务资源
        // 使用消息事件通知业务系统删除对应业务资源详情
        perOrgMapper.deleteById(id);
        roleManage.deleteWithOrg(id);

    }

    public boolean hasChildren(Long id) {
        QueryWrapper<PerOrg> queryWrapper = new QueryWrapper();
        queryWrapper.eq("pid", id);
        return perOrgMapper.selectCount(queryWrapper) > 0;
    }

    public boolean busiExist(Long oid) {
        return orgExtMapper.busiCount(oid) > 0;
    }
}

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
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrgPageManage {

    private final static int ROOTID = 0;

    @Resource
    private PerOrgMapper perOrgMapper;

    @Resource
    private OrgExtMapper orgExtMapper;

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
            result.add(vo);
            List<OrgTreeNode> children = null;
            if (!CollectionUtils.isEmpty(children = orgTreeNode.getChildren())) {
                vo.setChildren(convertMountedTree(children));
            }
        }
        return result;
    }



    public List<PerOrgItem> query(String keyword) {
        QueryWrapper<PerOrg> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like("name", keyword);
        }
        List<PerOrg> perOrgs = perOrgMapper.selectList(queryWrapper);
        List<PerOrgItem> perOrgItems = perOrgs.stream().map(org -> {
            PerOrgItem perOrgItem = BeanUtils.copyBean(new PerOrgItem(), org);
            perOrgItem.setDisabled(false);
            return perOrgItem;
        }).toList();
        if (StringUtils.isNotBlank(keyword) && CollectionUtil.isNotEmpty(perOrgs)) {
            List<Long> matchIds = perOrgs.stream().map(PerOrg::getId).collect(Collectors.toList());
            List<String> ids = perOrgs.stream().flatMap(item -> Arrays.stream(StringUtils.split(item.getRootWay(), ","))).distinct().filter(item -> !matchIds.contains(item)).collect(Collectors.toList());
            queryWrapper.clear();
            queryWrapper.in(CollectionUtil.isNotEmpty(ids), "id", ids);
            List<PerOrgItem> orgItems = perOrgMapper.selectList(queryWrapper).stream().map(org -> {
                PerOrgItem perOrgItem = BeanUtils.copyBean(new PerOrgItem(), org);
                return perOrgItem;
            }).toList();
            perOrgItems.addAll(orgItems);
        }
        return perOrgItems;
    }

    public List<PerOrgItem> queryByUser(Long userId) {
        List<PerOrg> perOrgs = orgExtMapper.queryByUserId(userId);
        List<PerOrgItem> perOrgItems = perOrgs.stream().map(org -> {
            PerOrgItem perOrgItem = BeanUtils.copyBean(new PerOrgItem(), org);
            perOrgItem.setDisabled(false);
            return perOrgItem;
        }).toList();
        if (CollectionUtil.isNotEmpty(perOrgs)) {
            List<Long> matchIds = perOrgs.stream().map(PerOrg::getId).collect(Collectors.toList());
            List<String> ids = perOrgs.stream().flatMap(item -> Arrays.stream(StringUtils.split(item.getRootWay(), ","))).distinct().filter(item -> !matchIds.contains(item)).collect(Collectors.toList());
            QueryWrapper<PerOrg> queryWrapper = new QueryWrapper<>();
            queryWrapper.in(CollectionUtil.isNotEmpty(ids), "id", ids);
            List<PerOrgItem> orgItems = perOrgMapper.selectList(queryWrapper).stream().map(org -> {
                PerOrgItem perOrgItem = BeanUtils.copyBean(new PerOrgItem(), org);
                return perOrgItem;
            }).toList();
            perOrgItems.addAll(orgItems);
        }
        return perOrgItems;
    }

    /**
     * 保存了组织就完了？
     * 建角色 组织管理员 查看角色
     * 当前用户挂给组织管理员
     *
     */
    public void save(String name) {
        TokenUserBO user = AuthUtils.getUser();
        Long pid = user.getDefaultOid();
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
        perOrgMapper.insert(perOrg);
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
        perOrgMapper.deleteById(id);
    }

    public boolean hasChildren(Long id) {
        QueryWrapper<PerOrg> queryWrapper = new QueryWrapper();
        queryWrapper.eq("pid", id);
        return perOrgMapper.selectCount(queryWrapper) > 0;
    }
}

package io.dataease.service.panel;

import com.google.gson.Gson;
import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.base.domain.PanelGroup;
import io.dataease.base.domain.PanelShare;
import io.dataease.base.domain.PanelShareExample;
import io.dataease.base.mapper.PanelGroupMapper;
import io.dataease.base.mapper.PanelShareMapper;
import io.dataease.base.mapper.ext.ExtPanelShareMapper;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.commons.model.AuthURD;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.controller.request.panel.PanelShareRequest;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.dto.panel.PanelShareDto;
import io.dataease.dto.panel.PanelSharePo;
import io.dataease.service.message.DeMsgutil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ShareService {

    @Autowired(required = false)
    private PanelShareMapper mapper;

    @Resource
    private PanelGroupMapper panelGroupMapper;

    @Resource
    private ExtPanelShareMapper extPanelShareMapper;

    @Transactional
    public void save(PanelShareRequest request){
        List<PanelGroup> panelGroups = queryGroup(request.getPanelIds());
        //1.先根据仪表板删除所有已经分享的
        Integer type = request.getType();
        List<String> panelIds = request.getPanelIds();
        List<Long> targetIds = request.getTargetIds();
        // 使用原生对象会导致事物失效 所以这里需要使用spring代理对象
        if (CollectionUtils.isNotEmpty(panelIds)){
            ShareService proxy = CommonBeanFactory.getBean(ShareService.class);
            panelIds.forEach(panelId -> {
                proxy.delete(panelId, type);
            });
        }
        if (CollectionUtils.isEmpty(targetIds)) return;

        long now = System.currentTimeMillis();
        List<PanelShare> shares = panelIds.stream().flatMap(panelId ->
                targetIds.stream().map(targetId -> {
                PanelShare share = new PanelShare();
                share.setCreateTime(now);
                share.setPanelGroupId(panelId);
                share.setTargetId(targetId);
                share.setType(type);
                return share;
            })
        ).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(shares)){
            extPanelShareMapper.batchInsert(shares);
        }

        // 下面是发送提醒消息逻辑
        Set<Long> userIdSet = new HashSet<Long>();
        AuthURD authURD = new AuthURD();
        if (type == 0) {
            authURD.setUserIds(targetIds);
        }
        if (type == 1) {
            authURD.setRoleIds(targetIds);
        }
        if(type == 2) {
            authURD.setDeptIds(targetIds);
        }
        userIdSet = AuthUtils.userIdsByURD(authURD);

        CurrentUserDto user = AuthUtils.getUser();
        String msg = StringUtils.joinWith("，", panelGroups.stream().map(PanelGroup::getName).collect(Collectors.toList()));
        Gson gson = new Gson();
        userIdSet.forEach(userId -> {
            // DeMsgutil.sendMsg(userId, 0, user.getNickName()+" 分享了仪表板【"+msg+"】给您，请查收!");
            DeMsgutil.sendMsg(userId, 0, user.getNickName()+" 分享了仪表板【"+msg+"】给您，请查收!", gson.toJson(panelIds));
        });

    }

    private List<PanelGroup> queryGroup(List<String> panelIds) {
        return panelIds.stream().map(panelGroupMapper::selectByPrimaryKey).collect(Collectors.toList());
    }

    /**
     * panel_group_id建了索引 效率不会很差
     * @param panel_group_id
     */
    @Transactional
    public void delete(String panel_group_id, Integer type){
        PanelShareExample example = new PanelShareExample();
        PanelShareExample.Criteria criteria = example.createCriteria();
        criteria.andPanelGroupIdEqualTo(panel_group_id);
        if(type != null){
            criteria.andTypeEqualTo(type);
        }
        mapper.deleteByExample(example);
    }


    public List<PanelShareDto> queryTree(BaseGridRequest request){
        CurrentUserDto user = AuthUtils.getUser();
        Long userId = user.getUserId();
        Long deptId = user.getDeptId();
        List<Long> roleIds = user.getRoles().stream().map(CurrentRoleDto::getId).collect(Collectors.toList());

        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("deptId", deptId);
        param.put("roleIds", roleIds);

        List<PanelSharePo> datas = extPanelShareMapper.query(param);

        /*List<Long> targetIds = new ArrayList<>();
        targetIds.add(userId);
        targetIds.add(deptId);
        targetIds.addAll(roleIds);

        ConditionEntity condition = new ConditionEntity();
        condition.setField("s.target_id");
        condition.setOperator("in");
        condition.setValue(targetIds);

        request.setConditions(new ArrayList<ConditionEntity>(){{add(condition);}});

        GridExample example = request.convertExample();
        List<PanelSharePo> datas = extPanelShareMapper.query(example);*/
        List<PanelShareDto> dtoLists = datas.stream().map(po -> BeanUtils.copyBean(new PanelShareDto(), po)).collect(Collectors.toList());
        return convertTree(dtoLists);
    }

    //List构建Tree
    private List<PanelShareDto> convertTree(List<PanelShareDto> datas){
        String username = AuthUtils.getUser().getUsername();
        Map<String, List<PanelShareDto>> map = datas.stream().filter(panelShareDto -> StringUtils.isNotEmpty(panelShareDto.getCreator()) && !StringUtils.equals(username, panelShareDto.getCreator())).collect(Collectors.groupingBy(PanelShareDto::getCreator));
        return map.entrySet().stream().map(entry -> {
            PanelShareDto panelShareDto = new PanelShareDto();
            panelShareDto.setName(entry.getKey());
            panelShareDto.setChildren(entry.getValue());
            return panelShareDto;
        }).collect(Collectors.toList());
    }

    public List<PanelShare> queryWithResource(BaseGridRequest request){
        GridExample example = request.convertExample();
        return extPanelShareMapper.queryWithResource(example);
    }


}

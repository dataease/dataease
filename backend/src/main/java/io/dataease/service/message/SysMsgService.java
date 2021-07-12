package io.dataease.service.message;


import io.dataease.base.domain.*;
import io.dataease.base.mapper.SysMsgChannelMapper;
import io.dataease.base.mapper.SysMsgMapper;
import io.dataease.base.mapper.SysMsgSettingMapper;
import io.dataease.base.mapper.SysMsgTypeMapper;
import io.dataease.base.mapper.ext.ExtSysMsgMapper;
import io.dataease.commons.constants.SysMsgConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.controller.message.dto.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMsgService {

    @Resource
    private SysMsgMapper sysMsgMapper;

    @Resource
    private ExtSysMsgMapper extSysMsgMapper;


    @Resource
    private SysMsgTypeMapper sysMsgTypeMapper;


    @Resource
    private SysMsgChannelMapper sysMsgChannelMapper;

    @Resource
    private SysMsgSettingMapper sysMsgSettingMapper;

    public List<SysMsg> query(Long userId, MsgRequest msgRequest) {
        String orderClause = " create_time desc";
        SysMsgExample example = new SysMsgExample();
        SysMsgExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(userId);

        List<String> orders = msgRequest.getOrders();

        if (CollectionUtils.isNotEmpty(orders)) {
            orderClause = String.join(", ", orders);
        }

        if (ObjectUtils.isNotEmpty(msgRequest.getType())) {
            criteria.andTypeIdEqualTo(msgRequest.getType());
        }

        if (ObjectUtils.isNotEmpty(msgRequest.getStatus())) {
            criteria.andStatusEqualTo(msgRequest.getStatus());
        }

        example.setOrderByClause(orderClause);
        List<SysMsg> sysMsgs = sysMsgMapper.selectByExample(example);
        return sysMsgs;
    }

    public List<MsgGridDto> queryGrid(Long userId, MsgRequest msgRequest) {
        String orderClause = " create_time desc";
        SysMsgExample example = new SysMsgExample();
        SysMsgExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(userId);

        List<String> orders = msgRequest.getOrders();

        if (CollectionUtils.isNotEmpty(orders)) {
            orderClause = String.join(", ", orders);
        }

        if (ObjectUtils.isNotEmpty(msgRequest.getType())) {
            SysMsgTypeExample sysMsgTypeExample = new SysMsgTypeExample();
            sysMsgTypeExample.createCriteria().andPidEqualTo(msgRequest.getType());

            List<SysMsgType> sysMsgTypes = sysMsgTypeMapper.selectByExample(sysMsgTypeExample);
            List<Long> typeIds = sysMsgTypes.stream().map(SysMsgType::getMsgTypeId).collect(Collectors.toList());
            criteria.andTypeIdIn(typeIds);
        }

        if (ObjectUtils.isNotEmpty(msgRequest.getStatus())) {
            criteria.andStatusEqualTo(msgRequest.getStatus());
        }

        example.setOrderByClause(orderClause);
        List<MsgGridDto> msgGridDtos = extSysMsgMapper.queryGrid(example);
        return msgGridDtos;
    }

    public void setReaded(Long msgId) {
        SysMsg sysMsg = new SysMsg();
        sysMsg.setMsgId(msgId);
        sysMsg.setStatus(true);
        sysMsg.setReadTime(System.currentTimeMillis());
        sysMsgMapper.updateByPrimaryKeySelective(sysMsg);
    }

    public void setBatchReaded(List<Long> msgIds) {
        extSysMsgMapper.batchStatus(msgIds);
    }

    public void batchDelete(List<Long> msgIds) {
        extSysMsgMapper.batchDelete(msgIds);
    }

    public void save(SysMsg sysMsg) {
        sysMsgMapper.insert(sysMsg);
    }


    public List<SettingTreeNode> treeNodes() {
        SysMsgService proxy = CommonBeanFactory.getBean(SysMsgService.class);
        List<SysMsgType> sysMsgTypes = proxy.queryMsgTypes();
        return buildTree(sysMsgTypes);
    }

    @Cacheable(SysMsgConstants.SYS_MSG_TYPE)
    public List<SysMsgType> queryMsgTypes() {
        SysMsgTypeExample example = new SysMsgTypeExample();
        List<SysMsgType> sysMsgTypes = sysMsgTypeMapper.selectByExample(example);
        return sysMsgTypes;
    }

    private List<SettingTreeNode> buildTree(List<SysMsgType> lists){
        List<SettingTreeNode> rootNodes = new ArrayList<>();
        lists.forEach(node -> {
            SettingTreeNode settingTreeNode = convert(node);
            if (isParent(node)) {
                rootNodes.add(settingTreeNode);
            }
            lists.forEach(tNode -> {
                if (tNode.getPid() == settingTreeNode.getId()) {
                    if (settingTreeNode.getChildren() == null) {
                        settingTreeNode.setChildren(new ArrayList<SettingTreeNode>());
                    }
                    settingTreeNode.getChildren().add(convert(tNode));
                }
            });
        });
        return rootNodes;
    }

    private Boolean isParent(SysMsgType typeNode) {
        return typeNode.getPid() == 0L;
    }

    public SettingTreeNode convert(SysMsgType typeNode) {
        SettingTreeNode settingTreeNode = new SettingTreeNode();
        settingTreeNode.setId(typeNode.getMsgTypeId());
        settingTreeNode.setName(typeNode.getTypeName());
        return settingTreeNode;
    }


    @Cacheable(SysMsgConstants.SYS_MSG_CHANNEL)
    public List<SysMsgChannel> channelList() {
        SysMsgChannelExample example = new SysMsgChannelExample();
        return sysMsgChannelMapper.selectByExample(example);
    }

    public List<SysMsgSetting> settingList() {
        Long userId = AuthUtils.getUser().getUserId();
        SysMsgSettingExample example = new SysMsgSettingExample();
        example.createCriteria().andUserIdEqualTo(userId);
        List<SysMsgSetting> sysMsgSettings = sysMsgSettingMapper.selectByExample(example);
        return sysMsgSettings;
    }

    /**
     * 修改了订阅信息 需要清除缓存
     * @param request
     * @param userId
     */
    @Transactional
    @CacheEvict(value = SysMsgConstants.SYS_MSG_USER_SUBSCRIBE, key = "#userId")
    public void updateSetting(MsgSettingRequest request, Long userId) {
        Long typeId = request.getTypeId();
        Long channelId = request.getChannelId();
        // Long userId = AuthUtils.getUser().getUserId();
        SysMsgSettingExample example = new SysMsgSettingExample();
        example.createCriteria().andUserIdEqualTo(userId).andTypeIdEqualTo(typeId).andChannelIdEqualTo(channelId);
        List<SysMsgSetting> sysMsgSettings = sysMsgSettingMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(sysMsgSettings)) {
            sysMsgSettings.forEach(setting -> {
                setting.setEnable(!setting.getEnable());
                sysMsgSettingMapper.updateByPrimaryKeySelective(setting);
            });
            return;
        }
        SysMsgSetting sysMsgSetting = new SysMsgSetting();
        sysMsgSetting.setEnable(true);
        sysMsgSetting.setChannelId(channelId);
        sysMsgSetting.setTypeId(typeId);
        sysMsgSetting.setUserId(userId);
        sysMsgSettingMapper.insert(sysMsgSetting);
    }

    public void sendMsg(Long userId, Long typeId, Long channelId, String content, String param) {
        SysMsg sysMsg = new SysMsg();
        sysMsg.setUserId(userId);
        sysMsg.setTypeId(typeId);
        sysMsg.setContent(content);
        sysMsg.setStatus(false);
        sysMsg.setCreateTime(System.currentTimeMillis());
        sysMsg.setParam(param);
        save(sysMsg);
    }

    /**
     * 查询用户订阅的消息 并缓存
     * @param userId
     * @return
     */
    @Cacheable(value = SysMsgConstants.SYS_MSG_USER_SUBSCRIBE, key = "#userId")
    public List<SubscribeNode> subscribes(Long userId) {
        SysMsgSettingExample example = new SysMsgSettingExample();
        example.createCriteria().andUserIdEqualTo(userId).andEnableEqualTo(true);
        List<SysMsgSetting> sysMsgSettings = sysMsgSettingMapper.selectByExample(example);
        List<SubscribeNode> resultLists = sysMsgSettings.stream().map(item -> {
            SubscribeNode subscribeNode = new SubscribeNode();
            subscribeNode.setTypeId(item.getTypeId());
            subscribeNode.setChannelId(item.getChannelId());
            return subscribeNode;
        }).collect(Collectors.toList());
        return resultLists;
    }

}

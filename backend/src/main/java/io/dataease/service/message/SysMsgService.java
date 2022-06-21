package io.dataease.service.message;

import io.dataease.ext.ExtSysMsgMapper;
import io.dataease.commons.constants.SysMsgConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.controller.sys.request.BatchSettingRequest;
import io.dataease.controller.sys.request.MsgRequest;
import io.dataease.controller.sys.request.MsgSettingRequest;
import io.dataease.controller.sys.response.MsgGridDto;
import io.dataease.controller.sys.response.SettingTreeNode;
import io.dataease.controller.sys.response.SubscribeNode;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.SysMsgChannelMapper;
import io.dataease.plugins.common.base.mapper.SysMsgMapper;
import io.dataease.plugins.common.base.mapper.SysMsgSettingMapper;
import io.dataease.plugins.common.base.mapper.SysMsgTypeMapper;
import io.dataease.service.message.service.SendService;
import io.dataease.service.system.SystemParameterService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static int overDays = 30;

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

    @Autowired
    private SystemParameterService systemParameterService;

    public List<MsgGridDto> queryGrid(Long userId, MsgRequest msgRequest, List<Long> typeIds, Long startTime) {
        String orderClause = " create_time desc";
        SysMsgExample example = new SysMsgExample();
        SysMsgExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(userId);

        List<String> orders = msgRequest.getOrders();

        if (CollectionUtils.isNotEmpty(orders)) {
            orderClause = String.join(", ", orders);
        }

        if (CollectionUtils.isNotEmpty(typeIds)){
            criteria.andTypeIdIn(typeIds);
        }

        if (ObjectUtils.isNotEmpty(msgRequest.getStatus())) {
            criteria.andStatusEqualTo(msgRequest.getStatus());
        }

        criteria.andCreateTimeGreaterThanOrEqualTo(startTime);

        example.setOrderByClause(orderClause);
        return extSysMsgMapper.queryGrid(example);
    }

    public Long queryCount(Long userId) {
        SysMsgExample example = new SysMsgExample();
        SysMsgExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(userId).andStatusEqualTo(false);
        criteria.andCreateTimeGreaterThanOrEqualTo(overTime());
        return sysMsgMapper.countByExample(example);
    }

    public void setReaded(Long msgId) {
        SysMsg sysMsg = new SysMsg();
        sysMsg.setMsgId(msgId);
        sysMsg.setStatus(true);
        sysMsg.setReadTime(System.currentTimeMillis());
        sysMsgMapper.updateByPrimaryKeySelective(sysMsg);
    }

    public void setBatchReaded(List<Long> msgIds) {
        extSysMsgMapper.batchStatus(msgIds, System.currentTimeMillis());
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

    /*@Cacheable(SysMsgConstants.SYS_MSG_TYPE)*/
    public List<SysMsgType> queryMsgTypes() {
        SysMsgTypeExample example = new SysMsgTypeExample();
        return sysMsgTypeMapper.selectByExample(example);
    }

    private List<SettingTreeNode> buildTree(List<SysMsgType> lists){
        List<SettingTreeNode> rootNodes = new ArrayList<>();
        lists.forEach(node -> {
            SettingTreeNode settingTreeNode = convert(node);
            if (isParent(node)) {
                rootNodes.add(settingTreeNode);
            }
            lists.forEach(tNode -> {
                if (tNode.getPid().equals(settingTreeNode.getId())) {
                    if (settingTreeNode.getChildren() == null) {
                        settingTreeNode.setChildren(new ArrayList<>());
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
        sysMsgSettings = addDefault(sysMsgSettings);
        return sysMsgSettings;
    }

    public List<SysMsgSetting> defaultSettings() {
        SysMsgSetting sysMsgSetting1 = new SysMsgSetting();
        sysMsgSetting1.setTypeId(2L);
        sysMsgSetting1.setChannelId(1L);
        sysMsgSetting1.setEnable(true);
        SysMsgSetting sysMsgSetting2 = new SysMsgSetting();
        sysMsgSetting2.setTypeId(6L);
        sysMsgSetting2.setChannelId(1L);
        sysMsgSetting2.setEnable(true);
        List<SysMsgSetting> lists = new ArrayList<>();
        lists.add(sysMsgSetting1);
        lists.add(sysMsgSetting2);
        return lists;
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

        sysMsgSetting.setChannelId(channelId);
        sysMsgSetting.setTypeId(typeId);

        List<SysMsgSetting> defaultSettings = defaultSettings();

        sysMsgSetting.setEnable(!defaultSettings.stream().anyMatch(setting -> setting.match(sysMsgSetting)));

        sysMsgSetting.setUserId(userId);

        sysMsgSettingMapper.insert(sysMsgSetting);
    }


    @Transactional
    @CacheEvict(value = SysMsgConstants.SYS_MSG_USER_SUBSCRIBE, key = "#userId")
    public void batchUpdate(BatchSettingRequest request, Long userId) {
        // 先删除
        SysMsgSettingExample example = new SysMsgSettingExample();
        example.createCriteria().andUserIdEqualTo(userId).andChannelIdEqualTo(request.getChannelId()).andTypeIdIn(request.getTypeIds());
        sysMsgSettingMapper.deleteByExample(example);
        // 再写入
        List<SysMsgSetting> settings = request.getTypeIds().stream().map(typeId -> {
            SysMsgSetting sysMsgSetting = new SysMsgSetting();
            sysMsgSetting.setUserId(userId);
            sysMsgSetting.setTypeId(typeId);
            sysMsgSetting.setChannelId(request.getChannelId());
            sysMsgSetting.setEnable(request.getEnable());
            return sysMsgSetting;
        }).collect(Collectors.toList());

        extSysMsgMapper.batchInsert(settings);
    }

    public void sendMsg(Long userId, Long typeId, String content, String param) {
        List<SubscribeNode> subscribes = subscribes(userId);

        if (CollectionUtils.isNotEmpty(subscribes)) {
            subscribes.stream().filter(item -> item.getTypeId().equals(typeId)).forEach(sub -> {
                SendService sendService = serviceByChannel(sub.getChannelId());
                sendService.sendMsg(userId, typeId, content, param);
            });

        }

    }

    private SendService serviceByChannel(Long channelId){
        String beanName = sysMsgChannelMapper.selectByPrimaryKey(channelId).getServiceName();
        return (SendService)CommonBeanFactory.getBean(beanName);
    }

    /**
     * 查询用户订阅的消息 并缓存
     * @param userId
     * @return
     */
    @Cacheable(value = SysMsgConstants.SYS_MSG_USER_SUBSCRIBE, key = "#userId")
    public List<SubscribeNode> subscribes(Long userId) {
        SysMsgSettingExample example = new SysMsgSettingExample();
        example.createCriteria().andUserIdEqualTo(userId);
        List<SysMsgSetting> sysMsgSettings = sysMsgSettingMapper.selectByExample(example);
        // 添加默认订阅
        sysMsgSettings = addDefault(sysMsgSettings);
        sysMsgSettings = sysMsgSettings.stream().filter(SysMsgSetting::getEnable).collect(Collectors.toList());
        return sysMsgSettings.stream().map(item -> {
            SubscribeNode subscribeNode = new SubscribeNode();
            subscribeNode.setTypeId(item.getTypeId());
            subscribeNode.setChannelId(item.getChannelId());
            return subscribeNode;
        }).collect(Collectors.toList());
    }

    public List<SysMsgSetting> addDefault(List<SysMsgSetting> sourceLists) {
        List<SysMsgSetting> defaultSettings = defaultSettings();

        defaultSettings.forEach(setting -> {
            if (!sourceLists.stream().anyMatch(item -> item.match(setting))){
                sourceLists.add(setting);
            }
        });
        return sourceLists;
    }

    public void setAllRead() {
        SysMsg record = new SysMsg();
        record.setStatus(true);
        SysMsgExample example = new SysMsgExample();
        example.createCriteria().andUserIdEqualTo(AuthUtils.getUser().getUserId()).andStatusEqualTo(false);
        sysMsgMapper.updateByExampleSelective(record, example);
    }

    public Long overTime() {
        String msgTimeOut = systemParameterService.basicInfo().getMsgTimeOut();
        if(StringUtils.isNotBlank(msgTimeOut)) {
            overDays = Integer.parseInt(msgTimeOut);
        }
        Long currentTime = System.currentTimeMillis();

        long oneDayTime = 24 * 60 * 60 * 1000;

        long temp = overDays * oneDayTime;

        return currentTime - (currentTime + 8 * 60 * 60 * 1000) % oneDayTime - temp;
                 
    }

    

}

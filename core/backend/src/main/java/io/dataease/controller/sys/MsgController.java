package io.dataease.controller.sys;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.annotation.SqlInjectValidator;
import io.dataease.auth.service.AuthUserService;
import io.dataease.plugins.common.base.domain.SysMsgChannel;
import io.dataease.plugins.common.base.domain.SysMsgSetting;
import io.dataease.plugins.common.base.domain.SysMsgType;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.sys.request.BatchSettingRequest;
import io.dataease.controller.sys.request.MsgRequest;
import io.dataease.controller.sys.request.MsgSettingRequest;
import io.dataease.controller.sys.response.MsgGridDto;
import io.dataease.controller.sys.response.SettingTreeNode;
import io.dataease.service.message.SysMsgService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

@Api(tags = "系统：消息管理")
@ApiSupport(order = 230)
@RequestMapping("/api/sys_msg")
@RestController
public class MsgController {

    @Resource
    private SysMsgService sysMsgService;

    @Resource
    private AuthUserService authUserService;

    @ApiOperation("分页查询")
    @PostMapping("/list/{goPage}/{pageSize}")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "goPage", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "pageSize", value = "页容量", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "msgRequest", value = "查询条件", required = true)
    })
    @SqlInjectValidator(value = {"create_time", "type_id"})
    public Pager<List<MsgGridDto>> messages(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody MsgRequest msgRequest) {
        Long userId = AuthUtils.getUser().getUserId();
        List<Long> typeIds = null;
        if (ObjectUtils.isNotEmpty(msgRequest.getType())) {
            List<SysMsgType> sysMsgTypes = sysMsgService.queryMsgTypes();
            typeIds = sysMsgTypes.stream().filter(sysMsgType -> msgRequest.getType() == sysMsgType.getPid()).map(SysMsgType::getMsgTypeId).collect(Collectors.toList());
        }
        Long overTime = sysMsgService.overTime();
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        Pager<List<MsgGridDto>> listPager = PageUtils.setPageInfo(page, sysMsgService.queryGrid(userId, msgRequest, typeIds, overTime));
        return listPager;
    }

    @ApiOperation("查询未读数量")
    @PostMapping("/unReadCount")
    public Long unReadCount() {
        ;
        Long userId = null;
        if (null == AuthUtils.getUser() || (userId = AuthUtils.getUser().getUserId()) == null) {
            throw new RuntimeException("缺少用户ID");
        }
        return sysMsgService.queryCount(userId);
    }

    @ApiOperation("设置已读")
    @PostMapping("/setReaded/{msgId}")
    @ApiImplicitParam(paramType = "path", name = "msgId", value = "消息ID", required = true, dataType = "Long")
    public void setReaded(@PathVariable Long msgId) {
        sysMsgService.setRead(msgId);
    }


    @ApiOperation("批量设置已读")
    @PostMapping("/batchRead")
    @ApiImplicitParam(name = "msgIds", value = "消息ID集合", required = true, dataType = "List")
    public void batchRead(@RequestBody List<Long> msgIds) {
        sysMsgService.setBatchRead(msgIds);
    }

    @ApiOperation("全部设置已读")
    @PostMapping("/allRead")
    public void allRead() {
        sysMsgService.setAllRead();
    }

    @ApiOperation("批量删除")
    @PostMapping("/batchDelete")
    @ApiImplicitParam(name = "msgIds", value = "消息ID集合", required = true, dataType = "List")
    public void batchDelete(@RequestBody List<Long> msgIds) {
        sysMsgService.batchDelete(msgIds);
    }

    @ApiOperation("查询类型")
    @PostMapping("/types")
    public List<SysMsgType> allTypes() {
        List<SysMsgType> sysMsgTypes = sysMsgService.queryMsgTypes();
        return sysMsgTypes;
    }

    @ApiOperation("类型树")
    @PostMapping("/treeNodes")
    public List<SettingTreeNode> treeNodes() {

        return sysMsgService.treeNodes();
    }

    @ApiOperation("查询渠道")
    @PostMapping("/channelList")
    public List<SysMsgChannel> channelList() {
        List<SysMsgChannel> sysMsgChannels = sysMsgService.channelList();
        if (ObjectUtils.isEmpty(sysMsgChannels)) return sysMsgChannels;
        return sysMsgChannels.stream().filter(channel -> {
            Long msgChannelId = channel.getMsgChannelId();
            if (msgChannelId == 3L) {
                return authUserService.supportWecom();
            }
            if (msgChannelId == 4L) {
                return authUserService.supportDingtalk();
            }
            if (msgChannelId == 5L) {
                return authUserService.supportLark();
            }
            if (msgChannelId == 6L) {
                return authUserService.supportLarksuite();
            }
            return true;
        }).collect(Collectors.toList());
    }

    @ApiOperation("查询订阅")
    @PostMapping("/settingList")
    public List<SysMsgSetting> settingList() {
        return sysMsgService.settingList();
    }

    @ApiOperation("更新订阅")
    @PostMapping("/updateSetting")
    public void updateSetting(@RequestBody MsgSettingRequest request) {
        Long userId = AuthUtils.getUser().getUserId();
        sysMsgService.updateSetting(request, userId);
    }

    @ApiOperation("批量更新订阅")
    @PostMapping("/batchUpdate")
    public void batchUpdate(@RequestBody BatchSettingRequest request) {
        Long userId = AuthUtils.getUser().getUserId();
        sysMsgService.batchUpdate(request, userId);
    }
}

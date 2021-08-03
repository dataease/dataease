package io.dataease.controller.sys;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.base.domain.SysMsgChannel;
import io.dataease.base.domain.SysMsgSetting;
import io.dataease.base.domain.SysMsgType;
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

@Api(tags = "系统：消息管理")
@ApiSupport(order = 230)
@RequestMapping("/api/sys_msg")
@RestController
public class MsgController {

    @Resource
    private SysMsgService sysMsgService;

    @ApiOperation("分页查询")
    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<MsgGridDto>> messages(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody MsgRequest msgRequest) {
        Long userId = AuthUtils.getUser().getUserId();
        List<Long> typeIds = null;
        if (ObjectUtils.isNotEmpty(msgRequest.getType())){
            List<SysMsgType> sysMsgTypes = sysMsgService.queryMsgTypes();
            typeIds = sysMsgTypes.stream().filter(sysMsgType -> msgRequest.getType() == sysMsgType.getPid()).map(SysMsgType::getMsgTypeId).collect(Collectors.toList());
        }
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        Pager<List<MsgGridDto>> listPager = PageUtils.setPageInfo(page, sysMsgService.queryGrid(userId, msgRequest, typeIds));
        return listPager;
    }

    @ApiOperation("设置已读")
    @PostMapping("/setReaded/{msgId}")
    public void setReaded(@PathVariable Long msgId) {
        sysMsgService.setReaded(msgId);
    }


    @ApiOperation("批量设置已读")
    @PostMapping("/batchRead")
    public void batchRead(@RequestBody List<Long> msgIds) {
        sysMsgService.setBatchReaded(msgIds);
    }

    @ApiOperation("批量删除")
    @PostMapping("/batchDelete")
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
        return sysMsgService.channelList();
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

package io.dataease.controller.message;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.base.domain.SysMsgChannel;
import io.dataease.base.domain.SysMsgSetting;
import io.dataease.base.domain.SysMsgType;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.message.dto.MsgGridDto;
import io.dataease.controller.message.dto.MsgRequest;
import io.dataease.controller.message.dto.MsgSettingRequest;
import io.dataease.controller.message.dto.SettingTreeNode;
import io.dataease.service.message.SysMsgService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Api(tags = "系统：消息管理")
@RequestMapping("/api/sys_msg")
@RestController
public class MsgController {

    @Resource
    private SysMsgService sysMsgService;

    @ApiOperation("查询消息")
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

    @PostMapping("/setReaded/{msgId}")
    public void setReaded(@PathVariable Long msgId) {
        sysMsgService.setReaded(msgId);
    }


    @PostMapping("/batchRead")
    public void batchRead(@RequestBody List<Long> msgIds) {
        sysMsgService.setBatchReaded(msgIds);
    }

    @PostMapping("/batchDelete")
    public void batchDelete(@RequestBody List<Long> msgIds) {
        sysMsgService.batchDelete(msgIds);
    }

    @PostMapping("/treeNodes")
    public List<SettingTreeNode> treeNodes() {

        return sysMsgService.treeNodes();
    }

    @PostMapping("/channelList")
    public List<SysMsgChannel> channelList() {
        return sysMsgService.channelList();
    }

    @PostMapping("/settingList")
    public List<SysMsgSetting> settingList() {
        return sysMsgService.settingList();
    }

    @PostMapping("/updateSetting")
    public void updateSetting(@RequestBody MsgSettingRequest request) {
        Long userId = AuthUtils.getUser().getUserId();
        sysMsgService.updateSetting(request, userId);
    }

    @PostMapping("/types")
    public List<SysMsgType> allTypes() {
        List<SysMsgType> sysMsgTypes = sysMsgService.queryMsgTypes();
        return sysMsgTypes;
    }
}

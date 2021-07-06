package io.dataease.controller.message;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.base.domain.SysMsg;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.message.dto.MsgRequest;
import io.dataease.service.message.SysMsgService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "系统：消息管理")
@RequestMapping("/api/sys_msg")
@RestController
public class MsgController {

    @Resource
    private SysMsgService sysMsgService;

    @ApiOperation("查询消息")
    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<SysMsg>> messages(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody MsgRequest msgRequest) {
        Long userId = AuthUtils.getUser().getUserId();
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        Pager<List<SysMsg>> listPager = PageUtils.setPageInfo(page, sysMsgService.query(userId, msgRequest));
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
}

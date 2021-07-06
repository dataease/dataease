package io.dataease.service.message;


import io.dataease.base.domain.SysMsg;
import io.dataease.base.domain.SysMsgExample;
import io.dataease.base.mapper.SysMsgMapper;
import io.dataease.base.mapper.ext.ExtSysMsgMapper;
import io.dataease.controller.message.dto.MsgRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class SysMsgService {

    @Resource
    private SysMsgMapper sysMsgMapper;

    @Resource
    private ExtSysMsgMapper extSysMsgMapper;

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
            criteria.andTypeEqualTo(msgRequest.getType());
        }

        if (ObjectUtils.isNotEmpty(msgRequest.getStatus())) {
            criteria.andStatusEqualTo(msgRequest.getStatus());
        }

        example.setOrderByClause(orderClause);
        List<SysMsg> sysMsgs = sysMsgMapper.selectByExample(example);
        return sysMsgs;
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


}

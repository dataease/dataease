package io.dataease.service.message;


import io.dataease.base.domain.SysMsg;
import io.dataease.base.domain.SysMsgExample;
import io.dataease.base.mapper.SysMsgMapper;
import io.dataease.controller.message.dto.MsgRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class SysMsgService {

    @Resource
    private SysMsgMapper sysMsgMapper;

    public List<SysMsg> query(Long userId, MsgRequest msgRequest) {
        String orderClause = "";
        SysMsgExample example = new SysMsgExample();
        SysMsgExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(userId);


        if (ObjectUtils.isNotEmpty(msgRequest.getType())) {
            criteria.andTypeEqualTo(msgRequest.getType());
        }

        if (ObjectUtils.isNotEmpty(msgRequest.getStatus())) {
            criteria.andStatusEqualTo(msgRequest.getStatus());
        }else {
            orderClause += " status asc ,";
        }

        orderClause += " create_time desc";
        example.setOrderByClause(orderClause);
        List<SysMsg> sysMsgs = sysMsgMapper.selectByExample(example);
        return sysMsgs;
    }

    public void setReaded(Long msgId) {
        SysMsg sysMsg = new SysMsg();
        sysMsg.setMsgId(msgId);
        sysMsg.setStatus(true);
        sysMsgMapper.updateByPrimaryKeySelective(sysMsg);
    }

    public void save(SysMsg sysMsg) {
//        sysMsg.setStatus(false);
//        sysMsg.setCreateTime(System.currentTimeMillis());
        sysMsgMapper.insert(sysMsg);
    }

    public void update(SysMsg sysMsg) {

        sysMsgMapper.updateByPrimaryKey(sysMsg);
    }


}

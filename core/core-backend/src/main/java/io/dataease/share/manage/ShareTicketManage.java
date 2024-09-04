package io.dataease.share.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.xpack.share.request.TicketCreator;
import io.dataease.api.xpack.share.request.TicketDelRequest;
import io.dataease.api.xpack.share.request.TicketSwitchRequest;
import io.dataease.api.xpack.share.vo.TicketVO;
import io.dataease.api.xpack.share.vo.TicketValidVO;
import io.dataease.commons.utils.CodingUtil;
import io.dataease.exception.DEException;
import io.dataease.share.dao.auto.entity.CoreShareTicket;
import io.dataease.share.dao.auto.entity.XpackShare;
import io.dataease.share.dao.auto.mapper.CoreShareTicketMapper;
import io.dataease.share.dao.auto.mapper.XpackShareMapper;
import io.dataease.share.dao.ext.mapper.XpackShareExtMapper;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ShareTicketManage {

    @Resource
    private CoreShareTicketMapper coreShareTicketMapper;

    @Resource
    private XpackShareMapper xpackShareMapper;

    @Resource
    private XpackShareExtMapper xpackShareExtMapper;

    public CoreShareTicket getByTicket(String ticket) {
        QueryWrapper<CoreShareTicket> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ticket", ticket);
        return coreShareTicketMapper.selectOne(queryWrapper);
    }

    public String saveTicket(TicketCreator creator) {
        String ticket = creator.getTicket();
        if (StringUtils.isNotBlank(ticket)) {
            CoreShareTicket ticketEntity = getByTicket(ticket);
            if (ObjectUtils.isNotEmpty(ticketEntity)) {
                if (creator.isGenerateNew()) {
                    ticketEntity.setAccessTime(null);
                    ticketEntity.setTicket(CodingUtil.shortUuid());
                }
                ticketEntity.setArgs(creator.getArgs());
                ticketEntity.setExp(creator.getExp());
                ticketEntity.setUuid(creator.getUuid());
                coreShareTicketMapper.deleteById(ticketEntity);
                coreShareTicketMapper.insert(ticketEntity);
                return ticketEntity.getTicket();
            }
        }
        ticket = CodingUtil.shortUuid();
        CoreShareTicket linkTicket = new CoreShareTicket();
        linkTicket.setId(IDUtils.snowID());
        linkTicket.setTicket(ticket);
        linkTicket.setArgs(creator.getArgs());
        linkTicket.setExp(creator.getExp());
        linkTicket.setUuid(creator.getUuid());
        coreShareTicketMapper.insert(linkTicket);
        return ticket;
    }

    public void deleteTicket(TicketDelRequest request) {
        String ticket = request.getTicket();
        if (StringUtils.isBlank(ticket)) {
            DEException.throwException("ticket为必填参数");
        }
        QueryWrapper<CoreShareTicket> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ticket", ticket);
        coreShareTicketMapper.delete(queryWrapper);
    }

    public void switchRequire(TicketSwitchRequest request) {
        String resourceId = request.getResourceId();
        Boolean require = request.getRequire();
        QueryWrapper<XpackShare> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("resource_id", resourceId);
        queryWrapper.eq("creator", AuthUtils.getUser().getUserId());
        XpackShare xpackShare = xpackShareMapper.selectOne(queryWrapper);
        xpackShare.setTicketRequire(require);
        xpackShareMapper.updateById(xpackShare);
    }

    public List<TicketVO> query(Long resourceId) {
        QueryWrapper<XpackShare> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("resource_id", resourceId);
        queryWrapper.eq("creator", AuthUtils.getUser().getUserId());
        XpackShare xpackShare = xpackShareMapper.selectOne(queryWrapper);
        if (ObjectUtils.isEmpty(xpackShare)) return null;
        String uuid = xpackShare.getUuid();
        if (StringUtils.isBlank(uuid)) return null;
        QueryWrapper<CoreShareTicket> ticketQueryWrapper = new QueryWrapper<>();
        ticketQueryWrapper.eq("uuid", uuid);
        List<CoreShareTicket> coreShareTickets = coreShareTicketMapper.selectList(ticketQueryWrapper);
        if (CollectionUtils.isEmpty(coreShareTickets)) return null;
        return coreShareTickets.stream().map(item -> BeanUtils.copyBean(new TicketVO(), item)).toList();
    }

    @Transactional
    public void updateByUuidChange(String originalUuid, String newUuid) {
        xpackShareExtMapper.updateTicketUuid(originalUuid, newUuid);
    }

    @Transactional
    public void deleteByShare(String uuid) {
        QueryWrapper<CoreShareTicket> ticketQueryWrapper = new QueryWrapper<>();
        ticketQueryWrapper.eq("uuid", uuid);
        coreShareTicketMapper.delete(ticketQueryWrapper);
    }

    public TicketValidVO validateTicket(String ticket, XpackShare share) {
        TicketValidVO vo = new TicketValidVO();
        if (StringUtils.isBlank(ticket)) {
            vo.setTicketValid(!share.getTicketRequire());
            return vo;
        }
        CoreShareTicket linkTicket = getByTicket(ticket);
        if (ObjectUtils.isEmpty(linkTicket)) {
            vo.setTicketValid(false);
            return vo;
        }
        vo.setTicketValid(true);
        vo.setArgs(linkTicket.getArgs());
        Long accessTime = linkTicket.getAccessTime();
        long now = System.currentTimeMillis();
        if (ObjectUtils.isEmpty(accessTime)) {
            accessTime = now;
            vo.setTicketExp(false);
            linkTicket.setAccessTime(accessTime);
            coreShareTicketMapper.updateById(linkTicket);
            return vo;
        }
        Long exp = linkTicket.getExp();
        if (ObjectUtils.isEmpty(exp) || exp.equals(0L)) {
            vo.setTicketExp(false);
            return vo;
        }
        long expTime = exp * 60L * 1000L;
        long time = now - accessTime;
        vo.setTicketExp(time > expTime);
        return vo;
    }
}

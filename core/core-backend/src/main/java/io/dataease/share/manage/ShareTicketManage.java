package io.dataease.share.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.IDUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

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

    @Transactional
    public String saveTicket(TicketCreator creator) {
        String uuid = creator.getUuid();
        if (StringUtils.isBlank(uuid)) {
            DEException.throwException("uuid为必填参数");
        }
        QueryWrapper<XpackShare> shareQuery = new QueryWrapper<>();
        shareQuery.eq("uuid", uuid);
        shareQuery.eq("creator", AuthUtils.getUser().getUserId());
        if (ObjectUtils.isEmpty(xpackShareMapper.selectOne(shareQuery))) {
            DEException.throwException("无权操作此分享的Ticket");
        }
        String ticket = creator.getTicket();
        if (StringUtils.isNotBlank(ticket)) {
            CoreShareTicket ticketEntity = getByTicket(ticket);
            if (ObjectUtils.isNotEmpty(ticketEntity)) {
                if (creator.isGenerateNew()) {
                    ticketEntity.setAccessTime(null);
                    ticketEntity.setTicket(CodingUtil.shortUuid());
                    coreShareTicketMapper.deleteById(ticketEntity);
                    coreShareTicketMapper.insert(ticketEntity);
                    return ticketEntity.getTicket();
                }
                ticketEntity.setArgs(creator.getArgs());
                ticketEntity.setExp(creator.getExp());
                ticketEntity.setUuid(creator.getUuid());
                coreShareTicketMapper.deleteById(ticketEntity);
                coreShareTicketMapper.insert(ticketEntity);
                return ticketEntity.getTicket();
            }
        }
        if (StringUtils.isBlank(ticket)) {
            ticket = CodingUtil.shortUuid();
        }
        CoreShareTicket linkTicket = new CoreShareTicket();
        linkTicket.setId(IDUtils.snowID());
        linkTicket.setTicket(ticket);
        linkTicket.setArgs(creator.getArgs());
        linkTicket.setExp(creator.getExp());
        linkTicket.setUuid(creator.getUuid());
        Objects.requireNonNull(CommonBeanFactory.proxy(this.getClass())).saveDao(linkTicket);
        return ticket;
    }

    public void saveDao(CoreShareTicket ticket) {
        coreShareTicketMapper.insert(ticket);
    }

    public void deleteTicket(TicketDelRequest request) {
        String ticket = request.getTicket();
        if (StringUtils.isBlank(ticket)) {
            DEException.throwException("ticket为必填参数");
        }
        CoreShareTicket existingTicket = getByTicket(ticket);
        if (ObjectUtils.isEmpty(existingTicket)) {
            DEException.throwException("Ticket不存在");
        }
        QueryWrapper<XpackShare> shareQuery = new QueryWrapper<>();
        shareQuery.eq("uuid", existingTicket.getUuid());
        shareQuery.eq("creator", AuthUtils.getUser().getUserId());
        if (ObjectUtils.isEmpty(xpackShareMapper.selectOne(shareQuery))) {
            DEException.throwException("无权删除此Ticket");
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

    public IPage<TicketVO> query(Long resourceId, Page<TicketVO> page) {
        QueryWrapper<XpackShare> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("resource_id", resourceId);
        queryWrapper.eq("creator", AuthUtils.getUser().getUserId());
        XpackShare xpackShare = xpackShareMapper.selectOne(queryWrapper);
        if (ObjectUtils.isEmpty(xpackShare)) return null;
        String uuid = xpackShare.getUuid();
        if (StringUtils.isBlank(uuid)) return null;
        QueryWrapper<CoreShareTicket> ticketQueryWrapper = new QueryWrapper<>();
        ticketQueryWrapper.eq("uuid", uuid);
        IPage<CoreShareTicket> pager = xpackShareExtMapper.pager(page, ticketQueryWrapper);
        List<CoreShareTicket> records = pager.getRecords();
        IPage<TicketVO> iPage = new Page<>();
        iPage.setPages(pager.getPages());
        iPage.setTotal(pager.getTotal());
        iPage.setCurrent(pager.getCurrent());
        iPage.setSize(pager.getSize());
        List<TicketVO> vos = records.stream().map(record -> BeanUtils.copyBean(new TicketVO(), record)).toList();
        iPage.setRecords(vos);
        return iPage;
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
        if (!StringUtils.equals(linkTicket.getUuid(), share.getUuid())) {
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

    public Integer getLimit() {
        return 0;
    }

    public long ticketCount(String uuid) {
        QueryWrapper<CoreShareTicket> ticketQueryWrapper = new QueryWrapper<>();
        ticketQueryWrapper.eq("uuid", uuid);
        return coreShareTicketMapper.selectCount(ticketQueryWrapper);
    }
}

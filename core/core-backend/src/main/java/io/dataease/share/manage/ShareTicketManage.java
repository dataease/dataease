package io.dataease.share.manage;


import io.dataease.api.xpack.share.request.TicketCreator;
import io.dataease.api.xpack.share.request.TicketDelRequest;
import io.dataease.api.xpack.share.request.TicketSwitchRequest;
import io.dataease.api.xpack.share.vo.TicketVO;
import io.dataease.api.xpack.share.vo.TicketValidVO;
import io.dataease.exception.DEException;
import io.dataease.result.PageResult;
import io.dataease.share.dao.auto.entity.CoreShareTicket;
import io.dataease.share.dao.auto.entity.XpackShare;
import io.dataease.share.dao.auto.mapper.CoreShareTicketRepository;
import io.dataease.share.dao.auto.mapper.XpackShareRepository;
import io.dataease.utils.*;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Component
public class ShareTicketManage {

    @Resource
    private CoreShareTicketRepository coreShareTicketRepository;

    @Resource
    private XpackShareRepository xpackShareRepository;



    public CoreShareTicket getByTicket(String ticket) {
        Specification<CoreShareTicket> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("ticket"), ticket);
        return coreShareTicketRepository.findOne(spec).orElse(null);
    }

    @Transactional
    public String saveTicket(TicketCreator creator) {
        String ticket = creator.getTicket();
        if (StringUtils.isNotBlank(ticket)) {
            CoreShareTicket ticketEntity = getByTicket(ticket);
            if (ObjectUtils.isNotEmpty(ticketEntity)) {
                if (creator.isGenerateNew()) {
                    ticketEntity.setAccessTime(null);
                    ticketEntity.setTicket(CodingUtil.shortUuid());
                    coreShareTicketRepository.deleteById(ticketEntity.getId());
                    coreShareTicketRepository.saveAndFlush(ticketEntity);
                    return ticketEntity.getTicket();
                }
                ticketEntity.setArgs(creator.getArgs());
                ticketEntity.setExp(creator.getExp());
                ticketEntity.setUuid(creator.getUuid());
                coreShareTicketRepository.deleteById(ticketEntity.getId());
                coreShareTicketRepository.saveAndFlush(ticketEntity);
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
        coreShareTicketRepository.saveAndFlush(ticket);
    }

    public void deleteTicket(TicketDelRequest request) {
        String ticket = request.getTicket();
        if (StringUtils.isBlank(ticket)) {
            DEException.throwException("ticket为必填参数");
        }
        coreShareTicketRepository.deleteByTicket(ticket);
    }

    public void switchRequire(TicketSwitchRequest request) {
        String resourceId = request.getResourceId();
        Boolean require = request.getRequire();

        Specification<XpackShare> xpackShareSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("resourceId"), resourceId));
            predicates.add(cb.equal(root.get("creator"), AuthUtils.getUser().getUserId()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        XpackShare xpackShare = xpackShareRepository.findOne(xpackShareSpec).orElse(null);
        xpackShare.setTicketRequire(require);
        xpackShareRepository.saveAndFlush(xpackShare);
    }

    private Function<CoreShareTicket, TicketVO> dtoConverter = c -> {
        TicketVO dto = new TicketVO();
        BeanUtils.copyBean(dto, c);
        return dto;
    };

    public PageResult<TicketVO> query(Long resourceId, int goPage, int pageSize) {
        Specification<XpackShare> xpackShareSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("resourceId"), resourceId));
            predicates.add(cb.equal(root.get("creator"), AuthUtils.getUser().getUserId()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        XpackShare xpackShare = xpackShareRepository.findOne(xpackShareSpec).orElse(null);
        if (ObjectUtils.isEmpty(xpackShare)) return null;
        String uuid = xpackShare.getUuid();
        if (StringUtils.isBlank(uuid)) return null;
        Pageable pageable = PageRequest.of(goPage - 1, pageSize);
        Specification<CoreShareTicket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("uuid"), uuid));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TicketVO> pager = coreShareTicketRepository.findAll(spec, pageable).map(dtoConverter);
        return new PageResult<>(pager.getContent(), pager.getTotalElements(), pageable);
    }

    @Transactional
    public void updateByUuidChange(String originalUuid, String newUuid) {
        coreShareTicketRepository.updateTicketUuid(newUuid, originalUuid);
    }

    @Transactional
    public void deleteByShare(String uuid) {
        coreShareTicketRepository.deleteByUuid(uuid);
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
            coreShareTicketRepository.saveAndFlush(linkTicket);
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
        Specification<CoreShareTicket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("uuid"), uuid));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return coreShareTicketRepository.count(spec);
    }
}

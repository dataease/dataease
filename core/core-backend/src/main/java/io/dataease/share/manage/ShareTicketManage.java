package io.dataease.share.manage;


import io.dataease.api.xpack.share.request.TicketCreator;
import io.dataease.api.xpack.share.request.TicketDelRequest;
import io.dataease.api.xpack.share.request.TicketSwitchRequest;
import io.dataease.api.xpack.share.vo.TicketVO;
import io.dataease.api.xpack.share.vo.TicketValidVO;
import io.dataease.exception.DEException;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.result.PageResult;
import io.dataease.share.dao.auto.entity.CoreShareTicket;
import io.dataease.share.dao.auto.entity.XpackShare;
import io.dataease.share.dao.auto.mapper.CoreShareTicketRepository;
import io.dataease.share.dao.auto.mapper.XpackShareRepository;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.CodingUtil;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.IDUtils;
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

    @Resource
    private ShareAuthorizationManage shareAuthorizationManage;


    public CoreShareTicket getByTicket(String ticket) {
        Specification<CoreShareTicket> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("ticket"), ticket);
        return coreShareTicketRepository.findOne(spec).orElse(null);
    }

    @Transactional
    public String saveTicket(TicketCreator creator) {
        String uuid = creator.getUuid();
        if (StringUtils.isBlank(uuid)) {
            DEException.throwException("uuid为必填参数");
        }
        if (!isShareOwner(uuid)) {
            DEException.throwException("无权操作此分享的Ticket");
        }
        String ticket = creator.getTicket();
        if (StringUtils.isNotBlank(ticket)) {
            CoreShareTicket ticketEntity = getByTicket(ticket);
            if (ObjectUtils.isNotEmpty(ticketEntity)) {
                if (!isShareOwner(ticketEntity.getUuid())) {
                    DEException.throwException("无权操作此Ticket");
                }
                if (creator.isGenerateNew()) {
                    if (!StringUtils.equals(uuid, ticketEntity.getUuid())) {
                        DEException.throwException("Ticket不属于此分享");
                    }
                    coreShareTicketRepository.deleteById(ticketEntity.getId());
                    String newTicket = CodingUtil.shortUuid();
                    CoreShareTicket newTicketEntity = new CoreShareTicket();
                    newTicketEntity.setId(IDUtils.snowID());
                    newTicketEntity.setTicket(newTicket);
                    newTicketEntity.setArgs(ticketEntity.getArgs());
                    newTicketEntity.setExp(ticketEntity.getExp());
                    newTicketEntity.setUuid(ticketEntity.getUuid());
                    coreShareTicketRepository.saveAndFlush(newTicketEntity);
                    return newTicket;
                }
                ticketEntity.setArgs(creator.getArgs());
                ticketEntity.setExp(creator.getExp());
                ticketEntity.setUuid(creator.getUuid());
                coreShareTicketRepository.saveAndFlush(ticketEntity);
                return ticketEntity.getTicket();
            }
        }
        if (creator.isGenerateNew()) {
            DEException.throwException("Ticket不存在");
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
        CoreShareTicket existingTicket = getByTicket(ticket);
        if (ObjectUtils.isEmpty(existingTicket)) {
            DEException.throwException("Ticket不存在");
        }
        if (!isShareOwner(existingTicket.getUuid())) {
            DEException.throwException("无权删除此Ticket");
        }
        coreShareTicketRepository.deleteByTicket(ticket);
    }

    private boolean isShareOwner(String uuid) {
        Specification<XpackShare> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("uuid"), uuid));
            predicates.add(cb.equal(root.get("creator"), V3UserUtil.getUid()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        XpackShare share = xpackShareRepository.findAll(spec).stream().findFirst().orElse(null);
        if (share == null) return false;
        shareAuthorizationManage.requireManage(share.getResourceId());
        return true;
    }

    public void switchRequire(TicketSwitchRequest request) {
        String resourceId = request.getResourceId();
        shareAuthorizationManage.requireManage(Long.valueOf(resourceId));
        Boolean require = request.getRequire();

        Specification<XpackShare> xpackShareSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("resourceId"), resourceId));
            predicates.add(cb.equal(root.get("creator"), V3UserUtil.getUid()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        XpackShare xpackShare = xpackShareRepository.findAll(xpackShareSpec).stream().findFirst().orElse(null);
        xpackShare.setTicketRequire(require);
        xpackShareRepository.saveAndFlush(xpackShare);
    }

    private Function<CoreShareTicket, TicketVO> dtoConverter = c -> {
        TicketVO dto = new TicketVO();
        BeanUtils.copyBean(dto, c);
        return dto;
    };

    public PageResult<TicketVO> query(Long resourceId, int goPage, int pageSize) {
        shareAuthorizationManage.requireManage(resourceId);
        Specification<XpackShare> xpackShareSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("resourceId"), resourceId));
            predicates.add(cb.equal(root.get("creator"), V3UserUtil.getUid()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        XpackShare xpackShare = xpackShareRepository.findAll(xpackShareSpec).stream().findFirst().orElse(null);
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
            linkTicket.setAccessTime(accessTime);
            coreShareTicketRepository.saveAndFlush(linkTicket);
        }
        Long expiresAt = ticketExpiresAt(linkTicket);
        vo.setTicketExp(expiresAt != null && now >= expiresAt);
        return vo;
    }

    public record TicketGrant(Long id, Long expiresAt) { }

    public TicketGrant tokenTicket(String ticket, XpackShare share) {
        if (StringUtils.isBlank(ticket)) return verifyTokenTicket(null, share);
        return requireTokenTicket(getByTicket(ticket), share);
    }

    // Read-only validation on every authenticated share request; never reset first access time.
    public TicketGrant verifyTokenTicket(Long ticketId, XpackShare share) {
        if (ticketId == null) {
            if (Boolean.TRUE.equals(share.getTicketRequire())) {
                throw new IllegalArgumentException("Share Ticket required");
            }
            return new TicketGrant(null, null);
        }
        return requireTokenTicket(coreShareTicketRepository.findById(ticketId).orElse(null), share);
    }

    private TicketGrant requireTokenTicket(CoreShareTicket ticket, XpackShare share) {
        if (ticket == null || !Objects.equals(ticket.getUuid(), share.getUuid())
                || ticket.getAccessTime() == null) {
            throw new IllegalArgumentException("Invalid share Ticket");
        }
        Long expiresAt = ticketExpiresAt(ticket);
        if (expiresAt != null && expiresAt <= System.currentTimeMillis()) {
            throw new IllegalArgumentException("Share Ticket expired");
        }
        return new TicketGrant(ticket.getId(), expiresAt);
    }

    private Long ticketExpiresAt(CoreShareTicket ticket) {
        Long exp = ticket.getExp();
        if (exp == null || exp == 0L) return null;
        return Math.addExact(ticket.getAccessTime(), Math.multiplyExact(exp, 60_000L));
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

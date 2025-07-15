package io.dataease.share.dao.auto.mapper;

import io.dataease.share.dao.auto.entity.CoreShareTicket;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface CoreShareTicketRepository extends JpaRepository<CoreShareTicket, Long>, JpaSpecificationExecutor<CoreShareTicket> {

    @Transactional
    default void deleteByUuid(String uuid) {
        Specification<CoreShareTicket> spec = (root, query, cb) ->
                cb.equal(root.get("uuid"), uuid);
        List<CoreShareTicket> tickets = findAll(spec);
        if (!tickets.isEmpty()) {
            deleteAll(tickets);
        }
    }

    @Transactional
    default void deleteByTicket(String ticket) {
        Specification<CoreShareTicket> spec = (root, query, cb) ->
                cb.equal(root.get("ticket"), ticket);
        List<CoreShareTicket> tickets = findAll(spec);
        if (!tickets.isEmpty()) {
            deleteAll(tickets);
        }
    }

    @Transactional
    default void updateTicketUuid(String newUuid, String oldUuid) {
        Specification<CoreShareTicket> spec = (root, query, cb) ->
                cb.equal(root.get("uuid"), oldUuid);
        List<CoreShareTicket> tickets = findAll(spec);
        for (CoreShareTicket ticket : tickets) {
            ticket.setUuid(newUuid);
        }
        saveAll(tickets);
    }

}

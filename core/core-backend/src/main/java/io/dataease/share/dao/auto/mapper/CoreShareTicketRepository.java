package io.dataease.share.dao.auto.mapper;

import io.dataease.share.dao.auto.entity.CoreShareTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;


public interface CoreShareTicketRepository extends JpaRepository<CoreShareTicket, Long>, JpaSpecificationExecutor<CoreShareTicket> {

    @Modifying
    @Transactional
    @Query("DELETE FROM CoreShareTicket c WHERE c.uuid = :uuid")
    void deleteByUuid(String uuid);

    @Modifying
    @Transactional
    @Query("DELETE FROM CoreShareTicket c WHERE c.ticket = :ticket")
    void deleteByTicket(String ticket);

    @Modifying
    @Transactional
    @Query("UPDATE CoreShareTicket c set c.uuid = :newUuid where c.uuid = :oldUuid")
    void updateTicketUuid(String newUuid, String oldUuid);

}

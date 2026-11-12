package io.dataease.ai.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

/** Minimal tombstone: late REQUIRES_NEW writes and gap repair may never recreate expired evidence. */
@Getter
@Setter
@Entity
@Table(name = "core_ai_audit_retired")
public class CoreAiAuditRetired {
    @Id
    @Column(name = "id", length = 100, nullable = false)
    @Comment("Retired execution id only; contains no subject, resource or body")
    private String id;
}

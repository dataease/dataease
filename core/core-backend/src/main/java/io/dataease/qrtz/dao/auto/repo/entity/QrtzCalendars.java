package io.dataease.qrtz.dao.auto.repo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.sql.Blob;

@Data
@Entity
@Table(name = "QRTZ_CALENDARS")
@IdClass(QrtzCalendarsId.class)
@Comment("Quartz日历（开源作业调度框架Quartz）")
public class QrtzCalendars {

    @Id
    @Column(name = "SCHED_NAME", length = 120, nullable = false)
    private String schedName;

    @Id
    @Column(name = "CALENDAR_NAME", length = 200, nullable = false)
    private String calendarName;

    @Column(name = "CALENDAR", nullable = false, length = 16777216)
    private byte[] calendar;
}

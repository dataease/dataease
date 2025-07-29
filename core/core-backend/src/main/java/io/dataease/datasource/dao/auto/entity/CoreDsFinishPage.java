package io.dataease.datasource.dao.auto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Comment("是否显示完成页面记录表")
@Table(name = "core_ds_finish_page")
public class CoreDsFinishPage {
    @Id
    @Comment("主键")
    @Column(name = "id", nullable = false)
    private Long id;

}

package io.dataease.listener.demo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Getter
@Setter
@Comment("连锁茶饮销售看板demo数据")
@Entity
@Table(name = "demo_tea_material")
public class DemoTeaMaterial {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "date")
    private Date date;

    @Column(name = "shop", length = 16777216)
    private String shop;

    @Column(name = "purpose", length = 16777216)
    private String purpose;

    @Column(name = "amount")
    private Long amount;

    public DemoTeaMaterial(Integer id, Date date, String shop, String purpose, Long amount) {
        this.id = id;
        this.date = date;
        this.shop = shop;
        this.purpose = purpose;
        this.amount = amount;
    }

    public DemoTeaMaterial() {
    }

}

package io.dataease.listener.demo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@Comment("连锁茶饮销售看板demo数据")
@Entity
@Table(name = "demo_tea_order")
public class DemoTeaOrder {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Lob
    @Column(name = "shop", length = 16777216)
    private String shop;

    @Lob
    @Column(name = "product", length = 16777216)
    private String product;

    @Lob
    @Column(name = "dish", length = 16777216)
    private String dish;

    @Lob
    @Column(name = "temperature", length = 16777216)
    private String temperature;

    @Lob
    @Column(name = "specifications", length = 16777216)
    private String specifications;

    @Column(name = "sales")
    private Long sales;

    @Column(name = "price")
    private Long price;

    @Lob
    @Column(name = "serial", length = 16777216)
    private String serial;

    @Column(name = "sale_date")
    private Date saleDate;

    public DemoTeaOrder(Integer id, String shop, String product, String dish, String temperature, String specifications, Integer sales, Integer price, String serial, Date saleDate) {
        this.id = id;
        this.shop = shop;
        this.product = product;
        this.dish = dish;
        this.temperature = temperature;
        this.specifications = specifications;
        this.sales = Long.valueOf(sales);
        this.price = Long.valueOf(price);
        this.serial = serial;
        this.saleDate = saleDate;
    }

    public DemoTeaOrder() {
    }


}

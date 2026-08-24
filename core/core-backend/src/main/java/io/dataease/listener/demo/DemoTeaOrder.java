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
@Table(name = "demo_tea_order")
public class DemoTeaOrder {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "shop", length = 16777216)
    private String shop;

    @Column(name = "product_line", length = 16777216)
    private String productLine;

    @Column(name = "dish_name", length = 16777216)
    private String dishName;

    @Column(name = "temperature", length = 16777216)
    private String temperature;

    @Column(name = "specification", length = 16777216)
    private String specification;

    @Column(name = "sales_quantity")
    private Long salesQuantity;

    @Column(name = "unit_price")
    private Long unitPrice;

    @Column(name = "bill_number", length = 16777216)
    private String billNumber;

    @Column(name = "sales_date")
    private Date salesDate;

    public DemoTeaOrder(Integer id, String shop, String productLine, String dishName, String temperature,
                        String specification, Integer salesQuantity, Integer unitPrice,
                        String billNumber, Date salesDate) {
        this.id = id;
        this.shop = shop;
        this.productLine = productLine;
        this.dishName = dishName;
        this.temperature = temperature;
        this.specification = specification;
        this.salesQuantity = Long.valueOf(salesQuantity);
        this.unitPrice = Long.valueOf(unitPrice);
        this.billNumber = billNumber;
        this.salesDate = salesDate;
    }

    public DemoTeaOrder() {
    }


}

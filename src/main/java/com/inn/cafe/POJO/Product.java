package com.inn.cafe.POJO;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

@NamedQuery(name = "Product.getAllProduct",query = "select  new com.inn.cafe.wrapper.ProductWrapper(p.id, p.name, p.description, p.price, p.status, p.category.id, p.category.name ) from Product p")

//@NamedQuery(name = "Product.updateProductStatus",query = "update Product p set p.status=:status where p.id=id")

@Data
@Entity
@DynamicUpdate
@DynamicInsert
public class Product implements Serializable {

    public  static  final Long serialVersionUID=12345L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_FK",nullable = false)
    private Category category;

    private String description;

    private Integer price;

    private String status;




}

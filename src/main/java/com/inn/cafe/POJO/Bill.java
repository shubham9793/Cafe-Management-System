package com.inn.cafe.POJO;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

@NamedQuery(name = "Bill.getAllBills", query = "select b from Bill b order by b.id desc")

@NamedQuery(name = "getBillByCreatedBy", query = "select b from Bill b where b.createdBy=:username order by b.id desc")


@Data
@Entity
@DynamicUpdate
@DynamicInsert
public class Bill implements Serializable {

    private  static  final Long serialVersionUID=1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Integer id;

    private String uuid;

    private String name;

    private String email;

    private String contactNumber;

    private String paymentMethod;

    private Integer totalAmount;

    @Column(columnDefinition = "json")
    private String productDetails;

    private String createdBy;

    private String cafeAddress;



}

package com.inn.cafe.POJO;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

@NamedQuery(name = "Category.getAllCategory",query = "select c from Category c where c.id in (select p.category from Product p where p.status='true')")


@Data
@Entity
@DynamicUpdate
@DynamicInsert
public class Category  implements Serializable {

    private  static  final Long serialVersionUID=1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Integer id;

    private String name;
}

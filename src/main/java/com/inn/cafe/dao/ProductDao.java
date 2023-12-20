package com.inn.cafe.dao;

import com.inn.cafe.POJO.Product;
import com.inn.cafe.wrapper.ProductWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface ProductDao extends JpaRepository<Product,Integer> {

    List<ProductWrapper> getAllProduct();

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.status = :status WHERE p.id = :id")
    Integer updateProductStatus(@Param("status") String status, @Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("SELECT new com.inn.cafe.wrapper.ProductWrapper(p.id,p.name) from Product p where p.category.id=:id and p.status='true'")
    List<ProductWrapper> getProductByCategory(@Param("id") Integer id);



    @Transactional
    @Query("SELECT new com.inn.cafe.wrapper.ProductWrapper(p.id,p.name,p.description,p.price) from Product p where p.id=:id")
    ProductWrapper getProductById(Integer id);
}

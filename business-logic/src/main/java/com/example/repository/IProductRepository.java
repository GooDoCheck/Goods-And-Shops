package com.example.repository;

import com.example.entity.Product;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT p FROM Product p " +
            "WHERE upper(p.name) LIKE CONCAT('%', upper(:keyword), '%') " +
            "OR upper(p.brand) LIKE CONCAT('%', upper(:keyword), '%') " +
            "OR upper(p.manufacturer) LIKE CONCAT('%', upper(:keyword), '%')")
    Optional<List<Product>> findAllProductByKeywordContains(@Param("keyword") String keyword, Sort sort);


    @Query(value = "SELECT p FROM Product p " +
            "INNER JOIN Subcategory sc ON p.subcategory.id = sc.id " +
            "INNER JOIN Category c ON sc.category.id = c.id " +
            "WHERE UPPER(c.name) = UPPER(:category)")
    Optional<List<Product>> findAllProductByCategoryName(@Param("category") String category, Sort sort);

    @Query(value = "SELECT p FROM Product p " +
            "INNER JOIN Subcategory sc ON p.subcategory.id = sc.id " +
            "INNER JOIN Category c ON sc.category.id = c.id " +
            "WHERE UPPER(c.name) = UPPER(:category) " +
            "AND upper(p.name) LIKE CONCAT('%', upper(:keyword), '%') " +
            "OR upper(p.brand) LIKE CONCAT('%', upper(:keyword), '%') " +
            "OR upper(p.manufacturer) LIKE CONCAT('%', upper(:keyword), '%')")
    Optional<List<Product>> findAllProductByKeywordContainsAndByCategoryName(@Param("category") String category, @Param("keyword") String keyword, Sort sort);

    @Query(value = "SELECT p FROM Product p " +
            "INNER JOIN Subcategory sc ON p.subcategory.id = sc.id " +
            "WHERE UPPER(sc.name) = UPPER(:subcategory)")
    Optional<List<Product>> findAllProductBySubcategoryName(@Param("subcategory") String subcategory, Sort sort);

    @Query(value = "SELECT p FROM Product p " +
            "INNER JOIN Subcategory sc ON p.subcategory.id = sc.id " +
            "WHERE UPPER(sc.name) = UPPER(:subcategory) " +
            "AND upper(p.name) LIKE CONCAT('%', upper(:keyword), '%') " +
            "OR upper(p.brand) LIKE CONCAT('%', upper(:keyword), '%') " +
            "OR upper(p.manufacturer) LIKE CONCAT('%', upper(:keyword), '%')")
    Optional<List<Product>> findAllProductByKeywordContainsAndBySubcategoryName(@Param("subcategory") String subcategory, @Param("keyword") String keyword, Sort sort);

}

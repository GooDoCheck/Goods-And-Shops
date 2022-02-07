package com.example.repository;

import com.example.entity.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISubcategoryRepository extends JpaRepository<Subcategory, Long> {

}

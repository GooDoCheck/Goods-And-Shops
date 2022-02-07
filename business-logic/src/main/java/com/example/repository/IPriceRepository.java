package com.example.repository;

import com.example.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPriceRepository extends JpaRepository<Price, Long> {

    @Query("SELECT p FROM Price p " +
            "INNER JOIN PriceList pl ON p.priceList.id = pl.id " +
            "WHERE pl.id IN :priseListId " +
            "AND p.date BETWEEN :startDate AND :endDate " +
            "ORDER BY p.date DESC ")
    Optional<List<Price>> findAllPriceBetweenDateParam(@Param("priseListId") Long priseListId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);
}

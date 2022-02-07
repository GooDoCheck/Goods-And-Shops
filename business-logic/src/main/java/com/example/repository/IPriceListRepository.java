package com.example.repository;

import com.example.entity.PriceList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPriceListRepository extends JpaRepository<PriceList, Long> {

    @Query("SELECT pl FROM PriceList pl " +
            "WHERE pl.product.id IN :productsId")
    Optional<List<PriceList>> findAllPriceListsByProductsId(@Param("productsId") ArrayList<Long> productsId);

    @Query("SELECT pl FROM PriceList pl " +
            "WHERE pl.product.id IN :productsId " +
            "AND pl.store.id IN :storesId")
    Optional<List<PriceList>> findAllPriceListsByProductsIdAndStoresId(@Param("productsId") List<Long> productsId,
                                                                       @Param("storesId") List<Long> storesId);

    @Override
    Optional<PriceList> findById(Long id);
}

package com.example.repository;

import com.example.entity.Store;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IStoreRepository extends JpaRepository<Store, Long> {
    Optional<List<Store>> getStoresByCityEquals(Sort sort, String city);
    Optional<List<Store>> getStoresByNameEquals(Sort sort, String name);
    Optional<List<Store>> getStoresByCityAndNameEquals(Sort sort, String city, String name);
}

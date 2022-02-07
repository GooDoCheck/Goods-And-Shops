package com.example.repository;

import com.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface IUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String userName);
    Boolean existsByUsername(String userName);
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
}

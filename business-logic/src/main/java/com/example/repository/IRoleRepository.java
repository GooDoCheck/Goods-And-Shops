package com.example.repository;

import com.example.entity.Role;
import com.example.enums.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(ERole name);

}

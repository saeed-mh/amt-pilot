package com.amtpilot.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amtpilot.entity.Authority;

public interface AuthorityRepository extends JpaRepository<Authority, UUID> {

    List<Authority> findByCityIgnoreCaseOrderByNameAsc(String city);
}